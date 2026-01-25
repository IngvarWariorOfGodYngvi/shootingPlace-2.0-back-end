package com.shootingplace.shootingplace.portal;

import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.exceptions.domain.DomainNotFoundException;
import com.shootingplace.shootingplace.tournament.TournamentEntity;
import com.shootingplace.shootingplace.tournament.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortalExportService {

    private static final String PORTAL_IMPORT_URL =
            "https://smartstrzelnica.pl/_backend/public/api/import/tournaments";
    private static final String PORTAL_PAYLOAD = "portal-export:v1";

    private static final String PRIVATE_KEY_PEM = """
            -----BEGIN PRIVATE KEY-----
            MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCuUhqmmYYEcuTirhmqmL1XrFc53FnXhQqgEg8+v9QlxWz9JWzynT8GEg3hpEeI+PsFwVgmKC1B5NRiwy3n5QUu5PvtxYxqWKeFlLK1+L2nwfA0fxEsbEQFmr5pU6Sx+OcUQgKqZ9zJWuVB68pDoioJChO8YfoX0yHtqiN43BrM1KEeof45Sa59f/dfA8jP48eSCKD+JXizLC9FcIrEcZf1xsZCHJGx0rgpCX4+84/V3U0mGaCVu06NAXndvBIRi/9+I5X6UE8gshejztOD/zYGAatSaDMqnbP8wNZArk1SRB1rZTh4sbo5sMAc6hBWLu7JhdMQHIQlAzuikmU/QDY3AgMBAAECggEAUbfek3wD1eEQiG1KTe11UXzSlSbFnSFapkhhCi7+IkjHO3LIfIGXkl5zUHRlLoJdlsiY7KZH9QxzPes2gW+cuuuQaFoD5bSyr43SKzk0m0hwtQNeRx9n0eLFYXXbDq1akmYRftXarC/mqj0BYCxMnVkygEzD41hQHuxz3Yv9Kki6qsX9RJgDM0UBeMSCy4uF7wLswOZs0zXo4Lg8wVgVRyShQmmeglGuGWxIB8zIx0x+n8+Z9XriP74C7WN+MWVkFPcT/SRZdvtqdv5GPxm8nHob7e9nrUUck8UxV72WaiO8juu2+NAq6eYkC/toCHF1pT2MmEBh+3X/7jqbbhlD9QKBgQDDlZRTXjYGV1/+48zwCNBLOEUC5MwYb+vCH3c7uytJumnuEsDSIJeEUCbAt+HunTdC43tuh4ssPWjeIBbutIqr4/nk7DKCnxxNuUiYdBsIx9xyrc2rJyBZYAwPxigSlI+W/VFdz38RF7I9EFGRCNrvfkAr4TZ5D9KjgsxK7j6NuwKBgQDkKwsGjlKGNjrtzg2w98lCNZF5PsHQznv1UctZpa4PApjj8Sf4R6XumQq5mAtFc/uwG0+ZhGRoMzRgqtKAHGBcVgVvxnk8gb6Sx5ml4SH8kedF3dVs8p0XE7LJepqkpr9Y+30gKdLiHGX6m/ngcPVneb3eoV5fizr5NLrrRuFztQKBgCH/Xkkfl0SC66zi7DzNS2fH4DcgjlmxGsojrhYz8tJeFQvNNrdP8waM6C+Xxy4zJef6ovoTlZ2bDx+NdG8J8xDuEAI7DIyoG8Nm8beOdySPmUJV8+pMYtMmXvJe/5g7OrqETiCAcRYHiHQU4hjT8TqwN3dpLo7csUC6+8gKodUBAoGAcNfI0Ck7Lx1K76lkpA0oABK2K5yaBkbYj28wftmtx8alDraJ6gSlT+doonlLucGuzF31dBtB/Ta1xMk26h0emwwADFPASehw8+67UoqRYHRYSPl5QDSM9IjNd0+ng57kK4HVVD7bCPC6jsLtRc8Xz2EqQhzUq7QUKoOlng6kGbECgYAmROQ5GuFyNt+6LB5//nd0F9nULf3RW7+mrlxUtCP1QMitg/jaFThq0G44BlCFAQlYFE+Mhk1lEbWOW5FHBlY2sZF4UbUF9E5TG4K+zq9utZOQ4IgXV5/cXjKAnRXNgZVBynv+UsQSfGRzzsqQnTNLcD9jaF5R9bzzLNJxiT5qaQ==
            -----END PRIVATE KEY-----
            """;
    private final TournamentRepository tournamentRepository;
    private final ClubRepository clubRepository;
    private final RestTemplate restTemplate;

    public void exportTournament(String tournamentUUID) {
        ClubEntity c = clubRepository.findById(1).orElseThrow(() -> new DomainNotFoundException("Club", "1"));
        TournamentEntity t = tournamentRepository.findById(tournamentUUID)
                .orElseThrow(() ->
                        new DomainNotFoundException("Tournament", tournamentUUID)
                );

        TournamentExportDto tournamentDto = Mappers.map(t, c);

        PortalExportRequestDto request =
                new PortalExportRequestDto(List.of(tournamentDto));

        sendToPortal(request);
    }

    private String signPayload() {
        try {
            String key = PRIVATE_KEY_PEM
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] decoded = java.util.Base64.getDecoder().decode(key);

            var privateKey = java.security.KeyFactory.getInstance("RSA")
                    .generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(decoded));

            var signature = java.security.Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(PortalExportService.PORTAL_PAYLOAD.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            return java.util.Base64.getEncoder().encodeToString(signature.sign());

        } catch (Exception e) {
            throw new IllegalStateException("Nie udało się podpisać payloadu", e);
        }
    }

    private void sendToPortal(PortalExportRequestDto request) {
        String signature = signPayload();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.add("X-Portal-Payload", PORTAL_PAYLOAD);
        headers.add("X-Portal-Signature", signature);

        HttpEntity<PortalExportRequestDto> entity =
                new HttpEntity<>(request, headers);

        try {
            restTemplate.postForEntity(
                    PORTAL_IMPORT_URL,
                    entity,
                    Void.class
            );
        } catch (HttpClientErrorException.Conflict e) {
            throw new IllegalStateException("Zawody już istnieją w portalu", e);
        } catch (HttpClientErrorException e) {
            throw new IllegalStateException("Błąd 4xx podczas eksportu do portalu", e);
        } catch (HttpServerErrorException e) {
            throw new IllegalStateException("Błąd 5xx po stronie portalu", e);
        }
    }

}

