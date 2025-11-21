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

        if (hover.getValue(hover.getAction()) instanceof Component hoverText) {
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