/*
 * Copyright © 2020 IBM Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package dev.ebullient.dnd.beastiary.compendium.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import dev.ebullient.dnd.beastiary.Beast;
import dev.ebullient.dnd.beastiary.compendium.Attack;
import dev.ebullient.dnd.beastiary.compendium.Monster;
import dev.ebullient.dnd.beastiary.compendium.Multiattack;
import dev.ebullient.dnd.mechanics.HitPoints;

/**
 * One time conversion of a heaping pile of HTML into a tidy JSON compendium.
 * This is just for posterity, so whoever reads this can weep.
 *
 *
 */
public class ConvertHtmlToJson {

    public static void main(String[] args) throws Exception {
        ConvertHtmlToJson converter = new ConvertHtmlToJson();
        Map<String, Monster> compendium = new HashMap<>();

        Path basePath = FileSystems.getDefault().getPath("./monsters/");

        converter.createMonster(basePath.resolve("5e_SRD:Clay_Golem").toFile());

        try (BufferedReader br = new BufferedReader(new FileReader(basePath.resolve("list.txt").toFile()))) {
            br.lines().forEach(line -> {
                if ( line.contains("Deck_of_Many_Things") ) {
                    return;
                }
                File f = basePath.resolve(line).toFile();
                try {
                    log(f.getAbsolutePath());
                    Monster m = converter.createMonster(f);
                    compendium.put(m.getName().toLowerCase(Locale.ROOT), m);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1); // fail fast
                }
            });
        }
    }

    public Monster createMonster(File f) throws Exception {
        Monster monster = new Monster();
        Map<String, String> description = new HashMap<>();

        Document doc = Jsoup.parse(f, "UTF-8");

        Elements headlines = doc.getElementsByClass("mw-headline");
        Element title = headlines.first();
        monster.setName(title.text());
        Element beginning = title.parent();

        // table
        Element outerTable = beginning.nextElementSibling();
        if ( ! "table".equals(outerTable.tagName())) {
            throw new IllegalArgumentException("Unexpected document structure, can't find table");
        }
        Element data = outerTable.children().first()    // tbody
                        .children().first()             // tr
                        .children().first();            // td

        if ( !data.text().contains("Armor Class") ) {
            throw new IllegalArgumentException("Data doesn't match (no Armor Class)\n" + data.text());
        }

        List<Element> notes = new ArrayList<>();
        List<Element> actions = new ArrayList<>();
        List<Element> legend = new ArrayList<>();
        List<Element> reactions = new ArrayList<>();
        int section = 0;
        Element next = data.children().first();
        while ( next != null ) {
            if ( "hr".equals(next.tagName())) {
                section++;
            } else if ("h4".equals(next.tagName())) {
                switch(next.text()) {
                    case "ACTIONS":
                        section = 50;
                        break;
                    case "LEGENDARY ACTIONS":
                        section = 60;
                        break;
                    case "REACTIONS":
                        section = 70;
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown section" + next.text());
                }
            } else {
                switch(section) {
                    case 0 :
                        getSizeAndType(monster, next, description);
                        break;
                    case 1 :
                        getBasicStats(monster, next, description);
                        break;
                    case 2 :
                        getScoresAndModifiers(monster, next, description);
                        break;
                    case 3 :
                        getSavingThrows(monster, next, description);
                        break;
                    case 4 :
                        notes.add(next);
                        break;
                    case 50 :
                        actions.add(next);
                        break;
                    case 60 :
                        legend.add(next);
                        break;
                    case 70 :
                        reactions.add(next);
                        break;
                    default :
                        throw new IllegalArgumentException("Data doesn't match (unexpected number of sections)\n" + next.text());
                }
            }
            next = next.nextElementSibling();
        }

        List<String> noteText = new ArrayList<>();

        // Look for additional background information
        next = data.nextElementSibling();
        if ( "td".equals(next.tagName())) {
            noteText.clear();
            for (Node n : next.childNodes() ) {
                noteText.add(getNodeText(n));
            }
            description.put("Background", String.join("\n", noteText));
        }

        // Gather together notes from above, with any additional notes that follow
        noteText.clear();
        for(Element e : notes) {
            noteText.add(e.text());
        }
        description.put("Notes", String.join("\n", noteText));

        // Gether together legendary actions (for use)
        noteText.clear();
        for(Element e : legend) {
            noteText.add(e.text());
        }
        description.put("Legendary Actions", String.join("\n", noteText));

        parseActions(monster, actions, description);
        parseReactions(monster, reactions, description);

        monster.setDescription(description);
        return monster;
    }

    private void parseReactions(Monster monster, List<Element> reactions, Map<String, String> description) {
    }

    void parseActions(Monster monster, List<Element> elements, Map<String, String> description) {
        final Pattern HIT = Pattern.compile("(\\d+\\s*[-+d()0-9 ]*)\\s*(\\w+)\\s*damage");
        final Pattern ATTACK = Pattern.compile(".*Attack: ([-+0-9]+) .*");
        final Pattern DC = Pattern.compile("a DC (\\d+) (\\w+) saving throw");

        List<String> noteText = new ArrayList<>();
        Map<String, Attack> attacks = new HashMap<>();
        String multiattack = null;

        nextelement:
        for(Element e : elements ) {
            String all = e.text().trim();
            noteText.add(all);

            String[] segments = all.replaceAll("ft\\.", "ft").split("\\.\\s*");
            if ( "Multiattack".equalsIgnoreCase(segments[0]) ) {
                if  ( segments.length < 2 ) {
                    throw new IllegalArgumentException("Unexpected Multiattack definition: " + all);
                }
                List<String> slice = Arrays.asList(segments).subList(1, segments.length);
                multiattack = String.join(". ", slice);
            } else if ( all.contains("Attack") ) {
                String name = segments[0];

                Attack a = new Attack();
                a.setName(name);

                List<Attack.Damage> damage = new ArrayList<>();
                List<String> slice = Arrays.asList(segments).subList(1, segments.length);

                for ( String s : slice ) {
                    if ( s.startsWith("Hit") ) {
                        Matcher m = HIT.matcher(s);
                        if ( m.find() ) {
                            Attack.Damage d = new Attack.Damage();
                            d.setAmount(m.group(1).replaceAll("\\s+",""));
                            d.setType(m.group(2).trim());
                            damage.add(d);
                        } else {
                            log("Unexpected Hit definition: " + s);
                            break nextelement;
                        }
                    } else if (s.contains("Attack:")) {
                        Matcher m = ATTACK.matcher(s);
                        if ( m.matches()) {
                            a.setAttackModifier(Integer.parseInt(m.group(1)));
                        } else {
                            throw new IllegalArgumentException("Unexpected Attack definition: " + s);
                        }
                    } else if ( s.contains(" DC ") && s.contains("damage") ) {
                        Attack.Damage d = new Attack.Damage();
                        Matcher m1 = DC.matcher(s);
                        if ( m1.find() ) {
                            d.setSavingThrow(m1.group(1) + " " + Beast.Statistic.convert(m1.group(2)));
                            Matcher m2 = HIT.matcher(s);
                            if ( m2.find() ) {
                                d.setAmount(m2.group(1).replaceAll("\\s+",""));
                                d.setType(m2.group(2).trim());
                                damage.add(d);
                            } else {
                                log("Unexpected DC damage definition: " + s);
                            }
                        } else {
                            log("Different kind of DC check: " + s);
                        }
                    }
                }

                if ( damage.size() == 0 ) {
                    throw new IllegalArgumentException("Missing hit damage for attack: " + all);
                }
                a.setDamage(damage);
                attacks.put(name.toLowerCase(Locale.ROOT), a);
            }
        }

        if ( multiattack != null ) {
            Multiattack m = new Multiattack();
            monster.setMultiattack(m);
        }

        monster.setActions(attacks);
        description.put("Attacks", String.join("\n", noteText));
    }

    void getSavingThrows(Monster monster, Element parent, Map<String, String> description) {
        final Pattern perception = Pattern.compile("Perception (\\d+)");

        List<String> slice;
        String[] segments = parent.html().split("\\s*<br[^>]*>\\s*");

        for (String s : segments) {
            String body = Jsoup.parseBodyFragment(s).body().text().trim();
            String[] chunks = body.split("\\s+");
            switch(chunks[0]) {
                case "Saving" :
                    slice = Arrays.asList(chunks).subList(2, chunks.length);
                    if ((slice.size() % 2) != 0) {
                        throw new IllegalArgumentException("uneven number of elements" + slice);
                    }
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < slice.size(); i = i+2) {
                        sb.append(slice.get(i).toUpperCase(Locale.ROOT))
                          .append("(")
                          .append(slice.get(i+1).replace(",",""))
                          .append(")");
                        if ( i+2 < slice.size() ) {
                            sb.append(",");
                        }
                    }
                    description.put("Saving Throws", sb.toString());
                    monster.setSavingThrows(sb.toString());
                    break;
                case "Skills" :
                    slice = Arrays.asList(chunks).subList(1, chunks.length);
                    description.put("Skills", String.join(" ", slice));
                    break;
                case "Senses" :
                    slice = Arrays.asList(chunks).subList(1, chunks.length);
                    String value = String.join(" ", slice);
                    description.put("Senses", value);
                    // passive perception
                    Matcher m = perception.matcher(value);
                    if ( m.find() ) {
                        monster.setPassivePerception(Integer.parseInt(m.group(1)));
                    }
                    break;
                case "Languages":
                    slice = Arrays.asList(chunks).subList(1, chunks.length);
                    description.put("Languages", String.join(" ", slice));
                    break;
                case "Challenge":
                    slice = Arrays.asList(chunks).subList(1, chunks.length);
                    description.put("Challenge", String.join(" ", slice));
                    monster.setChallengeRating(slice.get(0));
                    break;
                case "Damage":
                    if ( body.contains("Resist") ) {
                        description.put("Damage Resistance", body);
                    } else if ( body.contains("Vulner") ) {
                        description.put("Damage Vulnerability", body);
                    } else if ( body.contains("Immun") ) {
                        description.put("Damage Immunity", body);
                    } else {
                        throw new IllegalArgumentException("Unknown Damage resistance or vulnerability: " + body);
                    }
                    break;
                case "Condition":
                    if ( body.contains("Immun") ) {
                        description.put("Condition Immunity", body);
                    } else {
                        throw new IllegalArgumentException("Unkonwn Condition resistence or vulnerability: " + body);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Chunk in Saving Throws doesn't match expected headings\n" + body);
            }
        }
        //log(parent.text() + "\n   --> " + monster);
    }

    void getScoresAndModifiers(Monster monster, Element parent, Map<String, String> description) {
        // this information is in two rows of a table:
        // one has headers, the second has the goods
        Element statsRow = parent.getElementsByTag("tr").last();
        String[] elements = statsRow.text().trim().split("\\s+");

        if ( elements.length != 12) {
            throw new IllegalArgumentException("Data doesn't match expected description of scores/modifiers\n" + statsRow.text());
        }
        StringBuilder sb = new StringBuilder();

        String strength = elements[0] + elements[1];
        sb.append("STR=").append(strength).append(", ");
        monster.setStrength(strength);

        String dexterity = elements[2] + elements[3];
        sb.append("DEX=").append(dexterity).append(", ");
        monster.setDexterity(dexterity);

        String constitution = elements[4] + elements[5];
        sb.append("CON=").append(constitution).append(", ");
        monster.setConstitution(constitution);

        String intelligence = elements[6] + elements[7];
        sb.append("INT=").append(intelligence).append(", ");
        monster.setIntelligence(intelligence);

        String wisdom = elements[8] + elements[9];
        sb.append("WIS=").append(wisdom).append(", ");
        monster.setWisdom(wisdom);

        String charisma = elements[10] + elements[11];
        sb.append("CHA=").append(charisma);
        monster.setCharisma(charisma);

        description.put("Scores", sb.toString());
        //log(parent.text() + "\n   --> " + monster);
    }

    void getBasicStats(Monster monster, Element parent, Map<String, String> description) {
        final Pattern AC = Pattern.compile(".*(\\d+).*");
        final String HP = "Hit Points";
        final String SPEED = "Speed";

        String[] lines = parent.html().split("\\s*<br[^>]*>\\s*");
        if ( lines.length != 3 ) {
            throw new IllegalArgumentException("Unexpected statistics string: " + parent.html());
        }

        String acString = Jsoup.parseBodyFragment(lines[0]).body().text().trim();
        description.put("Armor Class", acString.replace("Armor Class ", ""));
        Matcher m1 = AC.matcher(acString);
        if ( m1.matches() ) {
            monster.setArmorClass(Integer.parseInt(m1.group(1)));
        } else {
            throw new IllegalArgumentException("Unexpected armor class definition: " + acString);
        }

        String hpString = Jsoup.parseBodyFragment(lines[1]).body().text().trim();
        description.put(HP, acString.replace("Hit Points ", ""));
        monster.setHitPoints(HitPoints.validate(hpString.substring(HP.length()).trim()));

        String speedString = Jsoup.parseBodyFragment(lines[2]).body().text().trim();
        description.put(SPEED, speedString.replace("Speed ", ""));
    }

    void getSizeAndType(Monster monster, Element parent, Map<String, String> description) {
        final Pattern sizeTypeAlign = Pattern.compile("(\\w+) (\\w+).*, ([^,]+)");

        Matcher m = sizeTypeAlign.matcher(parent.text().trim());
        if (m.matches()) {
            monster.setSize(Beast.Size.valueOf(m.group(1).toUpperCase(Locale.ROOT)));
            monster.setType(Beast.Type.valueOf(m.group(2).toUpperCase(Locale.ROOT)));
            monster.setAlignment(m.group(3));
        } else {
            throw new IllegalArgumentException("Bad size/type string: " + parent.text());
        }

        description.put("General", parent.text());
        //log(parent.text() + "\n   --> " + monster);
    }

    String getNodeText(Node n) {
        if ( n instanceof TextNode ) {
            return ((TextNode) n).text().trim();
        } else if ( n instanceof Element ) {
            return ((Element) n).text().trim();
        }

        throw new IllegalArgumentException("Unexpected node type " + n.getClass());
    }

    static void log(String msg, String... vals) {
        System.out.println(String.format(msg, vals));
    }

    static void log(Object o) {
        System.out.println(o.toString());
    }
}
