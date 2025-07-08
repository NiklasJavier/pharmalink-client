package de.jklein.pharmalinkclient.views.login;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.jklein.pharmalinkclient.service.AuthService;

@Route("login")
@PageTitle("Login | PharmaLink")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthService authService;
    private final de.jklein.pharmalinkclient.security.UserSession userSession;

    public LoginView(AuthService authService, de.jklein.pharmalinkclient.security.UserSession userSession) {
        this.authService = authService;
        this.userSession = userSession;

        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        setSizeFull();

        LoginForm loginForm = new LoginForm();
        loginForm.setAction("login"); // Spring Security fängt dies ab

        add(loginForm);

        loginForm.addLoginListener(event -> {
            authService.login(event.getUsername(), event.getPassword())
                    .thenAccept(success -> {
                        UI.getCurrent().access(() -> {
                            if (success) {
                                // Bei Erfolg zur Hauptseite navigieren
                                UI.getCurrent().navigate("");
                            } else {
                                // Bei Fehler eine Benachrichtigung anzeigen
                                loginForm.setError(true);
                            }
                        });
                    });
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Wenn der Benutzer bereits eingeloggt ist, zur Hauptseite weiterleiten
        if (userSession.isLoggedIn()) {
            event.forwardTo("");
        }
    }
}