package my.musicplayer.spotify_player;

import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/player")
public class SpotifyPlayerController {

    private final SpotifyService spotifyService;

    public SpotifyPlayerController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @PostMapping("/play")
    public ResponseEntity<String> play(@RequestBody Map<String, String> body) {
        String contextUri = body.get("context_uri");
        String deviceId = body.get("device_id"); // device_id muss Frontend schicken

        if (deviceId == null || contextUri == null) {
            return ResponseEntity.badRequest().body("context_uri und device_id müssen gesetzt sein");
        }

        String url = "https://api.spotify.com/v1/me/player/play?device_id=" + deviceId;
        String jsonBody = "{\"context_uri\":\"" + contextUri + "\"}";

        return spotifyService.sendSpotifyRequest(url, HttpMethod.PUT, jsonBody);
    }

    @PostMapping("/pause")
    public ResponseEntity<String> pause(@RequestParam("device_id") String deviceId) {
        String url = "https://api.spotify.com/v1/me/player/pause?device_id=" + deviceId;
        return spotifyService.sendSpotifyRequest(url, HttpMethod.PUT, null);
    }

    @PostMapping("/next")
    public ResponseEntity<String> next(@RequestParam("device_id") String deviceId) {
        String url = "https://api.spotify.com/v1/me/player/next?device_id=" + deviceId;
        return spotifyService.sendSpotifyRequest(url, HttpMethod.POST, null);
    }

    @PostMapping("/previous")
    public ResponseEntity<String> previous(@RequestParam("device_id") String deviceId) {
        String url = "https://api.spotify.com/v1/me/player/previous?device_id=" + deviceId;
        return spotifyService.sendSpotifyRequest(url, HttpMethod.POST, null);
    }

    @GetMapping("/devices")
    public ResponseEntity<String> getDevices() {
        String url = "https://api.spotify.com/v1/me/player/devices";
        return spotifyService.sendSpotifyRequest(url, HttpMethod.GET, null);
    }

    @PostMapping("/seek")
    public ResponseEntity<String> seek(@RequestParam("device_id") String deviceId,
            @RequestParam("position_ms") long positionMs) {
        String url = "https://api.spotify.com/v1/me/player/seek?device_id=" + deviceId + "&position_ms=" + positionMs;
        return spotifyService.sendSpotifyRequest(url, HttpMethod.PUT, null);
    }

    @PostMapping("/volume")
    public ResponseEntity<String> setVolume(@RequestParam("device_id") String deviceId,
            @RequestParam("volume_percent") int volumePercent) {
        // Spotify API erwartet 0-100
        String url = "https://api.spotify.com/v1/me/player/volume?device_id=" + deviceId + "&volume_percent=" + volumePercent;
        return spotifyService.sendSpotifyRequest(url, HttpMethod.PUT, null);
    }

}
