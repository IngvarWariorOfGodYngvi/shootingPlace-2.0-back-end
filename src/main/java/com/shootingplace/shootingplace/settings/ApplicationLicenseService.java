package com.shootingplace.shootingplace.settings;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ApplicationLicenseService {

    private final ApplicationLicenseProperties props;

    @PostConstruct
    void debug() {
        System.out.println(">>> LICENSE endDate = " + props.getEndDate());
        System.out.println(">>> LICENSE signature present = " + (props.getSignature() != null));
    }

    public LocalDate getEndDate() {
        return props.getEndDate();
    }

    public boolean isExpired() {
        return !isSignatureValid() || LocalDate.now().isAfter(getEndDate());
    }

    public boolean isSignatureValid() {
        try {
            String payload = "endDate=" + props.getEndDate();

            byte[] signatureBytes = Base64.getDecoder().decode(props.getSignature().replaceAll("\\s+", ""));

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(loadPublicKey());
            signature.update(payload.getBytes(StandardCharsets.UTF_8));

            return signature.verify(signatureBytes);

        } catch (Exception e) {
            return false;
        }
    }

    private PublicKey loadPublicKey() throws Exception {
        InputStream is = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("license-public.key"));

        String key = new String(is.readAllBytes(), StandardCharsets.UTF_8).replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "").replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
    }
}

