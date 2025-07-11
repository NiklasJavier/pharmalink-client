package de.jklein.pharmalinkclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.jklein.pharmalinkclient.config.BackendConfig;
import de.jklein.pharmalinkclient.dto.CreateMedikamentRequestDto;
import de.jklein.pharmalinkclient.dto.MedikamentFilterCriteriaDto;
import de.jklein.pharmalinkclient.dto.MedikamentResponseDto;
import de.jklein.pharmalinkclient.dto.UpdateMedikamentRequestDto;
import de.jklein.pharmalinkclient.dto.UpdateMedicationStatusRequestDto;
import de.jklein.pharmalinkclient.security.UserSession;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.slf4j.Logger; // NEU: Import für Logger
import org.slf4j.LoggerFactory; // NEU: Import für LoggerFactory

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@SpringComponent
@UIScope
public class MedikamentService {

    private static final Logger log = LoggerFactory.getLogger(MedikamentService.class); // NEU: Logger-Instanz

    private final RestTemplate restTemplate;
    private final BackendConfig backendConfig;
    private final UserSession userSession;

    public MedikamentService(BackendConfig backendConfig, UserSession userSession) {
        this.restTemplate = new RestTemplate();
        this.backendConfig = backendConfig;
        this.userSession = userSession;
    }

    private HttpEntity<String> createHttpEntityWithJwt() {
        HttpHeaders headers = new HttpHeaders();
        String jwt = userSession.getJwt();
        if (jwt != null && !jwt.isEmpty()) {
            headers.setBearerAuth(jwt);
        } else {
            log.warn("Kein JWT-Token in der UserSession verfügbar. Authentifizierung erforderlich."); // GEÄNDERT: zu log.warn
            return new HttpEntity<>(headers);
        }
        return new HttpEntity<>(headers);
    }

    private <T> HttpEntity<T> createHttpEntityWithJwtAndBody(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String jwt = userSession.getJwt();
        if (jwt != null && !jwt.isEmpty()) {
            headers.setBearerAuth(jwt);
        } else {
            log.warn("Kein JWT-Token in der UserSession verfügbar. Authentifizierung erforderlich."); // GEÄNDERT: zu log.warn
        }
        return new HttpEntity<>(body, headers);
    }

    /**
     * Sucht nach Medikamenten anhand verschiedener Kriterien.
     * Entspricht GET /api/v1/search/medikamente (searchMedikamente)
     */
    public List<MedikamentResponseDto> searchMedikamente(MedikamentFilterCriteriaDto criteria) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(backendConfig.getBaseUrl() + "/v1/search/medikamente");

        if (criteria.getSearchTerm() != null && !criteria.getSearchTerm().isEmpty()) {
            uriBuilder.queryParam("query", criteria.getSearchTerm());
        }
        if (criteria.getStatusFilter() != null && !criteria.getStatusFilter().isEmpty() && !criteria.getStatusFilter().equals("Ohne Filter")) {
            uriBuilder.queryParam("status", criteria.getStatusFilter());
        }
        // Annahme: Wenn tags in MedikamentFilterCriteriaDto vorhanden wären, würden sie hier hinzugefügt
        // if (criteria.getTags() != null && !criteria.getTags().isEmpty()) {
        //     uriBuilder.queryParam("tags", String.join(",", criteria.getTags()));
        // }
        if (criteria.isFilterByCurrentActor()) {
            uriBuilder.queryParam("ownedByMe", true);
        }

        String url = uriBuilder.toUriString();
        log.info("Sende GET-Anfrage für Medikamente an URL: {}", url); // NEU: Log der URL
        HttpEntity<String> entity = createHttpEntityWithJwt();

        try {
            ResponseEntity<MedikamentResponseDto[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    MedikamentResponseDto[].class
            );
            if (response.getBody() != null) {
                List<MedikamentResponseDto> result = Arrays.asList(response.getBody());
                log.info("Erfolgreich {} Medikamenten-Einträge erhalten.", result.size()); // NEU: Log der Anzahl
                return result;
            }
        } catch (HttpClientErrorException e) {
            log.error("Fehler beim Suchen/Filtern der Medikamente: {} {}", e.getStatusCode(), e.getResponseBodyAsString(), e); // GEÄNDERT: zu log.error mit Stacktrace
        } catch (Exception e) {
            log.error("Allgemeiner Fehler beim Suchen/Filtern der Medikamente: {}", e.getMessage(), e); // GEÄNDERT: zu log.error mit Stacktrace
        }
        return Collections.emptyList();
    }

    /**
     * Ruft ein einzelnes Medikament anhand seiner ID ab.
     * Entspricht GET /api/v1/medications/{medId} (getMedicationById)
     */
    public MedikamentResponseDto getMedikamentById(String medId) {
        String url = backendConfig.getBaseUrl() + "/v1/medications/" + medId;
        log.info("Sende GET-Anfrage für Medikament ID {} an URL: {}", medId, url);
        HttpEntity<String> entity = createHttpEntityWithJwt();

        try {
            ResponseEntity<MedikamentResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    MedikamentResponseDto.class
            );
            log.info("Medikament mit ID {} erfolgreich abgerufen.", medId);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Fehler beim Laden des Medikaments mit ID '{}': {} {}", medId, e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Allgemeiner Fehler beim Laden des Medikaments mit ID: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Erstellt ein neues Medikament.
     * Entspricht POST /api/v1/medications (createMedikament)
     */
    public MedikamentResponseDto createMedikament(CreateMedikamentRequestDto request) {
        String url = backendConfig.getBaseUrl() + "/v1/medications";
        log.info("Sende POST-Anfrage zum Erstellen eines Medikaments an URL: {}", url);
        HttpEntity<CreateMedikamentRequestDto> entity = createHttpEntityWithJwtAndBody(request);

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
            String jsonOutput = mapper.writeValueAsString(request);
            log.debug("----- Ausgehendes JSON für Medikament-Erstellung -----\n{}\n--------------------------------------------------", jsonOutput); // GEÄNDERT: zu log.debug

            ResponseEntity<MedikamentResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    MedikamentResponseDto.class
            );
            log.info("Medikament erfolgreich erstellt. Response Body: {}", response.getBody());
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Fehler beim Erstellen des Medikaments: {} {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return null;
        } catch (Exception e) {
            log.error("Allgemeiner Fehler beim Erstellen des Medikaments: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Aktualisiert ein bestehendes Medikament.
     * Entspricht PUT /api/v1/medications/{medId} (updateMedication)
     */
    public MedikamentResponseDto updateMedikament(String medId, UpdateMedikamentRequestDto request) {
        String url = backendConfig.getBaseUrl() + "/v1/medications/" + medId;
        log.info("Sende PUT-Anfrage zum Aktualisieren von Medikament ID {} an URL: {}", medId, url);
        HttpEntity<UpdateMedikamentRequestDto> entity = createHttpEntityWithJwtAndBody(request);

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
            String jsonOutput = mapper.writeValueAsString(request);
            log.debug("----- Ausgehendes JSON für Medikament-Update -----\n{}\n--------------------------------------------------", jsonOutput); // GEÄNDERT: zu log.debug

            ResponseEntity<MedikamentResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    MedikamentResponseDto.class
            );
            log.info("Medikament mit ID {} erfolgreich aktualisiert. Response Body: {}", medId, response.getBody());
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Fehler beim Aktualisieren des Medikaments mit ID '{}': {} {}", medId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            return null;
        } catch (Exception e) {
            log.error("Allgemeiner Fehler beim Aktualisieren des Medikaments: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Genehmigt oder lehnt ein Medikament ab.
     * Entspricht POST /api/v1/medications/{medId}/approval (approveMedication)
     */
    public boolean approveMedication(String medId, UpdateMedicationStatusRequestDto request) {
        String url = backendConfig.getBaseUrl() + "/v1/medications/" + medId + "/approval";
        log.info("Sende POST-Anfrage zum Genehmigen/Ablehnen von Medikament ID {} an URL: {}", medId, url);
        HttpEntity<UpdateMedicationStatusRequestDto> entity = createHttpEntityWithJwtAndBody(request);

        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Void.class
            );
            log.info("Medikament mit ID {} erfolgreich genehmigt/abgelehnt.", medId);
            return true;
        } catch (HttpClientErrorException e) {
            log.error("Fehler beim Genehmigen/Ablehnen des Medikaments mit ID '{}': {} {}", medId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            return false;
        } catch (Exception e) {
            log.error("Allgemeiner Fehler beim Genehmigen/Ablehnen des Medikaments: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Löscht ein Medikament bedingt, wenn keine Einheiten damit verbunden sind.
     * Entspricht DELETE /api/v1/medications/{medId}/conditional-delete (deleteMedikamentIfNoUnits)
     */
    public boolean deleteMedikamentIfNoUnits(String medId) {
        String url = backendConfig.getBaseUrl() + "/v1/medications/" + medId + "/conditional-delete";
        log.info("Sende DELETE-Anfrage zum bedingten Löschen von Medikament ID {} an URL: {}", medId, url);
        HttpEntity<String> entity = createHttpEntityWithJwt();

        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    entity,
                    Void.class
            );
            log.info("Medikament mit ID {} erfolgreich bedingt gelöscht.", medId);
            return true;
        } catch (HttpClientErrorException e) {
            log.error("Fehler beim bedingten Löschen des Medikaments mit ID '{}': {} {}", medId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            return false;
        } catch (Exception e) {
            log.error("Allgemeiner Fehler beim bedingten Löschen des Medikaments: {}", e.getMessage(), e);
            return false;
        }
    }
}