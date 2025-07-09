package de.jklein.pharmalinkclient.dto;

public class TransferUnitRequestDto {
    private String fromActorId;
    private String toActorId;

    public TransferUnitRequestDto(String fromActorId, String toActorId) {
        this.fromActorId = fromActorId;
        this.toActorId = toActorId;
    }

    // Getter und Setter
    public String getFromActorId() {
        return fromActorId;
    }

    public void setFromActorId(String fromActorId) {
        this.fromActorId = fromActorId;
    }

    public String getToActorId() {
        return toActorId;
    }

    public void setToActorId(String toActorId) {
        this.toActorId = toActorId;
    }
}