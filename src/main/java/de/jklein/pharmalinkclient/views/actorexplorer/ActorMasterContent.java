package de.jklein.pharmalinkclient.views.actorexplorer;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

import de.jklein.pharmalinkclient.dto.ActorResponseDto;
import de.jklein.pharmalinkclient.service.ActorService;   // Import für den Akteur Service
import de.jklein.pharmalinkclient.service.StateService;   // Import für den State Service

import org.springframework.beans.factory.annotation.Autowired; // Import für Spring Autowiring

import java.util.List; // Import für List


@SpringComponent // Macht diese Klasse zu einer Spring-verwalteten Komponente
@UIScope        // Stellt sicher, dass eine Instanz pro UI-Sitzung erstellt wird
public class ActorMasterContent extends Div {

    private final ActorService actorService;
    private final StateService stateService;
    private final Grid<ActorResponseDto> grid; // Das Grid zur Anzeige der Akteure

    // Konstruktor: Spring injiziert automatisch die benötigten Services
    @Autowired
    public ActorMasterContent(ActorService actorService, StateService stateService) {
        this.actorService = actorService;
        this.stateService = stateService;

        // Grundlegendes Styling und Layout für diesen Master-Bereich
        addClassName("actor-master-content"); // CSS-Klasse für spezifisches Styling
        getStyle().set("background-color", "var(--lumo-contrast-5pct)"); // Leichter Hintergrund
        getStyle().set("padding", "var(--lumo-space-m)");                // Innenabstand
        setSizeFull(); // Füllt den gesamten verfügbaren Platz aus

        add(new H3("Akteur-Liste (Master)")); // Überschrift des Master-Bereichs

        // Initialisierung des Grids
        grid = new Grid<>(ActorResponseDto.class, false); // Grid für ActorResponseDto, keine automatische Spalten
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER); // Lumo-Styling-Varianten
        grid.setSizeFull(); // Grid füllt den Container aus

        // Spaltendefinitionen für das Akteur-Grid
        grid.addColumn("bezeichnung").setHeader("Bezeichnung").setAutoWidth(true);
        grid.addColumn("email").setHeader("E-Mail").setAutoWidth(true);
        grid.addColumn("role").setHeader("Rolle").setAutoWidth(true);
        grid.addColumn("actorId").setHeader("Akteur ID").setAutoWidth(true);
        grid.addColumn("ipfsLink").setHeader("IPFS Link").setAutoWidth(true);

        // Daten laden
        List<ActorResponseDto> actors = actorService.getAllActors(); // Alle Akteure vom Service abrufen
        grid.setItems(actors); // Daten im Grid anzeigen

        // Automatische Selektion des ersten Elements beim Laden der Seite
        if (!actors.isEmpty()) {
            grid.asSingleSelect().setValue(actors.get(0)); // Den ersten Akteur auswählen
        }

        // Listener für die Grid-Auswahl: Bei Auswahl eines Akteurs den StateService aktualisieren
        grid.asSingleSelect().addValueChangeListener(event -> {
            stateService.setSelectedActor(event.getValue()); // Setzt den ausgewählten Akteur im StateService
        });

        add(grid); // Das Grid zum Master-Bereich hinzufügen
    }
}