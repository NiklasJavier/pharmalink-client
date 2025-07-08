package de.jklein.pharmalinkclient.service;

import de.jklein.pharmalinkclient.dto.auth.LoginRequest;
import de.jklein.pharmalinkclient.dto.auth.LoginResponse;
import de.jklein.pharmalinkclient.security.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@Service
public class AuthService {

    private final WebClient webClient;
    private final UserSession userSession;

    // Die Backend-URL für den Login
    private final String loginUrl = "https://d1.navine.tech/api/v1/auth/login";

    @Autowired
    public AuthService(WebClient.Builder webClientBuilder, de.jklein.pharmalinkclient.security.UserSession userSession) {
        this.webClient = webClientBuilder.baseUrl(loginUrl).build();
        this.userSession = userSession;
    }

    public CompletableFuture<Boolean> login(String username, String password) {
        LoginRequest loginRequest = new LoginRequest(username, password);

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(loginRequest), LoginRequest.class)
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .subscribe(response -> {
                    // Bei Erfolg den JWT in der Session speichern
                    userSession.setJwt(response.getJwt());
                    future.complete(true);
                }, error -> {
                    // Bei einem Fehler (z.B. 401 Unauthorized)
                    future.complete(false);
                });

        return future;
    }

    public void logout() {
        // Den JWT aus der Session entfernen
        userSession.setJwt(null);
    }
}