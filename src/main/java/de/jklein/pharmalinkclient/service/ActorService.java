package de.jklein.pharmalinkclient.service;

import de.jklein.pharmalinkclient.config.BackendConfig;
import de.jklein.pharmalinkclient.dto.ActorFilterCriteriaDto; // NEU: Import
import de.jklein.pharmalinkclient.dto.ActorResponseDto;
import de.jklein.pharmalinkclient.dto.ActorUpdateRequestDto;
import de.jklein.pharmalinkclient.dto.MedikamentResponseDto; // NEU: Import für getMedicationsByHersteller
import de.jklein.pharmalinkclient.dto.ActorIdResponse; // NEU: Import für getMyActorId
import de.jklein.pharmalinkclient.security.UserSession;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException; // Für Debug-Ausgabe
import com.fasterxml.jackson.databind.ObjectMapper; // Für Debug-Ausgabe


@SpringComponent
@UIScope
public class ActorService {

    private final RestTemplate restTemplate;
    private final BackendConfig backendConfig;
    private final UserSession userSession;

    public ActorService(BackendConfig backendConfig, UserSession userSession) {
        this.restTemplate = new RestTemplate();
        this.backendConfig = backendConfig;
        this.userSession = userSession;
    }

    // Hilfsmethode zum Erstellen der HttpEntity mit JWT (für GET)
    private HttpEntity<String> createHttpEntityWithJwt() {
        HttpHeaders headers = new HttpHeaders();
        String jwt = userSession.getJwt();
        if (jwt != null && !jwt.isEmpty()) {
            headers.setBearerAuth(jwt);
        } else {
            System.err.println("Kein JWT-Token in der UserSession verfügbar. Authentifizierung erforderlich.");
        }
        return new HttpEntity<>(headers);
    }

    // Hilfsmethode zum Erstellen der HttpEntity mit JWT und Body (für POST/PUT)
    private <T> HttpEntity<T> createHttpEntityWithJwtAndBody(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String jwt = userSession.getJwt();
        if (jwt != null && !jwt.isEmpty()) {
            headers.setBearerAuth(jwt);
        } else {
            System.err.println("Kein JWT-Token in der UserSession verfügbar. Authentifizierung erforderlich.");
        }
        return new HttpEntity<>(body, headers);
    }

    /**
     * Sucht nach Akteuren anhand verschiedener Kriterien.
     * Ersetzt getAllActors() und getActorsByRole().
     * Entspricht GET /api/v1/search/actors (searchActors)
     */
    public List<ActorResponseDto> searchActors(String role, String bezeichnung, String actorId) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(backendConfig.getBaseUrl() + "/v1/search/actors");

        if (role != null && !role.isEmpty()) {
            uriBuilder.queryParam("role", role);
        }
        if (bezeichnung != null && !bezeichnung.isEmpty()) {
            uriBuilder.queryParam("bezeichnung", bezeichnung);
        }
        if (actorId != null && !actorId.isEmpty()) {
            uriBuilder.queryParam("actorId", actorId);
        }

        String url = uriBuilder.toUriString();
        HttpEntity<String> entity = createHttpEntityWithJwt();

        try {
            ResponseEntity<ActorResponseDto[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ActorResponseDto[].class
            );
            if (response.getBody() != null) {
                return Arrays.asList(response.getBody());
            }
        } catch (HttpClientErrorException e) {
            System.err.println("Fehler beim Suchen/Filtern der Akteure: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Allgemeiner Fehler beim Suchen/Filtern der Akteure: " + e.getMessage());
        }
        return Collections.emptyList();
    }


    /**
     * Ruft Hersteller-Informationen anhand der Hersteller-ID ab.
     * Entspricht GET /api/v1/hersteller/{herstellerId} (getHerstellerById)
     */
    public ActorResponseDto getHerstellerById(String herstellerId) {
        String url = backendConfig.getBaseUrl() + "/v1/hersteller/" + herstellerId; // Korrigierter URI-Pfad
        HttpEntity<String> entity = createHttpEntityWithJwt();

        try {
            ResponseEntity<ActorResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ActorResponseDto.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.err.println("Fehler beim Laden von Hersteller-Details mit ID '" + herstellerId + "': " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Allgemeiner Fehler beim Laden von Hersteller-Details mit ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Ruft alle Medikamente für einen spezifischen Hersteller ab.
     * Entspricht GET /api/v1/hersteller/{herstellerId}/medications (getMedicationsByHersteller)
     */
    public List<MedikamentResponseDto> getMedicationsByHersteller(String herstellerId) { // NEU: Methode hinzugefügt
        String url = backendConfig.getBaseUrl() + "/v1/hersteller/" + herstellerId + "/medications"; // Korrigierter URI-Pfad
        HttpEntity<String> entity = createHttpEntityWithJwt();

        try {
            ResponseEntity<MedikamentResponseDto[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    MedikamentResponseDto[].class
            );
            if (response.getBody() != null) {
                return Arrays.asList(response.getBody());
            }
        } catch (HttpClientErrorException e) {
            System.err.println("Fehler beim Laden von Medikamenten für Hersteller ID '" + herstellerId + "': " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Allgemeiner Fehler beim Laden von Medikamenten für Hersteller: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Ruft Informationen für den aktuell initialisierten Hersteller ab.
     * Entspricht GET /api/v1/hersteller/me (getMyInfo)
     */
    public ActorResponseDto getMyHerstellerInfo() { // NEU: Methode hinzugefügt
        String url = backendConfig.getBaseUrl() + "/v1/hersteller/me"; // Korrigierter URI-Pfad
        HttpEntity<String> entity = createHttpEntityWithJwt();

        try {
            ResponseEntity<ActorResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ActorResponseDto.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.err.println("Fehler beim Laden der eigenen Hersteller-Informationen: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Allgemeiner Fehler beim Laden der eigenen Hersteller-Informationen: " + e.getMessage());
        }
        return null;
    }

    /**
     * Gibt die actorId des Benutzers zurück, der beim Anwendungsstart initialisiert wurde.
     * Entspricht GET /api/v1/hersteller/id (getMyActorId)
     */
    public ActorIdResponse getMyActorId() { // NEU: Methode hinzugefügt
        String url = backendConfig.getBaseUrl() + "/v1/hersteller/id"; // Korrigierter URI-Pfad
        HttpEntity<String> entity = createHttpEntityWithJwt();

        try {
            ResponseEntity<ActorIdResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ActorIdResponse.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.err.println("Fehler beim Laden der eigenen Akteur-ID: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Allgemeiner Fehler beim Laden der eigenen Akteur-ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Aktualisiert einen Akteur.
     * Entspricht PUT /api/v1/actors/{actorId} (updateActor)
     */
    public boolean updateActor(String actorId, ActorUpdateRequestDto requestDto) {
        String url = backendConfig.getBaseUrl() + "/v1/actors/" + actorId; // Korrigierter URI-Pfad
        HttpEntity<ActorUpdateRequestDto> entity = createHttpEntityWithJwtAndBody(requestDto);

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
            String jsonOutput = mapper.writeValueAsString(requestDto);
            System.out.println("----- Ausgehendes JSON für Akteur-Update -----");
            System.out.println(jsonOutput);
            System.out.println("-------------------------------------------");
        } catch (JsonProcessingException jsonEx) {
            System.err.println("Fehler beim Umwandeln des DTO in JSON: " + jsonEx.getMessage());
        }

        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    Void.class
            );
            System.out.println("Akteur mit ID " + actorId + " erfolgreich aktualisiert.");
            return true;
        } catch (HttpClientErrorException e) {
            System.err.println("Fehler beim Aktualisieren des Akteurs mit ID '" + actorId + "': " + e.getStatusCode() + " " + e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            System.err.println("Allgemeiner Fehler beim Aktualisieren des Akteurs: " + e.getMessage());
            return false;
        }
    }
}