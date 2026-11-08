package io.chanuth.signup.producer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SignupController {

    public record SignupRequest(@NotBlank @Size(max = 100) String name, @NotBlank @Email String email) {
    }

    private final WelcomeEmailPublisher publisher;

    public SignupController(WelcomeEmailPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * 202 Accepted: the request is taken, the email is sent later by another application. The
     * client does not wait for the mail server.
     */
    @PostMapping("/signups")
    public ResponseEntity<Map<String, String>> signUp(@Valid @RequestBody SignupRequest request) {
        // A real application would save the user in its database here.
        String messageId = publisher.publish(request.name().trim(), request.email().trim());
        return ResponseEntity.accepted().body(Map.of(
                "message", "Signup received, a welcome email is on its way",
                "requestId", messageId));
    }
}
