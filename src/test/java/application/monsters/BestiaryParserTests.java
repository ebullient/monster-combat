package application.monsters;

import org.junit.Assert;
import org.junit.Test;

public class BestiaryParserTests {

    @Test
    public void testBasicParser() throws Exception {
        Beastiary beastiary = new Beastiary();

        BeastiaryParser p = new BeastiaryParser();
        p.parse(beastiary);

        Monster m = beastiary.getRandomMonster();
        Assert.assertFalse("Random monster name should not contain 'Generated': " + m.getName(), m.getName().startsWith("Generated"));
    }
}
