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
     * replace keywords in a {@link MutableComponent}
     *
     * @param text      the text
     * @param oldStringPattern the RegEx pattern of the old string
     * @param newString new string
     * @return text after replacement
     */
    public static MutableComponent replaceTextInComponent(MutableComponent component, Pattern pattern, String replacement) {
        if (component.getContents() instanceof LiteralContents literal) {
            String text = literal.text();
            String replaced = pattern.matcher(text).replaceAll(replacement);
            if (!replaced.equals(text)) {
                component = Component.literal(replaced).withStyle(component.getStyle());
            }
        }

        List<Component> siblings = component.getSiblings();
        for (int i = 0; i < siblings.size(); i++) {
            Component sibling = siblings.get(i);
            if (sibling instanceof MutableComponent mutableSibling) {
                siblings.set(i, replaceTextInComponent(mutableSibling, pattern, replacement));
            }
        }

        return component;
    }

    public static Component recolorComponent(Component component, List<Map.Entry<String, Integer>> colorReplacements) {
        Style newStyle = component.getStyle();
        if (newStyle.getColor() != null) {
            String color = newStyle.getColor().formatValue();
            for (var entry : colorReplacements) {
                if (color.equalsIgnoreCase(entry.getKey())) {
                    newStyle = newStyle.withColor(entry.getValue());
                    break;
                }
            }
        }

        List<Component> newChildren = new ArrayList<>();
        for (Component child : component.getSiblings()) {
            newChildren.add(recolorComponent(child, colorReplacements));
        }

        MutableComponent result = Component.literal(component.getContents() instanceof LiteralContents lc ? lc.text() : "")
                .setStyle(newStyle);

        for (Component child : newChildren) {
            result.append(child);
        }

        return result;
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
