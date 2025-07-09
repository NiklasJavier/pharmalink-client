package de.jklein.pharmalinkclient.service;

import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import de.jklein.pharmalinkclient.config.BackendConfig;
import de.jklein.pharmalinkclient.dto.ActorIdResponse; // NEU: Import für ActorIdResponse
import de.jklein.pharmalinkclient.security.UserSession;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Map;

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

    // GEÄNDERT: Gibt jetzt Mono<String> zurück, das direkt die bereinigte Actor ID enthält
    public Mono<String> getCurrentActorId() {
        return webClient.get()
                .uri("/system/current-actor-id")
                .headers(headers -> {
                    String jwt = userSession.getJwt();
                    if (jwt != null && !jwt.isEmpty()) {
                        headers.setBearerAuth(jwt);
                    } else {
                        System.err.println("Kein JWT-Token in der UserSession verfügbar für getCurrentActorId.");
                    }
                })
                .retrieve()
                // Deserialisiert direkt in ActorIdResponse und mappt dann zum reinen actorId String
                .bodyToMono(ActorIdResponse.class)
                .map(ActorIdResponse::getActorId);
    }

    public Mono<Map<String, Object>> getCacheStats() {
        return webClient.get()
                .uri("/system/cache/stats")
                .headers(headers -> {
                    String jwt = userSession.getJwt();
                    if (jwt != null && !jwt.isEmpty()) {
                        headers.setBearerAuth(jwt);
                    } else {
                        System.err.println("Kein JWT-Token in der UserSession verfügbar für getCacheStats.");
                    }
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}