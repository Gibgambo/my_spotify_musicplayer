package my.musicplayer.spotify_player;


import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SpotifyService {
    private String accessToken;

    public void setAccessToken(String token) {
        this.accessToken = token;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public ResponseEntity<String> sendSpotifyRequest(String url, HttpMethod method) {
        return sendSpotifyRequest(url, method, null);
    }


    public ResponseEntity<String> sendSpotifyRequest(String url, HttpMethod method, String body) {
        if (accessToken == null) {
            return ResponseEntity.badRequest().body("Kein Token gespeichert!");
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Content-Type", "application/json");

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, method, request, String.class);
    }
}
