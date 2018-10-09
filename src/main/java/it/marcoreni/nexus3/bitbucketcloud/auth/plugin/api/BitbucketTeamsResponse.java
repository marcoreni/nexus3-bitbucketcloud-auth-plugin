package it.marcoreni.nexus3.bitbucketcloud.auth.plugin.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BitbucketTeamsResponse {
    private List<BitbucketTeam> values;

    public List<BitbucketTeam> getValues() {
        return values;
    }

    public BitbucketTeamsResponse setValues(List<BitbucketTeam> values) {
        this.values = values;
        return this;
    }
}
