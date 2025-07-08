package de.jklein.views.login;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.jklein.pharmalinkclient.security.AuthService;

@Route("login")
@PageTitle("Login")
public class LoginView extends VerticalLayout {

    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Notification notification; // Für Fehlermeldungen

    public LoginView() {
        // UI-Komponenten initialisieren
        usernameField = new TextField("Benutzername");
        passwordField = new PasswordField("Passwort");
        loginButton = new Button("Anmelden");
        notification = new Notification();
        notification.setDuration(3000); // Zeigt die Nachricht für 3 Sekunden

        // Layout hinzufügen
        add(usernameField, passwordField, loginButton);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSizeFull();

        // Event-Listener für den Login-Button
        loginButton.addClickListener(event -> {
            performLogin();
        });
    }

    private void performLogin() {
        String username = usernameField.getValue();
        String password = passwordField.getValue();

        // Hier wird die Anfrage an das Backend gesendet
        // Dies ist der kritische Teil, der überprüft werden muss
        sendLoginRequest(username, password);
    }

    private void sendLoginRequest(String username, String password) {
        // Implementieren Sie hier die Logik, um eine HTTP POST-Anfrage an Ihr Backend zu senden
        // Sie benötigen eine HTTP-Client-Bibliothek (z.B. Java 11+ HttpClient, Spring RestTemplate, Vaadin's own client utilities)
        // Oder rufen Sie einen Service auf, der dies für Sie erledigt.
    }
}