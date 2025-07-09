package de.jklein.pharmalinkclient.dto;

import java.util.Map;

public class ActorUpdateRequestDto {
    private String name;
    private Map<String, Object> ipfsData;

    public ActorUpdateRequestDto() {
    }

    public ActorUpdateRequestDto(String name, String role, Map<String, Object> ipfsData) {
        this.name = name;
        this.ipfsData = ipfsData;
    }

    // Getter und Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getIpfsData() {
        return ipfsData;
    }

    public void setIpfsData(Map<String, Object> ipfsData) {
        this.ipfsData = ipfsData;
    }
}