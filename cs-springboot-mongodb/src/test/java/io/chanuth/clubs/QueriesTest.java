package io.chanuth.clubs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.chanuth.clubs.model.Address;
import io.chanuth.clubs.model.Runner;
import io.chanuth.clubs.model.RunnerCategory;
import io.chanuth.clubs.model.RunningClub;
import io.chanuth.clubs.repository.ClubRepository;
import io.chanuth.clubs.repository.RunnerRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The read side: query methods, sorting and paging, on a small fixed data set. */
@SpringBootTest(properties = "spring.mongodb.database=devcraft_clubs_test")
@AutoConfigureMockMvc
class QueriesTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    JsonMapper json;
    @Autowired
    ClubRepository clubs;
    @Autowired
    RunnerRepository runners;

    Runner nadiaP;
    Runner nadiaS;
    Runner kasun;
    Runner imani;
    Runner ravi;
    Runner hana;
    Runner malik;
    Runner isuri;

    @BeforeEach
    void loadData() {
        clubs.deleteAll();
        runners.deleteAll();
        nadiaP = new Runner("Nadia Perera", LocalDate.of(1990, 5, 10), RunnerCategory.SPRINTER, true);
        nadiaS = new Runner("Nadia Silva", LocalDate.of(1992, 3, 1), RunnerCategory.MARATHON, true);
        kasun = new Runner("Kasun Fernando", LocalDate.of(1994, 7, 15), RunnerCategory.SPRINTER, false);
        imani = new Runner("Imani Rahman", LocalDate.of(1996, 11, 30), RunnerCategory.TRAIL, true);
        ravi = new Runner("Ravi Kumar", LocalDate.of(1998, 2, 2), RunnerCategory.SPRINTER, true);
        hana = new Runner("Hana Bandara", LocalDate.of(2000, 6, 6), RunnerCategory.HURDLER, false);
        malik = new Runner("Malik Herath", LocalDate.of(2002, 9, 9), RunnerCategory.MARATHON, false);
        isuri = new Runner("Isuri Wickrama", LocalDate.of(2004, 12, 24), RunnerCategory.MIDDLE_DISTANCE, true);
        runners.saveAll(List.of(nadiaP, nadiaS, kasun, imani, ravi, hana, malik, isuri));

        RunningClub colombo = new RunningClub("Colombo Harriers", "CHAR", new Address("Colombo", "00100", "12 Galle Road"));
        colombo.addMembers(List.of(nadiaP, nadiaS, kasun));
        RunningClub night = new RunningClub("Colombo Night Owls", "CNOW", new Address("Colombo", "00700", "44 Union Place"));
        night.addMembers(List.of(nadiaP, ravi));
        RunningClub kandy = new RunningClub("Kandy Hill Runners", "KHRU", new Address("Kandy", "20000", "8 Temple Street"));
        kandy.addMembers(List.of(imani));
        clubs.saveAll(List.of(colombo, night, kandy));
    }

    JsonNode fetch(String path) throws Exception {
        return json.readTree(mvc.perform(get(path)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    static List<String> names(JsonNode array) {
        List<String> names = new ArrayList<>();
        array.forEach((item) -> names.add(item.get("name").asString()));
        return names;
    }

    @Test
    void findAllWithASortOrdersByTheGivenProperty() throws Exception {
        assertThat(names(fetch("/runners"))).first().isEqualTo("Hana Bandara");
        assertThat(names(fetch("/clubs"))).containsExactly("Kandy Hill Runners", "Colombo Night Owls", "Colombo Harriers");
    }

    @Test
    void twoConditionsInTheMethodName() throws Exception {
        assertThat(names(fetch("/runners?category=SPRINTER&active=true"))).containsExactlyInAnyOrder("Nadia Perera", "Ravi Kumar");
        assertThat(runners.findByCategoryAndActive(RunnerCategory.SPRINTER, false)).extracting(Runner::getName)
                .containsExactly("Kasun Fernando");
    }

    @Test
    void anyOfSeveralValues() throws Exception {
        assertThat(names(fetch("/runners/by-categories?categories=MARATHON,TRAIL")))
                .containsExactlyInAnyOrder("Nadia Silva", "Malik Herath", "Imani Rahman");
        mvc.perform(get("/runners/by-categories?categories=SWIMMER")).andExpect(status().isBadRequest());
    }

    @Test
    void containsIgnoringCase() throws Exception {
        assertThat(names(fetch("/runners?search=NADIA"))).containsExactlyInAnyOrder("Nadia Perera", "Nadia Silva");
        assertThat(names(fetch("/clubs?search=owls"))).containsExactly("Colombo Night Owls");
    }

    @Test
    void betweenLeavesOutBothEndsOfTheRange() throws Exception {
        // Nadia Silva (1992-03-01) and Ravi Kumar (1998-02-02) sit exactly on the limits.
        assertThat(names(fetch("/runners/born-between?from=1992-03-01&to=1998-02-02")))
                .containsExactly("Kasun Fernando", "Imani Rahman");
        // One day wider on each side brings them in.
        assertThat(names(fetch("/runners/born-between?from=1992-02-29&to=1998-02-03")))
                .containsExactly("Nadia Silva", "Kasun Fernando", "Imani Rahman", "Ravi Kumar");
        mvc.perform(get("/runners/born-between?from=nope&to=2000-01-01")).andExpect(status().isBadRequest());
    }

    @Test
    void firstAndTopNByOrder() throws Exception {
        assertThat(fetch("/runners/youngest").get("name").asString()).isEqualTo("Isuri Wickrama");
        assertThat(names(fetch("/runners/oldest"))).containsExactly("Nadia Perera", "Nadia Silva", "Kasun Fernando",
                "Imani Rahman", "Ravi Kumar");
    }

    @Test
    void countingWithoutLoadingTheDocuments() {
        assertThat(runners.countByCategory(RunnerCategory.SPRINTER)).isEqualTo(3);
        assertThat(runners.countByCategory(RunnerCategory.TRAIL)).isEqualTo(1);
    }

    @Test
    void nestedFieldsAndReferencesCanBeQueried() throws Exception {
        assertThat(names(fetch("/clubs?city=colombo"))).containsExactlyInAnyOrder("Colombo Harriers", "Colombo Night Owls");
        // Nadia Perera belongs to two clubs, Imani to one, Hana to none.
        assertThat(names(fetch("/clubs/of-runner/" + nadiaP.getId()))).containsExactlyInAnyOrder("Colombo Harriers", "Colombo Night Owls");
        assertThat(names(fetch("/clubs/of-runner/" + imani.getId()))).containsExactly("Kandy Hill Runners");
        assertThat(fetch("/clubs/of-runner/" + hana.getId())).isEmpty();
    }

    @Test
    void pagesStartAtZeroAndTheLastPageHoldsTheRest() throws Exception {
        JsonNode first = fetch("/runners/page?page=0&size=3&sort=name,asc");
        assertThat(first.get("totalElements").asInt()).isEqualTo(8);
        assertThat(first.get("totalPages").asInt()).isEqualTo(3);
        assertThat(names(first.get("content"))).containsExactly("Hana Bandara", "Imani Rahman", "Isuri Wickrama");

        JsonNode last = fetch("/runners/page?page=2&size=3&sort=name,asc");
        assertThat(names(last.get("content"))).containsExactly("Nadia Silva", "Ravi Kumar");

        assertThat(fetch("/runners/page?page=3&size=3").get("content")).isEmpty();
    }

    @Test
    void severalSortOrdersApplyInTurnAndEnumsSortByTheirText() throws Exception {
        JsonNode page = fetch("/runners/page?size=8&sort=category,asc&sort=name,desc");
        // HURDLER comes first although it is the last constant of the enum: MongoDB sorts the stored text.
        assertThat(names(page.get("content"))).containsExactly("Hana Bandara", "Nadia Silva", "Malik Herath",
                "Isuri Wickrama", "Ravi Kumar", "Nadia Perera", "Kasun Fernando", "Imani Rahman");
    }

    @Test
    void anUnknownSortPropertyAndANegativePageAreNotErrors() throws Exception {
        assertThat(fetch("/runners/page?size=3&sort=doesNotExist,asc").get("content")).hasSize(3);
        assertThat(fetch("/runners/page?page=-1&size=3").get("page").asInt()).isZero();
    }

    @Test
    void thePageSizeIsCapped() throws Exception {
        assertThat(fetch("/runners/page?size=100000").get("size").asInt()).isEqualTo(2000);
    }

    @Test
    void pagingCanSortByAnotherPropertyAndDescending() throws Exception {
        JsonNode page = fetch("/runners/page?size=2&sort=birthDate,desc");
        assertThat(names(page.get("content"))).containsExactly("Isuri Wickrama", "Malik Herath");
    }
}

