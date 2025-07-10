// src/main/java/de/jklein/pharmalinkclient/dto/CreateMedikamentRequestDto.java
package de.jklein.pharmalinkclient.dto;

import jakarta.validation.constraints.NotBlank; // NEU: Import hinzufügen
import java.util.Map;

public class CreateMedikamentRequestDto {

    @NotBlank(message = "Die Bezeichnung darf nicht leer sein.") // HINZUGEFÜGT
    private String bezeichnung;

    /**
     * Ein optionaler, bereits existierender IPFS-Hash. Dieses Feld wird nur verwendet,
     * wenn 'ipfsData' nicht zur Verfügung gestellt wird.
     */
    private String infoblattHash; // UMBENANNT von ipfsLink

    /**
     * Ein optionales, beliebiges JSON-Objekt. Wenn dieses Feld gesetzt ist,
     * wird es priorisiert, in IPFS gespeichert und der resultierende Hash verwendet.
     */
    private Map<String, Object> ipfsData;

    public CreateMedikamentRequestDto() {
        // Standardkonstruktor
    }

    // Angepasster Konstruktor, herstellerId entfernt
    public CreateMedikamentRequestDto(String bezeichnung, String infoblattHash, Map<String, Object> ipfsData) {
        this.bezeichnung = bezeichnung;
        this.infoblattHash = infoblattHash;
        this.ipfsData = ipfsData;
    }

    // --- Getter und Setter ---
    public String getBezeichnung() {
        return bezeichnung;
    }

    public void setBezeichnung(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    // Getter und Setter für infoblattHash
    public String getInfoblattHash() {
        return infoblattHash;
    }

    public void setInfoblattHash(String infoblattHash) {
        this.infoblattHash = infoblattHash;
    }

    public Map<String, Object> getIpfsData() {
        return ipfsData;
    }

    public void setIpfsData(Map<String, Object> ipfsData) {
        this.ipfsData = ipfsData;
    }
}