package ru.practicum.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ViewStatsClient extends BaseClient {

    @Autowired
    public ViewStatsClient(@Value("${stats.server.url}") String serverUrl,
                           RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public Integer getViews(String uri) {
        ResponseEntity<Integer> serverResponse = rest.exchange("/views?uri={uri}",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                }, uri);
        return serverResponse.getBody();
    }

    public List<ViewStatsDto> getStat(Map parameters) {
        ResponseEntity<List<ViewStatsDto>> serverResponse = rest.exchange("/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                }, parameters);
        List<ViewStatsDto> viewStatsList = serverResponse.getBody();
        return viewStatsList;
    }

    public List<ViewStatsDto> getStat(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", start,
                "end", end,
                "uris", uris,
                "unique", unique
        );
        ResponseEntity<List<ViewStatsDto>> serverResponse = rest.exchange("/stats?start={}&end={}&uris={}&unique={}",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                }, parameters);
        List<ViewStatsDto> viewStatsList = serverResponse.getBody();
        return viewStatsList;
        //get("/stats?start={}&end={}&uris={}&unique={}", parameters);
    }

    public ResponseEntity<Object> postHit(EndpointHitDto endpointHitDto) {
        return post("/hit", endpointHitDto);
    }
}
