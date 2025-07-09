package de.jklein.pharmalinkclient.service;

import de.jklein.pharmalinkclient.dto.ActorResponseDto;
import de.jklein.pharmalinkclient.dto.MedikamentFilterCriteriaDto;
import de.jklein.pharmalinkclient.dto.MedikamentResponseDto;
import org.springframework.stereotype.Service;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

import java.io.Serializable;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;


@SpringComponent
@UIScope
public class StateService implements Serializable {

    private static final long serialVersionUID = 1L;

    // Medikamenten-bezogener Zustand (bleibt)
    private Optional<MedikamentResponseDto> selectedMedikament = Optional.empty();
    private Optional<MedikamentFilterCriteriaDto> currentMedikamentFilterCriteria = Optional.empty();

    private final transient CopyOnWriteArrayList<Consumer<Optional<MedikamentResponseDto>>> selectedMedikamentListeners = new CopyOnWriteArrayList<>();
    private final transient CopyOnWriteArrayList<Consumer<Optional<MedikamentFilterCriteriaDto>>> medikamentFilterCriteriaListeners = new CopyOnWriteArrayList<>();

    // Akteur-bezogener Zustand (bleibt)
    private ActorResponseDto selectedActor;
    private final PropertyChangeSupport actorSelectionChangeSupport = new PropertyChangeSupport(this);

    // **NEU: Zustand für Navigation zu einem spezifischen Medikament**
    private String navigateToMedikamentId; // Die ID des Medikaments, zu dem navigiert werden soll
    private final PropertyChangeSupport navigateToMedikamentSupport = new PropertyChangeSupport(this);


    public StateService() {
        this.currentMedikamentFilterCriteria = Optional.of(new MedikamentFilterCriteriaDto("", "Ohne Filter"));
    }

    // Methoden für selectedMedikament (bleiben unverändert)
    public Optional<MedikamentResponseDto> getSelectedMedikament() {
        return selectedMedikament;
    }

    public void setSelectedMedikament(MedikamentResponseDto medikament) {
        this.selectedMedikament = Optional.ofNullable(medikament);
        selectedMedikamentListeners.forEach(listener -> listener.accept(this.selectedMedikament));
    }

    public void clearSelectedMedikament() {
        this.selectedMedikament = Optional.empty();
        selectedMedikamentListeners.forEach(listener -> listener.accept(this.selectedMedikament));
    }

    public void addSelectedMedikamentListener(Consumer<Optional<MedikamentResponseDto>> listener) {
        selectedMedikamentListeners.add(listener);
    }

    public void removeSelectedMedikamentListener(Consumer<Optional<MedikamentResponseDto>> listener) {
        selectedMedikamentListeners.remove(listener);
    }

    // Methoden für MedikamentFilterCriteriaDto (bleiben unverändert)
    public Optional<MedikamentFilterCriteriaDto> getCurrentMedikamentFilterCriteria() {
        return currentMedikamentFilterCriteria;
    }

    public void setCurrentMedikamentFilterCriteria(MedikamentFilterCriteriaDto criteria) {
        Optional<MedikamentFilterCriteriaDto> oldCriteria = this.currentMedikamentFilterCriteria;
        this.currentMedikamentFilterCriteria = Optional.ofNullable(criteria);
        medikamentFilterCriteriaListeners.forEach(listener -> listener.accept(this.currentMedikamentFilterCriteria));
    }

    public void addMedikamentFilterCriteriaListener(Consumer<Optional<MedikamentFilterCriteriaDto>> listener) {
        medikamentFilterCriteriaListeners.add(listener);
    }

    public void removeMedikamentFilterCriteriaListener(Consumer<Optional<MedikamentFilterCriteriaDto>> listener) {
        medikamentFilterCriteriaListeners.remove(listener);
    }

    // Methoden für die Akteur-Auswahl (bleiben unverändert)
    public ActorResponseDto getSelectedActor() {
        return selectedActor;
    }

    public void setSelectedActor(ActorResponseDto newSelectedActor) {
        ActorResponseDto oldSelectedActor = this.selectedActor;
        this.selectedActor = newSelectedActor;
        actorSelectionChangeSupport.firePropertyChange("selectedActor", oldSelectedActor, newSelectedActor);
    }

    public void addSelectedActorListener(PropertyChangeListener listener) {
        actorSelectionChangeSupport.addPropertyChangeListener("selectedActor", listener);
    }

    public void addSelectedActorListener(Consumer<ActorResponseDto> consumer) {
        actorSelectionChangeSupport.addPropertyChangeListener("selectedActor", evt -> {
            if (evt.getNewValue() instanceof ActorResponseDto) {
                consumer.accept((ActorResponseDto) evt.getNewValue());
            } else {
                consumer.accept(null);
            }
        });
    }

    public void removeSelectedActorListener(PropertyChangeListener listener) {
        actorSelectionChangeSupport.removePropertyChangeListener("selectedActor", listener);
    }

    public void removeSelectedActorListener(Consumer<ActorResponseDto> consumer) {
        // Implementierung zum Entfernen, falls benötigt
    }

    // **NEU: Methoden für Navigation zu einem spezifischen Medikament**
    public String getNavigateToMedikamentId() {
        return navigateToMedikamentId;
    }

    public void setNavigateToMedikamentId(String navigateToMedikamentId) {
        String oldId = this.navigateToMedikamentId;
        this.navigateToMedikamentId = navigateToMedikamentId;
        // Feuern des Events nur, wenn sich die ID ändert oder sie gelöscht wird
        if (!Objects.equals(oldId, navigateToMedikamentId)) { // Importieren Sie java.util.Objects
            navigateToMedikamentSupport.firePropertyChange("navigateToMedikamentId", oldId, navigateToMedikamentId);
        }
    }

    public void addNavigateToMedikamentListener(PropertyChangeListener listener) {
        navigateToMedikamentSupport.addPropertyChangeListener("navigateToMedikamentId", listener);
    }

    // Hilfsmethode für Consumer-basierten Listener
    public void addNavigateToMedikamentListener(Consumer<String> consumer) {
        navigateToMedikamentSupport.addPropertyChangeListener("navigateToMedikamentId", evt -> {
            if (evt.getNewValue() instanceof String) {
                consumer.accept((String) evt.getNewValue());
            } else {
                consumer.accept(null); // Wenn die ID null oder nicht String ist
            }
        });
    }

    public void removeNavigateToMedikamentListener(PropertyChangeListener listener) {
        navigateToMedikamentSupport.removePropertyChangeListener("navigateToMedikamentId", listener);
    }
    // Implementierung zum Entfernen von Consumer-basierten Listenern bei Bedarf
}