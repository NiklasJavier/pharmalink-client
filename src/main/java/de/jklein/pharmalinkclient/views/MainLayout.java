package de.jklein.pharmalinkclient.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import de.jklein.pharmalinkclient.security.UserSession;
import de.jklein.pharmalinkclient.service.AuthService;
import de.jklein.pharmalinkclient.views.actorexplorer.ActorExplorerView;
import de.jklein.pharmalinkclient.views.dashboard.DashboardView;
import de.jklein.pharmalinkclient.views.medikamente.MedikamenteView;
import de.jklein.pharmalinkclient.views.units.UnitsView;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class MainLayout extends AppLayout {

    private H1 viewTitle;
    private final AuthService authService;
    private final UserSession userSession;

    public MainLayout(AuthService authService, UserSession userSession) {
        this.authService = authService;
        this.userSession = userSession;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        Span appName = new Span("Pharmalink Client");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Dashboard", DashboardView.class, VaadinIcon.DASHBOARD.create()));
        nav.addItem(new SideNavItem("Akteure", ActorExplorerView.class, VaadinIcon.GROUP.create()));
        nav.addItem(new SideNavItem("Medikamente", MedikamenteView.class, VaadinIcon.PILLS.create()));
        nav.addItem(new SideNavItem("Einheiten", UnitsView.class, VaadinIcon.PACKAGE.create()));
        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        // Zeige den Benutzerbereich nur an, wenn der Benutzer eingeloggt ist.
        if (userSession.isLoggedIn()) {
            Avatar avatar = new Avatar("User");
            avatar.setThemeName("xsmall");
            avatar.addClassNames(LumoUtility.Margin.End.SMALL);

            Button logoutButton = new Button("Logout", VaadinIcon.SIGN_OUT.create());
            logoutButton.addClickListener(event -> {
                // Zuerst die serverseitige Session leeren
                authService.logout();
                // Dann die Seite neu laden. Spring Security leitet dann zur Login-Seite um.
                UI.getCurrent().getPage().reload();
            });

            HorizontalLayout userLayout = new HorizontalLayout(avatar, logoutButton);
            userLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            userLayout.setSpacing(true);
            userLayout.addClassNames(LumoUtility.Padding.Vertical.XSMALL);
            layout.add(userLayout);
        }

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}