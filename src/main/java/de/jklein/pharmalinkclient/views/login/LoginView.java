package de.jklein.views.login;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.jklein.pharmalinkclient.security.AuthService;

@Route("login")
@PageTitle("Login | Pharmalink")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthService authService;

    public LoginView(AuthService authService) {
        this.authService = authService;
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        LoginForm loginForm = new LoginForm();
        loginForm.setForgotPasswordButtonVisible(false);

        add(new H1("Pharmalink"), loginForm);

        loginForm.addLoginListener(e -> {
            authService.login(e.getUsername(), e.getPassword())
                    .subscribe(success -> {
                        if (!success) {
                            UI.getCurrent().access(() -> {
                                loginForm.setError(true);
                                Notification.show("Login fehlgeschlagen. Bitte überprüfen Sie Ihre Eingaben.");
                            });
                        }
                    });
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthService.isAuthenticated()) {
            event.forwardTo("");
        }
    }
}