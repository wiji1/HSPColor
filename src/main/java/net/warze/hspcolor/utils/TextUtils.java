package net.warze.hspcolor.utils;

import net.minecraft.network.chat.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents.LiteralContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ResourceLocationPattern;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author Warze
 */
public class TextUtils {
    public static Component empty() {
        return Component.empty();
    }

    public static List<Map.Entry<String, Integer>> colorReplacements = List.of(
            Map.entry("#D4448C", 0xE985F7), // HERO+
            Map.entry("#FDDD5C", 0xB8B8B8), // Bomb bell
            Map.entry("#F3E6B2", 0xE6DA5E), // Bomb bell
            Map.entry("#A0C84B", 0xDFDAB7), // 1 time bomb
            Map.entry("#FFD750", 0xFFF7F2), // 1 time bomb
            Map.entry("#BD45FF", 0x8684FA), // shout
            Map.entry("#FAD9F7", 0xD8D8FA)  // shout
    );

    /**
     * replace keywords in a {@link Component} by creating a new component
     *
     * @param component   the text
     * @param pattern     the RegEx pattern of the old string
     * @param replacement new string
     * @return new component with replacements applied
     */
    public static MutableComponent replaceTextInComponent(Component component, Pattern pattern, String replacement) {
        MutableComponent newComponent;

        if (component.getContents() instanceof LiteralContents literal) {
            String text = literal.text();
            String replaced = pattern.matcher(text).replaceAll(replacement);
            newComponent = Component.literal(replaced).withStyle(component.getStyle());
        } else newComponent = component.copy();

        List<Component> siblings = component.getSiblings();
        List<Component> newSiblings = new ArrayList<>(siblings.size());

        for (Component sibling : siblings) newSiblings.add(replaceTextInComponent(sibling, pattern, replacement));
        for (Component newSibling : newSiblings) newComponent.append(newSibling);

        return newComponent;
    }

    public static boolean messageHasNickHoverDeep(Component message) {
        boolean hasNick = false;
        if (!message.getSiblings().isEmpty()) {
            for (Component messageSibling : message.getSiblings()) {
                hasNick = hasNick || messageHasNickHoverDeep(messageSibling);
            }
        } else {
            return messageHasNickHover(message);
        }
        return hasNick;
    }

    public static boolean messageHasNickHover(Component message) {
        HoverEvent hover = message.getStyle().getHoverEvent();

        if (hover == null) return false;

        System.out.println(hover.getValue(hover.getAction()));


        if (hover.getValue(hover.getAction()) instanceof Component hoverText) {
            System.out.println(hoverText.getString());
            return hoverText.getString().contains("real name") || hoverText.getString().contains("nickname");
        }
        return false;
    }

    public static Component GetSiblingWithHover(Component message) {
        for (Component sibling : message.getSiblings()) {

            HoverEvent hover = sibling.getStyle().getHoverEvent();
            if (hover != null) return sibling;

            Component hoverComponent = GetSiblingWithHover(sibling);

            if (hoverComponent != null) return hoverComponent;
        }

        return null;
    }
}