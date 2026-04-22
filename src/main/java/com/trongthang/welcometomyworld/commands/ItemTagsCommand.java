package com.trongthang.welcometomyworld.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;

import java.util.List;
import java.util.stream.Collectors;

public class ItemTagsCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> build() {
        return CommandManager.literal("gettags")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    ServerPlayerEntity player = source.getPlayer();

                    if (player == null) {
                        source.sendError(Text.literal("This command must be run by a player."));
                        return 0;
                    }

                    ItemStack stack = player.getMainHandStack();
                    if (stack.isEmpty()) {
                        source.sendFeedback(() -> Text.literal("§e[Welcome To My World] You are not holding any item."),
                                false);
                        return 1;
                    }

                    List<String> tags = stack.getRegistryEntry().streamTags()
                            .map(tag -> "#" + tag.id().toString())
                            .collect(Collectors.toList());

                    String itemName = stack.getItem().getName().getString();

                    if (tags.isEmpty()) {
                        source.sendFeedback(
                                () -> Text.literal("§e[Welcome To My World] Item §6" + itemName + "§e has no tags."),
                                false);
                    } else {
                        MutableText feedback = Text.literal("§a[Welcome To My World] Item §6" + itemName + "§a tags: ");
                        for (int i = 0; i < tags.size(); i++) {
                            String tag = tags.get(i);
                            MutableText tagText = Text.literal("§7" + tag);
                            tagText.styled(style -> style
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, tag))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Text.literal("Click to copy"))));
                            feedback.append(tagText);
                            if (i < tags.size() - 1) {
                                feedback.append(Text.literal("§8, "));
                            }
                        }
                        source.sendFeedback(() -> (Text) feedback, false);
                    }

                    return 1;
                });
    }
}
