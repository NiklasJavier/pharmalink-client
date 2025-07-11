package de.jklein.pharmalinkclient.views.system; // Beispielpfad, passe ihn an deine Struktur an

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.Route;
import de.jklein.pharmalinkclient.service.StateService;
import de.jklein.pharmalinkclient.service.SystemService;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.scheduler.Schedulers;

@Route("system-state-loader")
@PermitAll
public class SystemStateLoaderView extends Div {

    private final SystemService systemService;
    private final StateService stateService;

    @Autowired
    public SystemStateLoaderView(SystemService systemService, StateService stateService) {
        this.systemService = systemService;
        this.stateService = stateService;

        // Button, um den Systemzustand manuell zu laden
        Button loadStateButton = new Button("Systemzustand laden", event -> loadSystemState());
        add(loadStateButton);

        // Optional: Listener, um auf Änderungen des Systemzustands im StateService zu reagieren
        stateService.addSystemStateListener(systemState -> {
            if (systemState != null) {
                Notification.show("Systemzustand erfolgreich im StateService aktualisiert.");
                // Hier könntest du weitere UI-Updates oder Logik triggern,
                // z.B. die Anzahl der geladenen Akteure anzeigen:
                // System.out.println("Geladene Akteure: " + systemState.getAllActors().size());
            } else {
                Notification.show("Systemzustand im StateService wurde auf null gesetzt.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
    }

    private void loadSystemState() {
        UI ui = UI.getCurrent(); // Vaadin UI-Instanz für sichere UI-Updates

        Notification.show("Lade Systemzustand...", 2000, Notification.Position.TOP_CENTER);

        // Aufruf des SystemService, um den SystemStateDto vom Backend abzurufen
        systemService.getCacheState()
                .subscribeOn(Schedulers.boundedElastic()) // Führt den Netzwerkaufruf in einem Hintergrund-Thread aus
                .subscribe(
                        systemState -> ui.access(() -> { // Wechselt zurück in den Vaadin UI-Thread
                            // Speichere den gesamten SystemStateDto im StateService
                            stateService.setSystemState(systemState);

                            // Optional: Speichere auch die Unterlisten separat im StateService,
                            // wenn du sie dort direkt verwalten oder mit separaten Listenern überwachen möchtest.
                            stateService.setAllSystemActors(systemState.getAllActors());
                            stateService.setAllSystemMedikamente(systemState.getAllMedikamente());
                            stateService.setMySystemUnits(systemState.getMyUnits());

                            // Markiere, dass die Systemdaten für die Sitzung geladen wurden
                            stateService.setSystemDataLoadedForSession(true);

                            Notification.show("Systemzustand erfolgreich geladen!");
                        }),
                        error -> ui.access(() -> { // Fehlerbehandlung im Vaadin UI-Thread
                            Notification.show("Fehler beim Laden des Systemzustands: " + error.getMessage(), 5000, Notification.Position.MIDDLE)
                                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                            System.err.println("Fehler beim Laden des Systemzustands: " + error.getMessage());
                        })
                );
    }
}