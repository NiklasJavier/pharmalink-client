package de.jklein.pharmalinkclient.dto;

public class MedikamentFilterCriteriaDto {
    private String searchTerm;
    private String statusFilter; // "Alle", "angelegt", "freigegeben", "abgelehnt"

    public MedikamentFilterCriteriaDto(String searchTerm, String statusFilter) {
        this.searchTerm = searchTerm;
        this.statusFilter = statusFilter;
    }

    // Getter
    public String getSearchTerm() {
        return searchTerm;
    }

    public String getStatusFilter() {
        return statusFilter;
    }

    // Optional: Setter, falls Kriterien nach Erstellung geändert werden sollen (eher selten für DTOs)
    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public void setStatusFilter(String statusFilter) {
        this.statusFilter = statusFilter;
    }
}