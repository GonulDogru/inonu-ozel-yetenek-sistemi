package tr.edu.inonu.oys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import tr.edu.inonu.oys.config.SecurityConfig;

// @Import anotasyonu ile SecurityConfig'i doğrudan ve zorla yüklüyoruz.
// Bu, Spring'in onu gözden kaçırma ihtimalini ortadan kaldırır.
@SpringBootApplication
@Import(SecurityConfig.class)
public class InonuOysBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(InonuOysBackendApplication.class, args);
    }

}