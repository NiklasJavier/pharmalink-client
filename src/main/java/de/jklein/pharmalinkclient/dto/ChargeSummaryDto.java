// src/main/java/de/jklein/pharmalinkclient/dto/ChargeSummaryDto.java
package de.jklein.pharmalinkclient.dto;

public class ChargeSummaryDto {
    private String chargeBezeichnung;
    private long anzahl;

    // Add this public no-argument constructor
    public ChargeSummaryDto() {
        // Required by many frameworks (e.g., Spring, Jackson, Vaadin) for instantiation
    }

    public ChargeSummaryDto(String chargeBezeichnung, long anzahl) {
        this.chargeBezeichnung = chargeBezeichnung;
        this.anzahl = anzahl;
    }

    public String getChargeBezeichnung() {
        return chargeBezeichnung;
    }

    public void setChargeBezeichnung(String chargeBezeichnung) {
        this.chargeBezeichnung = chargeBezeichnung;
    }

    public long getAnzahl() {
        return anzahl;
    }

    public void setAnzahl(long anzahl) {
        this.anzahl = anzahl;
    }
}