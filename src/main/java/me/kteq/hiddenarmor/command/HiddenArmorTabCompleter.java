package me.kteq.hiddenarmor.command;

import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.util.ConfigHolder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class HiddenArmorTabCompleter implements TabCompleter, ConfigHolder {
    private boolean defaultPermissionToggle;
    private boolean defaultPermissionToggleOther;

    public HiddenArmorTabCompleter(HiddenArmor plugin) {
        plugin.addConfigHolder(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equals("hiddenarmor")) return null;
        if (args.length == 2) {
            boolean canTarget = sender.hasPermission("hiddenarmor.toggle.other") || defaultPermissionToggleOther;
            return canTarget && List.of("toggle", "hide", "show").contains(args[0].toLowerCase()) ? null : List.of();
        }
        if (args.length > 2) return List.of();
        List<String> options = new ArrayList<>();

        if (sender.hasPermission("hiddenarmor.toggle") || defaultPermissionToggle) {
            options.add("toggle");
            options.add("hide");
            options.add("show");
        }
        if (sender.hasPermission("hiddenarmor.reload")) {
            options.add("reload");
        }
        options.add("help");

        return options.stream().filter(option -> option.startsWith(args[0].toLowerCase())).toList();
    }

    @Override
    public void loadConfig(FileConfiguration config) {
        this.defaultPermissionToggle = config.getBoolean("default-permissions.toggle");
        this.defaultPermissionToggleOther = config.getBoolean("default-permissions.toggle-other");
    }
}
