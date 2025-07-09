package de.jklein.pharmalinkclient.service;

import de.jklein.pharmalinkclient.config.BackendConfig;
import de.jklein.pharmalinkclient.dto.MedikamentResponseDto;
import de.jklein.pharmalinkclient.security.UserSession;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
}