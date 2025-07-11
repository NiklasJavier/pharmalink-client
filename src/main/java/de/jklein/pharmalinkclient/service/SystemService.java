package de.jklein.pharmalinkclient.service;

import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import de.jklein.pharmalinkclient.config.BackendConfig;
import de.jklein.pharmalinkclient.dto.ActorIdResponse;
import de.jklein.pharmalinkclient.dto.SystemStatsDto; // NEU: Import
import de.jklein.pharmalinkclient.dto.SystemStateDto; // NEU: Import
import de.jklein.pharmalinkclient.security.UserSession;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.core.ParameterizedTypeReference; // Beibehalten für generische Typen, falls nötig

import java.util.Map; // Beibehalten für allgemeine Map-Typen, falls nötig

@SpringComponent
@VaadinSessionScope
public class SystemService {

    private final WebClient webClient;
    private final UserSession userSession;
    private final BackendConfig backendConfig;

    public SystemService(WebClient.Builder webClientBuilder, UserSession userSession, BackendConfig backendConfig) {
        this.backendConfig = backendConfig;
        this.webClient = webClientBuilder.baseUrl(this.backendConfig.getBaseUrl()).build();
        this.userSession = userSession;
    }

    /**
     * Ruft die ID des aktuell im Backend registrierten Akteurs ab.
     * Entspricht GET /api/system/current-actor-id (getCurrentActorId)
     */
    public Mono<String> getCurrentActorId() {
        return webClient.get()
                .uri("/system/current-actor-id") // Korrigierter URI-Pfad
                .headers(headers -> {
                    String jwt = userSession.getJwt();
                    if (jwt != null && !jwt.isEmpty()) {
                        headers.setBearerAuth(jwt);
                    } else {
                        System.err.println("Kein JWT-Token in der UserSession verfügbar für getCurrentActorId.");
                    }
                })
                .retrieve()
                .bodyToMono(ActorIdResponse.class)
                .map(ActorIdResponse::getActorId);
    }

    /**
     * Ruft eine schnelle Zusammenfassung der im In-Memory-Cache gehaltenen Elemente ab.
     * Entspricht GET /api/system/cache/stats (getCacheStats)
     */
    public Mono<SystemStatsDto> getCacheStats() { // GEÄNDERT: Rückgabetyp zu SystemStatsDto
        return webClient.get()
                .uri("/system/cache/stats") // Korrigierter URI-Pfad
                .headers(headers -> {
                    String jwt = userSession.getJwt();
                    if (jwt != null && !jwt.isEmpty()) {
                        headers.setBearerAuth(jwt);
                    } else {
                        System.err.println("Kein JWT-Token in der UserSession verfügbar für getCacheStats.");
                    }
                })
                .retrieve()
                .bodyToMono(SystemStatsDto.class); // GEÄNDERT: Deserialisierung zu SystemStatsDto.class
    }

    /**
     * Ruft den vollständigen aktuellen In-Memory-Zustand des Anwendungscaches ab.
     * Entspricht GET /api/system/cache/state (getCacheState)
     */
    public Mono<SystemStateDto> getCacheState() { // NEU: Methode hinzugefügt
        return webClient.get()
                .uri("/system/cache/state") // Korrigierter URI-Pfad
                .headers(headers -> {
                    String jwt = userSession.getJwt();
                    if (jwt != null && !jwt.isEmpty()) {
                        headers.setBearerAuth(jwt);
                    } else {
                        System.err.println("Kein JWT-Token in der UserSession verfügbar für getCacheState.");
                    }
                })
                .retrieve()
                .bodyToMono(SystemStateDto.class); // Deserialisierung zu SystemStateDto.class
    }
}