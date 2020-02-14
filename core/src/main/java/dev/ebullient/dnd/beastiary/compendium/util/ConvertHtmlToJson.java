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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import dev.ebullient.dnd.beastiary.compendium.Monster;
import dev.ebullient.dnd.beastiary.compendium.MonsterAttack;
import dev.ebullient.dnd.beastiary.compendium.MonsterDamage;
import dev.ebullient.dnd.beastiary.compendium.Multiattack;
import dev.ebullient.dnd.mechanics.Ability;
import dev.ebullient.dnd.mechanics.HitPoints;
import dev.ebullient.dnd.mechanics.Size;
import dev.ebullient.dnd.mechanics.Type;

/**
 * One time conversion of a heaping pile of HTML into a tidy JSON compendium.
 * This is just for posterity, so whoever reads this can weep.
 *
 *
 */
public class ConvertHtmlToJson {

    final Pattern HIT = Pattern.compile("(\\d+\\s*[-+d()0-9 ]*)\\s*(\\w+)\\s*damage");
    final Pattern ATTACK = Pattern.compile(".*Attack: ([-+0-9]+) .*");
    final Pattern DC = Pattern.compile("a DC (\\d+) (\\w+) saving throw");
    final Pattern SPELL_SAVE_DC = Pattern.compile("spell save DC (\\d+)\\b");

    Map<String, String> sanitizedAttacks = new HashMap<>();
    Map<String, String> pluralSingular = new HashMap<>();

    public static void main(String[] args) throws Exception {
        ConvertHtmlToJson converter = new ConvertHtmlToJson();
        Map<String, Monster> compendium = new HashMap<>();

        Path basePath = FileSystems.getDefault().getPath("./monsters/");

        // converter.createMonster(basePath.resolve("5e_SRD:Kraken").toFile());
        // System.exit(1);

        try (BufferedReader br = new BufferedReader(new FileReader(basePath.resolve("list.txt").toFile()))) {
            br.lines().forEach(line -> {
                // Remove creatures with no attack actions
                if (line.contains("Deck_of_Many_Things")
                        || line.contains("Rug_of_Smothering")
                        || line.contains("Shrieker")) {
                    return;
                }
                File f = basePath.resolve(line).toFile();
                try {
                    // log(f.getAbsolutePath());
                    Monster m = converter.createMonster(f);
                    Map<String, MonsterAttack> actions = m.getActions();
                    if (actions == null || actions.isEmpty()) {
                        throw new IllegalArgumentException("Monster has no defined actions: " + m);
                    }
                    compendium.put(toLower(m.getName()), m);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1); // fail fast
                }
            });
        }

        log(compendium.size());
        ObjectMapper mapper = new ObjectMapper()
                .setSerializationInclusion(Include.NON_EMPTY);

        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

        writer.writeValue(Paths.get("./src/main/resources/compendium.json").toFile(), compendium);

        Map<String, Monster> single = new HashMap<>();
        single.put("owlbear", compendium.get("owlbear"));
        writer.writeValue(Paths.get("./src/test/resources/owlbear.json").toFile(), single);

        single.clear();
        single.put("young red dragon", compendium.get("young red dragon"));
        writer.writeValue(Paths.get("./src/test/resources/dragon.json").toFile(), single);

        single.clear();
        single.put("erinyes", compendium.get("erinyes"));
        writer.writeValue(Paths.get("./src/test/resources/erinyes.json").toFile(), single);

        single.clear();
        single.put("lamia", compendium.get("lamia"));
        writer.writeValue(Paths.get("./src/test/resources/lamia.json").toFile(), single);

        single.clear();
        single.put("wraith", compendium.get("wraith"));
        writer.writeValue(Paths.get("./src/test/resources/wraith.json").toFile(), single);

        single.clear();
        single.put("gibbering mouther", compendium.get("gibbering mouther"));
        writer.writeValue(Paths.get("./src/test/resources/gibbering-mouther.json").toFile(), single);
    }

    public Monster createMonster(File f) throws Exception {
        Monster monster = new Monster();
        Map<String, String> description = new HashMap<>();

        Document doc = Jsoup.parse(f, "UTF-8");

        Elements headlines = doc.getElementsByClass("mw-headline");
        Element title = headlines.first();
        monster.setName(title.text().replaceAll("\\(.*", "").trim());
        Element beginning = title.parent();

        // table
        Element outerTable = beginning.nextElementSibling();
        if (!"table".equals(outerTable.tagName())) {
            throw new IllegalArgumentException("Unexpected document structure, can't find table");
        }
        Element data = outerTable.children().first() // tbody
                .children().first() // tr
                .children().first(); // td

        if (!data.text().contains("Armor Class")) {
            throw new IllegalArgumentException("Data doesn't match (no Armor Class)\n" + data.text());
        }

        List<Element> notes = new ArrayList<>();
        List<Element> actions = new ArrayList<>();
        List<Element> legend = new ArrayList<>();
        List<Element> reactions = new ArrayList<>();
        int section = 0;
        Element next = data.children().first();
        while (next != null) {
            if ("hr".equals(next.tagName())) {
                section++;
            } else if ("h4".equals(next.tagName())) {
                switch (next.text()) {
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
                switch (section) {
                    case 0:
                        getSizeAndType(monster, next, description);
                        break;
                    case 1:
                        getBasicStats(monster, next, description);
                        break;
                    case 2:
                        getScoresAndModifiers(monster, next, description);
                        break;
                    case 3:
                        getSavingThrows(monster, next, description);
                        break;
                    case 4:
                        notes.add(next);
                        break;
                    case 50:
                        actions.add(next);
                        break;
                    case 60:
                        legend.add(next);
                        break;
                    case 70:
                        reactions.add(next);
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "Data doesn't match (unexpected number of sections)\n" + next.text());
                }
            }
            next = next.nextElementSibling();
        }

        List<String> noteText = new ArrayList<>();

        // Look for additional background information
        next = data.nextElementSibling();
        if ("td".equals(next.tagName())) {
            noteText.clear();
            for (Node n : next.childNodes()) {
                String text = getNodeText(n);
                if (!text.isEmpty()) {
                    noteText.add(getNodeText(n));
                }
            }
            if (!noteText.isEmpty()) {
                description.put("Background", String.join("\n", noteText));
            }
        }

        // Gather together notes from above, with any additional notes that follow
        if (!notes.isEmpty()) {
            noteText.clear();
            for (Element e : notes) {
                noteText.add(e.text());
            }
            String allNotes = String.join("\n", noteText);
            Matcher m = SPELL_SAVE_DC.matcher(allNotes);
            if (m.find()) {
                monster.setSpellSaveDC(Integer.parseInt(m.group(1)));
            }
            description.put("Notes", allNotes);
        }

        // Gether together legendary actions (for use)
        if (!legend.isEmpty()) {
            noteText.clear();
            for (Element e : legend) {
                noteText.add(e.text());
            }
            description.put("Legendary Actions", String.join("\n", noteText));
        }

        parseActions(monster, actions, description);
        parseReactions(monster, reactions, description);

        monster.setDescription(description);

        return monster;
    }

    private void parseReactions(Monster monster, List<Element> reactions, Map<String, String> description) {
    }

    void parseActions(Monster monster, List<Element> elements, Map<String, String> description) {
        List<String> noteText = new ArrayList<>();
        Map<String, MonsterAttack> attacks = new HashMap<>();
        String multiattack = null;

        nextelement: for (Element e : elements) {
            String all = e.text().trim();
            noteText.add(all);

            if (all.contains("one of the following")) {
                continue;
            }

            String[] segments = all.replaceAll("ft\\.", "ft").split("\\.\\s*");
            if ("Multiattack".equalsIgnoreCase(segments[0])) {
                if (segments.length < 2) {
                    throw new IllegalArgumentException("Unexpected Multiattack definition: " + all);
                }
                List<String> slice = Arrays.asList(segments).subList(1, segments.length);
                multiattack = String.join(". ", slice);
            } else if (all.contains("Attack")) {
                String name = segments[0];

                MonsterAttack a = new MonsterAttack();
                a.setName(name.replaceAll("\\(.*", "").trim());

                List<MonsterDamage> damage = new ArrayList<>();

                List<String> slice = Arrays.asList(segments).subList(1, segments.length);
                for (String s : slice) {
                    if (s.startsWith("Hit")) {
                        Matcher m = HIT.matcher(s);
                        if (m.find()) {
                            MonsterDamage d = new MonsterDamage();
                            d.setAmount(m.group(1).replaceAll("\\s+", ""));
                            d.setType(m.group(2).trim());
                            d.setDescription(s);
                            damage.add(d);
                        } else if (s.contains("cursed")) {
                            MonsterDamage d = parseDamage(all, a);
                            damage.add(d);
                        } else {
                            log("Unexpected Hit definition: " + s);
                            break nextelement;
                        }
                    } else if (s.contains("Attack:")) {
                        a.setMelee(s.contains("Melee"));
                        Matcher m = ATTACK.matcher(s);
                        if (m.matches()) {
                            a.setAttackModifier(Integer.parseInt(m.group(1)));
                        } else {
                            throw new IllegalArgumentException("Unexpected Attack definition: " + s);
                        }
                    } else if (s.startsWith("The target must") && s.contains("damage") && s.contains("saving throw")) {
                        log("Additional Effect: " + s);
                        MonsterDamage d = parseDamage(s, null);
                        if (d != null) {
                            a.setAdditionalEffect(d);
                        }
                    }
                }

                if (damage.isEmpty() || damage.size() > 1) {
                    throw new IllegalArgumentException("Incorrect definition of damage: \n"
                            + damage + "\n"
                            + all);
                }

                a.setDamage(damage.get(0));
                a.setDescription(all);
                attacks.put(sanitizeAttack(name, true), a);
            } else if (all.contains("Recharge")) {
                String name = segments[0];
                MonsterAttack a = new MonsterAttack();
                MonsterDamage d = parseDamage(all, a);
                if (d != null) {
                    a.setName(name.replaceAll("\\(.*", "").trim());
                    a.setDamage(d);
                    a.setDescription(all);
                    attacks.put(sanitizeAttack(name, true), a);
                }
            }
        }

        if (multiattack != null) {
            monster.setMultiattack(parseMultiattack(multiattack, attacks));
        }
        monster.setActions(attacks);
        description.put("Attacks", String.join("\n", noteText));
    }

    private MonsterDamage parseDamage(String s, MonsterAttack a) {
        MonsterDamage d = new MonsterDamage();
        d.setDescription(s);

        if (s.contains("for 1 minute")) {
            d.setDuration(10);
        } else if (s.contains("for 1 hour")) {
            d.setDuration(60);
        } else if (s.contains("until the end of the mouther's next turn")) {
            d.setDuration(1);
        }

        Matcher m1 = DC.matcher(s);
        if (m1.find()) {
            if (a == null) {
                d.setSavingThrow(Ability.convert(m1.group(2)) + "(" + m1.group(1) + ")");
            } else {
                a.setSavingThrow(Ability.convert(m1.group(2)) + "(" + m1.group(1) + ")");
            }

            Matcher m2 = HIT.matcher(s);
            if (m2.find()) {
                d.setAmount(m2.group(1).replaceAll("\\s+", ""));
                d.setType(m2.group(2).trim());
                return d;
            } else if (s.contains("have its hit point maximum reduced") || s.contains("its hit point maximum is reduced")) {
                d.setType("hpdrain");
                d.setAmount("");
                return d;
            } else if (s.contains("blinded")) {
                d.setType("blinded");
                d.setAmount("");
                return d;
            } else if (s.contains("frightened")) {
                d.setType("frightened");
                d.setAmount("");
                return d;
            } else if (s.contains("possessed")) {
                d.setType("possessed");
                d.setAmount("");
                return d;
            } else if (s.contains("restrained")) {
                d.setType("restrained");
                d.setAmount("");
                return d;
            } else if (s.contains("it can't make more than one attack on its turn")) {
                d.setType("slowed");
                d.setAmount("");
                return d;
            } else {
                log("Unexpected DC damage definition: " + s);
                throw new IllegalArgumentException();
            }
        } else if (s.contains("cursed")) {
            d.setType("cursed");
            d.setAmount("");
            d.setDisadvantage(getDisadvantage(toLower(s)));
            return d;
        } else {
            log("Different kind of DC check: " + s);
        }
        return null;
    }

    private List<Ability> getDisadvantage(String s) {
        if (s.contains("disadvantage")) {
            List<Ability> list = new ArrayList<>();
            if (s.contains("strength")) {
                list.add(Ability.STR);
            }
            if (s.contains("dexterity")) {
                list.add(Ability.DEX);
            }
            if (s.contains("constitution")) {
                list.add(Ability.CON);
            }
            if (s.contains("intelligence")) {
                list.add(Ability.INT);
            }
            if (s.contains("wisdom")) {
                list.add(Ability.WIS);
            }
            if (s.contains("charisma")) {
                list.add(Ability.CHA);
            }
            return list;
        }
        return Collections.emptyList();
    }

    public Multiattack parseMultiattack(String multiattack, Map<String, MonsterAttack> attacks) {
        final Pattern LIST_ATTACKS = Pattern.compile("([-a-z]+)(?: melee)? attacks(?:\\:|,)\\s+(.*)");
        final Pattern EITHER_ATTACK = Pattern.compile("\\beither (with its [-a-z]+) or (its [-a-z]+)(?:\\b|\\.)");
        final Pattern REPEAT_WITH_ATTACK = Pattern.compile("\\b([-a-z]+) attacks? with its ([-a-z]+)(?:\\b|\\.)");
        final Pattern WITH_ATTACK = Pattern.compile("\\b([-a-z]+) with its ([-a-z]+)(?:\\b|\\.)");
        final Pattern REPEAT_ATTACK = Pattern.compile("\\b([-a-z]+) ([-a-z]+) attacks?(?:\\b|\\.)");
        final Pattern VARIABLE_ATTACK = Pattern.compile("\\b([d0-9]+) ([-a-z]+) attacks?(?:\\b|\\.)");
        final Pattern USES_ATTACK = Pattern.compile("\\buses its ([-a-z]+)(?: ([-a-z]+))?");
        final Pattern MULTIATTACK_STR = Pattern.compile("(([d0-9]+)\\*)([-a-z]+)");

        // dump completely unparsable alternatives. Maybe someday
        // drop "The whatever makes .. " prefix
        // drop "when .. ", "while .. ", and "if .. " clauses
        String lower = toLower(multiattack)
                .replaceFirst("alternat(?:iv)?ely,.*$", "")
                .replaceFirst("^.* makes ", "")
                .replaceAll("\\. when .*$", "")
                .replaceAll("\\. while .*$", "")
                .replaceAll("\\. if .*$", "")
                .replaceAll(". in hybrid form.*$", "")
                .replaceAll(", if it can,", "")
                .replaceAll("only one of which can be a bite attack", "only-one-bite")
                .replaceAll("ranged ", "")
                .trim();

        // deal with multi-word attack names, matching word boundary
        for (Map.Entry<String, String> attack : sanitizedAttacks.entrySet()) {
            lower = lower.replaceAll(attack.getKey() + "\\b", attack.getValue());
        }
        //log("*: " + lower);

        int total = 0;
        Matcher m;
        String clause = "";

        int pos = lower.indexOf(".");
        if (pos > 0) {
            clause = lower.substring(pos);
            lower = lower.substring(0, pos + 1);
        }

        // blech, clauses suck.
        if (lower.contains("as many ")) {
            log("INDETERMINATE: " + lower);
            return null;
        }

        // x attacks: ...
        m = LIST_ATTACKS.matcher(lower);
        if (m.matches()) {
            total = wordToInt(m.group(1));
            lower = m.group(2);
        }
        // log("   1: " + lower);

        StringBuffer sb = new StringBuffer();

        // Either/or phrasing --> one with its  .. or one with its ..
        if (lower.contains("either")) {
            m = EITHER_ATTACK.matcher(lower);
            sb = new StringBuffer();
            while (m.find()) {
                m.appendReplacement(sb, "one " + m.group(1) + " or one with " + m.group(2));
            }
            m.appendTail(sb);
            lower = sb.toString();
            // log("   -: " + lower);
        }

        // uses its ...
        if (lower.contains("uses")) {
            m = USES_ATTACK.matcher(lower);
            sb = new StringBuffer();
            while (m.find()) {
                String wordNum = m.group(2);
                if (wordNum == null) {
                    wordNum = "once";
                }
                m.appendReplacement(sb, wordToInt(wordNum) + "*" + m.group(1));
            }
            m.appendTail(sb);
            lower = sb.toString();
            // log("   -: " + lower);
        }

        if (lower.contains("with its")) {
            m = REPEAT_WITH_ATTACK.matcher(lower);
            sb = new StringBuffer();
            while (m.find()) {
                m.appendReplacement(sb, wordToInt(m.group(1)) + "*" + m.group(2));
            }
            m.appendTail(sb);
            lower = sb.toString();
            // log("   2: " + lower);

            m = WITH_ATTACK.matcher(lower);
            sb = new StringBuffer();
            while (m.find()) {
                m.appendReplacement(sb, wordToInt(m.group(1)) + "*" + m.group(2));
            }
            m.appendTail(sb);
            lower = sb.toString();
            // log("   3: " + lower);
        }

        m = VARIABLE_ATTACK.matcher(lower);
        sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, m.group(1) + "*" + m.group(2));
        }
        m.appendTail(sb);
        lower = sb.toString();

        m = REPEAT_ATTACK.matcher(lower);
        sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, wordToInt(m.group(1)) + "*" + m.group(2));
        }
        m.appendTail(sb);
        lower = sb.toString();
        //log("   4: " + lower);

        // hangers on
        lower = lower.replace("three attacks", "3*melee")
                .replace("one to constrict", "1*constrict")
                .replaceAll("\\s+", " ")
                .replaceAll("\\.", "")
                .trim();

        // Down to choices now..
        List<String> combinations = new ArrayList<>();

        // HACK HACK HACK. But seriously, I'm done with this.
        if (clause.contains("it can make one tail attack in place of its two claw attacks")) { // dragon turtle
            combinations.add("1*bite 2*claws");
            combinations.add("1*bite 1*tail");
        } else if (clause.contains("it can replace one of those attacks with a bite attack")) { // drider
            combinations.add("3*longsword");
            combinations.add("3*longbow");
            combinations.add("1*bite 2*longsword");
        } else if (clause.contains("it can use hurl-flame in place of any melee attack")) {
            combinations.add("2*fork 1*tail");
            combinations.add("2*fork 1*hurl-flame");
            combinations.add("1*fork 1*hurl-flame 1*tail");
            // } else if ( clause.contains("it can use its swallow instead of its bite") ) {
            //     combinations.add("1*bite 2*claws 1*horns 1*tail");
            //     combinations.add("1*swallow 2*claws 1*horns 1*tail");
        } else if (clause.contains("it can use its life-drain in place of one longsword attack")) {
            combinations.add("2*longsword");
            combinations.add("1*longsword 1*life-drain");
            combinations.add("2*longbow");
        } else if (lower.contains("each of which it can replace with one use of fling")) {
            combinations.add("3*tentacle");
        } else if ("1*pike and 1*hooves or 2*longbow".equals(lower)) {
            combinations.add("1*pike and 1*hooves");
            combinations.add("2*longbow");
        } else if ("2*scimitar or 2*hurl-flame".equals(lower)) {
            combinations.add("2*scimitar");
            combinations.add("2*hurl-flame");
        } else if ("1*claw and 1*dagger or intoxicating-touch".equals(lower)) {
            combinations.add("1*claws 1*dagger");
            combinations.add("1*claws 1*intoxicating-touch");
        } else if ("1*bite and 2*claws or 3*tail-spikes".equals(lower)) {
            combinations.add("1*bite 2*claws");
            combinations.add("3*tail-spikes");
        } else if ("either 3*melee — 1*snake-hair and 2*shortsword — or 2*longbow".equals(lower)) {
            combinations.add("1*snake-hair 2*shortsword");
            combinations.add("2*longbow");
        } else if ("1*bite and 1*claw or harpoon".equals(lower)) {
            combinations.add("1*bite 1*claw");
            combinations.add("1*bite 1*harpoon");
        } else if ("1*claw or 1*glaive".equals(lower)) {
            combinations.add("2*claw");
            combinations.add("2*glaive");
        } else if ("1*bite and 1*claw or spear".equals(lower)) {
            combinations.add("1*bite 1*claw");
            combinations.add("1*bite 1*spear");
        } else if ("1*longsword or 1*longbow".equals(lower)) {
            combinations.add("1*longsword");
            combinations.add("1*longbow");
        } else if ("1*pike and 1*hoove or 2*longbow".equals(lower)) {
            combinations.add("1*pike 1*hoove");
            combinations.add("2*longbow");
        } else if ("1*bite and 2*claw or 3*tail-spike".equals(lower)) {
            combinations.add("1*bite 2*claw");
            combinations.add("3*tail-spike");
        } else if ("each one with a different weapon".equals(lower)) {
            combinations.add("1*spiked-shield 1*javelin");
            combinations.add("1*spiked-shield 1*bite");
            combinations.add("1*spiked-shield 1*heavy-club");
            combinations.add("1*javelin 1*bite");
            combinations.add("1*javelin 1*heavy-club");
            combinations.add("1*bite 1*heavy-club");
        } else if ("only-one-bite".equals(lower)) {
            combinations.add("1*bite 1*claw");
        } else if (lower.contains("either") || lower.contains(" or ")) {
            log("Should handle this: " + lower + " with clause " + clause);
        } else {
            lower = lower.replaceAll("and", "")
                    .replaceAll(",", "")
                    .replaceAll("\\.", "")
                    .trim();
            // only one choice!
            combinations.add(lower);
        }

        if (combinations.isEmpty()) {
            throw new IllegalArgumentException("bad multiattack string: " + multiattack);
        }

        for (int i = 0; i < combinations.size(); i++) {
            int count = 0;
            String s = combinations.get(i);
            m = MULTIATTACK_STR.matcher(s);
            sb = new StringBuffer();
            while (m.find()) {
                String key = sanitizeAttack(m.group(3), false);
                m.appendReplacement(sb, m.group(1) + checkExists(key, attacks));
                if (total > 0) {
                    count += Integer.parseInt(m.group(2));
                }
            }
            m.appendTail(sb);
            lower = sb.toString();
            combinations.set(i, lower);
            if (total > 0 && count != total) {
                log("WARNING: Multiattack doesn't match total allowed (" + total + "): " + multiattack + " --> " + lower);
            }
        }

        Multiattack attack = new Multiattack();
        attack.setCombinations(combinations);
        return attack;
    }

    private String checkExists(String name, Map<String, MonsterAttack> attacks) {
        if ("melee".equals(name)) {
            return name;
        }

        if (attacks.get(name) != null) {
            return name;
        }

        if (name.endsWith("er")) {
            String sting = name.substring(0, name.length() - 2);
            if (attacks.get(sting) != null) {
                return sting;
            }
        }

        throw new IllegalStateException(name + " not found in " + attacks.keySet());
    }

    public int wordToInt(String number) {
        switch (toLower(number)) {
            case "one":
                return 1;
            case "once":
                return 1;
            case "two":
                return 2;
            case "twice":
                return 2;
            case "three":
                return 3;
            case "four":
                return 4;
            case "five":
                return 5;
            case "six":
                return 6;
            case "seven":
                return 7;
            default:
                throw new IllegalArgumentException("Should understand this number: " + number);
        }
    }

    void getSavingThrows(Monster monster, Element parent, Map<String, String> description) {
        final Pattern perception = Pattern.compile("Perception (\\d+)");

        List<String> slice;
        String[] segments = parent.html().split("\\s*<br[^>]*>\\s*");

        for (String s : segments) {
            String body = Jsoup.parseBodyFragment(s).body().text().trim();
            String[] chunks = body.split("\\s+");
            switch (chunks[0]) {
                case "Saving":
                    slice = Arrays.asList(chunks).subList(2, chunks.length);
                    if ((slice.size() % 2) != 0) {
                        throw new IllegalArgumentException("uneven number of elements" + slice);
                    }
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < slice.size(); i = i + 2) {
                        sb.append(slice.get(i).toUpperCase(Locale.ROOT))
                                .append("(")
                                .append(slice.get(i + 1).replace(",", ""))
                                .append(")");
                        if (i + 2 < slice.size()) {
                            sb.append(",");
                        }
                    }
                    description.put("Saving Throws", sb.toString());
                    monster.setSavingThrows(sb.toString());
                    break;
                case "Skills":
                    slice = Arrays.asList(chunks).subList(1, chunks.length);
                    description.put("Skills", String.join(" ", slice));
                    break;
                case "Senses":
                    slice = Arrays.asList(chunks).subList(1, chunks.length);
                    String value = String.join(" ", slice);
                    description.put("Senses", value);
                    // passive perception
                    Matcher m = perception.matcher(value);
                    if (m.find()) {
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
                    if (body.contains("Resist")) {
                        description.put("Damage Resistance", body);
                    } else if (body.contains("Vulner")) {
                        description.put("Damage Vulnerability", body);
                    } else if (body.contains("Immun")) {
                        description.put("Damage Immunity", body);
                    } else {
                        throw new IllegalArgumentException("Unknown Damage resistance or vulnerability: " + body);
                    }
                    break;
                case "Condition":
                    if (body.contains("Immun")) {
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

        if (elements.length != 12) {
            throw new IllegalArgumentException(
                    "Data doesn't match expected description of scores/modifiers\n" + statsRow.text());
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
        if (lines.length != 3) {
            throw new IllegalArgumentException("Unexpected statistics string: " + parent.html());
        }

        String acString = Jsoup.parseBodyFragment(lines[0]).body().text().trim();
        description.put("Armor Class", acString.replace("Armor Class ", ""));
        Matcher m1 = AC.matcher(acString);
        if (m1.matches()) {
            monster.setArmorClass(Integer.parseInt(m1.group(1)));
        } else {
            throw new IllegalArgumentException("Unexpected armor class definition: " + acString);
        }

        String hpString = Jsoup.parseBodyFragment(lines[1]).body().text().trim();
        description.put(HP, hpString.replace("Hit Points ", ""));
        monster.setHitPoints(HitPoints.validate(hpString.substring(HP.length()).trim()));

        String speedString = Jsoup.parseBodyFragment(lines[2]).body().text().trim();
        description.put(SPEED, speedString.replace("Speed ", ""));
    }

    void getSizeAndType(Monster monster, Element parent, Map<String, String> description) {
        final Pattern sizeTypeAlign = Pattern.compile("(\\w+) (\\w+).*, ([^,]+)");

        Matcher m = sizeTypeAlign.matcher(parent.text().trim());
        if (m.matches()) {
            monster.setSize(Size.valueOf(m.group(1).toUpperCase(Locale.ROOT)));
            monster.setType(Type.valueOf(m.group(2).toUpperCase(Locale.ROOT)));
            monster.setAlignment(m.group(3));
        } else {
            throw new IllegalArgumentException("Bad size/type string: " + parent.text());
        }

        description.put("General", parent.text());
        //log(parent.text() + "\n   --> " + monster);
    }

    String getNodeText(Node n) {
        if (n instanceof TextNode) {
            return ((TextNode) n).text().trim();
        } else if (n instanceof Element) {
            return ((Element) n).text().trim();
        }

        throw new IllegalArgumentException("Unexpected node type " + n.getClass());
    }

    String sanitizeAttack(String s, boolean write) {
        // Trim any parenthical stuff, we end up ignoring it later anyway
        String key = toLower(s).replaceAll("\\(.*\\)", "").trim();

        // replace any spaces with dashes (to keep/preserve two-word attacks, e.g. rotting touch or dreadful glare)
        // use some keys consistently
        String value = key.replaceAll(" ", "-")
                .replaceAll("(.*)s\\b", "$1");

        if (!key.equals(value) && write) {
            sanitizedAttacks.put(key, value);
            sanitizedAttacks.put(key + "s", value);
        }
        return value;
    }

    static void log(String msg, String... vals) {
        System.out.println(String.format(msg, vals));
    }

    static void log(Object o) {
        System.out.println(o.toString());
    }

    static String toLower(String s) {
        return s.toLowerCase(Locale.ROOT);
    }

    static String toUpper(String s) {
        return s.toUpperCase(Locale.ROOT);
    }
}
