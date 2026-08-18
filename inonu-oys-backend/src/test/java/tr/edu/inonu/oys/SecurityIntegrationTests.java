package tr.edu.inonu.oys;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tr.edu.inonu.oys.config.JwtService;
import tr.edu.inonu.oys.model.Role;
import tr.edu.inonu.oys.model.User;
import tr.edu.inonu.oys.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired JwtService jwtService;

    @Test
    void publicRegistrationCannotCreateAdminAndApplicantCannotReadAdminEndpoint() throws Exception {
        String registration = """
                {
                  "username":"11111111110",
                  "password":"StrongPass1!",
                  "firstName":"Test",
                  "lastName":"Aday",
                  "role":"ADMIN"
                }
                """;
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registration))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("APPLICANT"));

        String login = """
                {"username":"11111111110","password":"StrongPass1!"}
                """;
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String token = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(response).get("token").asText();
        mockMvc.perform(get("/api/applications/all")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateJury() throws Exception {
        User admin = new User();
        admin.setUsername("90000000000");
        admin.setPassword("encoded");
        admin.setFirstName("Test");
        admin.setLastName("Admin");
        admin.setRole(Role.ADMIN);
        admin = userRepository.save(admin);

        mockMvc.perform(post("/api/users/create")
                        .header("Authorization", "Bearer " + jwtService.createToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"33333333332","password":"Test1234!",
                                 "firstName":"Jüri","lastName":"Üyesi","role":"JURY","juryField":"SPOR"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("JURY"));
    }
}
