package de.jklein.pharmalinkclient.views.actorexplorer;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.jklein.pharmalinkclient.views.MainLayout;
import jakarta.annotation.security.PermitAll;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;


@PageTitle("Actor Explorer")
@Route(value = "actorExplorer", layout = MainLayout.class)
@PermitAll
public class ActorExplorerView extends VerticalLayout {

    private final ObjectProvider<ActorMasterContent> actorMasterContentProvider;
    private final ObjectProvider<ActorDetailContent> actorDetailContentProvider; // **GEÄNDERT: ActorDetailContent**

    @Autowired
    public ActorExplorerView(ObjectProvider<ActorMasterContent> actorMasterContentProvider,
                             ObjectProvider<ActorDetailContent> actorDetailContentProvider) { // **GEÄNDERT: ActorDetailContent**
        this.actorMasterContentProvider = actorMasterContentProvider;
        this.actorDetailContentProvider = actorDetailContentProvider;

        addClassName("actor-explorer-view");
        setSizeFull();
        setSpacing(false);
        setPadding(false);

        ActorMasterContent master = actorMasterContentProvider.getObject();
        ActorDetailContent detail = actorDetailContentProvider.getObject(); // **Instanz von ActorDetailContent**

        SplitLayout splitLayout = new SplitLayout(master, detail);
        splitLayout.setSplitterPosition(70); // 70% für Master, 30% für Detail
        splitLayout.setSizeFull();
        splitLayout.addClassName("actor-explorer-split-layout");

        add(splitLayout);
        expand(splitLayout);
    }
}