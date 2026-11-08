package io.chanuth.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.chanuth.auth.repositories.RoleRepository;
import io.chanuth.auth.repositories.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthApiIntegrationTest {

    private static final String SUPER_ADMIN_EMAIL = "root@devcraft.io";
    private static final String SUPER_ADMIN_PASSWORD = "RootPassword123!";

    @Autowired
    MockMvc mvc;

    @Autowired
    JsonMapper jsonMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Value("${security.jwt.secret-key}")
    String secretKey;

    // ---------- Chapter 18: authentication ----------

    @Test
    void registersAMemberWithoutLeakingThePasswordHash() throws Exception {
        String email = uniqueEmail();
        register("Ada Lovelace", email, "Password123!")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("MEMBER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.authorities").doesNotExist());

        assertThat(userRepository.findByEmail(email).orElseThrow().getPassword()).startsWith("$2a$");
    }

    @Test
    void rejectsADuplicateEmailWith409() throws Exception {
        String email = uniqueEmail();
        register("First", email, "Password123!").andExpect(status().isCreated());
        register("Second", email, "Password123!")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("An account with this email already exists"));
    }

    @Test
    void validatesTheSignUpForm() throws Exception {
        register("", "not-an-email", "short")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());

        // BCrypt only uses the first 72 bytes; longer passwords are refused up front.
        register("Long", uniqueEmail(), "x".repeat(73))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void logsInAndReturnsATokenWithItsLifetimeInSeconds() throws Exception {
        String email = uniqueEmail();
        register("Grace Hopper", email, "Password123!");

        login(email, "Password123!")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    void givesTheSameAnswerForAWrongPasswordAndAnUnknownEmail() throws Exception {
        String email = uniqueEmail();
        register("Linus", email, "Password123!");

        login(email, "WrongPassword!")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email or password is incorrect"));
        login(uniqueEmail(), "Password123!")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email or password is incorrect"));
    }

    @Test
    void protectedRoutesNeedAToken() throws Exception {
        mvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required"));
    }

    @Test
    void returnsTheCurrentUserForAValidToken() throws Exception {
        String email = uniqueEmail();
        String token = registerAndLogin(email);

        mvc.perform(get("/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("MEMBER"));
    }

    @Test
    void rejectsATamperedToken() throws Exception {
        String token = registerAndLogin(uniqueEmail());
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("A") ? "BB" : "AA");

        mvc.perform(get("/users/me").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("The token is invalid"));
        mvc.perform(get("/users/me").header("Authorization", "Bearer not.a.jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("The token is invalid"));
    }

    @Test
    void rejectsAnExpiredToken() throws Exception {
        String email = uniqueEmail();
        register("Expired", email, "Password123!");
        String expired = Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(Instant.now().minusSeconds(7200)))
                .expiration(Date.from(Instant.now().minusSeconds(3600)))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .compact();

        mvc.perform(get("/users/me").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("The token has expired"));
    }

    @Test
    void aDeletedUsersTokenStopsWorkingAndTheRoleSurvives() throws Exception {
        String email = uniqueEmail();
        String token = registerAndLogin(email);
        long rolesBefore = roleRepository.count();

        userRepository.delete(userRepository.findByEmail(email).orElseThrow());

        assertThat(roleRepository.count()).isEqualTo(rolesBefore);
        mvc.perform(get("/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("The account behind this token no longer exists"));
    }

    // ---------- Chapter 19: role-based access control ----------

    @Test
    void theThreeRolesAreSeededByTheMigration() {
        assertThat(roleRepository.count()).isEqualTo(3);
    }

    @Test
    void aMemberCannotListUsers() throws Exception {
        String token = registerAndLogin(uniqueEmail());

        mvc.perform(get("/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have permission to access this resource"));
    }

    @Test
    void aMemberCannotCreateAnAdmin() throws Exception {
        String token = registerAndLogin(uniqueEmail());

        mvc.perform(post("/admins").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Sneaky", uniqueEmail(), "Password123!")))
                .andExpect(status().isForbidden());
    }

    @Test
    void theSuperAdminCanCreateAnAdminWhoCanListUsersButNotCreateAdmins() throws Exception {
        String superToken = tokenFor(SUPER_ADMIN_EMAIL, SUPER_ADMIN_PASSWORD);
        String adminEmail = uniqueEmail();

        mvc.perform(post("/admins").header("Authorization", "Bearer " + superToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Ops Admin", adminEmail, "AdminPassword123!")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.password").doesNotExist());

        String adminToken = tokenFor(adminEmail, "AdminPassword123!");

        mvc.perform(get("/users").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value(SUPER_ADMIN_EMAIL))
                .andExpect(jsonPath("$[0].role").value("SUPER_ADMIN"))
                .andExpect(jsonPath("$[0].password").doesNotExist());

        mvc.perform(post("/admins").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Another", uniqueEmail(), "Password123!")))
                .andExpect(status().isForbidden());
    }

    @Test
    void theSuperAdminCanListUsers() throws Exception {
        mvc.perform(get("/users").header("Authorization", "Bearer " + tokenFor(SUPER_ADMIN_EMAIL, SUPER_ADMIN_PASSWORD)))
                .andExpect(status().isOk());
    }

    // ---------- helpers ----------

    private ResultActions register(String fullName, String email, String password) throws Exception {
        return mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(json(fullName, email, password)));
    }

    private ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }

    private String registerAndLogin(String email) throws Exception {
        register("Test User", email, "Password123!").andExpect(status().isCreated());
        return tokenFor(email, "Password123!");
    }

    private String tokenFor(String email, String password) throws Exception {
        String body = login(email, password).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return jsonMapper.readTree(body).get("token").asString();
    }

    private static String json(String fullName, String email, String password) {
        return "{\"fullName\":\"" + fullName + "\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private static String uniqueEmail() {
        return "user-" + UUID.randomUUID().toString().substring(0, 8) + "@devcraft.io";
    }
}
