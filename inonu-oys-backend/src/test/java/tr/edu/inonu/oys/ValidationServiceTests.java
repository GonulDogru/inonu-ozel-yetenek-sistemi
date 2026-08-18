package tr.edu.inonu.oys;

import org.junit.jupiter.api.Test;
import tr.edu.inonu.oys.service.ValidationService;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationServiceTests {
    private final ValidationService validationService = new ValidationService();

    @Test
    void acceptsElevenDigitsEndingWithEvenNumber() {
        assertThat(validationService.isValidTCKN("10000000146")).isTrue();
        assertThat(validationService.isValidTCKN("11111111110")).isTrue();
        assertThat(validationService.isValidTCKN("12345678900")).isTrue();
        assertThat(validationService.isValidTCKN("02345678902")).isTrue();
        assertThat(validationService.isValidTCKN("12345678901")).isFalse();
        assertThat(validationService.isValidTCKN("1234567890A")).isFalse();
        assertThat(validationService.isValidTCKN("1234")).isFalse();
    }
}
