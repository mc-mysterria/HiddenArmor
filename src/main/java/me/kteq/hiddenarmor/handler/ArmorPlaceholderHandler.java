package me.kteq.hiddenarmor.handler;

import com.google.common.collect.Multimap;
import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.util.ConfigHolder;
import me.kteq.hiddenarmor.util.ItemUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArmorPlaceholderHandler implements ConfigHolder {

    private final HiddenArmor plugin;

    private boolean ignoreLeatherArmor;
    private boolean ignoreTurtleHelmet;

    public ArmorPlaceholderHandler(HiddenArmor plugin) {
        plugin.addConfigHolder(this);
        this.plugin = plugin;
    }

    public ItemStack buildItemPlaceholder(ItemStack itemStack) {
        if (itemStack.getType() == Material.AIR) return itemStack;

        Material placeholderMaterial = getPlaceholderMaterial(itemStack);
        if (placeholderMaterial == null) return itemStack;
        ItemMeta newItemMeta = buildNewItemMeta(itemStack, placeholderMaterial);
        if (newItemMeta == null) return itemStack;

        List<Component> lore = newItemMeta.lore();
        if (lore == null) lore = new ArrayList<>();
        Component durability = buildDurabilityText(itemStack);
        if (durability != null) lore.add(durability);
        newItemMeta.lore(lore);

        Component displayName = buildName(itemStack);
        newItemMeta.displayName(displayName);

        itemStack = itemStack.withType(placeholderMaterial);
        itemStack.setItemMeta(newItemMeta);

        return itemStack;
    }

    private ItemMeta buildNewItemMeta(ItemStack itemStack, Material material) {
        ItemMeta oldItemMeta = itemStack.getItemMeta();
        if (oldItemMeta == null) return null;

        Map<Enchantment, Integer> enchantments = oldItemMeta.getEnchants();
        Multimap<Attribute, AttributeModifier> attributes = oldItemMeta.getAttributeModifiers();
        int damage = ((Damageable) oldItemMeta).getDamage();

        ItemMeta newItemMeta = plugin.getServer().getItemFactory().getItemMeta(material);
        if (newItemMeta == null) return null;

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            newItemMeta.addEnchant(entry.getKey(), entry.getValue(), true);
        }

        newItemMeta.setAttributeModifiers(attributes);

        if (newItemMeta instanceof Damageable damageable) {
            damageable.setDamage(damage);
        }

        return newItemMeta;
    }

    private Material getPlaceholderMaterial(ItemStack armor) {
        if (ItemUtil.isNotArmorPiece(armor)) return null;

        String m = armor.getType().toString();
        if (m.startsWith("NETHERITE_"))
            return Material.POLISHED_BLACKSTONE_BUTTON;
        if (m.startsWith("DIAMOND_"))
            return Material.WARPED_BUTTON;
        if (m.startsWith("GOLDEN_"))
            return Material.BIRCH_BUTTON;
        if (m.startsWith("IRON_"))
            return Material.STONE_BUTTON;
        if (m.startsWith("LEATHER_") && !ignoreLeatherArmor)
            return Material.ACACIA_BUTTON;
        if (m.startsWith("CHAINMAIL_"))
            return Material.JUNGLE_BUTTON;
        if (m.startsWith("TURTLE_") && !ignoreTurtleHelmet)
            return Material.CRIMSON_BUTTON;
        return null;
    }

    private Component buildDurabilityText(ItemStack itemStack) {
        int percentage = ItemUtil.getDurabilityPercentage(itemStack);
        if (percentage != -1) {
            NamedTextColor color = NamedTextColor.YELLOW;
            if (percentage >= 70) color = NamedTextColor.GREEN;
            if (percentage < 30) color = NamedTextColor.RED;
            
            return Component.text("Durability: ", NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(percentage + "%", color));
        }
        return null;
    }

    private Component buildName(ItemStack itemStack) {
        String typeName = itemStack.getType().toString().replace("_", " ").toUpperCase();
        ItemMeta itemMeta = itemStack.getItemMeta();
        
        Component baseName = Component.text(typeName).color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false);
        
        if (itemMeta != null && itemMeta.hasDisplayName()) {
            Component customName = itemMeta.displayName();
            if (customName == null) customName = Component.empty();
            return customName.append(Component.space())
                    .append(Component.text("(", NamedTextColor.GRAY))
                    .append(baseName)
                    .append(Component.text(")", NamedTextColor.GRAY));
        }
        
        return baseName;
    }

    @Override
    public void loadConfig(FileConfiguration config) {
        this.ignoreLeatherArmor = config.getBoolean("ignore.leather-armor");
        this.ignoreTurtleHelmet = config.getBoolean("ignore.turtle-helmet");
    }
}
