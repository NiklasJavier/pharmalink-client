package de.jklein.pharmalinkclient.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;
import de.jklein.pharmalinkclient.views.actorexplorer.ActorExplorerView;
import de.jklein.pharmalinkclient.views.dashboard.DashboardView;
import de.jklein.pharmalinkclient.views.medikamente.MedikamenteView;
import de.jklein.pharmalinkclient.views.units.UnitsView;
import de.jklein.pharmalinkclient.security.AuthService;
import org.springframework.beans.factory.annotation.Autowired;

public class MainLayout extends AppLayout {

    private H1 viewTitle;
    private final AuthService authService;

    public MainLayout(@Autowired AuthService authService) {
        this.authService = authService;
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

    private com.vaadin.flow.component.html.Footer createFooter() {
        com.vaadin.flow.component.html.Footer layout = new com.vaadin.flow.component.html.Footer();

        Button logoutButton = new Button("Logout", VaadinIcon.SIGN_OUT.create());
        logoutButton.addClickListener(event -> authService.logout());
        layout.add(logoutButton);

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