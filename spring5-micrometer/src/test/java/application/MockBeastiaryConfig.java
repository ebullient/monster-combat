package application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import application.monsters.Beastiary;
import application.monsters.MonsterMaker;

@Profile("mockBeastiary")
@Configuration
public class MockBeastiaryConfig {
    @Bean
    @Primary
    public Beastiary createMockBeastiary() {
        Beastiary beastiary = new Beastiary();
        MonsterMaker maker = new MonsterMaker();
        // Based on information digested from an XML compendium
        beastiary.addMonster(maker.make());
        beastiary.addMonster(maker.make());
        return beastiary;
    }
}
