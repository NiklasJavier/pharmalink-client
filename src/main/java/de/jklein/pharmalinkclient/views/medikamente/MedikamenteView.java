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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.jklein.pharmalinkclient.dto.MedikamentFilterCriteriaDto;
import de.jklein.pharmalinkclient.views.MainLayout;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.orderedlayout.FlexComponent; // **WICHTIG: Dieser Import MUSS vorhanden sein**
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.html.Div;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import de.jklein.pharmalinkclient.service.StateService;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.router.HighlightConditions;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import com.vaadin.flow.component.UI;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
// import com.vaadin.flow.router.Location; // Nicht mehr direkt benötigt


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
    private final ObjectProvider<DetailContent> detailContentProvider;
    private final StateService stateService;


    @Autowired
    public MedikamenteView(ObjectProvider<MasterContent> masterContentProvider,
                           ObjectProvider<DetailContent> detailContentProvider,
                           StateService stateService) {
        this.masterContentProvider = masterContentProvider;
        this.detailContentProvider = detailContentProvider;
        this.stateService = stateService;

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
        // **Hier ist die Problemzeile**
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
        HorizontalLayout searchInputLayout = new HorizontalLayout();
        searchInputLayout.setSpacing(false);
        searchInputLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        searchInputLayout.addClassName("search-input-layout");


        // Suchfeld
        searchField = new TextField();
        searchField.setPlaceholder("Medikament suchen...");
        searchField.setWidth("300px");
        searchField.setValueChangeMode(ValueChangeMode.ON_CHANGE);
        searchField.addValueChangeListener(event -> {
            performSearch();
        });

        searchInputLayout.add(searchField);

        // Reihenfolge der Elemente im searchAndSortArea
        searchAndSortArea.add(sortBySelect, searchInputLayout);
        operationBar.add(searchAndSortArea);

        // Menübar für Aktionen (rechtsbündig)
        MenuBar menuBar = createMenuBar();
        operationBar.add(menuBar);

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
                    // 1. Zur MedikamenteView navigieren
                    try {
                        String currentPathSegment = "";
                        String fullPath = UI.getCurrent().getPage().getLocation().getPath();
                        if (fullPath != null && !fullPath.isEmpty()) {
                            String[] segments = fullPath.split("/");
                            if (segments.length > 1) {
                                currentPathSegment = segments[1];
                            }
                        }

                        if (!currentPathSegment.equals("medikamente")) {
                            UI.getCurrent().navigate(MedikamenteView.class);
                        }
                    } catch (Exception e) {
                        System.err.println("Fehler beim Abrufen/Parsen der aktuellen Pfadsegmente: " + e.getMessage());
                    }

                    // 2. Den "Alle" Tab auswählen (falls der Inhalt nicht sowieso dieser ist)
                    tabs.setSelectedTab(alleTab);
                    // 3. Suchfeld mit ID füllen und Suche auslösen
                    searchField.setValue(medId);
                    performSearch(); // Löst die Suche aus

                    // 4. Navigationswunsch im StateService zurücksetzen, um Mehrfachauslösung zu vermeiden
                    stateService.setNavigateToMedikamentId(null);
                }
            }
        });
    }

    private void showAlleContent() {
        mainContentContainer.removeAll();

        MasterContent master = masterContentProvider.getObject();
        DetailContent detail = detailContentProvider.getObject();

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
        DetailContent detail = detailContentProvider.getObject();

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

        Notification.show("Suche ausgelöst mit: '" + searchTerm + "', Status: '" + selectedStatus + "'");
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_DROPDOWN_INDICATORS);

        ComponentEventListener<ClickEvent<MenuItem>> menuListener = e -> {
            Notification.show("Aktion: " + e.getSource().getText());
        };

        menuBar.addItem("Anzeigen", menuListener);

        MenuItem aktionen = menuBar.addItem("Aktionen");
        SubMenu aktionenSubMenu = aktionen.getSubMenu();

        aktionenSubMenu.addItem("Medikament bearbeiten", menuListener);
        aktionenSubMenu.addItem("Charge anzeigen", menuListener);
        aktionenSubMenu.addItem("Einheiten anzeigen", menuListener);
        aktionenSubMenu.addItem("Löschen", menuListener);

        return menuBar;
    }
}