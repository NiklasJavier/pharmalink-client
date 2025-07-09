package de.jklein.pharmalinkclient.views.actorexplorer;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.jklein.pharmalinkclient.views.MainLayout;
import jakarta.annotation.security.PermitAll;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.component.icon.VaadinIcon; // Import wird für das Suchfeld-Icon nicht mehr direkt verwendet
import com.vaadin.flow.component.icon.Icon; // Import wird für das Suchfeld-Icon nicht mehr direkt verwendet
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;


import de.jklein.pharmalinkclient.dto.ActorFilterCriteriaDto;
import de.jklein.pharmalinkclient.service.StateService;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;


@PageTitle("Actor Explorer")
@Route(value = "actorExplorer", layout = MainLayout.class)
@PermitAll
public class ActorExplorerView extends VerticalLayout {

    private final ObjectProvider<ActorMasterContent> actorMasterContentProvider;
    private final ObjectProvider<ActorDetailContent> actorDetailContentProvider;
    private final StateService stateService;

    private TextField searchField;


    @Autowired
    public ActorExplorerView(ObjectProvider<ActorMasterContent> actorMasterContentProvider,
                             ObjectProvider<ActorDetailContent> actorDetailContentProvider,
                             StateService stateService) {
        this.actorMasterContentProvider = actorMasterContentProvider;
        this.actorDetailContentProvider = actorDetailContentProvider;
        this.stateService = stateService;

        addClassName("actor-explorer-view");
        setSizeFull();
        setSpacing(false);
        setPadding(false);

        // Suchleiste für Akteure
        HorizontalLayout searchBar = new HorizontalLayout();
        searchBar.setWidthFull();
        searchBar.setPadding(true);
        searchBar.setAlignItems(FlexComponent.Alignment.CENTER);
        searchBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        searchBar.addClassName("actor-explorer-search-bar");

        searchField = new TextField();
        searchField.setPlaceholder("Akteur suchen (Bezeichnung, ID, E-Mail)...");
        searchField.setWidth("400px");
        // Der Präfix-Icon für die Suche wurde in früheren Schritten entfernt.
        // searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.ON_CHANGE);
        searchField.addValueChangeListener(event -> performActorSearch());

        // Aktions-MenuBar hinzufügen
        MenuBar actionsMenuBar = createActorActionsMenuBar();

        // Komponenten zur Suchleiste hinzufügen
        searchBar.add(searchField, actionsMenuBar);
        add(searchBar);


        ActorMasterContent master = actorMasterContentProvider.getObject();
        ActorDetailContent detail = actorDetailContentProvider.getObject();

        SplitLayout splitLayout = new SplitLayout(master, detail);
        splitLayout.setSplitterPosition(50);
        splitLayout.setSizeFull();
        splitLayout.addClassName("actor-explorer-split-layout");

        add(splitLayout);
        expand(splitLayout);

        // Initialen Filter für MasterContent setzen
        performActorSearch();
    }

    private void performActorSearch() {
        String searchTerm = searchField.getValue();
        ActorFilterCriteriaDto criteria = new ActorFilterCriteriaDto(searchTerm);
        stateService.setCurrentActorFilterCriteria(criteria);
    }

    // Methode zum Erstellen der Aktions-MenuBar für Akteure
    private MenuBar createActorActionsMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_DROPDOWN_INDICATORS);

        ComponentEventListener<ClickEvent<MenuItem>> menuListener = event -> {
            String selectedItemText = event.getSource().getText();
            Notification.show("Aktion für Akteur: " + selectedItemText, 3000, Notification.Position.MIDDLE);
        };

        // "Meine Stammdaten" als Dropdown-Menü
        MenuItem meineStammdaten = menuBar.addItem("Meine Stammdaten");
        SubMenu stammdatenSubMenu = meineStammdaten.getSubMenu();
        stammdatenSubMenu.addItem("Stammdaten anzeigen", menuListener);
        stammdatenSubMenu.addItem("Stammdaten bearbeiten", menuListener);

        // Der Menüpunkt "Mehr Aktionen" und dessen Unterpunkte wurden in früheren Schritten entfernt.
        // MenuItem moreActions = menuBar.addItem("Mehr Aktionen");
        // SubMenu moreActionsSubMenu = moreActions.getSubMenu();
        // moreActionsSubMenu.addItem("Exportieren", menuListener);
        // moreActionsSubMenu.addItem("Löschen", menuListener);

        return menuBar;
    }
}