package de.jklein.pharmalinkclient.service;

import de.jklein.pharmalinkclient.dto.ActorFilterCriteriaDto;
import de.jklein.pharmalinkclient.dto.ActorResponseDto; // Import für ActorResponseDto
import de.jklein.pharmalinkclient.dto.MedikamentFilterCriteriaDto;
import de.jklein.pharmalinkclient.dto.MedikamentResponseDto;
import org.springframework.stereotype.Service;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;

import java.io.Serializable;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections; // Import für Collections.emptyList()
import java.util.List; // Import für List
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;


@SpringComponent
@VaadinSessionScope
public class StateService implements Serializable {

    private static final long serialVersionUID = 1L;

    // Medikamenten-bezogener Zustand (bestehend)
    private Optional<MedikamentResponseDto> selectedMedikament = Optional.empty();
    private Optional<MedikamentFilterCriteriaDto> currentMedikamentFilterCriteria = Optional.empty();

    private final transient CopyOnWriteArrayList<Consumer<Optional<MedikamentResponseDto>>> selectedMedikamentListeners = new CopyOnWriteArrayList<>();
    private final transient CopyOnWriteArrayList<Consumer<Optional<MedikamentFilterCriteriaDto>>> medikamentFilterCriteriaListeners = new CopyOnWriteArrayList<>();

    // Akteur-bezogener Zustand (bestehend)
    private ActorResponseDto selectedActor;
    private final PropertyChangeSupport actorSelectionChangeSupport = new PropertyChangeSupport(this);

    // Akteur-Filterkriterien (bestehend)
    private Optional<ActorFilterCriteriaDto> currentActorFilterCriteria = Optional.empty();
    private final transient CopyOnWriteArrayList<Consumer<Optional<ActorFilterCriteriaDto>>> actorFilterCriteriaListeners = new CopyOnWriteArrayList<>();

    // Zustand für Navigation zu einem spezifischen Medikament (bestehend)
    private String navigateToMedikamentId;
    private final PropertyChangeSupport navigateToMedikamentSupport = new PropertyChangeSupport(this);

    // NEUE ZUSTANDSVARIABLEN (bereits vorhanden)
    private String currentActorId;
    private Map<String, Object> cacheStats;

    // Listener für neue Zustandsvariablen (bereits vorhanden)
    private final transient CopyOnWriteArrayList<Consumer<String>> currentActorIdListeners = new CopyOnWriteArrayList<>();
    private final transient CopyOnWriteArrayList<Consumer<Map<String, Object>>> cacheStatsListeners = new CopyOnWriteArrayList<>();

    // NEU: Flag für den Ladezustand der Systemdaten pro Sitzung (bereits vorhanden)
    private boolean systemDataLoadedForSession = false;

    // NEU HINZUGEFÜGT: Liste für alle geladenen Akteure im StateService
    private List<ActorResponseDto> allLoadedActors = Collections.emptyList();
    private final transient CopyOnWriteArrayList<Consumer<List<ActorResponseDto>>> allLoadedActorsListeners = new CopyOnWriteArrayList<>();


    public StateService() {
        this.currentMedikamentFilterCriteria = Optional.of(new MedikamentFilterCriteriaDto("", "Ohne Filter"));
        this.currentActorFilterCriteria = Optional.of(new ActorFilterCriteriaDto(""));
    }

    // --- Methoden für selectedMedikament (unverändert) ---
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

    // --- Methoden für MedikamentFilterCriteriaDto (unverändert) ---
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

    // --- Methoden für die Akteur-Auswahl (unverändert) ---
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

    // --- Methoden für Navigation zu einem spezifischen Medikament (unverändert) ---
    public String getNavigateToMedikamentId() {
        return navigateToMedikamentId;
    }

    public void setNavigateToMedikamentId(String navigateToMedikamentId) {
        String oldId = this.navigateToMedikamentId;
        this.navigateToMedikamentId = navigateToMedikamentId;
        if (!Objects.equals(oldId, navigateToMedikamentId)) {
            navigateToMedikamentSupport.firePropertyChange("navigateToMedikamentId", oldId, navigateToMedikamentId);
        }
    }

    public void addNavigateToMedikamentListener(PropertyChangeListener listener) {
        navigateToMedikamentSupport.addPropertyChangeListener("navigateToMedikamentId", listener);
    }

    public void addNavigateToMedikamentListener(Consumer<String> consumer) {
        navigateToMedikamentSupport.addPropertyChangeListener("navigateToMedikamentId", evt -> {
            if (evt.getNewValue() instanceof String) {
                consumer.accept((String) evt.getNewValue());
            } else {
                consumer.accept(null);
            }
        });
    }

    public void removeNavigateToMedikamentListener(PropertyChangeListener listener) {
        navigateToMedikamentSupport.removePropertyChangeListener("navigateToMedikamentId", listener);
    }

    // --- Methoden für Akteur-Filterkriterien (unverändert) ---
    public Optional<ActorFilterCriteriaDto> getCurrentActorFilterCriteria() {
        return currentActorFilterCriteria;
    }

    public void setCurrentActorFilterCriteria(ActorFilterCriteriaDto criteria) {
        Optional<ActorFilterCriteriaDto> oldCriteria = this.currentActorFilterCriteria;
        this.currentActorFilterCriteria = Optional.ofNullable(criteria);
        actorFilterCriteriaListeners.forEach(listener -> listener.accept(this.currentActorFilterCriteria));
    }

    public void addActorFilterCriteriaListener(Consumer<Optional<ActorFilterCriteriaDto>> listener) {
        actorFilterCriteriaListeners.add(listener);
    }

    public void removeActorFilterCriteriaListener(Consumer<Optional<ActorFilterCriteriaDto>> listener) {
        actorFilterCriteriaListeners.remove(listener);
    }

    // --- Methoden für currentActorId und cacheStats (unverändert) ---
    public String getCurrentActorId() {
        return currentActorId;
    }

    public void setCurrentActorId(String currentActorId) {
        String oldActorId = this.currentActorId;
        this.currentActorId = currentActorId;
        currentActorIdListeners.forEach(listener -> listener.accept(this.currentActorId));
    }

    public void addCurrentActorIdListener(Consumer<String> listener) {
        currentActorIdListeners.add(listener);
    }

    public void removeCurrentActorIdListener(Consumer<String> listener) {
        currentActorIdListeners.remove(listener);
    }


    public Map<String, Object> getCacheStats() {
        return cacheStats;
    }

    public void setCacheStats(Map<String, Object> cacheStats) {
        Map<String, Object> oldCacheStats = this.cacheStats;
        this.cacheStats = cacheStats;
        cacheStatsListeners.forEach(listener -> listener.accept(this.cacheStats));
    }

    public void addCacheStatsListener(Consumer<Map<String, Object>> listener) {
        cacheStatsListeners.add(listener);
    }

    public void removeCacheStatsListener(Consumer<Map<String, Object>> listener) {
        cacheStatsListeners.remove(listener);
    }

    // NEU: GETTER und SETTER für die systemDataLoadedForSession Flag (unverändert)
    public boolean isSystemDataLoadedForSession() {
        return systemDataLoadedForSession;
    }

    public void setSystemDataLoadedForSession(boolean systemDataLoadedForSession) {
        this.systemDataLoadedForSession = systemDataLoadedForSession;
    }

    // NEU: Getter und Setter für alle geladenen Akteure
    public List<ActorResponseDto> getAllLoadedActors() {
        return allLoadedActors;
    }

    public void setAllLoadedActors(List<ActorResponseDto> allLoadedActors) {
        this.allLoadedActors = allLoadedActors != null ? allLoadedActors : Collections.emptyList();
        allLoadedActorsListeners.forEach(listener -> listener.accept(this.allLoadedActors));
    }

    public void addAllLoadedActorsListener(Consumer<List<ActorResponseDto>> listener) {
        allLoadedActorsListeners.add(listener);
    }

    public void removeAllLoadedActorsListener(Consumer<List<ActorResponseDto>> listener) {
        allLoadedActorsListeners.remove(listener);
    }
}