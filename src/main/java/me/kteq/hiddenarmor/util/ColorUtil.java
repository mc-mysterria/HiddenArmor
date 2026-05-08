package me.kteq.hiddenarmor.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;
import java.util.stream.Collectors;

public final class ColorUtil {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private ColorUtil() {
    }

    public static Component color(String text) {
        if (text == null) return Component.empty();

        if (text.contains("<") && (text.contains(">") || text.contains("/"))) {
            return MINI_MESSAGE.deserialize(text);
        }

        return LEGACY_SERIALIZER.deserialize(text);
    }

    public static List<Component> color(List<String> texts) {
        if (texts == null) return List.of();
        return texts.stream().map(ColorUtil::color).collect(Collectors.toList());
    }
}
