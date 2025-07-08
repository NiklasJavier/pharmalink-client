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

        loginButton.addClickListener(event -> {
            performLogin();
        });
    }

    private void performLogin() {
        String username = usernameField.getValue();
        String password = passwordField.getValue();

        sendLoginRequest(username, password);
    }

    private void sendLoginRequest(String username, String password) {
        try {
            String requestBody = objectMapper.writeValueAsString(
                    Map.of("username", username, "password", password)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/v1/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        UI.getCurrent().access(() -> {
                            if (response.statusCode() == 200) {
                                try {
                                    String responseBody = response.body();
                                    Map<String, String> jsonResponse = objectMapper.readValue(responseBody, Map.class);
                                    String jwt = jsonResponse.get("jwt");

                                    storeJwt(jwt);
                                    notification.setText("Erfolgreich angemeldet!");
                                    notification.open();
                                    UI.getCurrent().navigate(DashboardView.class);
                                } catch (Exception e) {
                                    notification.setText("Fehler beim Parsen der Antwort: " + e.getMessage());
                                    notification.open();
                                    e.printStackTrace();
                                }
                            } else {
                                notification.setText("Login fehlgeschlagen: " + response.body());
                                notification.open();
                            }
                        });
                    })
                    .exceptionally(e -> {
                        UI.getCurrent().access(() -> {
                            notification.setText("Fehler bei der Kommunikation mit dem Server: " + e.getMessage());
                            notification.open();
                        });
                        e.printStackTrace();
                        return null;
                    });

        } catch (Exception e) {
            notification.setText("Interner Frontend-Fehler: " + e.getMessage());
            notification.open();
            e.printStackTrace();
        }
    }

    private void storeJwt(String jwt) {
        WebStorage.setItem("jwt_token", jwt);
        System.out.println("JWT erfolgreich im Local Storage gespeichert.");
    }
}