package de.jklein.pharmalinkclient.service;

import de.jklein.pharmalinkclient.dto.ActorResponseDto;
import de.jklein.pharmalinkclient.dto.MedikamentResponseDto;
import de.jklein.pharmalinkclient.dto.UnitResponseDto;
import de.jklein.pharmalinkclient.security.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PharmalinkRestClient {

    private WebClient getClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8080")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + AuthService.getJwt())
                .build();
    }

    // Actor Endpoints
    public Flux<ActorResponseDto> getAkteure(String role) {
        return getClient().get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/actors").queryParam("role", role).build())
                .retrieve()
                .bodyToFlux(ActorResponseDto.class);
    }

    public Flux<ActorResponseDto> searchHersteller(String nameQuery) {
        return getClient().get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/hersteller/search").queryParam("search", nameQuery).build())
                .retrieve()
                .bodyToFlux(ActorResponseDto.class);
    }

    // Medikament Endpoints
    public Flux<MedikamentResponseDto> getAllMedikamente() {
        return getClient().get()
                .uri("/api/v1/medications")
                .retrieve()
                .bodyToFlux(MedikamentResponseDto.class);
    }

    public Flux<MedikamentResponseDto> searchMedikamente(String searchQuery) {
        return getClient().get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/medications/search").queryParam("search", searchQuery).build())
                .retrieve()
                .bodyToFlux(MedikamentResponseDto.class);
    }

    // Unit Endpoints
    public Mono<UnitResponseDto> getUnitById(String unitId) {
        return getClient().get()
                .uri("/api/v1/units/{unitId}", unitId)
                .retrieve()
                .bodyToMono(UnitResponseDto.class);
    }
}