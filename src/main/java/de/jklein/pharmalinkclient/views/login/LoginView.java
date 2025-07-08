package de.jklein.pharmalinkclient.views.login;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginForm;
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

    public LoginView(AuthService authService, UserSession userSession) {
        this.userSession = userSession;

        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        setSizeFull();

        LoginForm loginForm = new LoginForm();
        add(loginForm);

        loginForm.addLoginListener(event -> {
            loginForm.setEnabled(false);
            authService.fetchToken(event.getUsername(), event.getPassword())
                    .whenComplete((tokenOptional, ex) -> {
                        getUI().ifPresent(ui -> ui.access(() -> {
                            if (tokenOptional != null && tokenOptional.isPresent()) {
                                log.info("Token erhalten. Leite per Client-Redirect zur Session-Erstellung weiter.");
                                // **DIE ENTSCHEIDENDE ÄNDERUNG HIER**
                                // setLocation() führt eine echte Browser-Umleitung durch und erzeugt eine neue, saubere Anfrage.
                                ui.getPage().setLocation("login/token/" + tokenOptional.get());
                            } else {
                                log.warn("Login fehlgeschlagen für Benutzer: {}", event.getUsername());
                                loginForm.setError(true);
                                loginForm.setEnabled(true);
                            }
                        }));
                    });
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (userSession.isLoggedIn()) {
            event.forwardTo("");
        }
    }
}