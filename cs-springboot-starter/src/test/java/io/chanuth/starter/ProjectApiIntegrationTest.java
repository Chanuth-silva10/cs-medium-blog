package io.chanuth.starter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Boots the whole app (Flyway migrations + Hibernate schema validation) and exercises the full
 * project CRUD flow from chapters 13 and 14, plus the Swagger docs from chapter 16.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void fullProjectCrudFlow() throws Exception {
        String body = """
                {"title":"DevCraft API","description":"Core backend","maxMembers":5,"isPublic":true}
                """;

        String created = mvc.perform(post("/projects").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("DevCraft API"))
                .andExpect(jsonPath("$.status").value("PLANNING"))
                .andExpect(jsonPath("$.isPublic").value(true))
                .andReturn().getResponse().getContentAsString();

        JsonNode node = mapper.readTree(created);
        long id = node.get("id").asLong();

        mvc.perform(get("/projects")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(get("/projects/" + id)).andExpect(status().isOk());
        mvc.perform(get("/projects/99999")).andExpect(status().isNotFound());

        mvc.perform(patch("/projects/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.title").value("DevCraft API"));

        mvc.perform(delete("/projects/" + id)).andExpect(status().isNoContent());
        mvc.perform(delete("/projects/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void rejectsDuplicateTitleWithConflict() throws Exception {
        String body = "{\"title\":\"Unique Title\",\"maxMembers\":1}";

        mvc.perform(post("/projects").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mvc.perform(post("/projects").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("A project with this title already exists"));
    }

    @Test
    void rejectsBlankTitleWithValidationErrors() throws Exception {
        mvc.perform(post("/projects").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"  \"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors.title").exists());
    }
    @Test
    void patchValidatesAndRejectsTitleClashes() throws Exception {
        String first = mvc.perform(post("/projects").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Patch Alpha\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        mvc.perform(post("/projects").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Patch Beta\"}"))
                .andExpect(status().isCreated());
        long id = mapper.readTree(first).get("id").asLong();

        mvc.perform(patch("/projects/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"   \"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors.title").exists());
        mvc.perform(patch("/projects/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"maxMembers\":-1}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors.maxMembers").exists());
        mvc.perform(patch("/projects/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Patch Beta\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void apiDocsDescribeEveryEndpointAndErrorShape() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/projects'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/projects'].post.responses['409']").exists())
                .andExpect(jsonPath("$.paths['/projects'].post.responses['422']").exists())
                .andExpect(jsonPath("$.paths['/projects/{id}'].get.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/projects/{id}'].patch.responses['409']").exists())
                .andExpect(jsonPath("$.paths['/projects/{id}'].delete.responses['204']").exists())
                .andExpect(jsonPath("$.components.schemas.ErrorResponse").exists())
                .andExpect(jsonPath("$.components.schemas.Project.properties.isPublic").exists())
                .andExpect(jsonPath("$.components.schemas.Project.properties.public").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.CreateProjectDto.properties.isPublic").exists())
                .andExpect(jsonPath("$.components.schemas.CreateProjectDto.properties.public").doesNotExist());
    }
    @Test
    void openApiDocsAreServed() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("DevCraft Project API"));
    }
}
