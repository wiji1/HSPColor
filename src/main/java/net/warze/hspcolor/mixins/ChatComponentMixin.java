package net.warze.hspcolor.mixins;

import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.w3c.dom.Text;

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

    private boolean isProcessing = false;

    @Inject(method = "addMessage*", at = @At("HEAD"), cancellable = true)
    private void onAddMessage(Component message, CallbackInfo ci) {
        if (isProcessing) return;
        if (!MCServerUtils.isWynnCraft()) return;

        List<Component> siblings = message.getSiblings();
        List<Component> newSiblings = new ArrayList<>(siblings.size());

        if (siblings.isEmpty()) return;

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
            newSiblings.add(TextUtils.recolorComponent(sibling, colorReplacements));
        }

        Component prefix = siblings.getFirst();

        if ((prefix.getString().contains(GUILD_START) || prefix.getString().contains(GUILD_CONT)) && siblings.size() > 4) {
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

                Component nicknameSibling = null;
                int nicknameIndex = -1;

                Component usernameSibling = null;
                int usernameIndex = -1;

                for(int i = 0; i < siblings.size(); i++) {
                    Component sibling = siblings.get(i);

                    if (TextUtils.messageHasNickHoverDeep(sibling) && nicknameSibling == null) {
                        nicknameSibling = sibling;
                        nicknameIndex = i;
                        continue;
                    }

                    if (sibling.getString().contains(":") && i != nicknameIndex) {
                        usernameSibling = sibling;
                        usernameIndex = i;

                        break;
                    }
                }

                if (nicknameSibling != null && (usernameSibling == null || nicknameIndex < usernameIndex)) {
                    Component hoverComponent = TextUtils.GetSiblingWithHover(nicknameSibling);
                    Style nicknameStyle = hoverComponent == null ? nicknameSibling.getStyle() : hoverComponent.getStyle();

                    newSiblings.set(nicknameIndex, Component.literal(nicknameSibling.getString()).setStyle(nicknameStyle.withColor(r.namecolor)));

                    boolean isPrefix = nicknameSibling.getString().endsWith("/");
                    if (isPrefix) {
                        Component postfix = newSiblings.get(nicknameIndex + 1);
                        newSiblings.set(nicknameIndex + 1, Component.literal(postfix.getString()).setStyle(postfix.getStyle().withColor(r.namecolor)));
                    }
                }

                if (usernameSibling != null) newSiblings.set(usernameIndex, Component.literal(usernameSibling.getString()).setStyle(usernameSibling.getStyle().withColor(r.namecolor)));

                break;
            }
        }
//
        // Create a new message component with the modified siblings
        MutableComponent newMessage = Component.empty().setStyle(message.getStyle());
        for (Component sibling : newSiblings) {
            newMessage.append(sibling);
        }

        ci.cancel();
        isProcessing = true;
        try {
            ((ChatComponent)(Object)this).addMessage(newMessage);
        } finally {
            isProcessing = false;
        }
    }
}