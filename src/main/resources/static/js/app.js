console.log("Frontend ready.");

function spotifyPlayer() {
    return {
        token: null,
        player: null,
        deviceId: null,
        currentTrack: null,
        progressMs: 0,
        isPlaying: false,
        progressTimer: null,
        volume: 0.1,
        previousVolume: 0.1,
        isMuted: false,
        isSeeking: false,
        currentTrack: {
            name: "",
            artist: "",
            albumImage: "",
            duration: 0
        },

        playlists: {
            torparade: "spotify:playlist:3hZbMca6B5Xf6xpFr9fvcA",
            zweiMinGegner: "spotify:playlist:74o9YW1Q7GMzFliaBJxFam",
            einlaufmusik: "spotify:playlist:4B0Wjslz0oIkMfkfDqkz2c"
        },

        init() {
            // Token aus URL übernehmen
            const params = new URLSearchParams(window.location.search);
            this.token = params.get("access_token");
            if (this.token) {
                this.initPlayer();
            }
        },

        async login() {
            // Spring Boot Auth-Flow starten
            window.location.href = "/login";
        },

        logout() {
            // Token löschen
            this.token = null;
            localStorage.removeItem("spotify_token");
            window.location.href = "/"; // zurück zur Startseite
        },

        async pause() {
            if (!this.deviceId) {
                alert("Player noch nicht bereit");
                return;
            }

            await fetch("https://api.spotify.com/v1/me/player/pause?device_id=" + this.deviceId, {
                method: "PUT",
                headers: {
                    "Authorization": "Bearer " + this.token
                }
            });
        },

        async initPlayer() {
            window.onSpotifyWebPlaybackSDKReady = () => {
                this.player = new Spotify.Player({
                    name: 'Mein Web Player',
                    getOAuthToken: cb => { cb(this.token); },
                    volume: this.volume
                });

                this.player.addListener('ready', ({ device_id }) => {
                    console.log('Player bereit mit ID', device_id);
                    this.deviceId = device_id;
                });

                this.player.addListener('not_ready', ({ device_id }) => {
                    console.log('Gerät nicht bereit', device_id);
                });

                this.player.addListener('player_state_changed', state => {
                    if (!state) return;
                    this.isPlaying = !state.paused;
                    if (!this.isSeeking) {
                        this.progressMs = state.position; // nur updaten, wenn User nicht gerade zieht
                    }

                    const track = state.track_window.current_track;
                    this.currentTrack = {
                        name: track.name,
                        artist: track.artists.map(a => a.name).join(", "),
                        albumImage: track.album.images[0].url,
                        duration: track.duration_ms
                    };

                    // Timer für Fortschritt starten/stoppen
                    clearInterval(this.progressTimer);
                    if (this.isPlaying && !this.isSeeking) {
                        this.startProgressTimer();
                    }
                });

                this.player.connect();
            };
        },

        startProgressTimer() {
            clearInterval(this.progressTimer);
            this.progressTimer = setInterval(() => {
                this.progressMs += 1000;
                if (this.progressMs >= this.currentTrack.duration) {
                    clearInterval(this.progressTimer);
                }
            }, 1000);
        },


        async togglePlay() {
            if (!this.deviceId) {
                alert("Player noch nicht bereit");
                return;
            }
            await this.player.togglePlay();
        },

        async playPlaylist(playlistUri) {
            if (!this.deviceId) {
                alert("Player noch nicht bereit");
                return;
            }

            await fetch("https://api.spotify.com/v1/me/player/play?device_id=" + this.deviceId, {
                method: "PUT",
                body: JSON.stringify({ context_uri: playlistUri }),
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + this.token
                }
            });
        },
        formatTime(ms) {
            const minutes = Math.floor(ms / 60000);
            const seconds = Math.floor((ms % 60000) / 1000).toString().padStart(2, "0");
            return `${minutes}:${seconds}`;
        },
        async setVolume(value) {
            this.volume = value;
            if (this.player) {
                await this.player.setVolume(parseFloat(value));
            }
        },
        async seek(positionMs) {
            if (!this.deviceId) return;
            await fetch(`https://api.spotify.com/v1/me/player/seek?position_ms=${positionMs}&device_id=${this.deviceId}`, {
                method: "PUT",
                headers: {
                    "Authorization": "Bearer " + this.token
                }
            });
            this.progressMs = positionMs; // UI sofort updaten
            if (this.isPlaying) this.startProgressTimer();
        },

        async previousTrack() {
            if (!this.deviceId) return;
            await fetch(`https://api.spotify.com/v1/me/player/previous?device_id=${this.deviceId}`, {
                method: "POST",
                headers: { "Authorization": "Bearer " + this.token }
            });
        }, async nextTrack() {
            if (!this.deviceId) return;
            await fetch(`https://api.spotify.com/v1/me/player/next?device_id=${this.deviceId}`, {
                method: "POST",
                headers: { "Authorization": "Bearer " + this.token }
            });
        },
        toggleMute() {
            if (!this.player) return;
            if (!this.isMuted) {
                // Mute aktivieren
                this.previousVolume = this.volume;
                this.setVolume(0);
                this.isMuted = true;
            } else {
                // Mute deaktivieren
                this.setVolume(this.previousVolume);
                this.isMuted = false;
            }
        }

    }
}