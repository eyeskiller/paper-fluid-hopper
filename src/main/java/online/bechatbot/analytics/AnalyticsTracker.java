package online.bechatbot.analytics;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

public class AnalyticsTracker {

    private final JavaPlugin plugin;
    private final String apiUrl;
    private final HttpClient httpClient;
    private String serverUuid;

    /**
     * Initializes the AnalyticsTracker for a plugin.
     *
     * @param plugin The instance of your JavaPlugin.
     * @param apiUrl The full URL of your analytics API (e.g., "https://analytics.bechatbot.online/api/track")
     */
    public AnalyticsTracker(JavaPlugin plugin, String apiUrl) {
        this.plugin = plugin;
        this.apiUrl = apiUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
                
        setupUuid();
    }

    /**
     * Sets up the Server UUID. It tries to load it from the plugin's config.yml.
     * If it doesn't exist, it generates a new one and saves it.
     */
    private void setupUuid() {
        FileConfiguration config = plugin.getConfig();
        
        // Use a specific path in the config to store the anonymous UUID
        if (config.contains("analytics.server-uuid")) {
            this.serverUuid = config.getString("analytics.server-uuid");
        } else {
            this.serverUuid = UUID.randomUUID().toString();
            config.set("analytics.server-uuid", this.serverUuid);
            plugin.saveConfig();
        }
    }

    /**
     * Sends an analytics event asynchronously.
     * 
     * @param eventType The type of event (e.g., "STARTUP", "SHUTDOWN", "HEARTBEAT")
     */
    public void sendEvent(String eventType) {
        String pluginName = plugin.getName();
        String pluginVersion = plugin.getDescription().getVersion();
        String serverVersion = Bukkit.getMinecraftVersion(); // Requires modern Paper/Spigot
        String serverSoftware = Bukkit.getName();
        String javaVersion = System.getProperty("java.version");
        String osArch = System.getProperty("os.arch");
        String osName = System.getProperty("os.name");
        int playerCount = Bukkit.getOnlinePlayers().size();

        String jsonPayload = String.format("""
            {
                "server_uuid": "%s",
                "plugin_name": "%s",
                "plugin_version": "%s",
                "server_version": "%s",
                "server_software": "%s",
                "java_version": "%s",
                "os_arch": "%s",
                "os_name": "%s",
                "event_type": "%s",
                "player_count": %d
            }
        """, 
        escapeJson(serverUuid), 
        escapeJson(pluginName), 
        escapeJson(pluginVersion), 
        escapeJson(serverVersion), 
        escapeJson(serverSoftware), 
        escapeJson(javaVersion), 
        escapeJson(osArch), 
        escapeJson(osName), 
        escapeJson(eventType), 
        playerCount);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 201 || response.statusCode() == 200) {
                        String body = response.body();
                        if (body != null && body.contains("\"latest_version\"")) {
                            // Extract the version string using basic string manipulation to avoid JSON library dependencies
                            int keyIndex = body.indexOf("\"latest_version\"");
                            int startQuote = body.indexOf("\"", keyIndex + 16);
                            if (startQuote != -1) {
                                int endQuote = body.indexOf("\"", startQuote + 1);
                                if (endQuote != -1) {
                                    String latestVersion = body.substring(startQuote + 1, endQuote);
                                    if (!latestVersion.equals(pluginVersion) && !latestVersion.isEmpty()) {
                                        plugin.getLogger().warning("========================================");
                                        plugin.getLogger().warning("A new update is available for " + pluginName + "!");
                                        plugin.getLogger().warning("Current version: " + pluginVersion);
                                        plugin.getLogger().warning("Latest version: " + latestVersion);
                                        plugin.getLogger().warning("========================================");
                                    }
                                }
                            }
                        }
                    } else {
                        plugin.getLogger().warning("Failed to send analytics data. Status: " + response.statusCode());
                    }
                })
                .exceptionally(ex -> null);
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"").replace("\n", "").replace("\r", "");
    }
}
