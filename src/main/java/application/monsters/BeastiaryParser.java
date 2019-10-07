/*
 * Copyright © 2019 IBM Corp. All rights reserved.
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
package application.monsters;

import java.io.InputStream;
import java.io.IOException;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BeastiaryParser extends DefaultHandler {
    static final ClassLoader source = BeastiaryParser.class.getClassLoader();
    static final Logger logger = LoggerFactory.getLogger(BeastiaryParser.class);

    private class Element {
        String name;
        StringBuilder text;

        Element(String qName) {
            this.name = qName;
            this.text = new StringBuilder();
        }
    }

    private MonsterMaker maker = new MonsterMaker();
    private Stack<Element> elements = new Stack<>();
    private int count = 0;
    private Beastiary beastiary;

    public void parse(Beastiary beastiary) throws IOException {
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            InputStream inputStream = source.getResourceAsStream("BeastiaryCompendium.xml");

            this.beastiary = beastiary;
            parser.parse(inputStream, this);
        } catch (SAXException | ParserConfigurationException e) {
            throw new IOException("Exception while parsing Bestiary", e);
        }
    }

    public void startDocument() throws SAXException {
        logger.info("Start parsing Bestiary");
    }

    public void endDocument() throws SAXException {
        logger.info("Done parsing Bestiary, found {} monsters", count);
    }

    public void startElement (String namespaceURI, String localName, String qName, Attributes attributes)
    throws SAXException
    {
        switch(qName) {
            case "monster":
                maker.ensureMonster();
                break;
            default:
                elements.push(new Element(qName));
                break;
        }
    }

    public void endElement (String namespaceURI, String localName, String qName)
    throws SAXException
    {
        switch(qName) {
            case "monster":
                Monster m = maker.make();
                if ( m.isValid() ) {
                    beastiary.addMonster(m);
                    logger.debug("Added {}", m);
                    count++;
                } else {
                    logger.debug("BAD MONSTER! {}", m.dumpStats());
                }
                break;

            default:
                if ( maker.isEmpty() ) {
                    return;
                }

                Element e = elements.pop();
                String text = e.text.toString();
                switch(e.name) {
                    case "name":
                        if ( "multiattack".equals(text.toLowerCase()) ) {
                            maker.enableMultiattack();
                        } else {
                            maker.setName(text);
                        }
                        break;
                    case "size":
                        maker.setSize(text);
                        break;
                    case "type":
                        maker.setType(text);
                        break;
                    case "ac":
                        maker.setArmorClass(text);
                        break;
                    case "hp":
                        maker.setHitPoints(text);
                        break;
                    case "str":
                        maker.setStrength(text);
                        break;
                    case "dex":
                        maker.setDexterity(text);
                        break;
                    case "con":
                        maker.setConstitution(text);
                        break;
                    case "int":
                        maker.setIntelligence(text);
                        break;
                    case "wis":
                        maker.setWisdom(text);
                        break;
                    case "cha":
                        maker.setCharisma(text);
                        break;
                    case "attack":
                        if ( ! text.isEmpty() ) {
                            maker.addAttack(text);
                        }
                        break;
                    case "passive":
                        maker.setPassivePerception(text);
                        break;
                    default:
                        elements.push(new Element(qName));
                        break;
                }
                break;
        }
    }

    public void characters(char ch[], int start, int length)
    throws SAXException {
        if ( ! maker.isEmpty() && elements.size() > 0) {
            Element e = elements.peek();
            e.text.append(new String(ch, start, length).trim());
        }
    }
}
