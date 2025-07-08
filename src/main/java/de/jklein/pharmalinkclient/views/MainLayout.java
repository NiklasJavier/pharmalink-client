package de.jklein.pharmalinkclient.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.jklein.pharmalinkclient.security.UserSession;
import de.jklein.pharmalinkclient.service.AuthService;
import de.jklein.pharmalinkclient.views.actorexplorer.ActorExplorerView;
import de.jklein.pharmalinkclient.views.dashboard.DashboardView;
import de.jklein.pharmalinkclient.views.medikamente.MedikamenteView;
import de.jklein.pharmalinkclient.views.units.UnitsView;

import java.util.Optional;

public class MainLayout extends AppLayout {

    private final AuthService authService;
    private final UserSession userSession;
    private Tabs menu;
    private Button themeToggleButton;

    public MainLayout(AuthService authService, UserSession userSession) {
        this.authService = authService;
        this.userSession = userSession;

        setPrimarySection(Section.NAVBAR);
        addToNavbar(false, createTwoRowHeader());
    }

    private Component createTwoRowHeader() {
        H1 appName = new H1("PharmaLink Client");
        appName.addClassNames("app-name", LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        Component rightSideControls = createRightSideControls();

        HorizontalLayout topRow = new HorizontalLayout(appName, rightSideControls);
        topRow.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        topRow.setSpacing(false);
        topRow.expand(appName);
        topRow.setWidthFull();
        topRow.addClassNames(LumoUtility.Padding.Vertical.SMALL, LumoUtility.Padding.Horizontal.MEDIUM);

        menu = createNavigationTabs();
        HorizontalLayout bottomRow = new HorizontalLayout(menu);
        bottomRow.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        bottomRow.setWidthFull();
        bottomRow.addClassName("nav-tab-bar");

        VerticalLayout headerLayout = new VerticalLayout(topRow, bottomRow);
        headerLayout.setPadding(false);
        headerLayout.setSpacing(false);

        return headerLayout;
    }

    private Tabs createNavigationTabs() {
        Tabs tabs = new Tabs();
        tabs.add(createTab(VaadinIcon.DASHBOARD, "Dashboard", DashboardView.class));
        tabs.add(createTab(VaadinIcon.GROUP, "Akteure", ActorExplorerView.class));
        tabs.add(createTab(VaadinIcon.PILLS, "Medikamente", MedikamenteView.class));
        tabs.add(createTab(VaadinIcon.PACKAGE, "Einheiten", UnitsView.class));
        return tabs;
    }

    private Tab createTab(VaadinIcon viewIcon, String viewName, Class<? extends Component> navigationTarget) {
        Icon icon = viewIcon.create();
        icon.getStyle().set("margin-inline-end", "var(--lumo-space-s)");

        RouterLink link = new RouterLink();
        link.add(icon, new Span(viewName));
        link.setRoute(navigationTarget);
        link.setTabIndex(-1);

        Tab tab = new Tab(link);
        tab.getElement().setAttribute("data-route", navigationTarget.getName());
        return tab;
    }

    private Component createRightSideControls() {
        // --- Dark-Mode-Schalter ---
        themeToggleButton = new Button(); // Initialisierung ohne Icon
        themeToggleButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        themeToggleButton.getElement().setAttribute("aria-label", "Toggle theme");

        themeToggleButton.addClickListener(click -> {
            ThemeList themeList = UI.getCurrent().getElement().getThemeList();
            if (themeList.contains(Lumo.DARK)) {
                themeList.remove(Lumo.DARK);
                userSession.setTheme(Lumo.LIGHT);
                updateThemeToggleButtonIcon(false);
            } else {
                themeList.add(Lumo.DARK);
                userSession.setTheme(Lumo.DARK);
                updateThemeToggleButtonIcon(true);
            }
        });

        Component userMenu = createUserMenu();

        HorizontalLayout rightSide = new HorizontalLayout(themeToggleButton, userMenu);
        rightSide.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSide.addClassName(LumoUtility.Gap.XSMALL);

        return rightSide;
    }

    private Component createUserMenu() {
        if (userSession.isLoggedIn()) {
            Button userIconButton = new Button(VaadinIcon.USER.create());
            userIconButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
            userIconButton.getElement().setAttribute("aria-label", "User Menu");

            ContextMenu contextMenu = new ContextMenu();
            contextMenu.setTarget(userIconButton);
            contextMenu.setOpenOnClick(true);

            contextMenu.addItem("Angemeldet als: " + userSession.getUsername(), e -> {}).setEnabled(false);
            contextMenu.addItem("Abmelden", e -> {
                authService.logout();
                UI.getCurrent().getPage().reload();
            });

            return userIconButton;
        }
        return new Span();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        final UI ui = attachEvent.getUI();

        String sessionTheme = userSession.getTheme();
        if (sessionTheme != null) {
            // Wenn ein Theme in der Session gespeichert ist, wende es an
            ui.getElement().getThemeList().set(Lumo.DARK, sessionTheme.equals(Lumo.DARK));
            updateThemeToggleButtonIcon(sessionTheme.equals(Lumo.DARK));
        } else {
            // Wenn nichts in der Session ist, frage den Browser nach seiner Voreinstellung
            var js = "return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches";
            ui.getPage().executeJs(js).then(Boolean.class, prefersDark -> {
                ui.access(() -> {
                    if (prefersDark) {
                        ui.getElement().getThemeList().add(Lumo.DARK);
                        updateThemeToggleButtonIcon(true);
                    } else {
                        ui.getElement().getThemeList().remove(Lumo.DARK);
                        updateThemeToggleButtonIcon(false);
                    }
                });
            });
        }
    }

    private void updateThemeToggleButtonIcon(boolean isDark) {
        VaadinIcon icon = isDark ? VaadinIcon.SUN_O : VaadinIcon.MOON;
        themeToggleButton.setIcon(icon.create());
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        getTabForComponent(getContent()).ifPresent(menu::setSelectedTab);
    }

    private Optional<Tab> getTabForComponent(Component component) {
        return menu.getChildren()
                .filter(tab -> {
                    String route = ((Tab) tab).getElement().getAttribute("data-route");
                    return route != null && route.equals(component.getClass().getName());
                })
                .findFirst().map(Tab.class::cast);
    }
}