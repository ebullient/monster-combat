package application.battle;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import application.mechanics.Dice;
import application.monsters.Attack;
import application.monsters.Monster;


class Participant {
    int initiative;
    int hitPoints;
    int heathPercentage;
    boolean surprised = false;

    @JsonIgnore
    Monster m;

    @JsonIgnore
    ArrayList<Participant> adversaries = new ArrayList<>();

    @JsonIgnore
    int maxHitPoints;

    Participant() {}

    Participant(Monster m) {
        this.m = m;
        initiative = Dice.d20() + m.getDexterityModifier(); // roll 20
        maxHitPoints = hitPoints = m.getHitPoints();
        heathPercentage = 100;
    }

    public String toString() {
        return String.format("%s (%s), AC: %d, HP: %d (%s), health: %d%%",
            m.getName(), m.getFullType(), m.getArmorClass(), hitPoints, m.getHitPointStats(), heathPercentage);
    }

    public int getHitPoints() {
        return hitPoints;
    }
    public void hit(int damage) {
        this.hitPoints -= damage;
        if ( this.hitPoints < 0 ) {
            this.hitPoints = 0;
            this.heathPercentage = 0;
        } else {
            this.heathPercentage = this.hitPoints * 100 / maxHitPoints;
        }
    }
    public int getHeathPercentage() {
        return this.heathPercentage;
    }
    public boolean isAlive() {
        return this.hitPoints > 0;
    }
    public int getInitiative() {
        return initiative;
    }
    public String getName() {
        return m.getName();
    }
    public boolean isSurprised() {
        return surprised;
    }
    public void setSurprised(boolean surprised) {
        this.surprised = surprised;
    }
    public String getType() {
        return m.getType();
    }
    public String getSize() {
		return m.getSize();
	}
    public int getArmorClass() {
        return m.getArmorClass();
    }
    public int getDexterity() {
        return m.getDexterity();
    }
    public int getPerception() {
        return m.getPassivePerception();
    }
    public List<Attack> attack() {
        return m.attack();
    }

	public Participant chooseTarget() {
        // Find the target with the highest hit points
        adversaries.sort((p1, p2) -> {
            return p2.hitPoints - p1.hitPoints;
        });

		return adversaries.get(0);
	}
}
