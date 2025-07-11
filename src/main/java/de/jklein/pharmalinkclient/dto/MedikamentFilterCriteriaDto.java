// src/main/java/de/jklein/pharmalinkclient/dto/MedikamentFilterCriteriaDto.java
package de.jklein.pharmalinkclient.dto;

public class MedikamentFilterCriteriaDto {
    private String searchTerm;
    private String statusFilter; // "Alle", "angelegt", "freigegeben", "abgelehnt"
    private boolean filterByCurrentActor; // NEU: Flag für Filterung nach aktuellem Akteur

    public MedikamentFilterCriteriaDto(String searchTerm, String statusFilter) {
        this.searchTerm = searchTerm;
        this.statusFilter = statusFilter;
        this.filterByCurrentActor = false; // Standardwert
    }

    // NEU: Konstruktor mit filterByCurrentActor
    public MedikamentFilterCriteriaDto(String searchTerm, String statusFilter, boolean filterByCurrentActor) {
        this.searchTerm = searchTerm;
        this.statusFilter = statusFilter;
        this.filterByCurrentActor = filterByCurrentActor;
    }

    // Getter
    public String getSearchTerm() {
        return searchTerm;
    }

    public String getStatusFilter() {
        return statusFilter;
    }

    public boolean isFilterByCurrentActor() { // NEU: Getter für das Flag
        return filterByCurrentActor;
    }

    // Optional: Setter, falls Kriterien nach Erstellung geändert werden sollen (eher selten für DTOs)
    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public void setStatusFilter(String statusFilter) {
        this.statusFilter = statusFilter;
    }

    public void setFilterByCurrentActor(boolean filterByCurrentActor) { // NEU: Setter für das Flag
        this.filterByCurrentActor = filterByCurrentActor;
    }

    @Override
    public String toString() {
        return "MedikamentFilterCriteriaDto{" +
                "searchTerm='" + searchTerm + '\'' +
                ", statusFilter='" + statusFilter + '\'' +
                ", filterByCurrentActor=" + filterByCurrentActor +
                // weitere Felder hinzufügen
                '}';
    }
}