// src/main/java/de/jklein/pharmalinkclient/views/medikamente/DetailContent.java
package de.jklein.pharmalinkclient.views.medikamente;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

// Remove: import de.jklein.pharmalinkclient.views.medikamente.MedikamentIpfsDetailsPanel;
// Import MedikamentIpfsDataContent directly
import de.jklein.pharmalinkclient.views.medikamente.MedikamentIpfsDataContent;
import de.jklein.pharmalinkclient.views.medikamente.UnitInformationContent;

@SpringComponent
@UIScope
public class DetailContent extends Div {

    // Change provider type to MedikamentIpfsDataContent directly
    private final ObjectProvider<MedikamentIpfsDataContent> medikamentIpfsDataContentProvider;
    private final ObjectProvider<UnitInformationContent> unitInformationContentProvider;

    @Autowired
    public DetailContent(ObjectProvider<MedikamentIpfsDataContent> medikamentIpfsDataContentProvider, // Changed provider type in constructor
                         ObjectProvider<UnitInformationContent> unitInformationContentProvider) {
        this.medikamentIpfsDataContentProvider = medikamentIpfsDataContentProvider;
        this.unitInformationContentProvider = unitInformationContentProvider;

        addClassName("detail-content");
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        getStyle().set("padding", "var(--lumo-space-m)");
        setSizeFull();

        SplitLayout internalSplitLayout = new SplitLayout();
        internalSplitLayout.setOrientation(SplitLayout.Orientation.HORIZONTAL);
        internalSplitLayout.setSizeFull();
        // Splitter position: Chargeninfo (left/primary) and Medikament IPFS Data (right/secondary)
        // Assuming you want 50/50 split here. Adjust if needed (e.g., 25 for smaller left, 75 for larger right)
        internalSplitLayout.setSplitterPosition(50);

        // Chargeninformationen to primary (left)
        internalSplitLayout.addToPrimary(unitInformationContentProvider.getObject());
        // Medikament IPFS Data Content directly to secondary (right)
        internalSplitLayout.addToSecondary(medikamentIpfsDataContentProvider.getObject()); // Directly use MedikamentIpfsDataContent

        add(internalSplitLayout);
    }

    public void setContent(Component content) {
        removeAll(); // Clear any existing content
        if (content != null) {
            add(content); // Add the new content component
        }
    }
}