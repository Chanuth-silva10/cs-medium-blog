package io.chanuth.clubs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mongodb.DBRef;
import io.chanuth.clubs.model.RunningClub;
import io.chanuth.clubs.repository.ClubRepository;
import io.chanuth.clubs.repository.RunnerRepository;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Needs a running MongoDB (see the README). Uses its own database, emptied before every test. */
@SpringBootTest(properties = "spring.mongodb.database=devcraft_clubs_test")
@AutoConfigureMockMvc
class ClubsApiTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    JsonMapper json;
    @Autowired
    MongoTemplate mongo;
    @Autowired
    ClubRepository clubs;
    @Autowired
    RunnerRepository runners;

    @BeforeEach
    void emptyDatabase() {
        clubs.deleteAll();
        runners.deleteAll();
    }

    // ---------- helpers ----------

    JsonNode postJson(String path, String body, int expectedStatus) throws Exception {
        String response = mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is(expectedStatus)).andReturn().getResponse().getContentAsString();
        return response.isBlank() ? null : json.readTree(response);
    }

    JsonNode getJson(String path) throws Exception {
        return json.readTree(mvc.perform(get(path)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    static String clubBody(String name, String code, String city) {
        return """
                {"name":"%s","code":"%s","address":{"city":"%s","postalCode":"10000","street":"1 Main Street"}}
                """.formatted(name, code, city);
    }

    static String runnerBody(String name, String birthDate, String category, boolean active) {
        return """
                {"name":"%s","birthDate":"%s","category":"%s","active":%s}
                """.formatted(name, birthDate, category, active);
    }

    String createClub(String name, String code, String city) throws Exception {
        return postJson("/clubs", clubBody(name, code, city), 201).get("id").asString();
    }

    String createRunner(String name, String birthDate, String category, boolean active) throws Exception {
        return postJson("/runners", runnerBody(name, birthDate, category, active), 201).get("id").asString();
    }

    // ---------- Part 1: how the data is stored ----------

    @Test
    void aClubIsStoredWithANestedAddressAndReferencesToItsRunners() throws Exception {
        String runnerId = createRunner("Dinuka Alwis", "1998-04-12", "MARATHON", true);
        String clubId = createClub("Matara Sunrise Runners", "MSR", "Matara");
        postJson("/clubs/" + clubId + "/members", "[\"" + runnerId + "\"]", 200);

        Document raw = mongo.getCollection("clubs").find(new Document("_id", new ObjectId(clubId))).first();
        assertThat(raw.get("_id")).isInstanceOf(ObjectId.class);
        assertThat(raw.get("address")).isInstanceOf(Document.class);
        assertThat(raw.get("address", Document.class).getString("city")).isEqualTo("Matara");
        // The address is inside the club. The members are only references: a collection name and an id.
        List<?> members = raw.getList("members", Object.class);
        assertThat(members).hasSize(1);
        DBRef reference = (DBRef) members.get(0);
        assertThat(reference.getCollectionName()).isEqualTo("runners");
        assertThat(reference.getId()).isEqualTo(new ObjectId(runnerId));
    }

    @Test
    void enumsAreStoredAsTextAndDatesAsMidnightUtc() throws Exception {
        String runnerId = createRunner("Dinuka Alwis", "1998-04-12", "MARATHON", true);

        Document raw = mongo.getCollection("runners").find(new Document("_id", new ObjectId(runnerId))).first();
        assertThat(raw.getString("category")).isEqualTo("MARATHON");
        // The same instant on every machine, whatever the time zone of the server.
        assertThat(raw.get("birthDate", Date.class).toInstant()).isEqualTo(Instant.parse("1998-04-12T00:00:00Z"));
    }

    @Test
    void theIndexesDeclaredOnTheClassesAreCreated() {
        List<IndexInfo> clubIndexes = mongo.indexOps("clubs").getIndexInfo();
        assertThat(clubIndexes).filteredOn((index) -> index.getName().equals("name") || index.getName().equals("code"))
                .hasSize(2).allMatch(IndexInfo::isUnique);
        assertThat(mongo.indexOps("runners").getIndexInfo()).extracting(IndexInfo::getName)
                .contains("name", "category");
    }

    @Test
    void uniqueIndexesTurnADuplicateNameOrCodeIntoA409() throws Exception {
        createClub("Matara Sunrise Runners", "MSR", "Matara");
        postJson("/clubs", clubBody("Matara Sunrise Runners", "OTHER", "Matara"), 409);
        postJson("/clubs", clubBody("Another Name", "MSR", "Matara"), 409);
        assertThat(clubs.count()).isEqualTo(1);
    }

    @Test
    void codesAreStoredInUpperCase() throws Exception {
        JsonNode club = postJson("/clubs", clubBody("Matara Sunrise Runners", "msr", "Matara"), 201);
        assertThat(club.get("code").asString()).isEqualTo("MSR");
    }

    @Test
    void invalidBodiesAreRefusedWithClearMessages() throws Exception {
        mvc.perform(post("/clubs").contentType(MediaType.APPLICATION_JSON).content(clubBody("", "X", "")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.code").exists())
                .andExpect(jsonPath("$.errors['address.city']").exists());
        mvc.perform(post("/runners").contentType(MediaType.APPLICATION_JSON)
                        .content(runnerBody("A", "2999-01-01", "SPRINTER", true)))
                .andExpect(status().isUnprocessableContent()).andExpect(jsonPath("$.errors.birthDate").exists());
        // A category that does not exist, and a body that is not JSON at all.
        postJson("/runners", runnerBody("A", "2000-01-01", "SWIMMER", true), 400);
        postJson("/clubs", "{\"name\":", 400);
    }

    // ---------- Part 1: the timestamps ----------

    @Test
    void createdAtIsKeptAndUpdatedAtMovesWhenAClubIsUpdated() throws Exception {
        String id = createClub("Matara Sunrise Runners", "MSR", "Matara");
        RunningClub stored = clubs.findById(id).orElseThrow();
        Instant createdAt = stored.getCreatedAt();
        Instant updatedAt = stored.getUpdatedAt();
        assertThat(createdAt).isNotNull();
        assertThat(updatedAt).isNotNull();

        Thread.sleep(50);
        mvc.perform(put("/clubs/" + id).contentType(MediaType.APPLICATION_JSON)
                .content(clubBody("Matara Sunrise Runners", "MSR", "Galle"))).andExpect(status().isOk());

        RunningClub after = clubs.findById(id).orElseThrow();
        assertThat(after.getCreatedAt()).isEqualTo(createdAt);
        assertThat(after.getUpdatedAt()).isAfter(updatedAt);
        assertThat(after.getAddress().city()).isEqualTo("Galle");
    }

    // ---------- Part 1: members ----------

    @Test
    void membersAreAddedWithoutDuplicates() throws Exception {
        String clubId = createClub("Matara Sunrise Runners", "MSR", "Matara");
        String a = createRunner("Dinuka Alwis", "1998-04-12", "MARATHON", true);
        String b = createRunner("Sithara Ek", "2001-07-30", "SPRINTER", true);

        JsonNode first = postJson("/clubs/" + clubId + "/members", "[\"" + a + "\"]", 200);
        assertThat(first.get("members")).hasSize(1);
        // Adding again keeps the first runner and adds only the new one.
        JsonNode second = postJson("/clubs/" + clubId + "/members", "[\"" + a + "\",\"" + b + "\"]", 200);
        assertThat(second.get("members")).hasSize(2);
        assertThat(getJson("/clubs/" + clubId).get("members")).hasSize(2);
    }

    @Test
    void anUnknownRunnerStopsTheWholeRequest() throws Exception {
        String clubId = createClub("Matara Sunrise Runners", "MSR", "Matara");
        String a = createRunner("Dinuka Alwis", "1998-04-12", "MARATHON", true);

        postJson("/clubs/" + clubId + "/members", "[\"" + a + "\",\"" + new ObjectId() + "\"]", 404);
        assertThat(getJson("/clubs/" + clubId).get("members")).isEmpty();
    }

    @Test
    void deletingARunnerRemovesItFromEveryClub() throws Exception {
        String clubId = createClub("Matara Sunrise Runners", "MSR", "Matara");
        String a = createRunner("Dinuka Alwis", "1998-04-12", "MARATHON", true);
        String b = createRunner("Sithara Ek", "2001-07-30", "SPRINTER", true);
        postJson("/clubs/" + clubId + "/members", "[\"" + a + "\",\"" + b + "\"]", 200);

        mvc.perform(delete("/runners/" + a)).andExpect(status().isNoContent());

        JsonNode club = getJson("/clubs/" + clubId);
        assertThat(club.get("members")).hasSize(1);
        assertThat(club.get("members").get(0).get("id").asString()).isEqualTo(b);
        Document raw = mongo.getCollection("clubs").find().first();
        assertThat(raw.getList("members", Object.class)).hasSize(1);
    }

    @Test
    void deletingARunnerBehindTheApisBackLeavesAReferenceToNothing() throws Exception {
        String clubId = createClub("Matara Sunrise Runners", "MSR", "Matara");
        String a = createRunner("Dinuka Alwis", "1998-04-12", "MARATHON", true);
        postJson("/clubs/" + clubId + "/members", "[\"" + a + "\"]", 200);

        runners.deleteById(a);

        // The club document still holds the reference, because nothing removed it...
        Document raw = mongo.getCollection("clubs").find().first();
        assertThat(raw.getList("members", Object.class)).hasSize(1);
        // ...and when the club is loaded, the missing runner shows up as a null inside the set.
        RunningClub loaded = clubs.findById(clubId).orElseThrow();
        assertThat(loaded.getMembers()).hasSize(1).containsOnlyNulls();
    }

    @Test
    void deletingAClubLeavesItsRunners() throws Exception {
        String clubId = createClub("Matara Sunrise Runners", "MSR", "Matara");
        String a = createRunner("Dinuka Alwis", "1998-04-12", "MARATHON", true);
        postJson("/clubs/" + clubId + "/members", "[\"" + a + "\"]", 200);

        mvc.perform(delete("/clubs/" + clubId)).andExpect(status().isNoContent());

        mvc.perform(get("/runners/" + a)).andExpect(status().isOk());
        mvc.perform(get("/clubs/" + clubId)).andExpect(status().isNotFound());
    }

    @Test
    void bulkCreateAndUnknownOrMalformedIdsAreHandled() throws Exception {
        JsonNode created = postJson("/runners/bulk", "[" + runnerBody("A One", "2000-01-01", "TRAIL", true) + ","
                + runnerBody("B Two", "2001-01-01", "TRAIL", false) + "]", 201);
        assertThat(created).hasSize(2);
        assertThat(runners.count()).isEqualTo(2);

        mvc.perform(get("/clubs/" + new ObjectId())).andExpect(status().isNotFound());
        mvc.perform(get("/clubs/not-an-id")).andExpect(status().isNotFound());
        mvc.perform(get("/runners/not-an-id")).andExpect(status().isNotFound());
        mvc.perform(put("/runners/" + new ObjectId()).contentType(MediaType.APPLICATION_JSON)
                .content(runnerBody("A", "2000-01-01", "TRAIL", true))).andExpect(status().isNotFound());
    }
}
