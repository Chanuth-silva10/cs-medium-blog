package io.chanuth.clubs.seed;

import io.chanuth.clubs.model.Address;
import io.chanuth.clubs.model.Runner;
import io.chanuth.clubs.model.RunnerCategory;
import io.chanuth.clubs.model.RunningClub;
import io.chanuth.clubs.repository.ClubRepository;
import io.chanuth.clubs.repository.RunnerRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Loads demo data so the read queries have something to work on. It runs only when
 * app.seed.enabled=true and only when no club exists yet, so a restart never wipes real data.
 * The data is fixed (a seeded Random), so every run produces the same clubs and runners.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private static final String[][] CLUBS = {
        {"Colombo Harriers", "CHAR", "Colombo", "00100", "12 Galle Road"},
        {"Kandy Hill Runners", "KHRU", "Kandy", "20000", "8 Temple Street"},
        {"Galle Fort Striders", "GFST", "Galle", "80000", "3 Lighthouse Street"},
        {"Jaffna Dawn Club", "JDAW", "Jaffna", "40000", "21 Hospital Road"},
        {"Negombo Coast Athletics", "NCAT", "Negombo", "11500", "5 Beach Road"},
        {"Colombo Night Owls", "CNOW", "Colombo", "00700", "44 Union Place"},
    };
    private static final String[] FIRST = {"Nadia", "Kasun", "Imani", "Tharindu", "Sara", "Dilan", "Amaya", "Ravi",
        "Hana", "Malik", "Isuri", "Nuwan"};
    private static final String[] LAST = {"Perera", "Fernando", "Silva", "Jayasuriya", "Rahman", "Wickrama",
        "de Mel", "Kumar", "Bandara", "Herath"};

    private final ClubRepository clubs;
    private final RunnerRepository runners;

    public DemoDataSeeder(ClubRepository clubs, RunnerRepository runners) {
        this.clubs = clubs;
        this.runners = runners;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (clubs.count() > 0) {
            log.info("Clubs already exist, demo data skipped");
            return;
        }
        Random random = new Random(42);
        RunnerCategory[] categories = RunnerCategory.values();
        for (String[] club : CLUBS) {
            List<Runner> members = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                String name = FIRST[random.nextInt(FIRST.length)] + " " + LAST[random.nextInt(LAST.length)];
                LocalDate birthDate = LocalDate.of(1985 + random.nextInt(21), 1 + random.nextInt(12), 1 + random.nextInt(28));
                members.add(new Runner(name, birthDate, categories[random.nextInt(categories.length)], random.nextBoolean()));
            }
            RunningClub saved = new RunningClub(club[0], club[1], new Address(club[2], club[3], club[4]));
            saved.addMembers(runners.saveAll(members));
            clubs.save(saved);
        }
        log.info("Demo data loaded: {} clubs, {} runners", clubs.count(), runners.count());
    }
}
