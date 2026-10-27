package me.kteq.hiddenarmor.command;

import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.command.source.AbstractCommand;
import me.kteq.hiddenarmor.handler.MessageHandler;
import me.kteq.hiddenarmor.manager.PlayerManager;
import me.kteq.hiddenarmor.util.ConfigHolder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class HiddenArmorCommand extends AbstractCommand implements ConfigHolder {

    PlayerManager hiddenArmorManager;

    private boolean defaultPermissionToggle;
    private boolean defaultPermissionToggleOther;

    public HiddenArmorCommand(HiddenArmor plugin, String command) {
        super(plugin, command);
        plugin.addConfigHolder(this);
        this.hiddenArmorManager = plugin.getPlayerManager();
    }

    @Override
    public Status execute(CommandSender sender, Command command, String[] arguments) throws Exception {
        if ((arguments.length < 1) || (arguments[0].equalsIgnoreCase("help"))) {
            help(sender);
            return Status.SUCCESS;
        }

        MessageHandler messageHandler = plugin.getMessageHandler();

        String subcommand = arguments[0].toLowerCase();

        switch (subcommand) {
            case "reload":
                if (!hasSubPermission(sender, "reload")) break;
                plugin.saveDefaultConfig();
                plugin.reloadConfig();
                messageHandler.reloadLocales();
                messageHandler.message(sender, "%reload-success%", true);
                return Status.SUCCESS;
            case "toggle":
            case "hide":
            case "show":
                return toggleArmor(sender, arguments);
        }

        return Status.INVALID_USAGE;
    }

    private Status toggleArmor(CommandSender sender, String[] arguments) {
        if (!hasSubPermission(sender, "toggle") && !defaultPermissionToggle) return Status.NO_PERMISSION;
        MessageHandler messageHandler = plugin.getMessageHandler();
        Player player;
        if (arguments.length >= 2) {
            if (!hasSubPermission(sender, "toggle.other") && !defaultPermissionToggleOther) return Status.NO_PERMISSION;
            player = Bukkit.getPlayer(arguments[1]);

            if (player == null) {
                messageHandler.message(sender, "%player-not-found%");
                return Status.SUCCESS;
            }
        } else if (sender instanceof Player self) {
            player = self;
        } else {
            messageHandler.message(sender, "%console-togglearmor-warning%");
            return Status.SUCCESS;
        }

        String action = arguments[0].toLowerCase();

        switch (action) {
            case "toggle":
                hiddenArmorManager.togglePlayer(player, true);
                break;
            case "hide":
                hiddenArmorManager.enablePlayer(player, true);
                break;
            case "show":
                hiddenArmorManager.disablePlayer(player, true);
                break;
        }

        if (!player.equals(sender)) {
            Map<String, String> placeholderMap = new HashMap<>();
            placeholderMap.put("visibility", hiddenArmorManager.isEnabled(player) ? "%visibility-hidden%" : "%visibility-shown%");
            placeholderMap.put("player", player.getName());
            messageHandler.message(sender, "%armor-visibility-other%", false, placeholderMap);
        }
        return Status.SUCCESS;
    }

    private void help(CommandSender sender) {
        MessageHandler messageHandler = plugin.getMessageHandler();
        messageHandler.message(sender, "&6----------[ &fHiddenArmor &6]-----------------");

        // hiddenarmor <toggle/hide/show>
        if (sender.hasPermission("hiddenarmor.toggle") || defaultPermissionToggle)
            messageHandler.message(sender, "&e/hiddenarmor <toggle/hide/show> &6- %help-togglearmor%");

        // hiddenarmor <toggle/hide/show> <player>
        if (sender.hasPermission("hiddenarmor.toggle.other") || (defaultPermissionToggle && defaultPermissionToggleOther))
            messageHandler.message(sender, "&e/hiddenarmor <toggle/hide/show> [%player%] &6- %help-togglearmor-other%");

        // hiddenarmor reload
        if (sender.hasPermission("hiddenarmor.reload"))
            messageHandler.message(sender, "&e/hiddenarmor reload &6- %help-reload%");

        // help
        messageHandler.message(sender, "&e/hiddenarmor help &6- %help-help%");

        messageHandler.message(sender, "&6----------------------------------------");
    }

    @Override
    public void loadConfig(FileConfiguration config) {
        this.defaultPermissionToggle = config.getBoolean("default-permissions.toggle");
        this.defaultPermissionToggleOther = config.getBoolean("default-permissions.toggle-other");
    }
}
