package de.jklein.pharmalinkclient.views.actorexplorer;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import de.jklein.pharmalinkclient.views.MainLayout;
import jakarta.annotation.security.PermitAll;

@PageTitle("ActorExplorer")
@Route(value = "actorExplorer", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PermitAll
public class ActorExplorerView extends Main {

    public ActorExplorerView() {
        addClassName("ActorExplorer-view");
        add(new H2("Willkommen bei Pharmalink"));
        // Hier können Sie Dashboard-Widgets hinzufügen, die Daten
        // vom PharmalinkRestClient laden.
    }
}