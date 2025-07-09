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
import com.vaadin.flow.component.splitlayout.SplitLayout;

import de.jklein.pharmalinkclient.dto.ActorResponseDto;
import de.jklein.pharmalinkclient.dto.MedikamentResponseDto;
import de.jklein.pharmalinkclient.service.StateService;
import de.jklein.pharmalinkclient.service.MedikamentService;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.theme.lumo.LumoUtility; // Dieser Import bleibt, falls andere LumoUtility-Klassen/Methoden verwendet werden
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.dom.DomEventListener;


@SpringComponent
@UIScope
public class ActorDetailContent extends SplitLayout {

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
        // **KORREKTUR:** LumoUtility.Padding.LARGE ersetzt durch String "padding-l"
        addClassNames("padding-l");

        this.setOrientation(SplitLayout.Orientation.VERTICAL);
        this.setSplitterPosition(50);

        VerticalLayout actorInfoSection = new VerticalLayout();
        actorInfoSection.addClassName("actor-info-section");
        actorInfoSection.setSizeFull();
        actorInfoSection.addClassNames("padding-l"); // Padding für diesen Bereich
        actorInfoSection.setSpacing(false);

        mainTitle = new H3("Akteur Details");
        // **KORREKTUR:** LumoUtility.Margin.Bottom.XL ersetzt durch String "margin-bottom-xl"
        mainTitle.addClassNames("margin-bottom-xl");
        actorInfoSection.add(mainTitle);

        detailsLayout = new FormLayout();
        detailsLayout.addClassName("actor-main-details");
        detailsLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        // **KORREKTUR:** LumoUtility.Gap.SMALL ersetzt durch String "gap-s"
        detailsLayout.addClassNames("gap-s");

        actorIdField = createReadOnlyCopyableTextField("Akteur ID");
        bezeichnungField = createReadOnlyCopyableTextField("Bezeichnung");
        roleField = createReadOnlyCopyableTextField("Rolle");
        emailField = createReadOnlyCopyableTextField("E-Mail");

        detailsLayout.add(actorIdField, bezeichnungField, roleField, emailField);
        actorInfoSection.add(detailsLayout);

        ipfsDataSection = new VerticalLayout();
        ipfsDataSection.addClassName("ipfs-data-section");
        // **KORREKTUR:** LumoUtility.Margin.Top.L ersetzt durch String "margin-top-l"
        ipfsDataSection.addClassNames("margin-top-l", "padding-top-m"); // **KORREKTUR:** LumoUtility.Padding.Top.MEDIUM zu String "padding-top-m"
        ipfsDataSection.getStyle().set("border-top", "1px solid var(--lumo-contrast-10pct)");

        H4 ipfsDataTitle = new H4("IPFS Daten");
        // **KORREKTUR:** LumoUtility.Margin.Bottom.SMALL ersetzt durch String "margin-bottom-s"
        ipfsDataTitle.addClassNames("margin-bottom-s");
        ipfsDataSection.add(ipfsDataTitle);

        ipfsDataGrid = new Grid<>();
        ipfsDataGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        ipfsDataGrid.addColumn(Map.Entry::getKey).setHeader("Key").setAutoWidth(true);
        ipfsDataGrid.addColumn(Map.Entry::getValue).setHeader("Value").setAutoWidth(true);
        ipfsDataGrid.setHeight("200px");
        ipfsDataGrid.setWidthFull();

        ipfsDataSection.add(ipfsDataGrid);
        ipfsDataSection.setVisible(false);
        actorInfoSection.add(ipfsDataSection);

        actorMedicationsSection = new VerticalLayout();
        actorMedicationsSection.addClassName("actor-medications-section-container");
        // **KORREKTUR:** LumoUtility.Margin.Top.L ersetzt durch String "margin-top-l"
        actorMedicationsSection.addClassNames("margin-top-l", "padding-l"); // **KORREKTUR:** LumoUtility.Padding.LARGE zu String "padding-l"
        actorMedicationsSection.getStyle().set("border-top", "1px solid var(--lumo-contrast-10pct)");

        H4 medicationsTitle = new H4("Zugeordnete Medikamente");
        // **KORREKTUR:** LumoUtility.Margin.Bottom.SMALL ersetzt durch String "margin-bottom-s"
        medicationsTitle.addClassNames("margin-bottom-s");
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

        actorMedicationsGrid.asSingleSelect().addValueChangeListener(event -> {
            MedikamentResponseDto selectedMedikament = event.getValue();
            if (selectedMedikament != null) {
                UI.getCurrent().navigate(de.jklein.pharmalinkclient.views.medikamente.MedikamenteView.class);
                stateService.setNavigateToMedikamentId(selectedMedikament.getMedId());
                actorMedicationsGrid.asSingleSelect().clear();
            }
        });

        actorMedicationsSection.add(actorMedicationsGrid);
        actorMedicationsSection.setVisible(false);

        this.addToPrimary(actorInfoSection);
        this.addToSecondary(actorMedicationsSection);

        stateService.addSelectedActorListener(this::updateDetails);

        this.setVisible(false);
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
            this.setVisible(true);
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
            this.setVisible(false);
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