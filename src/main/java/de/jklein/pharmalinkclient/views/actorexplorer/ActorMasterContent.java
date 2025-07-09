package de.jklein.pharmalinkclient.views.actorexplorer;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;

import de.jklein.pharmalinkclient.dto.ActorResponseDto;
import de.jklein.pharmalinkclient.dto.ActorFilterCriteriaDto;
import de.jklein.pharmalinkclient.service.ActorService;
import de.jklein.pharmalinkclient.service.StateService; // StateService ist bereits injiziert und wird hier genutzt

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;


@SpringComponent
@UIScope
public class ActorMasterContent extends Div {

    private final ActorService actorService;
    private final StateService stateService;
    private Grid<ActorResponseDto> grid;

    @Autowired
    public ActorMasterContent(ActorService actorService, StateService stateService) {
        this.actorService = actorService;
        this.stateService = stateService;

        addClassName("actor-master-content");
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        getStyle().set("padding", "var(--lumo-space-m)");
        setSizeFull();

        // Titel und (entfernter) Button
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H3 title = new H3("Akteur-Liste (Master)");
        headerLayout.add(title);

        add(headerLayout);

        grid = new Grid<>(ActorResponseDto.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setSizeFull();

        grid.addColumn("bezeichnung").setHeader("Bezeichnung").setAutoWidth(true);
        grid.addColumn("email").setHeader("E-Mail").setAutoWidth(true);
        grid.addColumn("role").setHeader("Rolle").setAutoWidth(true);
        grid.addColumn("actorId").setHeader("Akteur ID").setAutoWidth(true);

        add(grid);

        stateService.addActorFilterCriteriaListener(this::updateGridWithFilters);

        // Initialer Aufruf zum Laden und Filtern der Akteure beim Start der Komponente
        updateGridWithFilters(stateService.getCurrentActorFilterCriteria());

        grid.asSingleSelect().addValueChangeListener(event -> {
            stateService.setSelectedActor(event.getValue());
        });
    }

    public void updateGridWithFilters(Optional<ActorFilterCriteriaDto> criteriaOptional) {
        // ALLE Akteure vom Dienst laden (dies ist die REST-Abfrage)
        List<ActorResponseDto> allActors = actorService.getAllActors();

        // WICHTIG: Speichern Sie die geladenen Akteure im StateService,
        // damit andere Teile der Anwendung (wie "Meine Stammdaten anzeigen") sie nutzen können.
        stateService.setAllLoadedActors(allActors); // <-- DIES IST DIE NEUE ZEILE

        List<ActorResponseDto> filteredActors = allActors;

        if (criteriaOptional.isPresent()) {
            ActorFilterCriteriaDto criteria = criteriaOptional.get();
            String searchTerm = criteria.getSearchTerm() != null ? criteria.getSearchTerm().toLowerCase() : "";

            if (!searchTerm.isEmpty()) {
                filteredActors = allActors.stream()
                        .filter(actor ->
                                (actor.getBezeichnung() != null && actor.getBezeichnung().toLowerCase().contains(searchTerm)) ||
                                        (actor.getActorId() != null && actor.getActorId().toLowerCase().contains(searchTerm)) ||
                                        (actor.getEmail() != null && actor.getEmail().toLowerCase().contains(searchTerm))
                        )
                        .collect(Collectors.toList());
            }
        }
        grid.setItems(filteredActors);

        if (filteredActors.isEmpty()) {
            grid.asSingleSelect().clear();
        } else {
            if (!grid.asSingleSelect().isEmpty() && !filteredActors.contains(grid.asSingleSelect().getValue())) {
                grid.asSingleSelect().clear();
            }
        }
    }
}