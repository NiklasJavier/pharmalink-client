package de.jklein.pharmalinkclient.views.login; // Paket prüfen

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.page.WebStorage; // Import für Local Storage
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route; // Falls dies Ihre Login-Route ist

import com.fasterxml.jackson.databind.ObjectMapper; // Import für Jackson
import de.jklein.pharmalinkclient.views.dashboard.DashboardView; // Passen Sie den Import Ihrer Dashboard-View an

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;

@Route("login") // Stellen Sie sicher, dass dies Ihre tatsächliche Login-Route ist
@PageTitle("Login")
public class LoginView extends VerticalLayout {

    private final TextField usernameField;
    private final PasswordField passwordField;
    private final Button loginButton;
    private final Notification notification;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public LoginView() {
        usernameField = new TextField("Benutzername");
        passwordField = new PasswordField("Passwort");
        loginButton = new Button("Anmelden");
        notification = new Notification();
        notification.setDuration(3000);

        httpClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();

        add(usernameField, passwordField, loginButton);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSizeFull();

        // Debug-Ausgabe: Button-Listener wird angehängt
        System.out.println("LoginView: Adding click listener to loginButton.");
        loginButton.addClickListener(event -> {
            System.out.println("LoginView: Login button clicked event received!"); // Debug
            performLogin();
        });
    }

    private void performLogin() {
        String username = usernameField.getValue();
        String password = passwordField.getValue();
        System.out.println("LoginView: performLogin called. Username: " + username); // Debug

        sendLoginRequest(username, password);
    }

    private void sendLoginRequest(String username, String password) {
    }
}