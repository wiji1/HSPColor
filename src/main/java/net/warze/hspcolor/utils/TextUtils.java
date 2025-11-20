package net.warze.hspcolor.utils;

import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.PlainTextContents.LiteralContents;

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
}
