package net.warze.hspcolor.utils;

import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.PlainTextContents.LiteralContents;

import java.util.ArrayList;
import java.util.List;
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
}