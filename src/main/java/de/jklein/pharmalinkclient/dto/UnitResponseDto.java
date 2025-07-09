package de.jklein.pharmalinkclient.dto;

import java.util.List;
import java.util.Map;

public class UnitResponseDto {
    private String unitId;
    private String medId;
    private String chargeBezeichnung;
    private String ipfsLink;
    private String currentOwnerActorId;
    private List<Map<String, String>> temperatureReadings; // Jede Map enthält "temperature" und "timestamp"
    private List<Map<String, String>> transferHistory; // Jede Map enthält "from", "to", "timestamp"
    private Map<String, Object> ipfsData;

    // Getter und Setter
    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getMedId() {
        return medId;
    }

    public void setMedId(String medId) {
        this.medId = medId;
    }

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

    public String getCurrentOwnerActorId() {
        return currentOwnerActorId;
    }

    public void setCurrentOwnerActorId(String currentOwnerActorId) {
        this.currentOwnerActorId = currentOwnerActorId;
    }

    public List<Map<String, String>> getTemperatureReadings() {
        return temperatureReadings;
    }

    public void setTemperatureReadings(List<Map<String, String>> temperatureReadings) {
        this.temperatureReadings = temperatureReadings;
    }

    public List<Map<String, String>> getTransferHistory() {
        return transferHistory;
    }

    public void setTransferHistory(List<Map<String, String>> transferHistory) {
        this.transferHistory = transferHistory;
    }

    public Map<String, Object> getIpfsData() {
        return ipfsData;
    }

    public void setIpfsData(Map<String, Object> ipfsData) {
        this.ipfsData = ipfsData;
    }
}