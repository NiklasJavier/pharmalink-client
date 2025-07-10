package de.jklein.pharmalinkclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.jklein.pharmalinkclient.config.BackendConfig;
import de.jklein.pharmalinkclient.dto.CreateMedikamentRequestDto;
import de.jklein.pharmalinkclient.dto.MedikamentResponseDto;
import de.jklein.pharmalinkclient.security.UserSession;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@SpringComponent
@UIScope
public class MedikamentService {

    private final RestTemplate restTemplate;
    private final BackendConfig backendConfig;
    private final UserSession userSession;

    public MedikamentService(BackendConfig backendConfig, UserSession userSession) {
        this.restTemplate = new RestTemplate();
        this.backendConfig = backendConfig;
        this.userSession = userSession;
    }

    public List<MedikamentResponseDto> getAllMedikamente() {
        String url = backendConfig.getBaseUrl() + "/v1/search/medikamente"; // baseUrl von BackendConfig
        HttpHeaders headers = new HttpHeaders();

        String jwt = userSession.getJwt(); // JWT nur von UserSession
        if (jwt != null && !jwt.isEmpty()) {
            headers.setBearerAuth(jwt);
        } else {
            System.err.println("Kein JWT-Token in der UserSession verfügbar. Authentifizierung erforderlich.");
            return Collections.emptyList();
        }

        HttpEntity<String> entity = new HttpEntity<>(headers);

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
            System.err.println("Fehler beim Laden der Medikamente: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Allgemeiner Fehler beim Laden der Medikamente: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    private <T> HttpEntity<T> createHttpEntityWithJwtAndBody(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // Set Content-Type for POST
        String jwt = userSession.getJwt();
        if (jwt != null && !jwt.isEmpty()) {
            headers.setBearerAuth(jwt);
        } else {
            System.err.println("Kein JWT-Token in der UserSession verfügbar. Authentifizierung erforderlich.");
        }
        return new HttpEntity<>(body, headers);
    }

    public MedikamentResponseDto createMedikament(CreateMedikamentRequestDto request) {
        String url = backendConfig.getBaseUrl() + "/v1/medications"; // Endpunkt zum Erstellen
        HttpEntity<CreateMedikamentRequestDto> entity = createHttpEntityWithJwtAndBody(request);

        // Debug-Ausgabe des Request-Bodies
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
            String jsonOutput = mapper.writeValueAsString(request);
            System.out.println("----- Ausgehendes JSON für Medikament-Erstellung -----");
            System.out.println(jsonOutput);
            System.out.println("--------------------------------------------------");
        } catch (JsonProcessingException jsonEx) {
            System.err.println("Fehler beim Umwandeln des DTO in JSON: " + jsonEx.getMessage());
        }

        try {
            ResponseEntity<MedikamentResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    MedikamentResponseDto.class
            );
            System.out.println("Medikament erfolgreich erstellt.");
            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.err.println("Fehler beim Erstellen des Medikaments: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            System.err.println("Allgemeiner Fehler beim Erstellen des Medikaments: " + e.getMessage());
            return null;
        }
    }
}