package de.jklein.pharmalinkclient.views.actorexplorer;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.component.textfield.TextField;

import de.jklein.pharmalinkclient.dto.ActorResponseDto;
import de.jklein.pharmalinkclient.dto.MedikamentResponseDto;
import de.jklein.pharmalinkclient.service.StateService;
import de.jklein.pharmalinkclient.service.MedikamentService;


import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.dom.DomEventListener;


@SpringComponent
@UIScope
public class ActorDetailContent extends Div {

    private final StateService stateService;
    private final MedikamentService medikamentService;

    private H3 mainTitle;
    private FormLayout detailsLayout;
    private VerticalLayout ipfsDataSection;
    private Grid<Map.Entry<String, Object>> ipfsDataGrid;

    private VerticalLayout actorMedicationsSection;
    private Grid<MedikamentResponseDto> actorMedicationsGrid;


    private TextField actorIdField;
    private TextField bezeichnungField;
    private TextField roleField;
    private TextField emailField;


    @Autowired
    public ActorDetailContent(StateService stateService, MedikamentService medikamentService) {
        this.stateService = stateService;
        this.medikamentService = medikamentService;
        addClassName("actor-detail-content");
        setSizeFull();
        addClassNames(LumoUtility.Padding.LARGE);

        mainTitle = new H3("Akteur Details");
        mainTitle.addClassNames("margin-bottom-xl");
        add(mainTitle);

        detailsLayout = new FormLayout();
        detailsLayout.addClassName("actor-main-details");
        detailsLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        detailsLayout.addClassNames(LumoUtility.Gap.SMALL);

        actorIdField = createReadOnlyCopyableTextField("Akteur ID");
        bezeichnungField = createReadOnlyCopyableTextField("Bezeichnung");
        roleField = createReadOnlyCopyableTextField("Rolle");
        emailField = createReadOnlyCopyableTextField("E-Mail");

        detailsLayout.add(actorIdField, bezeichnungField, roleField, emailField);

        add(detailsLayout);

        ipfsDataSection = new VerticalLayout();
        ipfsDataSection.addClassName("ipfs-data-section");
        ipfsDataSection.addClassNames("margin-top-l", LumoUtility.Padding.Top.MEDIUM);
        ipfsDataSection.getStyle().set("border-top", "1px solid var(--lumo-contrast-10pct)");

        H4 ipfsDataTitle = new H4("IPFS Daten");
        ipfsDataTitle.addClassNames(LumoUtility.Margin.Bottom.SMALL);
        ipfsDataSection.add(ipfsDataTitle);

        ipfsDataGrid = new Grid<>();
        ipfsDataGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        ipfsDataGrid.addColumn(Map.Entry::getKey).setHeader("Key").setAutoWidth(true);
        ipfsDataGrid.addColumn(Map.Entry::getValue).setHeader("Value").setAutoWidth(true);
        ipfsDataGrid.setHeight("200px");
        ipfsDataGrid.setWidthFull();

        ipfsDataSection.add(ipfsDataGrid);
        ipfsDataSection.setVisible(false);

        add(ipfsDataSection);

        actorMedicationsSection = new VerticalLayout();
        actorMedicationsSection.addClassName("actor-medications-section");
        actorMedicationsSection.addClassNames("margin-top-l", LumoUtility.Padding.Top.MEDIUM);
        actorMedicationsSection.getStyle().set("border-top", "1px solid var(--lumo-contrast-10pct)");

        H4 medicationsTitle = new H4("Zugeordnete Medikamente");
        medicationsTitle.addClassNames(LumoUtility.Margin.Bottom.SMALL);
        actorMedicationsSection.add(medicationsTitle);

        actorMedicationsGrid = new Grid<>(MedikamentResponseDto.class, false);
        actorMedicationsGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        actorMedicationsGrid.setSizeFull();
        actorMedicationsGrid.setHeight("300px");

        actorMedicationsGrid.addColumn(MedikamentResponseDto::getStatus)
                .setTooltipGenerator(MedikamentResponseDto::getStatus)
                .setHeader("Status")
                .setAutoWidth(true)
                .setClassNameGenerator(medikament -> {
                    String status = medikament.getStatus();
                    if (status == null) {
                        return "status-cell-unbekannt";
                    }
                    switch (status.toLowerCase()) {
                        case "freigegeben": return "status-cell-freigegeben";
                        case "abgelehnt": return "status-cell-abgelehnt";
                        case "erstellt": return "status-cell-angelegt";
                        default: return "status-cell-unbekannt";
                    }
                });
        actorMedicationsGrid.addColumn("bezeichnung").setHeader("Bezeichnung").setAutoWidth(true);
        actorMedicationsGrid.addColumn("medId").setHeader("Medikament ID").setAutoWidth(true);

        // Listener für Klick auf Medikament im Grid
        actorMedicationsGrid.asSingleSelect().addValueChangeListener(event -> {
            MedikamentResponseDto selectedMedikament = event.getValue();
            if (selectedMedikament != null) { // **KORREKTUR: Nur bei tatsächlicher Auswahl reagieren**
                stateService.setNavigateToMedikamentId(selectedMedikament.getMedId());
                actorMedicationsGrid.asSingleSelect().clear(); // Auswahl im Grid aufheben
            }
        });


        actorMedicationsSection.add(actorMedicationsGrid);
        actorMedicationsSection.setVisible(false);

        add(actorMedicationsSection);

        stateService.addSelectedActorListener(this::updateDetails);
    }

    private TextField createReadOnlyCopyableTextField(String label) {
        TextField textField = new TextField(label);
        textField.setReadOnly(true);
        textField.setWidthFull();
        textField.addClassNames("detail-field");

        textField.getElement().addEventListener("click", (DomEventListener) event -> {
            String valueToCopy = textField.getValue();
            if (valueToCopy != null && !valueToCopy.isEmpty()) {
                UI.getCurrent().getPage().executeJs(
                        "navigator.clipboard.writeText($0).then(function() { console.log('Text copied to clipboard'); }).catch(function(err) { console.error('Failed to copy text: ', err); });",
                        valueToCopy
                );
                Notification.show("'" + valueToCopy + "' in Zwischenablage kopiert.", 3000, Notification.Position.MIDDLE);
            }
        });
        return textField;
    }

    public void updateDetails(ActorResponseDto actor) {
        if (actor != null) {
            mainTitle.setText("Details für: " + actor.getBezeichnung());
            actorIdField.setValue(actor.getActorId() != null ? actor.getActorId() : "");
            bezeichnungField.setValue(actor.getBezeichnung() != null ? actor.getBezeichnung() : "");
            roleField.setValue(actor.getRole() != null ? actor.getRole() : "");
            emailField.setValue(actor.getEmail() != null ? actor.getEmail() : "");

            if (actor.getIpfsData() != null && !actor.getIpfsData().isEmpty()) {
                List<Map.Entry<String, Object>> ipfsEntries = actor.getIpfsData().entrySet().stream()
                        .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());
                ipfsDataGrid.setItems(ipfsEntries);
                ipfsDataSection.setVisible(true);
            } else {
                ipfsDataGrid.setItems(List.of());
                ipfsDataSection.setVisible(false);
            }

            List<MedikamentResponseDto> allMedikamente = medikamentService.getAllMedikamente();
            String currentActorId = actor.getActorId();

            List<MedikamentResponseDto> associatedMedikamente = allMedikamente.stream()
                    .filter(medikament -> medikament.getHerstellerId() != null && medikament.getHerstellerId().equals(currentActorId))
                    .collect(Collectors.toList());

            actorMedicationsGrid.setItems(associatedMedikamente);
            actorMedicationsSection.setVisible(!associatedMedikamente.isEmpty());

        } else {
            mainTitle.setText("Akteur Details");
            actorIdField.setValue("");
            bezeichnungField.setValue("");
            roleField.setValue("");
            emailField.setValue("");
            ipfsDataGrid.setItems(List.of());
            ipfsDataSection.setVisible(false);
            actorMedicationsGrid.setItems(List.of());
            actorMedicationsSection.setVisible(false);
        }
    }
}