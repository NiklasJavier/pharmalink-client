package de.jklein.pharmalinkclient.views.medikamente;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

@SpringComponent
@UIScope
public class DetailContent extends Div {
    public DetailContent() {
        add(new H3("Detail Content Placeholder"));
        setText("Dies ist der Detail-Bereich (z.B. Details des ausgewählten Elements).");
        getStyle().set("background-color", "var(--lumo-contrast-10pct)");
        getStyle().set("padding", "var(--lumo-space-m)");
        setSizeFull();
    }
}