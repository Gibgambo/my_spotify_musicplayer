package my.musicplayer.spotify_player;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/player")
public class SpotifyPlayerController {
    
    private final SpotifyService spotifyService;

    public SpotifyPlayerController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @GetMapping("/play")
    public ResponseEntity<String> play() {
        return spotifyService.sendSpotifyRequest(
                "https://api.spotify.com/v1/me/player/play", HttpMethod.PUT
        );
    }

    @GetMapping("/pause")
    public ResponseEntity<String> pause() {
        return spotifyService.sendSpotifyRequest(
                "https://api.spotify.com/v1/me/player/pause", HttpMethod.PUT
        );
    }

    @GetMapping("/next")
    public ResponseEntity<String> next() {
        return spotifyService.sendSpotifyRequest(
                "https://api.spotify.com/v1/me/player/next", HttpMethod.POST
        );
    }

}
