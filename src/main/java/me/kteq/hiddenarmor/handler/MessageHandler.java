package me.kteq.hiddenarmor.handler;

import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.util.ColorUtil;
import me.kteq.hiddenarmor.util.ConfigHolder;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MessageHandler implements ConfigHolder {

    private final Plugin plugin;

    private String defaultLocale;
    private String prefix = "";

    private Map<String, FileConfiguration> localeMap;

    public MessageHandler(HiddenArmor plugin, String prefix) {
        this.plugin = plugin;
        plugin.addConfigHolder(this);
        setPrefix(prefix);
        reloadLocales();
    }

    public void reloadLocales() {
        Set<String> includedLocales = new HashSet<>();
        includedLocales.add("en_us");
        includedLocales.add("pt_br");

        for (String locale : includedLocales) {
            String path = "locale/" + locale + ".yml";
            if (!new File(plugin.getDataFolder().getAbsolutePath() + "/" + path).exists()) {
                plugin.saveResource(path, false);
            }
        }

        localeMap = new HashMap<>();
        File localeFolder = new File(plugin.getDataFolder().getAbsolutePath() + "/locale");
        File[] files = localeFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                FileConfiguration localeYaml = getYamlConfiguration(file);
                if (localeYaml != null) {
                    localeMap.put(file.getName().replaceAll(".yml", ""), localeYaml);
                }
            }
        }
    }

    private FileConfiguration getYamlConfiguration(File file) {
        if (file == null || !file.exists()) return null;
        return YamlConfiguration.loadConfiguration(file);
    }

    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void message(CommandSender sender, String message) {
        message(sender, message, false);
    }

    public void message(CommandSender sender, String message, boolean prefix) {
        message(sender, message, prefix, new HashMap<>());
    }

    public void message(CommandSender sender, String message, boolean prefix, Map<String, String> placeholderMap) {
        message = replaceHoldersFromConfig(sender, message);

        for (Map.Entry<String, String> entry : placeholderMap.entrySet()) {
            String value = replaceHoldersFromConfig(sender, entry.getValue());
            message = message.replaceAll("%" + entry.getKey() + "%", value);
        }

        if (prefix) {
            message = this.prefix + message;
        }

        sender.sendMessage(ColorUtil.color(message));
    }

    public void sendActionBar(Player player, String message, Map<String, String> placeholderMap) {
        message = replaceHoldersFromConfig(player, message);

        for (Map.Entry<String, String> entry : placeholderMap.entrySet()) {
            String value = replaceHoldersFromConfig(player, entry.getValue());
            message = message.replaceAll("%" + entry.getKey() + "%", value);
        }

        player.sendActionBar(ColorUtil.color(message));
    }

    private String replaceHoldersFromConfig(CommandSender sender, String message) {
        if (message == null) return null;
        for (String string : message.split("%")) {
            if (string.contains(" ")) continue;
            String localizedMessage = getLocalizedMessage(sender, string);

            if (localizedMessage != null) {
                message = message.replaceAll("%" + string + "%", localizedMessage);
            }
        }

        return message;
    }

    private String getLocalizedMessage(CommandSender sender, String messageKey) {
        String locale = (sender instanceof Player player) ? player.getLocale() : defaultLocale;
        FileConfiguration localeYaml = localeMap.get(locale);
        if (localeYaml == null) {
            localeYaml = localeMap.get(defaultLocale);
            if (localeYaml == null) {
                localeYaml = getDefaultResourceLocale();
            }
        }

        return localeYaml.getString(messageKey, getDefaultResourceLocale().getString(messageKey));
    }

    private FileConfiguration getDefaultResourceLocale() {
        InputStream inputStream = plugin.getResource("locale/en_us.yml");
        if (inputStream == null) return new YamlConfiguration();
        return YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream));
    }

    @Override
    public void loadConfig(FileConfiguration config) {
        setDefaultLocale(config.getString("locale.default-locale", "en_us").replaceAll("-", "_"));
    }
}
