package org.poker.chess;

import com.fasterxml.jackson.databind.ObjectMapper;
import ioinformarics.oss.jackson.module.jsonld.JsonldModule;
import org.poker.chess.player.Profile;
import org.poker.chess.player.Stats;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

public class Players {
    private final String baseURL;
    ObjectMapper objectMapper = new ObjectMapper();

    public Players(String baseURL) {
        this.baseURL = String.format("%s/player", baseURL);
        objectMapper.registerModule(new JsonldModule());
    }

    public Profile getProfile(String userName) {
        String url = String.format("%s/%s", baseURL, userName);
        return get(url, Profile.class);
    }

    public Stats getStats(String userName) {
        String url = String.format("%s/%s/stats", baseURL, userName);
        return get(url, Stats.class);
    }

    private <T> T get(String url, Class<T> klass) {
        HttpClient client = newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .build();
        T value;
        try {
            HttpResponse<byte[]> response = client.send(request, BodyHandlers.ofByteArray());
            value = objectMapper.reader().forType(klass).readValue(response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return value;
    }

    public HttpClient newHttpClient() {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }
}
