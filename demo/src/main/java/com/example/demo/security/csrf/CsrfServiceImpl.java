package com.example.demo.security.csrf;

import com.example.demo.security.config.AppSecurityProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

@Service
public class CsrfServiceImpl implements CsrfService{
    private final String csrfSecretKey;
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String DELIMITER = "!";
    private static final String TOKEN_DELIMITER = ".";

    public CsrfServiceImpl(AppSecurityProperties properties) {
        this.csrfSecretKey = properties.getSecrets().getCsrf();
    }

    @Override
    public String generateToken(String sessionIdentifier) {
        String randomValue = UUID.randomUUID().toString();

        String message = buildHmacMessage(sessionIdentifier, randomValue);

        String hmac = calculateHmac(message);

        return hmac + TOKEN_DELIMITER + randomValue;
    }

    @Override
    public boolean validateToken(String token, String sessionIdentifier) {
        if (token == null || !token.contains(TOKEN_DELIMITER)) {
            return false;
        }

        String[] parts = token.split("\\" + TOKEN_DELIMITER);
        if (parts.length != 2) {
            return false;
        }

        String hmacFromRequest = parts[0];
        String randomValue = parts[1];

        String message = buildHmacMessage(sessionIdentifier, randomValue);
        String expectedHmac = calculateHmac(message);

        return constantTimeEquals(hmacFromRequest, expectedHmac);
    }

    private String calculateHmac(String message) {
        try {
            Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(
                    csrfSecretKey.getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM
            );
            hmac.init(secretKey);
            byte[] hmacBytes = hmac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to calculate HMAC", e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }

        int maxLength = Math.max(a.length(), b.length());
        int result = 0;

        for (int i = 0; i < maxLength; i++) {
            char charA = (i < a.length()) ? a.charAt(i) : 0;
            char charB = (i < b.length()) ? b.charAt(i) : 0;
            result |= charA ^ charB;
        }
        return result == 0 && a.length() == b.length();
    }

    private String buildHmacMessage(String sessionIdentifier, String randomValue) {
        return String.join(DELIMITER,
                String.valueOf(sessionIdentifier.length()),
                sessionIdentifier,
                String.valueOf(randomValue.length()),
                randomValue
        );
    }
}
