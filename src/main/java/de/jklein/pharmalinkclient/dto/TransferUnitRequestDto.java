package de.jklein.pharmalinkclient.dto;

import jakarta.validation.constraints.NotBlank;

public class TransferUnitRequestDto {

    @NotBlank(message = "newOwnerActorId darf nicht leer sein.")
    private String newOwnerActorId;

    // Leerer Konstruktor für Frameworks
    public TransferUnitRequestDto() {
    }

    public TransferUnitRequestDto(String newOwnerActorId) {
        this.newOwnerActorId = newOwnerActorId;
    }

    // Getter und Setter
    public String getNewOwnerActorId() {
        return newOwnerActorId;
    }

    public void setNewOwnerActorId(String newOwnerActorId) {
        this.newOwnerActorId = newOwnerActorId;
    }
}