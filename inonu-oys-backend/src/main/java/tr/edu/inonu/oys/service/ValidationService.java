package tr.edu.inonu.oys.service;

import org.springframework.stereotype.Service;

@Service
public class ValidationService {
    public boolean isValidTCKN(String tckn) {
        if (tckn == null || !tckn.matches("[0-9]{11}")) return false;
        return Character.getNumericValue(tckn.charAt(10)) % 2 == 0;
    }
}
