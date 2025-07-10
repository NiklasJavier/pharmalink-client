package de.jklein.pharmalinkclient.views.login;

import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.jklein.pharmalinkclient.security.UserSession;
import de.jklein.pharmalinkclient.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("login")
@PageTitle("Login | PharmaLink")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger log = LoggerFactory.getLogger(LoginView.class);
    private final UserSession userSession;
    private final LoginOverlay loginOverlay = new LoginOverlay();

    public LoginView(AuthService authService, UserSession userSession) {
        this.userSession = userSession;

        // --- Konfiguration des LoginOverlays ---
        loginOverlay.setTitle("Pharmalink");
        loginOverlay.setDescription("Sichere Authentifizierung für die Lieferkette");
        loginOverlay.setOpened(false);

        // "Passwort vergessen"-Button ausblenden
        loginOverlay.setForgotPasswordButtonVisible(false);

        add(loginOverlay);

        // --- Event-Listener für den Login-Versuch ---
        loginOverlay.addLoginListener(event -> {
            final String username = event.getUsername();
            final String password = event.getPassword();

            log.info("Login-Versuch für Benutzer '{}'", username);

            loginOverlay.setEnabled(false);

            authService.fetchToken(username, password)
                    .whenComplete((tokenOptional, ex) -> {
                        getUI().ifPresent(ui -> ui.access(() -> {
                            loginOverlay.setEnabled(true);
                            if (tokenOptional != null && tokenOptional.isPresent()) {
                                log.info("Token erhalten. Leite zur Session-Erstellung mit Benutzer '{}' weiter.", username);
                                String url = String.format("login/token/%s/%s", username, tokenOptional.get());
                                ui.getPage().setLocation(url);
                            } else {
                                log.warn("Login fehlgeschlagen für Benutzer: {}", username);
                                loginOverlay.setError(true);
                            }
                        }));
                    });
        });
    }

    /**
     * Stellt sicher, dass eingeloggte Benutzer weitergeleitet werden und
     * nicht eingeloggte Benutzer das Login-Overlay sehen.
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (userSession.isLoggedIn()) {
            event.forwardTo("");
        } else {
            loginOverlay.setOpened(true);
        }
    }
}