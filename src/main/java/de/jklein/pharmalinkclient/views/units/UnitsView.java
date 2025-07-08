package de.jklein.pharmalinkclient.views.units;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import de.jklein.pharmalinkclient.views.MainLayout;
import jakarta.annotation.security.PermitAll;

@PageTitle("UnitsView")
@Route(value = "unitsView", layout = MainLayout.class)
@PermitAll
public class UnitsView extends Main {

    public UnitsView() {
        addClassName("UnitsView-view");
        add(new H2("Willkommen bei Pharmalink"));
        // Hier können Sie Dashboard-Widgets hinzufügen, die Daten
        // vom PharmalinkRestClient laden.
    }
}