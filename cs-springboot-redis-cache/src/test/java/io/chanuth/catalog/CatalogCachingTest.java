package io.chanuth.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.chanuth.catalog.services.SlowDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CatalogCachingTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    SlowDatabase slowDatabase;

    int readsBefore;

    @BeforeEach
    void rememberReads() {
        readsBefore = slowDatabase.reads();
    }

    int newReads() {
        return slowDatabase.reads() - readsBefore;
    }

    @Test
    void theFullListIsReadFromTheDatabaseOnlyOnce() throws Exception {
        mvc.perform(get("/products")).andExpect(status().isOk()).andExpect(jsonPath("$.total").value(24));
        mvc.perform(get("/products")).andExpect(status().isOk()).andExpect(jsonPath("$.total").value(24));
        mvc.perform(get("/products")).andExpect(status().isOk());

        assertThat(newReads()).isEqualTo(1);
    }

    @Test
    void searchFiltersCombine() throws Exception {
        // "iPhone 16 Clear Case" also matches "iphone", but it is an accessory; "iPhone 15" is unavailable.
        mvc.perform(get("/products/search").param("name", "iphone").param("category", "phones").param("available", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.items[0].name").value("iPhone 16"))
                .andExpect(jsonPath("$.items[1].name").value("iPhone 16 Pro"));

        mvc.perform(get("/products/search").param("name", "iphone").param("maxPrice", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3));
    }

    @Test
    void equivalentSearchesShareACacheEntryAndDifferentOnesDoNot() throws Exception {
        mvc.perform(get("/products/search").param("name", "iPhone ").param("category", "Phones")).andExpect(status().isOk());
        mvc.perform(get("/products/search").param("name", "iphone").param("category", "phones")).andExpect(status().isOk());
        assertThat(newReads()).isEqualTo(1);

        mvc.perform(get("/products/search").param("name", "iphone").param("category", "accessories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("iPhone 16 Clear Case"));
        assertThat(newReads()).isEqualTo(2);
    }

    @Test
    void aSearchWithNoFiltersWorks() throws Exception {
        mvc.perform(get("/products/search")).andExpect(status().isOk()).andExpect(jsonPath("$.total").value(24));
    }

    @Test
    void changingAPriceClearsTheCachedListAndSearches() throws Exception {
        mvc.perform(get("/products")).andExpect(status().isOk());
        mvc.perform(get("/products/search").param("name", "nokia"))
                .andExpect(jsonPath("$.items[0].price").value(25.00));
        assertThat(newReads()).isEqualTo(2);

        mvc.perform(patch("/products/{id}", 6).contentType(MediaType.APPLICATION_JSON).content("{\"price\":19.99}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(19.99));

        mvc.perform(get("/products/search").param("name", "nokia"))
                .andExpect(jsonPath("$.items[0].price").value(19.99));
        mvc.perform(get("/products")).andExpect(status().isOk());
        assertThat(newReads()).isEqualTo(4);
    }

    @Test
    void addingAndDeletingAProductClearTheCache() throws Exception {
        mvc.perform(get("/products")).andExpect(jsonPath("$.total").value(24));

        String body = mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"iPhone 17\",\"price\":999.00,\"category\":\"phones\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("Phones"))
                .andExpect(jsonPath("$.available").value(true))
                .andReturn().getResponse().getContentAsString();
        mvc.perform(get("/products")).andExpect(jsonPath("$.total").value(25));

        long id = Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
        mvc.perform(delete("/products/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/products")).andExpect(jsonPath("$.total").value(24));
        mvc.perform(delete("/products/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void categoriesAreCachedToo() throws Exception {
        mvc.perform(get("/categories")).andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(5));
        mvc.perform(get("/categories")).andExpect(status().isOk());

        assertThat(newReads()).isEqualTo(1);
    }

    @Test
    void rejectsBadInput() throws Exception {
        mvc.perform(get("/products/search").param("minPrice", "-5"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors.minPrice").exists());

        mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"price\":-1,\"category\":\"phones\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists());

        mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Toaster\",\"price\":20,\"category\":\"kitchen\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No category named kitchen"));

        mvc.perform(patch("/products/{id}", 9999).contentType(MediaType.APPLICATION_JSON).content("{\"price\":1}"))
                .andExpect(status().isNotFound());
    }
}
