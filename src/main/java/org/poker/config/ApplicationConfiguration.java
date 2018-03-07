package org.poker.config;

import com.google.common.base.Enums;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.EnvironmentConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

public class ApplicationConfiguration {
    private static final Logger logger = LogManager.getLogger(ApplicationConfiguration.class);
    private final Configuration configuration;

    public ApplicationConfiguration() {
        CombinedConfiguration combinedConfiguration = new CombinedConfiguration();
        combinedConfiguration.addConfiguration(new EnvironmentConfiguration());
        this.configuration = combinedConfiguration;
    }

    public String getSlackApiToken() {
        return configuration.getString("SLACK_API_TOKEN", null);
    }

    public List<String> getChannelNames() {
        List<String> defaultChannels = Arrays.asList(new String[]{"general"});
        return configuration.getList(String.class, "CHANNEL_NAMES", defaultChannels);
    }

    public Stage getStage() {
        String value = configuration.getString("STAGE");
        if (value == null) {
            return Stage.Gamma;
        }
        Stage stage = Enums.getIfPresent(Stage.class, value).orNull();
        if (stage == null) {
            return Stage.Gamma;
        }
        return stage;
    }
}
