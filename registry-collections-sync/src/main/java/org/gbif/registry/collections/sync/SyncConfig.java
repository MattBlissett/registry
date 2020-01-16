package org.gbif.registry.collections.sync;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class SyncConfig {

  private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
  private static final ObjectReader YAML_READER = YAML_MAPPER.readerFor(SyncConfig.class);

  private String registryWsUrl;
  private String registryWsUser;
  private String registryWsPassword;
  private String ihWsUrl;
  private NotificationConfig notification;
  private boolean saveResultsToFile;
  private boolean dryRun;
  private boolean ignoreConflicts;

  @Getter
  @Setter
  public static class NotificationConfig {
    private String githubWsUrl;
    private String githubUser;
    private String githubPassword;
    private String ihPortalUrl;
    private String registryPortalUrl;
    private List<String> ghIssuesAssignees;
  }

  public static Optional<SyncConfig> fromFileName(String configFileName) {
    if (Strings.isNullOrEmpty(configFileName)) {
      log.error("No config file provided");
      return Optional.empty();
    }

    File configFile = new File(configFileName);
    SyncConfig config = null;
    try {
      config = YAML_READER.readValue(configFile);
    } catch (IOException e) {
      log.error("Couldn't load config from file {}", configFileName);
      return Optional.empty();
    }

    if (config == null) {
      return Optional.empty();
    }

    // do some checks for required fields
    if (Strings.isNullOrEmpty(config.getRegistryWsUrl())
        || Strings.isNullOrEmpty(config.getIhWsUrl())) {
      throw new IllegalArgumentException("Registry and IH WS URLs are required");
    }

    if (!config.isIgnoreConflicts()) {
      if (config.getNotification() == null) {
        throw new IllegalArgumentException("Notification config is required");
      }

      if (Strings.isNullOrEmpty(config.getNotification().getGithubUser())
          || Strings.isNullOrEmpty(config.getNotification().getGithubPassword())) {
        throw new IllegalArgumentException(
            "Github credentials are required if we are not ignoring conflicts.");
      }
      if (config.getNotification().getGhIssuesAssignees() == null
          || config.getNotification().getGhIssuesAssignees().isEmpty()) {
        throw new IllegalArgumentException("Github assignees are required.");
      }
      if (Strings.isNullOrEmpty(config.getNotification().registryPortalUrl)
          || Strings.isNullOrEmpty(config.getNotification().ihPortalUrl)) {
        throw new IllegalArgumentException("Portal URLs are required");
      }
    }

    if (!config.isDryRun()
        && (Strings.isNullOrEmpty(config.getRegistryWsUser())
            || Strings.isNullOrEmpty(config.getRegistryWsPassword()))) {
      throw new IllegalArgumentException(
          "Registry WS credentials are required if we are not doing a dry run");
    }

    return Optional.of(config);
  }
}
