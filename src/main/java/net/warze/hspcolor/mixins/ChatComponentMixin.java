package net.warze.hspcolor.mixins;

import net.warze.hspcolor.utils.LoggerUtils;
import net.warze.hspcolor.utils.MCServerUtils;
import net.warze.hspcolor.utils.Ranks;
import net.warze.hspcolor.utils.Replacement;
import net.warze.hspcolor.utils.TextUtils;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
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

        List<Map.Entry<String, Integer>> colorReplacements = List.of(
            Map.entry("#D4448C", 0xE985F7), // HERO+
            Map.entry("#FDDD5C", 0xB8B8B8), // Bomb bell
            Map.entry("#F3E6B2", 0xE6DA5E), // Bomb bell
            Map.entry("#A0C84B", 0xDFDAB7), // 1 time bomb
            Map.entry("#FFD750", 0xFFF7F2), // 1 time bomb
            Map.entry("#BD45FF", 0x8684FA), // shout
            Map.entry("#FAD9F7", 0xD8D8FA)  // shout
        );

        for (int i = 0; i < siblings.size(); i++) {
            Component sibling = siblings.get(i);
            if (sibling.getStyle().getColor() != null) {
                String color = sibling.getStyle().getColor().toString();
                // LoggerUtils.info("Color: " + color);
                for (var entry : colorReplacements) {
                    if (color.equalsIgnoreCase(entry.getKey())) {
                        siblings.set(i, Component.literal(sibling.getString())
                            .setStyle(sibling.getStyle().withColor(entry.getValue())));
                        break;
                    }
                }
            }
        }

        if (siblings.size() > 4 && siblings.get(2) instanceof MutableComponent second) {
            for (String rank : RANKS) {
                Replacement r = new Replacement(
                    Pattern.compile(Ranks.Old.get(rank)),
                    Ranks.New.get(rank),
                    Ranks.RoleColor.get(rank),
                    Ranks.NameColor.get(rank)
                );
                
                MutableComponent replaced = TextUtils.replaceTextInComponent(second, r.pattern, r.rolepill);

                // No changes were made, meaning it's not a guild chat message
                if (replaced == second) continue;

                // Replace the color of the blue role pill with the custom color-coded one 
                replaced.setStyle(second.getStyle()).withColor(r.rolecolor);
                siblings.set(2, replaced);
                
                if (siblings.size() <= 5) break;
                
                // Replace the color of the username with the custom color-coded one
                String fifthText = siblings.get(5).getString();
                siblings.set(5, Component.literal(fifthText).setStyle(siblings.get(5).getStyle()).withColor(r.namecolor));
                
                // If the username still has additional components, replace their colors too
                if (fifthText.endsWith(":") || siblings.size() <= 6) break;
            
                String sixthText = siblings.get(6).getString();
                siblings.set(6, Component.literal(sixthText).setStyle(siblings.get(6).getStyle()).withColor(r.namecolor));
            
                if (sixthText.endsWith(":") || siblings.size() <= 7) break;
            
                String seventhText = siblings.get(7).getString();
                siblings.set(7, Component.literal(seventhText).setStyle(siblings.get(7).getStyle()).withColor(r.namecolor));
            
                break;
            }
        }

        args.set(MESSAGE_IDX, message);
    }
}