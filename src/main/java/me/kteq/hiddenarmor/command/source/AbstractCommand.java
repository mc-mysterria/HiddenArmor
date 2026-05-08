package me.kteq.hiddenarmor.command.source;

import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.handler.MessageHandler;
import org.bukkit.command.*;

import java.util.logging.Level;


public abstract class AbstractCommand implements CommandExecutor {

    protected final HiddenArmor plugin;
    private final PluginCommand pluginCommand;

    private String permission = null;
    private boolean permissionRequired = true;
    private boolean playerOnly = false;

    public enum Status {
        SUCCESS,
        ERROR,
        INVALID_USAGE,
        NO_PERMISSION
    }

    public AbstractCommand(HiddenArmor plugin, String command) {
        this.plugin = plugin;
        this.pluginCommand = plugin.getCommand(command);
        
        if (pluginCommand != null) {
            pluginCommand.setExecutor(this);
            this.permission = pluginCommand.getPermission();
        } else {
            plugin.getLogger().severe("Could not register '/" + command + "' command. Is it present on plugin.yml?");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        MessageHandler messageHandler = plugin.getMessageHandler();
        if (sender instanceof ConsoleCommandSender && isPlayerOnly()) {
            messageHandler.message(sender, "%command-player-only%");
            return true;
        }
        if (!canUse(sender)) {
            messageHandler.message(sender, "%command-no-permission%");
            return true;
        }
        
        try {
            Status status = execute(sender, command, args);
            switch (status) {
                case INVALID_USAGE -> messageHandler.message(sender, "%command-invalid%");
                case NO_PERMISSION -> messageHandler.message(sender, "%command-no-permission%");
            }
        } catch (Exception e) {
            messageHandler.message(sender, "%command-error%");
            plugin.getLogger().log(Level.SEVERE, "Error executing command /" + label, e);
        }
        return true;
    }

    public boolean canUse(CommandSender sender) {
        if (!isPermissionRequired() || getPermission() == null) {
            return true;
        }
        return sender.hasPermission(getPermission()) || sender.isOp();
    }

    protected boolean hasSubPermission(CommandSender sender, String subPermission) {
        return sender.isOp() || sender.hasPermission(permission + "." + subPermission);
    }

    public boolean isPlayerOnly() {
        return playerOnly;
    }

    public AbstractCommand setPlayerOnly(boolean playerOnly) {
        this.playerOnly = playerOnly;
        return this;
    }

    public boolean isPermissionRequired() {
        return this.permissionRequired;
    }

    public AbstractCommand setPermissionRequired(boolean permissionRequired) {
        this.permissionRequired = permissionRequired;
        return this;
    }

    public String getPermission() {
        return this.permission;
    }

    public AbstractCommand setPermission(String permission) {
        this.permission = permission;
        return this;
    }

    public AbstractCommand setTabCompleter(TabCompleter tabCompleter) {
        if (pluginCommand != null) {
            pluginCommand.setTabCompleter(tabCompleter);
        }
        return this;
    }

    public abstract Status execute(CommandSender sender, Command command, String[] arguments) throws Exception;

}
