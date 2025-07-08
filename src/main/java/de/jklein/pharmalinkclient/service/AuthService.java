package de.jklein.pharmalinkclient.service;

import com.vaadin.flow.server.VaadinSession;
import de.jklein.pharmalinkclient.dto.auth.LoginRequest;
import de.jklein.pharmalinkclient.dto.auth.LoginResponse;
import de.jklein.pharmalinkclient.security.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final WebClient webClient;
    private final UserSession userSession;
    private final String loginUrl = "https://d1.navine.tech/api/v1/auth/login";

    public AuthService(WebClient.Builder webClientBuilder, UserSession userSession) {
        this.webClient = webClientBuilder.baseUrl(loginUrl).build();
        this.userSession = userSession;
    }

    /**
     * Ruft den Backend-Endpunkt auf und gibt bei Erfolg den JWT zurück.
     * @return Ein CompletableFuture, das bei Erfolg den Token enthält, sonst leer ist.
     */
    public CompletableFuture<Optional<String>> fetchToken(String username, String password) {
        log.info("Frage Token für Benutzer an: {}", username);
        LoginRequest loginRequest = new LoginRequest(username, password);
        CompletableFuture<Optional<String>> future = new CompletableFuture<>();

        webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(loginRequest), LoginRequest.class)
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .map(response -> Optional.ofNullable(response.getJwt()))
                .defaultIfEmpty(Optional.empty())
                .subscribe(
                        future::complete,
                        error -> {
                            log.error("Fehler beim Abrufen des Tokens für Benutzer '{}': {}", username, error.getMessage());
                            future.complete(Optional.empty());
                        }
                );
        return future;
    }

    /**
     * Führt den Logout-Prozess durch, indem alle Sitzungsdaten bereinigt werden.
     */
    public void logout() {
        if (VaadinSession.getCurrent() != null) {
            try {
                // Bereinige die benutzerdefinierte Session
                userSession.setJwt(null);
                userSession.setUsername(null);
                // Entferne den Security Context aus der zugrundeliegenden HttpSession
                VaadinSession.getCurrent().getSession().removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
                log.info("Benutzerdefinierte Session und Security Context aus HttpSession entfernt.");
            } catch (Exception e) {
                log.error("Fehler beim Bereinigen der Vaadin-Session.", e);
            }
        }
        // Bereinige den SecurityContext für den aktuellen Thread.
        SecurityContextHolder.clearContext();
        log.info("Benutzer ausgeloggt.");
    }
}