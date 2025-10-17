package my.musicplayer.spotify_player;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class SpotifyAuthController {

    private final SpotifyService spotifyService;

    public SpotifyAuthController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @Value("${app.auth.user}")
    private String user;

    @Value("${app.auth.pass}")
    private String pass;

    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    @Value("${spotify.redirect.uri}")
    private String redirectUri;

    private static final String AUTH_URL = "https://accounts.spotify.com/authorize";
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";

    // Prüft Basic Auth Header
    private boolean checkBasicAuth(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return false;
        }
        String base64Credentials = authHeader.substring("Basic ".length());
        String credentials = new String(Base64.getDecoder().decode(base64Credentials));
        String[] parts = credentials.split(":", 2);
        return parts.length == 2 && parts[0].equals(user) && parts[1].equals(pass);
    }

    @GetMapping("/login")
    public ResponseEntity<Void> login(@RequestHeader(value = "Authorization", required = false) String authHeader) throws Exception {
        if (!checkBasicAuth(authHeader)) {
            return ResponseEntity.status(401)
                    .header("WWW-Authenticate", "Basic realm=\"Spotify Player\"")
                    .build();
        }

        String scope = "user-read-playback-state user-modify-playback-state streaming";
        String url = AUTH_URL + "?client_id=" + clientId
                + "&response_type=code"
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8);

        return ResponseEntity.status(302)
                .header("Location", url)
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            HttpServletRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam("code") String code
    ) {
        if (!checkBasicAuth(authHeader)) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("WWW-Authenticate", "Basic realm=\"Spotify Player\"");
            return ResponseEntity.status(401).headers(headers).build();
        }

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(TOKEN_URL, HttpMethod.POST, requestEntity, Map.class);

        Map<String, Object> responseBody = response.getBody();
        String accessToken = (String) responseBody.get("access_token");

        spotifyService.setAccessToken(accessToken);

        // Optional: Redirect ans Frontend
        HttpHeaders redirectHeaders = new HttpHeaders();
        redirectHeaders.set("Location", "/?access_token=" + accessToken);
        return ResponseEntity.status(302).headers(redirectHeaders).build();
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (!checkBasicAuth(authHeader)) {
            return ResponseEntity.status(401)
                    .header("WWW-Authenticate", "Basic realm=\"Spotify Player\"")
                    .build();
        }

        spotifyService.clearAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Location", "/force-relogin");
        return ResponseEntity.status(302).headers(headers).build();
    }

    @GetMapping("/force-relogin")
    public ResponseEntity<Void> forceRelogin() {
        return ResponseEntity.status(401)
                .header("WWW-Authenticate", "Basic realm=\"Spotify Player Logout\"")
                .build();
    }
}
