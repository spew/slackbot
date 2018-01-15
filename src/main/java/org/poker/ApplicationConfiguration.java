package org.poker;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.EnvironmentConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
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
    return configuration.getString("SLACK_API_TOKEN");
  }

  public List<String> getChannelNames() {
    List<String> defaultChannels = Arrays.asList(new String[] { "general" });
    return configuration.getList(String.class, "CHANNEL_NAMES", defaultChannels);
  }
}
