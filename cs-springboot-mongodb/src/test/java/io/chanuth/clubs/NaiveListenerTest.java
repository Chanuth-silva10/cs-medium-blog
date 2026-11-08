package io.chanuth.clubs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;

/**
 * Shows why we use auditing instead of the lifecycle listener many tutorials show: a listener that
 * writes both timestamps on every save moves createdAt each time the document is updated.
 */
@SpringBootTest(properties = "spring.mongodb.database=devcraft_clubs_test")
@Import(NaiveListenerTest.NaiveConfig.class)
class NaiveListenerTest {

    @Document(collection = "naive_notes")
    static class NaiveNote {
        String id;
        String text;
        Date createdAt;
        Date updatedAt;
    }

    @TestConfiguration
    static class NaiveConfig {
        @Bean
        AbstractMongoEventListener<NaiveNote> naiveListener() {
            return new AbstractMongoEventListener<>() {
                @Override
                public void onBeforeConvert(BeforeConvertEvent<NaiveNote> event) {
                    Date now = new Date();
                    event.getSource().createdAt = now;
                    event.getSource().updatedAt = now;
                }
            };
        }
    }

    @Autowired
    MongoTemplate mongoTemplate;

    @AfterEach
    void cleanUp() {
        mongoTemplate.dropCollection(NaiveNote.class);
    }

    @Test
    void aListenerThatSetsBothDatesResetsCreatedAtOnEveryUpdate() throws Exception {
        NaiveNote note = new NaiveNote();
        note.text = "first version";
        mongoTemplate.save(note);
        Date firstCreatedAt = note.createdAt;

        Thread.sleep(50);
        note.text = "second version";
        mongoTemplate.save(note);

        assertThat(note.createdAt).isAfter(firstCreatedAt);
    }
}
