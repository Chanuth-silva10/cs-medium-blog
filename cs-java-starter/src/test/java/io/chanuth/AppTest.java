package io.chanuth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class AppTest {

    @Test
    void serializesAppInfoToJson() throws Exception {
        App.AppInfo info = new App.AppInfo("cs-java-starter", "1.0.0");
        String json = new ObjectMapper().writeValueAsString(info);

        assertEquals("{\"name\":\"cs-java-starter\",\"version\":\"1.0.0\"}", json);
    }
}
