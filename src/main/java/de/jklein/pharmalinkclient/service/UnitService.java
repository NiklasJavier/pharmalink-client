package de.jklein.pharmalinkclient.service;

import de.jklein.pharmalinkclient.config.BackendConfig;
import de.jklein.pharmalinkclient.dto.CreateUnitRequestDto;
import de.jklein.pharmalinkclient.dto.TransferUnitRequestDto;
import de.jklein.pharmalinkclient.dto.UnitResponseDto;
import de.jklein.pharmalinkclient.dto.auth.TemperatureReadingRequestDto;
import de.jklein.pharmalinkclient.security.UserSession;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.http.MediaType; // Für MediaType


@SpringComponent
@UIScope
public class UnitService {

    private final RestTemplate restTemplate;
    private final BackendConfig backendConfig;
    private final UserSession userSession;

    public UnitService(BackendConfig backendConfig, UserSession userSession) {
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
            // Optional: Eine spezifische Exception werfen, anstatt nur zu loggen
        }
        return new HttpEntity<>(headers);
    }

    // Hilfsmethode zum Erstellen der HttpEntity mit JWT und Body (für POST)
    private <T> HttpEntity<T> createHttpEntityWithJwtAndBody(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // Set Content-Type for POST
        String jwt = userSession.getJwt();
        if (jwt != null && !jwt.isEmpty()) {
            headers.setBearerAuth(jwt);
        } else {
            System.err.println("Kein JWT-Token in der UserSession verfügbar. Authentifizierung erforderlich.");
            // Optional: Eine spezifische Exception werfen
        }
        return new HttpEntity<>(body, headers);
    }


    // GET /api/v1/units/{unitId}
    public UnitResponseDto getUnitById(String unitId) {
        String url = backendConfig.getBaseUrl() + "/units/" + unitId;
        HttpEntity<String> entity = createHttpEntityWithJwt();

        try {
            ResponseEntity<UnitResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    UnitResponseDto.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.err.println("Fehler beim Laden der Einheit mit ID '" + unitId + "': " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Allgemeiner Fehler beim Laden der Einheit mit ID: " + e.getMessage());
        }
        return null;
    }

    // POST /api/v1/units/{medId}/units
    public UnitResponseDto createUnit(String medId, CreateUnitRequestDto request) {
        String url = backendConfig.getBaseUrl() + "/units/" + medId + "/units";
        HttpEntity<CreateUnitRequestDto> entity = createHttpEntityWithJwtAndBody(request);

        try {
            ResponseEntity<UnitResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    UnitResponseDto.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.err.println("Fehler beim Erstellen einer Einheit für Medikament ID '" + medId + "': " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Allgemeiner Fehler beim Erstellen einer Einheit: " + e.getMessage());
        }
        return null;
    }

    // POST /api/v1/units/{unitId}/transfer
    public UnitResponseDto transferUnit(String unitId, TransferUnitRequestDto request) {
        String url = backendConfig.getBaseUrl() + "/units/" + unitId + "/transfer";
        HttpEntity<TransferUnitRequestDto> entity = createHttpEntityWithJwtAndBody(request);

        try {
            ResponseEntity<UnitResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    UnitResponseDto.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.err.println("Fehler beim Transfer der Einheit mit ID '" + unitId + "': " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Allgemeiner Fehler beim Transfer der Einheit: " + e.getMessage());
        }
        return null;
    }

    // POST /api/v1/units/{unitId}/temperature-readings
    public UnitResponseDto addTemperatureReading(String unitId, TemperatureReadingRequestDto request) {
        String url = backendConfig.getBaseUrl() + "/units/" + unitId + "/temperature-readings";
        HttpEntity<TemperatureReadingRequestDto> entity = createHttpEntityWithJwtAndBody(request);

        try {
            ResponseEntity<UnitResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    UnitResponseDto.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.err.println("Fehler beim Hinzufügen von Temperaturmessung zu Einheit '" + unitId + "': " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Allgemeiner Fehler beim Hinzufügen von Temperaturmessung: " + e.getMessage());
        }
        return null;
    }

    // GET /api/v1/units/{medId}/units-by-charge
    // Beachten Sie, dass dieser Endpunkt laut Ihrer Beschreibung eine Map zurückgeben könnte.
    // Hier wird angenommen, dass es eine Liste von UnitResponseDto ist. Wenn es eine Map ist, müsste der Rückgabetyp angepasst werden.
    public List<UnitResponseDto> getUnitsByMedIdAndCharge(String medId) {
        String url = backendConfig.getBaseUrl() + "/units/" + medId + "/units-by-charge";
        HttpEntity<String> entity = createHttpEntityWithJwt();

        try {
            ResponseEntity<UnitResponseDto[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    UnitResponseDto[].class // Annahme: gibt Liste von Units zurück, auch wenn Map erwähnt ist
            );
            if (response.getBody() != null) {
                return Arrays.asList(response.getBody());
            }
        } catch (HttpClientErrorException e) {
            System.err.println("Fehler beim Laden von Einheiten nach Medikament-ID und Charge '" + medId + "': " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Allgemeiner Fehler beim Laden von Einheiten nach Medikament-ID und Charge: " + e.getMessage());
        }
        return Collections.emptyList();
    }
}