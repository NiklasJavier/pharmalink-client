package de.jklein.pharmalinkclient.security;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import de.jklein.pharmalinkclient.dto.auth.JwtResponse;
import de.jklein.pharmalinkclient.dto.auth.LoginRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

    private final WebClient webClient = WebClient.create("http://localhost:8080");
    private static final String JWT_SESSION_KEY = "jwtToken";

    public Mono<Boolean> login(String username, String password) {
        return webClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequest(username, password))
                .retrieve()
                .bodyToMono(JwtResponse.class)
                .map(response -> {
                    String jwt = response.getJwt();
                    if (jwt != null && !jwt.isEmpty()) {
                        VaadinSession.getCurrent().setAttribute(JWT_SESSION_KEY, jwt);
                        UI.getCurrent().navigate("");
                        return true;
                    }
                    return false;
                }).onErrorReturn(false);
    }

    public void logout() {
        VaadinSession.getCurrent().setAttribute(JWT_SESSION_KEY, null);
        UI.getCurrent().getPage().setLocation("/login");
    }

    public static String getJwt() {
        if (VaadinSession.getCurrent() == null) {
            return null;
        }
        return (String) VaadinSession.getCurrent().getAttribute(JWT_SESSION_KEY);
    }

    public static boolean isAuthenticated() {
        return getJwt() != null;
    }
}