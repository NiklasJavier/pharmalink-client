package de.jklein.pharmalinkclient.dto;

public class ActorFilterCriteriaDto {
    private String searchTerm;

    public ActorFilterCriteriaDto(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    // Getter
    public String getSearchTerm() {
        return searchTerm;
    }

    // Setter (optional, falls Kriterien nach Erstellung geändert werden sollen)
    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }
}