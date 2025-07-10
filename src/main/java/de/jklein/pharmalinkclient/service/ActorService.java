package de.jklein.pharmalinkclient.service;

import de.jklein.pharmalinkclient.config.BackendConfig;
import de.jklein.pharmalinkclient.dto.ActorResponseDto;
import de.jklein.pharmalinkclient.dto.ActorUpdateRequestDto;
import de.jklein.pharmalinkclient.security.UserSession;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder; // Für URL-Parameter

import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    // Hilfsmethode zum Erstellen der HttpEntity mit JWT
    private HttpEntity<String> createHttpEntityWithJwt() {
        HttpHeaders headers = new HttpHeaders();
        String jwt = userSession.getJwt();
        if (jwt != null && !jwt.isEmpty()) {
            headers.setBearerAuth(jwt);
        } else {
            System.err.println("Kein JWT-Token in der UserSession verfügbar. Authentifizierung erforderlich.");
            // Optional: Eine spezifische Exception werfen
        }
        return new HttpEntity<>(headers);
    }

    // GET /api/v1/actors
    public List<ActorResponseDto> getAllActors() {
        String url = backendConfig.getBaseUrl() + "/v1/search/actors";
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
            System.err.println("Fehler beim Laden aller Akteure: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Allgemeiner Fehler beim Laden aller Akteure: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    // GET /api/v1/actors?role={role}
    public List<ActorResponseDto> getActorsByRole(String role) {
        String url = UriComponentsBuilder.fromHttpUrl(backendConfig.getBaseUrl() + "/actors")
                .queryParam("role", role)
                .toUriString();
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
            System.err.println("Fehler beim Laden von Akteuren nach Rolle '" + role + "': " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Allgemeiner Fehler beim Laden von Akteuren nach Rolle: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    // GET /api/v1/hersteller
    public List<ActorResponseDto> getAllHersteller() {
        String url = backendConfig.getBaseUrl() + "/hersteller";
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
            System.err.println("Fehler beim Laden aller Hersteller: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Allgemeiner Fehler beim Laden aller Hersteller: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    // GET /api/v1/hersteller/{herstellerId} - Diese Methode wurde zuvor hinzugefügt und ist wichtig für die Akteur-Details.
    public ActorResponseDto getActorDetailsById(String actorId) {
        // Obwohl der Endpunkt '/hersteller/{herstellerId}' heißt, nehmen wir an, dass er allgemeine Akteur-Details abrufen kann.
        // Wenn nicht, müsste ein allgemeinerer Backend-Endpunkt '/actors/{actorId}' existieren und hier genutzt werden.
        String url = backendConfig.getBaseUrl() + "/hersteller/" + actorId;
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
            System.err.println("Fehler beim Laden von Akteur-Details mit ID '" + actorId + "': " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Allgemeiner Fehler beim Laden von Akteur-Details mit ID: " + e.getMessage());
        }
        return null;
    }


    // GET /api/v1/hersteller/search?search={nameQuery}
    public List<ActorResponseDto> searchHerstellerByName(String nameQuery) {
        String url = UriComponentsBuilder.fromHttpUrl(backendConfig.getBaseUrl() + "/hersteller/search")
                .queryParam("search", nameQuery)
                .toUriString();
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
            System.err.println("Fehler beim Suchen von Hersteller nach Name '" + nameQuery + "': " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Allgemeiner Fehler beim Suchen von Hersteller nach Name: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public boolean updateActor(String actorId, ActorUpdateRequestDto requestDto) {
        String url = backendConfig.getBaseUrl() + "/v1/actors/" + actorId;
        HttpEntity<ActorUpdateRequestDto> entity = createHttpEntityWithJwtAndBody(requestDto); // Nutzt die neue Methode

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT); // Für schöne Formatierung
            String jsonOutput = mapper.writeValueAsString(requestDto);
            System.out.println("----- Ausgehendes JSON für Akteur-Update -----");
            System.out.println(jsonOutput);
            System.out.println("-------------------------------------------");
        } catch (com.fasterxml.jackson.core.JsonProcessingException jsonEx) {
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

    private <T> HttpEntity<T> createHttpEntityWithJwtAndBody(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // Set Content-Type for JSON body
        String jwt = userSession.getJwt();
        if (jwt != null && !jwt.isEmpty()) {
            headers.setBearerAuth(jwt);
        } else {
            System.err.println("Kein JWT-Token in der UserSession verfügbar. Authentifizierung erforderlich.");
        }
        return new HttpEntity<>(body, headers);
    }
}