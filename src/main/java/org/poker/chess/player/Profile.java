package org.poker.chess.player;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Profile {
    @JsonProperty("@id")
    private String id;
    private String url;
    private String username;
    @JsonProperty("player_id")
    private long playerId;
    private String status;
    private String name;
    private String avatar;
    private String location;
    private String country;
    private long joined;
    @JsonProperty("last_online")
    private long lastOnline;
    private long followers;
    @JsonProperty("is_streamer")
    private boolean isStreamer;
    @JsonProperty("twitch_url")
    private String twitchURL;
    private long fide;

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public long getPlayerId() {
        return playerId;
    }

    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getLocation() {
        return location;
    }

    public String getCountry() {
        return country;
    }

    public long getJoined() {
        return joined;
    }

    public long getLastOnline() {
        return lastOnline;
    }

    public long getFollowers() {
        return followers;
    }

    public boolean isStreamer() {
        return isStreamer;
    }

    public String getTwitchURL() {
        return twitchURL;
    }

    public long getFide() {
        return fide;
    }
}
