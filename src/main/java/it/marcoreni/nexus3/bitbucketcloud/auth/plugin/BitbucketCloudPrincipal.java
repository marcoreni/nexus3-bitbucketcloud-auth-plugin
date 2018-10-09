package it.marcoreni.nexus3.bitbucketcloud.auth.plugin;

import java.io.Serializable;
import java.util.Set;

public class BitbucketCloudPrincipal implements Serializable {
    private String username;
    private Set<String> teams;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTeams(Set<String> teams) {
        this.teams = teams;
    }

    public String getUsername() {
        return username;
    }

    public Set<String> getTeams() {
        return teams;
    }

    @Override
    public String toString() {
        return username;
    }
}
