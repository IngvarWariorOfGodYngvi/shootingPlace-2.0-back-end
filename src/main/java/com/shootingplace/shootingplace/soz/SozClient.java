package com.shootingplace.shootingplace.soz;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shootingplace.shootingplace.exceptions.domain.DomainNotFoundException;
import com.shootingplace.shootingplace.exceptions.soz.SozExportException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class SozClient {

    private final SozConfigRepository sozConfigRepository;
    private static final String BASE_URL = "https://soz.pzss.org.pl";

    private final WebClient webClient;
    private final SozSession session;

    private static final Logger LOG = LogManager.getLogger(SozClient.class);

    public SozClient(SozConfigRepository sozConfigRepository, SozSession session) {
        this.sozConfigRepository = sozConfigRepository;
        this.session = session;

        HttpClient httpClient = HttpClient.create()
                .followRedirect(false);

        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer ->
                        configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)
                )
                .build();
    }

    /* ===================== PUBLIC API ===================== */

    public void uploadAndLoad(byte[] xlsx) throws JsonProcessingException {
        ensureLoggedIn();

        SozUploadResponse upload = uploadFile(xlsx);
        SozLoadDataEvent result = loadData(upload.fileName());

        if (!result.IsSuccess()) {
            throw new SozExportException(
                    "SOZ_EXPORT_FAILED",
                    String.join(" ", result.Errors())
            );
        }
    }
    public List<?> fetchInvitations() {
        ensureLoggedIn();
        return loadInvitations();
    }
    public List<?> fetchMembers() {
        ensureLoggedIn();
        return loadMembers();
    }
    public List<?> fetchLicenseAndPatents() {
        ensureLoggedIn();
        return loadLicenseAndPatents();
    }


    /* ===================== LOGIN ===================== */

    private void ensureLoggedIn() {
        if (session.isValid()) {
            return;
        }
        session.clear();
        login();
    }

    private void login() {

        // 1. GET /Account/Login – CSRF + cookies
        String loginPage = webClient.get()
                .uri("/Account/Login")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        String csrf = extractCsrfToken(loginPage);
        SozConfig config = sozConfigRepository.findById(1L).orElseThrow(() -> new DomainNotFoundException("sozConfig", "1"));
        // 2. POST /Account/Login
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("__RequestVerificationToken", csrf);
        form.add("Login", config.getLogin());
        form.add("Password", config.getPassword());

        ClientResponse response = webClient.post()
                .uri("/Account/Login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .cookies(c -> c.addAll(session.cookies()))
                .bodyValue(form)
                .exchange()
                .block();

        LOG.info("SOZ login POST status = {}", response.statusCode());

        extractCookies(response.headers().asHttpHeaders());

        if (!session.cookies().containsKey(".ASPXAUTH")) {
            throw new IllegalStateException("SOZ login failed – missing .ASPXAUTH cookie");
        }

        // 3. RĘCZNY redirect
        String location = response.headers()
                .header(HttpHeaders.LOCATION)
                .getFirst();

        webClient.get()
                .uri(location)
                .cookies(c -> c.addAll(session.cookies()))
                .retrieve()
                .toBodilessEntity()
                .block();

        session.markLoggedIn();

        LOG.info("SOZ login OK, cookies = {}", session.cookies().keySet());
    }

    /* ===================== UPLOAD ===================== */

    private SozUploadResponse uploadFile(byte[] xlsx) {

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(xlsx) {
            @Override
            public String getFilename() {
                return "import.xlsx";
            }
        });

        return webClient.post()
                .uri("/Club/Persons/Upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("X-Requested-With", "XMLHttpRequest")
                .cookies(c -> c.addAll(session.cookies()))
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .bodyToMono(SozUploadResponse.class)
                .block();
    }
    /* ===================== INVITATIONS ===================== */

    private List<?> loadInvitations() {

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("draw", "1");
        form.add("start", "0");
        form.add("length", "100000");

        String rawJson = webClient.post()
                .uri("/Club/Persons/InvitationsListDataAjax")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("X-Requested-With", "XMLHttpRequest")
                .cookies(c -> c.addAll(session.cookies()))
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawJson);
            JsonNode dataNode = root.get("data");

            return mapper.treeToValue(dataNode, List.class);

        } catch (Exception e) {
            throw new SozExportException(
                    "SOZ_INVITATIONS_PARSE_ERROR",
                    "Nie udało się sparsować listy zaproszeń"
            );
        }

    }
    private List<?> loadMembers() {

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("draw", "1");
        form.add("start", "0");
        form.add("length", "100000");

        String rawJson = webClient.post()
                .uri("/Club/Persons/ListDataAjax")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("X-Requested-With", "XMLHttpRequest")
                .cookies(c -> c.addAll(session.cookies()))
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawJson);
            JsonNode dataNode = root.get("data");

            return mapper.treeToValue(dataNode, List.class);

        } catch (Exception e) {
            throw new SozExportException(
                    "SOZ_MEMBERS_PARSE_ERROR",
                    "Nie udało się sparsować listy klubowiczów"
            );
        }

    }
    private List<?> loadLicenseAndPatents() {

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("draw", "1");
        form.add("start", "0");
        form.add("length", "100000");

        String rawJson = webClient.post()
                .uri("/Club/Qualif/IndexDataAjax")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("X-Requested-With", "XMLHttpRequest")
                .cookies(c -> c.addAll(session.cookies()))
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawJson);
            JsonNode dataNode = root.get("data");

            return mapper.treeToValue(dataNode, List.class);

        } catch (Exception e) {
            throw new SozExportException(
                    "SOZ_LICENSE_AND_PATENT_PARSE_ERROR",
                    "Nie udało się sparsować listy licencji i patentów"
            );
        }

    }

    /* ===================== LOAD DATA (SSE) ===================== */

    private SozLoadDataEvent loadData(String fileName) throws JsonProcessingException {

        String raw = webClient.get()
                .uri(uri -> uri.path("/Club/Persons/LoadData")
                        .queryParam("fileName", fileName)
                        .build())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .cookies(c -> c.addAll(session.cookies()))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<SozLoadDataEvent> events = parseSse(raw);

        if (events.isEmpty()) {
            throw new SozExportException(
                    "SOZ_EMPTY_RESPONSE",
                    "SOZ nie zwrócił wyniku importu"
            );
        }

        // 🔴 ZAWSZE JEDEN
        return events.getFirst();
    }


    /* ===================== HELPERS ===================== */

    private void extractCookies(HttpHeaders headers) {
        headers.getOrEmpty(HttpHeaders.SET_COOKIE).forEach(c -> {
            LOG.info("SOZ Set-Cookie => {}", c);
            String[] parts = c.split(";", 2);
            String[] kv = parts[0].split("=", 2);
            session.cookies().add(kv[0], kv[1]);
        });
    }

    private String extractCsrfToken(String html) {
        Document doc = Jsoup.parse(html);
        return doc.select("input[name=__RequestVerificationToken]").attr("value");
    }

    private List<SozLoadDataEvent> parseSse(String raw) throws JsonProcessingException {
        List<SozLoadDataEvent> events = new ArrayList<>();
        for (String line : raw.split("\n")) {
            if (line.startsWith("data:")) {
                String json = line.substring(5).trim();
                events.add(new ObjectMapper().readValue(json, SozLoadDataEvent.class));
            }
        }
        return events;
    }

}
