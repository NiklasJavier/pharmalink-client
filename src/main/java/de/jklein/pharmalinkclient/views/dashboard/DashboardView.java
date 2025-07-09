package de.jklein.pharmalinkclient.views.dashboard;


import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.lumo.LumoUtility; // Benötigt für Styling-Utilities
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.Border;
import com.vaadin.flow.theme.lumo.LumoUtility.BorderColor;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
// Shadow-Import bleibt entfernt


import de.jklein.pharmalinkclient.security.UserSession;
import de.jklein.pharmalinkclient.service.StateService;
import de.jklein.pharmalinkclient.views.MainLayout;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.util.Map;
import java.util.function.Consumer;

@PageTitle("Dashboard")
@Route(value = "", layout = MainLayout.class)
@PermitAll
@SpringComponent
@UIScope
public class DashboardView extends Main {

    private final StateService stateService;
    private final UserSession userSession;
    private final TextField usernameField;
    private final TextField roleField;
    private final TextField actorIdReadonlyField;

    private TextField actorCountField;
    private TextField medikamentCountField;
    private TextField myUnitsCountField;


    private Consumer<String> actorIdListener;
    private Consumer<Map<String, Object>> cacheStatsListener;


    @Autowired
    public DashboardView(StateService stateService, UserSession userSession) {
        this.stateService = stateService;
        this.userSession = userSession;
        addClassName("dashboard-view");

        VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setSpacing(false);
        rootLayout.setPadding(false);
        rootLayout.setAlignItems(FlexComponent.Alignment.CENTER);


        // 1. Karte: Benutzer- und Akteurinformationen
        VerticalLayout userInfoCard = createCardLayout();
        HorizontalLayout userInfoTitleRow = new HorizontalLayout(
                new H3("Ihre Informationen"),
                VaadinIcon.USER.create()
        );
        userInfoTitleRow.setAlignItems(FlexComponent.Alignment.CENTER);
        userInfoTitleRow.setSpacing(true);
        userInfoCard.add(userInfoTitleRow);

        HorizontalLayout userAndRoleRow = new HorizontalLayout();
        userAndRoleRow.addClassNames(Gap.MEDIUM);
        userAndRoleRow.setPadding(false);
        userAndRoleRow.setSpacing(true);

        usernameField = new TextField("Benutzername");
        usernameField.setReadOnly(true);
        usernameField.setValue(userSession.getUsername() != null ? userSession.getUsername() : "Nicht angemeldet");
        usernameField.setWidth("200px");
        userAndRoleRow.add(usernameField);

        roleField = new TextField("Ihre Rolle");
        roleField.setReadOnly(true);
        roleField.setValue("Lädt...");
        roleField.setWidth("200px");
        userAndRoleRow.add(roleField);

        userInfoCard.add(userAndRoleRow);

        actorIdReadonlyField = new TextField("Ihre Akteur-ID");
        actorIdReadonlyField.setReadOnly(true);
        actorIdReadonlyField.setValue("Lädt...");
        actorIdReadonlyField.setWidth("500px");
        userInfoCard.add(actorIdReadonlyField);


        // 2. Karte: Statistik-Informationen
        VerticalLayout statsCard = createCardLayout();
        statsCard.setMinWidth("350px");

        HorizontalLayout statsTitleRow = new HorizontalLayout(
                new H3("Statistiken"),
                VaadinIcon.CHART_GRID.create()
        );
        statsTitleRow.setAlignItems(FlexComponent.Alignment.CENTER);
        statsTitleRow.setSpacing(true);
        statsCard.add(statsTitleRow);

        FormLayout statsFormLayout = new FormLayout();
        statsFormLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1) // Alle Felder untereinander
        );

        actorCountField = createReadOnlyTextField("Akteur Anzahl", "Lädt...");
        actorCountField.addClassName("stat-value-field");
        medikamentCountField = createReadOnlyTextField("Medikament Anzahl", "Lädt...");
        medikamentCountField.addClassName("stat-value-field");
        myUnitsCountField = createReadOnlyTextField("Meine Einheiten Anzahl", "Lädt...");
        myUnitsCountField.addClassName("stat-value-field");

        statsFormLayout.add(actorCountField, medikamentCountField, myUnitsCountField);

        statsCard.add(statsFormLayout);


        // HorizontalLayout, um die beiden Karten nebeneinander anzuordnen
        HorizontalLayout cardsContainer = new HorizontalLayout();
        cardsContainer.addClassNames(Gap.MEDIUM);
        cardsContainer.setPadding(true);
        cardsContainer.setSpacing(true);
        cardsContainer.setWidth("auto");

        cardsContainer.add(userInfoCard, statsCard);

        rootLayout.add(cardsContainer);
        add(rootLayout);

        // Füge Copy-to-Clipboard-Funktion zu allen Textfeldern hinzu
        addCopyToClipboardOnClick(usernameField); // Nur das Feld übergeben
        addCopyToClipboardOnClick(roleField);
        addCopyToClipboardOnClick(actorIdReadonlyField);
        addCopyToClipboardOnClick(actorCountField);
        addCopyToClipboardOnClick(medikamentCountField);
        addCopyToClipboardOnClick(myUnitsCountField);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        actorIdListener = rawActorId -> {
            UI.getCurrent().access(() -> {
                if (rawActorId != null && !rawActorId.isEmpty()) {
                    actorIdReadonlyField.setValue(rawActorId);

                    String role = "Unbekannt";
                    if (rawActorId.contains("-")) {
                        String[] parts = rawActorId.split("-", 2);
                        if (parts.length > 0) {
                            role = parts[0];
                        }
                    }
                    roleField.setValue(role);
                } else {
                    roleField.setValue("Nicht verfügbar");
                    actorIdReadonlyField.setValue("Nicht verfügbar");
                }
            });
        };
        stateService.addCurrentActorIdListener(actorIdListener);

        cacheStatsListener = stats -> {
            UI.getCurrent().access(() -> {
                if (stats != null && !stats.isEmpty()) {
                    actorCountField.setValue(stats.getOrDefault("actorCount", 0).toString());
                    medikamentCountField.setValue(stats.getOrDefault("medikamentCount", 0).toString());
                    myUnitsCountField.setValue(stats.getOrDefault("myUnitsCount", 0).toString());
                } else {
                    actorCountField.setValue("N/A");
                    medikamentCountField.setValue("N/A");
                    myUnitsCountField.setValue("N/A");
                }
            });
        };
        stateService.addCacheStatsListener(cacheStatsListener);

        actorIdListener.accept(stateService.getCurrentActorId());
        cacheStatsListener.accept(stateService.getCacheStats());
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (actorIdListener != null) {
            stateService.removeCurrentActorIdListener(actorIdListener);
        }
        if (cacheStatsListener != null) {
            stateService.removeCacheStatsListener(cacheStatsListener);
        }
    }

    /**
     * Helfer-Methode zum Erstellen eines schreibgeschützten TextField.
     */
    private TextField createReadOnlyTextField(String label, String initialValue) {
        TextField field = new TextField(label);
        field.setReadOnly(true);
        field.setValue(initialValue);
        return field;
    }

    /**
     * Fügt einem TextField einen Klick-Listener hinzu, der dessen Inhalt in die Zwischenablage kopiert.
     * Nutzt die document.execCommand('copy') Methode.
     */
    private void addCopyToClipboardOnClick(TextField textField) {
        textField.getElement().addEventListener("click", event -> {
            UI.getCurrent().access(() -> {
                String js =
                        "try {" +
                                "    const inputElement = $0.inputElement || $0.shadowRoot.querySelector('input');" + // Vaadin 24 may use shadow DOM
                                "    if (inputElement) {" +
                                "        inputElement.select();" +
                                "        document.execCommand('copy');" +
                                "        console.log('Text copied using execCommand:', inputElement.value);" +
                                "    } else {" +
                                "        console.warn('Could not find input element to copy.');" +
                                "    }" +
                                "} catch (err) {" +
                                "    console.error('Failed to copy text using execCommand: ', err);" +
                                "}";
                // $0 bezieht sich auf das TextField-Element im DOM
                UI.getCurrent().getPage().executeJs(js, textField.getElement());
            });
        });
    }

    /**
     * Helfer-Methode zum Erstellen eines Layouts, das wie eine Karte aussieht.
     */
    private VerticalLayout createCardLayout() {
        VerticalLayout card = new VerticalLayout();
        card.addClassNames(
                Background.BASE,
                Border.ALL,
                BorderColor.CONTRAST_10,
                "border-radius-l", // Klasse für Abrundung (prüfen Sie Ihre CSS-Datei!)
                Padding.LARGE,     // Mehr Innenabstand
                Margin.MEDIUM      // Außenabstand
        );
        card.setSpacing(false);
        card.setPadding(true);
        card.setWidth("auto");
        card.setHeight("auto");
        return card;
    }
}