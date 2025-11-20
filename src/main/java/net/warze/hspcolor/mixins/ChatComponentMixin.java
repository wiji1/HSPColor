package net.warze.hspcolor.mixins;

import net.warze.hspcolor.utils.MCServerUtils;
import net.warze.hspcolor.utils.Ranks;
import net.warze.hspcolor.utils.Replacement;
import net.warze.hspcolor.utils.TextUtils;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * @author Warze
 */
@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {

    @Shadow
    public abstract void rescaleChat();

    private static final String GUILD_START = "󏿼󏿿󏿾";
    private static final String GUILD_CONT = "󏿼󐀆";
    private static final String GUILD_CHAT_COLOR = "#55FFFF";

    private static final List<String> RANKS = List.copyOf(Ranks.Old.keySet());

    public boolean isGuildMessage(List<Component> siblings) {
        if (siblings.isEmpty()) return false;
        try {
            Component first = siblings.getFirst();
            String color = String.valueOf(first.getStyle().getColor());
            String text = first.getString();
            return GUILD_CHAT_COLOR.equals(color) &&
                   (text.contains(GUILD_START) || text.contains(GUILD_CONT));
        } catch (Exception e) {
            return false;
        }
    }


    @ModifyArgs(
        method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/GuiMessage;<init>(ILnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V")
    )
    public void onReceivingMessages(Args args) {
        if (!MCServerUtils.isWynnCraft()) return;

        final int MESSAGE_IDX = 1;
        MutableComponent message = args.get(MESSAGE_IDX);

        List<Component> siblings = message.getSiblings();
        List<Component> newSiblings = new ArrayList<>(siblings.size());

        List<Map.Entry<String, Integer>> colorReplacements = List.of(
            Map.entry("#D4448C", 0xE985F7), // HERO+
            Map.entry("#FDDD5C", 0xB8B8B8), // Bomb bell
            Map.entry("#F3E6B2", 0xE6DA5E), // Bomb bell
            Map.entry("#A0C84B", 0xDFDAB7), // 1 time bomb
            Map.entry("#FFD750", 0xFFF7F2), // 1 time bomb
            Map.entry("#BD45FF", 0x8684FA), // shout
            Map.entry("#FAD9F7", 0xD8D8FA)  // shout
        );

        for (Component sibling : siblings) {
            Component newSibling = sibling;
            if (sibling.getStyle().getColor() != null) {
                String color = sibling.getStyle().getColor().toString();
                for (var entry : colorReplacements) {
                    if (color.equalsIgnoreCase(entry.getKey())) {
                        newSibling = Component.literal(sibling.getString())
                                .setStyle(sibling.getStyle().withColor(entry.getValue()));
                        break;
                    }
                }
            }
            newSiblings.add(newSibling);
        }

        if (newSiblings.size() > 4) {

            System.out.println("--------------------------------");
            newSiblings.forEach(c -> System.out.println(c.getString()));
            System.out.println("--------------------------------");

            Component secondSibling = newSiblings.get(2);

            for (String rank : RANKS) {
                Replacement r = new Replacement(
                        Pattern.compile(Ranks.Old.get(rank)),
                        Ranks.New.get(rank),
                        Ranks.RoleColor.get(rank),
                        Ranks.NameColor.get(rank)
                );

                if (!(secondSibling instanceof MutableComponent)) continue;

                MutableComponent replaced = TextUtils.replaceTextInComponent(secondSibling, r.pattern, r.rolepill);

                // No changes were made, meaning it's not a guild chat message
                if (replaced.getString().equals(secondSibling.getString())) continue;

                // Replace the color of the blue role pill with the custom color-coded one
                newSiblings.set(2, replaced.setStyle(secondSibling.getStyle().withColor(r.rolecolor)));

                if (newSiblings.size() <= 5) break;

                // Replace the color of the username with the custom color-coded one
                Component fifthSibling = newSiblings.get(4);
                String fifthText = fifthSibling.getString();
                newSiblings.set(4, Component.literal(fifthText).setStyle(fifthSibling.getStyle().withColor(r.namecolor)));

                // If the username still has additional components, replace their colors too
                if (fifthText.endsWith(":") || newSiblings.size() <= 6) break;

                Component sixthSibling = newSiblings.get(6);
                String sixthText = sixthSibling.getString();
                newSiblings.set(6, Component.literal(sixthText).setStyle(sixthSibling.getStyle().withColor(r.namecolor)));

                if (sixthText.endsWith(":") || newSiblings.size() <= 7) break;

                Component seventhSibling = newSiblings.get(7);
                String seventhText = seventhSibling.getString();
                newSiblings.set(7, Component.literal(seventhText).setStyle(seventhSibling.getStyle().withColor(r.namecolor)));

                break;
            }
        }

        // Create a new message component with the modified siblings
        MutableComponent newMessage = Component.empty().setStyle(message.getStyle());
        for (Component sibling : newSiblings) {
            newMessage.append(sibling);
        }

        args.set(MESSAGE_IDX, newMessage);
    }
}