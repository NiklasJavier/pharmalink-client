// src/main/java/de/jklein/pharmalinkclient/views/medikamente/MedikamenteView.java
package de.jklein.pharmalinkclient.views.medikamente;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.jklein.pharmalinkclient.dto.*;
import de.jklein.pharmalinkclient.views.MainLayout;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;


import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import de.jklein.pharmalinkclient.service.StateService;
import de.jklein.pharmalinkclient.security.UserSession;
import de.jklein.pharmalinkclient.service.MedikamentService;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import com.vaadin.flow.component.UI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@PageTitle("Medikamente")
@Route(value = "medikamente", layout = MainLayout.class)
@PermitAll
public class MedikamenteView extends VerticalLayout {

    private TextField searchField;
    private HorizontalLayout operationBar;
    private Select<String> sortBySelect;
    private Div mainContentContainer;
    private Tabs tabs;
    private Tab alleTab;
    private Tab eigeneTab;

    private final ObjectProvider<MasterContent> masterContentProvider;
    private final ObjectProvider<DetailContent> actorDetailContentProvider;
    private final StateService stateService;
    private final UserSession userSession;
    private final MedikamentService medikamentService;

    @Autowired
    public MedikamenteView(ObjectProvider<MasterContent> masterContentProvider,
                           ObjectProvider<DetailContent> detailContentProvider,
                           StateService stateService,
                           UserSession userSession,
                           MedikamentService medikamentService) {
        this.masterContentProvider = masterContentProvider;
        this.actorDetailContentProvider = detailContentProvider;
        this.stateService = stateService;
        this.userSession = userSession;
        this.medikamentService = medikamentService;

        addClassName("pharmalink-view");
        setSizeFull();
        setSpacing(false);
        setPadding(false);

        // 1. Tabs ganz oben hinzufügen
        alleTab = new Tab("Alle");
        eigeneTab = new Tab("Eigene");
        tabs = new Tabs(alleTab, eigeneTab);
        tabs.setWidthFull();
        add(tabs);

        // Listener für Tab-Wechsel
        tabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab() == alleTab) {
                showAlleContent();
            } else if (event.getSelectedTab() == eigeneTab) {
                showEigeneContent();
            }
        });

        // Operationsleiste
        operationBar = new HorizontalLayout();
        operationBar.setWidthFull();
        operationBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        operationBar.setAlignItems(FlexComponent.Alignment.CENTER);
        operationBar.setPadding(true);
        operationBar.addClassName("pharmalink-operation-bar");

        // Linker Bereich: Status-Select und Suchfeld
        HorizontalLayout searchAndSortArea = new HorizontalLayout();
        searchAndSortArea.setSpacing(true);
        searchAndSortArea.setAlignItems(FlexComponent.Alignment.CENTER);
        searchAndSortArea.addClassName("pharmalink-search-sort-area");

        // Select Box für Status
        sortBySelect = new Select<>();
        sortBySelect.setItems("Ohne Filter", "angelegt", "freigegeben", "abgelehnt");
        sortBySelect.setValue("Ohne Filter");
        sortBySelect.addValueChangeListener(event -> {
            performSearch();
        });

        // HorizontalLayout für Suchfeld
        searchField = new TextField();
        searchField.setPlaceholder("Medikament suchen...");
        searchField.setWidth("300px");
        searchField.setValueChangeMode(ValueChangeMode.ON_CHANGE);
        searchField.addValueChangeListener(event -> {
            performSearch();
        });

        HorizontalLayout searchInputLayout = new HorizontalLayout();
        searchInputLayout.setSpacing(false);
        searchInputLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        searchInputLayout.addClassName("search-input-layout");
        searchInputLayout.add(searchField);


        // Reihenfolge der Elemente im searchAndSortArea
        searchAndSortArea.add(sortBySelect, searchInputLayout);
        operationBar.add(searchAndSortArea);

        // Menübar für Aktionen (rechtsbündig)
        MenuBar menuBar = createMenuBar();
        if (menuBar != null) {
            operationBar.add(menuBar);
        }

        add(operationBar);

        // Hauptinhaltsbereich - jetzt ein Container, der seinen Inhalt wechselt
        mainContentContainer = new Div();
        mainContentContainer.addClassName("pharmalink-main-content-container");
        mainContentContainer.setSizeFull();
        add(mainContentContainer);
        expand(mainContentContainer);

        // Initialen Inhalt setzen (standardmäßig "Alle")
        showAlleContent();

        // Listener für Navigationswunsch von ActorDetailContent
        stateService.addNavigateToMedikamentListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String medId = (String) evt.getNewValue();
                if (medId != null && !medId.isEmpty()) {
                    UI.getCurrent().navigate(MedikamenteView.class);
                    tabs.setSelectedTab(alleTab);
                    searchField.setValue(medId);
                    performSearch();
                    stateService.setNavigateToMedikamentId(null);
                }
            }
        });
    }

    private void showAlleContent() {
        mainContentContainer.removeAll();
        MasterContent master = masterContentProvider.getObject();
        DetailContent detail = actorDetailContentProvider.getObject();
        SplitLayout splitLayout = new SplitLayout(master, detail);
        splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
        splitLayout.setSizeFull();
        splitLayout.addClassName("pharmalink-custom-split-layout-vertical");
        mainContentContainer.add(splitLayout);
        mainContentContainer.getStyle().remove("padding");
        mainContentContainer.getStyle().remove("border-top");
        master.updateGridWithFilters(stateService.getCurrentMedikamentFilterCriteria());
    }

    private void showEigeneContent() {
        mainContentContainer.removeAll();
        MasterContent master = masterContentProvider.getObject();
        DetailContent detail = actorDetailContentProvider.getObject();
        SplitLayout splitLayout = new SplitLayout(master, detail);
        splitLayout.setSplitterPosition(70);
        splitLayout.setSizeFull();
        splitLayout.addClassName("pharmalink-custom-split-layout-horizontal");
        mainContentContainer.add(splitLayout);
        mainContentContainer.getStyle().remove("padding");
        mainContentContainer.getStyle().remove("border-top");
        master.updateGridWithFilters(stateService.getCurrentMedikamentFilterCriteria());
    }

    private void performSearch() {
        String searchTerm = searchField.getValue();
        String selectedStatus = sortBySelect.getValue();
        MedikamentFilterCriteriaDto criteria = new MedikamentFilterCriteriaDto(searchTerm, selectedStatus);
        stateService.setCurrentMedikamentFilterCriteria(criteria);
        Notification notification = new Notification(
                "Suche ausgelöst mit: '" + searchTerm + "', Status: '" + selectedStatus + "'",
                3000,
                Position.BOTTOM_START
        );
        notification.open();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_DROPDOWN_INDICATORS);

        ComponentEventListener<ClickEvent<MenuItem>> menuListener = e -> {
            Notification notification = new Notification(
                    "Aktion: " + e.getSource().getText(),
                    3000,
                    Position.BOTTOM_START
            );
            notification.open();
        };

        // KORRIGIERT: "Anzeigen"-Button entfernt
        // menuBar.addItem("Anzeigen", menuListener);

        String currentActorId = stateService.getCurrentActorId();
        String userRole = null;
        if (currentActorId != null && currentActorId.contains("-")) {
            userRole = currentActorId.substring(0, currentActorId.indexOf("-")).toLowerCase();
        }

        List<String> actionsForRole = new ArrayList<>();

        if (userRole != null) {
            switch (userRole) {
                case "hersteller":
                    actionsForRole.add("Medikament anlegen");
                    actionsForRole.add("Medikament bearbeiten");
                    actionsForRole.add("Medikament löschen");
                    break;
                case "behoerde":
                    actionsForRole.add("Status vergeben");
                    break;
                default:
                    // Keine Aktionen für andere Rollen
                    break;
            }
        }

        if (!actionsForRole.isEmpty()) {
            MenuItem aktionen = menuBar.addItem("Aktionen");
            SubMenu aktionenSubMenu = aktionen.getSubMenu();
            for (String action : actionsForRole) {
                if ("Medikament anlegen".equals(action)) {
                    aktionenSubMenu.addItem(action, event -> showCreateMedikamentPopup());
                } else if ("Medikament bearbeiten".equals(action)) {
                    aktionenSubMenu.addItem(action, event -> {
                        Optional<MedikamentResponseDto> selectedMedikament = stateService.getSelectedMedikament();
                        if (selectedMedikament.isPresent()) {
                            showEditMedikamentPopup(selectedMedikament.get());
                        } else {
                            Notification notification = new Notification(
                                    "Bitte wählen Sie zuerst ein Medikament zum Bearbeiten aus der Liste aus.",
                                    3000,
                                    Position.MIDDLE
                            );
                            notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                            notification.open();
                        }
                    });
                }
                else {
                    aktionenSubMenu.addItem(action, menuListener);
                }
            }
        }
        return menuBar;
    }

    /**
     * Zeigt ein Popup zur Erstellung eines neuen Medikaments an.
     */
    private void showCreateMedikamentPopup() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Neues Medikament anlegen");
        dialog.setWidth("800px");
        dialog.setHeight("auto");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(55);

        VerticalLayout leftSide = new VerticalLayout(new H4("Allgemeine Informationen"));
        leftSide.setSpacing(true);
        leftSide.setPadding(true);

        VerticalLayout rightSide = new VerticalLayout(new H4("Zusätzliche IPFS Daten"));
        rightSide.setSpacing(true);
        rightSide.setPadding(true);

        // --- Linke Seite: Hauptdetails Formular ---
        CreateMedikamentRequestDto newMedikament = new CreateMedikamentRequestDto();
        Binder<CreateMedikamentRequestDto> binder = new Binder<>(CreateMedikamentRequestDto.class);

        FormLayout detailsForm = new FormLayout();
        detailsForm.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        TextField bezeichnungField = new TextField("Bezeichnung");
        TextField infoblattHashField = new TextField("Infoblatt Hash (optional)");

        detailsForm.add(bezeichnungField, infoblattHashField);
        leftSide.add(detailsForm);

        // Bind Felder mit Validierung
        binder.forField(bezeichnungField)
                .asRequired("Bezeichnung ist erforderlich")
                .bind(CreateMedikamentRequestDto::getBezeichnung, CreateMedikamentRequestDto::setBezeichnung);
        binder.forField(infoblattHashField)
                .bind(CreateMedikamentRequestDto::getInfoblattHash, CreateMedikamentRequestDto::setInfoblattHash);

        binder.setBean(newMedikament);


        // --- Rechte Seite: IPFS Daten Grid ---
        Grid<IpfsEntry> ipfsEditGrid = new Grid<>(IpfsEntry.class, false);
        ipfsEditGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        ipfsEditGrid.setHeight("200px");

        List<IpfsEntry> editableIpfsData = new ArrayList<>();
        ListDataProvider<IpfsEntry> ipfsDataProvider = new ListDataProvider<>(editableIpfsData);
        ipfsEditGrid.setDataProvider(ipfsDataProvider);

        ipfsEditGrid.addColumn(IpfsEntry::getKey).setHeader("Key Bezeichnung").setSortable(true);
        ipfsEditGrid.addColumn(IpfsEntry::getValue).setHeader("Key Value").setSortable(true);

        GridContextMenu<IpfsEntry> contextMenu = ipfsEditGrid.addContextMenu();
        contextMenu.addItem("Zeile bearbeiten", event -> event.getItem().ifPresent(item -> showIpfsEntryRowEditDialog(item, ipfsDataProvider)));

        // --- Buttons unter dem Grid ---
        Button addRowButton = new Button("Zeile hinzufügen", VaadinIcon.PLUS.create());
        addRowButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        addRowButton.addClickListener(e -> showIpfsEntryRowEditDialog(new IpfsEntry("", ""), ipfsDataProvider));

        Button removeRowButton = new Button("Zeile entfernen", VaadinIcon.TRASH.create());
        removeRowButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
        removeRowButton.setVisible(false);

        // Listener, um den Button bei Auswahl zu zeigen/verstecken
        ipfsEditGrid.asSingleSelect().addValueChangeListener(event -> {
            removeRowButton.setVisible(event.getValue() != null);
        });

        // Klick-Aktion für den Entfernen-Button
        removeRowButton.addClickListener(e -> {
            IpfsEntry selectedEntry = ipfsEditGrid.asSingleSelect().getValue();
            if (selectedEntry != null) {
                editableIpfsData.remove(selectedEntry);
                ipfsDataProvider.refreshAll();
                Notification notification = new Notification("Zeile entfernt.", 2000, Position.BOTTOM_START);
                notification.open();
            }
        });

        HorizontalLayout buttonLayout = new HorizontalLayout(addRowButton, removeRowButton);
        rightSide.add(ipfsEditGrid, buttonLayout);

        splitLayout.addToPrimary(leftSide);
        splitLayout.addToSecondary(rightSide);
        dialog.add(splitLayout);

        // --- Dialog-Footer mit Buttons ---
        Button saveButton = new Button("Speichern");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> {
            try {
                binder.writeBean(newMedikament);

                Map<String, Object> ipfsDataMap = editableIpfsData.stream()
                        .filter(entry -> entry.getKey() != null && !entry.getKey().isEmpty())
                        .collect(Collectors.toMap(IpfsEntry::getKey, IpfsEntry::getValue));
                newMedikament.setIpfsData(ipfsDataMap);

                MedikamentResponseDto createdMedikament = medikamentService.createMedikament(newMedikament);

                if (createdMedikament != null) {
                    Notification notification = new Notification(
                            "Medikament '" + createdMedikament.getBezeichnung() + "' erfolgreich angelegt. ID: " + createdMedikament.getMedId(),
                            5000,
                            Position.BOTTOM_START // GEÄNDERT: Position auf BOTTOM_START
                    );
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    notification.open();

                    dialog.close();
                    performSearch();
                } else {
                    Notification notification = new Notification(
                            "Fehler beim Anlegen des Medikaments im Backend.",
                            5000,
                            Position.MIDDLE
                    );
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notification.open();
                }

            } catch (ValidationException ex) {
                Notification notification = new Notification(
                        "Fehler beim Speichern: Bitte alle Pflichtfelder ausfüllen und überprüfen.",
                        3000,
                        Position.MIDDLE
                );
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.open();
            } catch (Exception ex) {
                Notification notification = new Notification(
                        "Ein unerwarteter Fehler ist aufgetreten: " + ex.getMessage(),
                        5000,
                        Position.MIDDLE
                );
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.open();
                ex.printStackTrace();
            }
        });


        Button cancelButton = new Button("Abbrechen", event -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getFooter().add(cancelButton, saveButton);

        dialog.open();
    }

    /**
     * Zeigt ein Popup zur Bearbeitung eines bestehenden Medikaments an.
     * @param medikamentToEdit Das Medikament, das bearbeitet werden soll.
     */
    private void showEditMedikamentPopup(MedikamentResponseDto medikamentToEdit) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Medikament bearbeiten: " + medikamentToEdit.getBezeichnung());
        dialog.setWidth("800px");
        dialog.setHeight("auto");
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(55);

        VerticalLayout leftSide = new VerticalLayout(new H4("Allgemeine Informationen"));
        leftSide.setSpacing(true);
        leftSide.setPadding(true);

        VerticalLayout rightSide = new VerticalLayout(new H4("Zusätzliche IPFS Daten"));
        rightSide.setSpacing(true);
        rightSide.setPadding(true);

        // --- Linke Seite: Hauptdetails Formular ---
        UpdateMedikamentRequestDto updateRequestDto = new UpdateMedikamentRequestDto();
        updateRequestDto.setBezeichnung(medikamentToEdit.getBezeichnung());
        updateRequestDto.setIpfsData(medikamentToEdit.getIpfsData());


        Binder<UpdateMedikamentRequestDto> binder = new Binder<>(UpdateMedikamentRequestDto.class);

        FormLayout detailsForm = new FormLayout();
        detailsForm.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        // MedID anzeigen (schreibgeschützt und oben)
        TextField medIdField = createReadOnlyTextField("Medikament ID", medikamentToEdit.getMedId());
        // Hersteller ID und Status ebenfalls als ReadOnly anzeigen
        TextField herstellerIdDisplayField = createReadOnlyTextField("Hersteller ID", medikamentToEdit.getHerstellerId());
        TextField statusDisplayField = createReadOnlyTextField("Status", medikamentToEdit.getStatus());

        TextField bezeichnungField = new TextField("Bezeichnung");
        TextField infoblattHashField = new TextField("Infoblatt Hash (optional)");

        // Elemente der linken Seite hinzufügen: Zuerst ReadOnly-Felder, dann das Formular
        leftSide.add(medIdField, herstellerIdDisplayField, statusDisplayField, detailsForm);
        detailsForm.add(bezeichnungField, infoblattHashField);

        // Bind Felder mit Validierung
        binder.forField(bezeichnungField)
                .asRequired("Bezeichnung ist erforderlich")
                .bind(UpdateMedikamentRequestDto::getBezeichnung, UpdateMedikamentRequestDto::setBezeichnung);
        binder.forField(infoblattHashField)
                .bind(UpdateMedikamentRequestDto::getInfoblattHash, UpdateMedikamentRequestDto::setInfoblattHash);

        binder.setBean(updateRequestDto);


        // --- Rechte Seite: IPFS Daten Grid ---
        Grid<IpfsEntry> ipfsEditGrid = new Grid<>(IpfsEntry.class, false);
        ipfsEditGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        ipfsEditGrid.setHeight("200px");

        // Initiale Befüllung der bearbeitbaren IPFS-Daten aus dem Medikament
        List<IpfsEntry> editableIpfsData = new ArrayList<>();
        if (medikamentToEdit.getIpfsData() != null) {
            medikamentToEdit.getIpfsData().forEach((key, value) -> editableIpfsData.add(new IpfsEntry(key, value != null ? value.toString() : "")));
        }
        ListDataProvider<IpfsEntry> ipfsDataProvider = new ListDataProvider<>(editableIpfsData);
        ipfsEditGrid.setDataProvider(ipfsDataProvider);

        ipfsEditGrid.addColumn(IpfsEntry::getKey).setHeader("Key Bezeichnung").setSortable(true);
        ipfsEditGrid.addColumn(IpfsEntry::getValue).setHeader("Key Value").setSortable(true);

        GridContextMenu<IpfsEntry> contextMenu = ipfsEditGrid.addContextMenu();
        contextMenu.addItem("Zeile bearbeiten", event -> event.getItem().ifPresent(item -> showIpfsEntryRowEditDialog(item, ipfsDataProvider)));

        // --- Buttons unter dem Grid ---
        Button addRowButton = new Button("Zeile hinzufügen", VaadinIcon.PLUS.create());
        addRowButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        addRowButton.addClickListener(e -> showIpfsEntryRowEditDialog(new IpfsEntry("", ""), ipfsDataProvider));

        Button removeRowButton = new Button("Zeile entfernen", VaadinIcon.TRASH.create());
        removeRowButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
        removeRowButton.setVisible(false);

        ipfsEditGrid.asSingleSelect().addValueChangeListener(event -> {
            removeRowButton.setVisible(event.getValue() != null);
        });

        removeRowButton.addClickListener(e -> {
            IpfsEntry selectedEntry = ipfsEditGrid.asSingleSelect().getValue();
            if (selectedEntry != null) {
                editableIpfsData.remove(selectedEntry);
                ipfsDataProvider.refreshAll();
                Notification notification = new Notification("Zeile entfernt.", 2000, Position.BOTTOM_START);
                notification.open();
            }
        });

        HorizontalLayout buttonLayout = new HorizontalLayout(addRowButton, removeRowButton);
        rightSide.add(ipfsEditGrid, buttonLayout);

        splitLayout.addToPrimary(leftSide);
        splitLayout.addToSecondary(rightSide);
        dialog.add(splitLayout);

        // --- Dialog-Footer mit Buttons ---
        Button saveButton = new Button("Speichern");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> {
            try {
                binder.writeBean(updateRequestDto);

                Map<String, Object> ipfsDataMap = editableIpfsData.stream()
                        .filter(entry -> entry.getKey() != null && !entry.getKey().isEmpty())
                        .collect(Collectors.toMap(IpfsEntry::getKey, IpfsEntry::getValue));
                updateRequestDto.setIpfsData(ipfsDataMap);

                MedikamentResponseDto updatedMedikament = medikamentService.updateMedikament(medikamentToEdit.getMedId(), updateRequestDto);

                if (updatedMedikament != null) {
                    Notification notification = new Notification(
                            "Medikament '" + updatedMedikament.getBezeichnung() + "' erfolgreich aktualisiert. ID: " + updatedMedikament.getMedId(),
                            5000,
                            Position.BOTTOM_START // GEÄNDERT: Position auf BOTTOM_START
                    );
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    notification.open();

                    dialog.close();
                    performSearch();
                } else {
                    Notification notification = new Notification(
                            "Fehler beim Aktualisieren des Medikaments im Backend.",
                            5000,
                            Position.MIDDLE // Fehler können weiterhin mittig sein
                    );
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notification.open();
                }

            } catch (ValidationException ex) {
                Notification notification = new Notification(
                        "Fehler beim Speichern: Bitte alle Pflichtfelder ausfüllen und überprüfen.",
                        3000,
                        Position.MIDDLE
                );
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.open();
            } catch (Exception ex) {
                Notification notification = new Notification(
                        "Ein unerwarteter Fehler ist aufgetreten: " + ex.getMessage(),
                        5000,
                        Position.MIDDLE
                );
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.open();
                ex.printStackTrace();
            }
        });


        Button cancelButton = new Button("Abbrechen", event -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getFooter().add(cancelButton, saveButton);

        dialog.open();
    }


    private void showIpfsEntryRowEditDialog(IpfsEntry entry, ListDataProvider<IpfsEntry> dataProvider) {
        Dialog rowEditDialog = new Dialog();
        boolean isNewEntry = entry.getKey() == null || entry.getKey().isEmpty();
        rowEditDialog.setHeaderTitle(isNewEntry ? "Neue IPFS Zeile" : "IPFS Zeile bearbeiten");

        Binder<IpfsEntry> rowBinder = new Binder<>(IpfsEntry.class);
        TextField keyField = new TextField("Key Bezeichnung");
        TextField valueField = new TextField("Key Value Eintrag");
        keyField.setWidthFull();
        valueField.setWidthFull();

        rowBinder.forField(keyField).asRequired("Key darf nicht leer sein").bind(IpfsEntry::getKey, IpfsEntry::setKey);
        rowBinder.forField(valueField).bind(IpfsEntry::getValue, IpfsEntry::setValue);
        rowBinder.setBean(entry);

        rowEditDialog.add(new VerticalLayout(keyField, valueField));

        Button saveRowButton = new Button("Speichern");
        saveRowButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveRowButton.addClickListener(e -> {
            try {
                rowBinder.writeBean(entry);
                if (isNewEntry && !((List<IpfsEntry>) dataProvider.getItems()).contains(entry)) {
                    ((List<IpfsEntry>) dataProvider.getItems()).add(entry);
                }
                dataProvider.refreshAll();
                rowEditDialog.close();
            } catch (ValidationException ex) {
                Notification notification = new Notification(
                        "Fehler: " + ex.getValidationErrors().get(0).getErrorMessage(),
                        3000,
                        Position.BOTTOM_START
                );
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.open();
            }
        });

        Button cancelRowButton = new Button("Abbrechen", e -> rowEditDialog.close());
        rowEditDialog.getFooter().add(cancelRowButton, saveRowButton);
        rowEditDialog.open();
    }

    private TextField createReadOnlyTextField(String label, String value) {
        TextField textField = new TextField(label);
        textField.setReadOnly(true);
        textField.setValue(value != null ? value : "");
        textField.setWidthFull();
        return textField;
    }
}