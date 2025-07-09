package de.jklein.pharmalinkclient.dto.auth;

public class TemperatureReadingRequestDto {
    private String temperature;
    private String timestamp; // Als String, wie in UnitResponseDto angegeben

    public TemperatureReadingRequestDto(String temperature, String timestamp) {
        this.temperature = temperature;
        this.timestamp = timestamp;
    }

    // Getter und Setter
    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}