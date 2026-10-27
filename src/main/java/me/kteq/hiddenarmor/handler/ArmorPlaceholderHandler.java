package me.kteq.hiddenarmor.handler;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.util.ConfigHolder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ArmorPlaceholderHandler implements ConfigHolder {

    private boolean ignoreLeatherArmor;
    private boolean ignoreTurtleHelmet;
    private boolean ignoreElytra;

    public ArmorPlaceholderHandler(HiddenArmor plugin) {
        plugin.addConfigHolder(this);
    }

    // Same item, same tooltip and durability bar, but without the equipment model the client draws on the body.
    public ItemStack buildItemPlaceholder(ItemStack itemStack, Player owner) {
        if (!shouldHide(itemStack, owner)) return itemStack;
        Equippable equippable = itemStack.getData(DataComponentTypes.EQUIPPABLE);
        itemStack.setData(DataComponentTypes.EQUIPPABLE, equippable.toBuilder().assetId(null).build());
        return itemStack;
    }

    public boolean shouldHide(ItemStack itemStack, Player owner) {
        Equippable equippable = itemStack.getData(DataComponentTypes.EQUIPPABLE);
        if (equippable == null || equippable.assetId() == null) return false;

        if (itemStack.hasData(DataComponentTypes.GLIDER)) {
            return owner.isInvisible() || !(ignoreElytra || owner.isGliding());
        }
        Material material = itemStack.getType();
        if (ignoreLeatherArmor && material.name().startsWith("LEATHER_")) return false;
        return !(ignoreTurtleHelmet && material == Material.TURTLE_HELMET);
    }

    @Override
    public void loadConfig(FileConfiguration config) {
        this.ignoreLeatherArmor = config.getBoolean("ignore.leather-armor");
        this.ignoreTurtleHelmet = config.getBoolean("ignore.turtle-helmet");
        this.ignoreElytra = config.getBoolean("ignore.elytra");
    }
}
