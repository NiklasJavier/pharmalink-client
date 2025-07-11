// src/main/java/de/jklein/pharmalinkclient/views/medikamente/MasterContent.java
package de.jklein.pharmalinkclient.views.medikamente;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.component.UI; // Import UI
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;

import de.jklein.pharmalinkclient.dto.MedikamentFilterCriteriaDto;
import de.jklein.pharmalinkclient.dto.MedikamentResponseDto;
import de.jklein.pharmalinkclient.service.MedikamentService;
import de.jklein.pharmalinkclient.service.StateService;

import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.data.renderer.LitRenderer;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer; // For the double-click listener
import java.util.stream.Collectors; // Beibehalten, obwohl clientseitige Filterung entfällt, falls Stream-Operationen für andere Zwecke genutzt werden


@SpringComponent
@UIScope
public class MasterContent extends Div {

    private final MedikamentService medikamentService;
    private final StateService stateService;
    private final Grid<MedikamentResponseDto> grid;

    // Consumer to notify parent view about double-click
    private Consumer<MedikamentResponseDto> doubleClickListener;


    @Autowired
    public MasterContent(MedikamentService medikamentService, StateService stateService) {
        this.medikamentService = medikamentService;
        this.stateService = stateService;
        addClassName("master-content");
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        getStyle().set("padding", "var(--lumo-space-m)");
        setSizeFull();

        grid = new Grid<>(MedikamentResponseDto.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setSizeFull();

        // Spalte: Status (mit LitRenderer und Inline-Styles für Badge)
        grid.addColumn(LitRenderer.<MedikamentResponseDto>of(
                                "<span style='" +
                                        "display: inline-block; " +
                                        "padding: 0.3em 0.6em; " +
                                        "border-radius: 0.2em; " +
                                        "font-size: 0.7em; " +
                                        "font-weight: 600; " +
                                        "line-height: 1; " +
                                        "text-transform: uppercase; " +
                                        "box-sizing: border-box; " +
                                        "min-width: 60px; " +
                                        "text-align: center; " +
                                        "white-space: nowrap; " +
                                        "background-color: ${item.bgColor}; " +
                                        "color: ${item.textColor};" +
                                        "'>" +
                                        "${item.status}" +
                                        "</span>")
                        .withProperty("status", MedikamentResponseDto::getStatus)
                        .withProperty("bgColor", MedikamentResponseDto -> {
                            String status = MedikamentResponseDto.getStatus();
                            if (status == null) return "#A9A9A9";
                            switch (status.toLowerCase()) {
                                case "freigegeben": return "#D3F8D3";
                                case "abgelehnt": return "#FFD3D3";
                                case "erstellt": return "#FFFACD";
                                default: return "#A9A9A9";
                            }
                        })
                        .withProperty("textColor", MedikamentResponseDto -> {
                            String status = MedikamentResponseDto.getStatus();
                            if (status == null) return "#FFFFFF";
                            switch (status.toLowerCase()) {
                                case "freigegeben": return "#006400";
                                case "abgelehnt": return "#8B0000";
                                case "erstellt": return "#B8860B";
                                default: return "#FFFFFF";
                            }
                        }))
                .setTooltipGenerator(MedikamentResponseDto::getStatus)
                .setHeader("Status")
                .setAutoWidth(true);

        grid.addColumn("bezeichnung").setHeader("Bezeichnung").setAutoWidth(true);
        grid.addColumn("medId").setHeader("Medikament ID").setAutoWidth(true);

        // Listener für die Auswahl im Grid, um das ausgewählte Medikament im StateService zu setzen
        grid.asSingleSelect().addValueChangeListener(event -> {
            stateService.setSelectedMedikament(event.getValue());
        });

        // Add double-click listener
        grid.addItemDoubleClickListener(event -> {
            MedikamentResponseDto selectedMedikament = event.getItem();
            if (selectedMedikament != null && doubleClickListener != null) {
                doubleClickListener.accept(selectedMedikament);
            } else if (selectedMedikament == null) {
                Notification.show("Kein Medikament zum Anzeigen ausgewählt.", 3000, Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });


        add(grid);

        // Initialer Aufruf zum Laden und Filtern der Medikamente beim Start der Komponente
        // Die `updateGridWithFilters`-Methode wird direkt aufgerufen und holt Daten über den Service.
        // updateGridWithFilters(stateService.getCurrentMedikamentFilterCriteria());

        // Listener für Änderungen der Filterkriterien im StateService
        stateService.addMedikamentFilterCriteriaListener(this::updateGridWithFilters);
    }

    // Public method to set the double-click listener from the parent view (MedikamenteView)
    public void addDoubleClickListener(Consumer<MedikamentResponseDto> listener) {
        this.doubleClickListener = listener;
    }


    public void updateGridWithFilters(Optional<MedikamentFilterCriteriaDto> criteriaOptional) {
        List<MedikamentResponseDto> filteredMedikamente;

        MedikamentFilterCriteriaDto criteria = criteriaOptional.orElse(new MedikamentFilterCriteriaDto("", "Ohne Filter", false));
        System.out.println("DEBUG MasterContent: Suche Medikamente mit Kriterien: " + criteria.getSearchTerm() + ", " + criteria.getStatusFilter() + ", " + criteria.isFilterByCurrentActor());

        filteredMedikamente = medikamentService.searchMedikamente(criteria);

        System.out.println("DEBUG MasterContent: Medikamente vom Service erhalten: " + filteredMedikamente.size() + " Einträge.");
        if (filteredMedikamente.isEmpty()) {
            System.out.println("DEBUG MasterContent: Erhaltene Liste ist leer.");
        } else {
            System.out.println("DEBUG MasterContent: Erster Medikamenten-Eintrag (Beispiel): " + filteredMedikamente.get(0).getBezeichnung() + ", ID: " + filteredMedikamente.get(0).getMedId());
        }

        grid.setItems(filteredMedikamente);

        if (!filteredMedikamente.contains(stateService.getSelectedMedikament().orElse(null))) {
            stateService.clearSelectedMedikament();
        }
    }
}