// src/main/java/de/jklein/pharmalinkclient/views/medikamente/MasterContent.java
package de.jklein.pharmalinkclient.views.medikamente;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

import de.jklein.pharmalinkclient.dto.MedikamentFilterCriteriaDto;
import de.jklein.pharmalinkclient.dto.MedikamentResponseDto;
import de.jklein.pharmalinkclient.service.MedikamentService;
import de.jklein.pharmalinkclient.service.StateService;

import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.data.renderer.LitRenderer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@SpringComponent
@UIScope
public class MasterContent extends Div {

    private final MedikamentService medikamentService;
    private final StateService stateService;
    private final Grid<MedikamentResponseDto> grid;

    @Autowired
    public MasterContent(MedikamentService medikamentService, StateService stateService) {
        this.medikamentService = medikamentService;
        this.stateService = stateService;
        addClassName("master-content");
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        getStyle().set("padding", "var(--lumo-space-m)");
        setSizeFull();

        add(new H3("Medikamenten-Liste (Master)"));

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

        // NEU (schon drin): Listener für die Auswahl im Grid, um das ausgewählte Medikament im StateService zu setzen
        grid.asSingleSelect().addValueChangeListener(event -> {
            stateService.setSelectedMedikament(event.getValue());
        });

        add(grid);

        updateGridWithFilters(stateService.getCurrentMedikamentFilterCriteria());

        stateService.addMedikamentFilterCriteriaListener(this::updateGridWithFilters);
    }

    public void updateGridWithFilters(Optional<MedikamentFilterCriteriaDto> criteriaOptional) {
        List<MedikamentResponseDto> allMedikamente = medikamentService.getAllMedikamente();

        List<MedikamentResponseDto> filteredMedikamente = allMedikamente;

        if (criteriaOptional.isPresent()) {
            MedikamentFilterCriteriaDto criteria = criteriaOptional.get();
            String searchTerm = criteria.getSearchTerm() != null ? criteria.getSearchTerm().toLowerCase() : "";
            String statusFilter = criteria.getStatusFilter();

            filteredMedikamente = allMedikamente.stream()
                    .filter(medikament -> {
                        boolean matchesSearch = true;
                        if (!searchTerm.isEmpty()) {
                            matchesSearch = (medikament.getBezeichnung() != null && medikament.getBezeichnung().toLowerCase().contains(searchTerm)) ||
                                    (medikament.getHerstellerId() != null && medikament.getHerstellerId().toLowerCase().contains(searchTerm)) ||
                                    (medikament.getMedId() != null && medikament.getMedId().toLowerCase().contains(searchTerm));
                        }

                        boolean matchesStatus = true;
                        if (!"Ohne Filter".equals(statusFilter) && statusFilter != null) {
                            matchesStatus = (medikament.getStatus() != null && medikament.getStatus().equalsIgnoreCase(statusFilter));
                        }
                        return matchesSearch && matchesStatus;
                    })
                    .collect(Collectors.toList());
        }
        grid.setItems(filteredMedikamente);
        // NEU (schon drin): Sicherstellen, dass nach dem Aktualisieren des Grids das selectedMedikament zurückgesetzt wird,
        // falls das zuvor ausgewählte Element nicht mehr in der Liste ist.
        if (!filteredMedikamente.contains(stateService.getSelectedMedikament().orElse(null))) {
            stateService.clearSelectedMedikament();
        }
    }
}