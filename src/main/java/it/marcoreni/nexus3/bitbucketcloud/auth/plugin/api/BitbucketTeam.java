package it.marcoreni.nexus3.bitbucketcloud.auth.plugin.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BitbucketTeam {
    private String permission;
    private Team team;

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public class Team {
        private String username;
        private String displayName;
        private String uuid;

        public String getUsername() {
            return username;
        }

        public Team setUsername(String username) {
            this.username = username;
            return this;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Team setDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public String getUuid() {
            return uuid;
        }

        public Team setUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }
    }
}
