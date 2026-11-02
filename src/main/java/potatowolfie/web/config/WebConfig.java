package potatowolfie.web.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WebConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("web.json");

    public static final int DEFAULT_SPIDER_EGG_CLUSTER_RARITY = 128;

    public int spiderEggClusterRarity = DEFAULT_SPIDER_EGG_CLUSTER_RARITY;

    private static WebConfig instance;

    public static WebConfig get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        if (Files.exists(PATH)) {
            try {
                instance = GSON.fromJson(Files.readString(PATH), WebConfig.class);
            } catch (IOException e) {
                instance = new WebConfig();
            }
        } else {
            instance = new WebConfig();
        }
        save();
    }

    public static void save() {
        try {
            Files.writeString(PATH, GSON.toJson(instance));
        } catch (IOException ignored) {
        }
    }
}