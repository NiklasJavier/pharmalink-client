package de.jklein.pharmalinkclient.dto;

public class CreateUnitRequestDto {
    private String chargeBezeichnung;
    private String ipfsLink; // Optional, falls beim Erstellen übergeben

    public CreateUnitRequestDto(String chargeBezeichnung, String ipfsLink) {
        this.chargeBezeichnung = chargeBezeichnung;
        this.ipfsLink = ipfsLink;
    }

    // Getter und Setter
    public String getChargeBezeichnung() {
        return chargeBezeichnung;
    }

    public void setChargeBezeichnung(String chargeBezeichnung) {
        this.chargeBezeichnung = chargeBezeichnung;
    }

    public String getIpfsLink() {
        return ipfsLink;
    }

    public void setIpfsLink(String ipfsLink) {
        this.ipfsLink = ipfsLink;
    }
}