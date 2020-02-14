package dev.ebullient.dnd.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import dev.ebullient.dnd.mechanics.Size;
import dev.ebullient.dnd.mechanics.Type;

@JsonIgnoreProperties(ignoreUnknown = true)
class Combatant implements dev.ebullient.dnd.combat.Combatant {

    String name;
    int cr;
    int armorClass;
    Size size;
    Type type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCR() {
        return cr;
    }

    public void setCR(int cr) {
        this.cr = cr;
    }

    public int getArmorClass() {
        return armorClass;
    }

    public void setArmorClass(int armorClass) {
        this.armorClass = armorClass;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
