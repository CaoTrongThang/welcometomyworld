package com.trongthang.welcometomyworld.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import com.google.common.collect.Multimap;
import java.util.Map;

public class WeaponStatsCommand {

    public static LiteralArgumentBuilder<ServerCommandSource> build() {
        return CommandManager.literal("weaponstats")
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
                        source.sendFeedback(() -> Text.literal("§eYou are not holding any item."), false);
                        return 1;
                    }

                    String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                    String itemName = stack.getName().getString();

                    source.sendFeedback(() -> Text.literal("§6=== Weapon Stats: §f" + itemName + " §7(" + itemId + ")"),
                            false);
                    WelcomeToMyWorld.LOGGER.info("=== Weapon Stats: {} ({}) ===", itemName, itemId);

                    boolean foundAny = false;

                    for (EquipmentSlot slot : EquipmentSlot.values()) {
                        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = stack
                                .getAttributeModifiers(slot);
                        if (modifiers.isEmpty())
                            continue;

                        String slotName = slot.getName();
                        source.sendFeedback(() -> Text.literal("§b[Slot: " + slotName + "]"), false);
                        WelcomeToMyWorld.LOGGER.info("  [Slot: {}]", slotName);

                        for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : modifiers.entries()) {
                            EntityAttribute attr = entry.getKey();
                            EntityAttributeModifier mod = entry.getValue();

                            Identifier attrId = Registries.ATTRIBUTE.getId(attr);
                            String attrIdStr = attrId != null ? attrId.toString() : "unknown";
                            String attrName = attr.getTranslationKey();
                            String opName = mod.getOperation().name();
                            double value = mod.getValue();
                            String uuid = mod.getId().toString();

                            MutableText feedbackLine = Text.literal("  §7" + attrName + " ");

                            MutableText idText = Text.literal("§8(" + attrIdStr + ") ");
                            idText.styled(style -> style
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, attrIdStr))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Text.literal("Click to copy ID"))));

                            feedbackLine.append(idText);
                            feedbackLine.append(Text.literal(String.format("§f%s %.4f §8[op=%s, uuid=%s]",
                                    opName.startsWith("MULTIPLY") ? "x" : "+", value, opName, uuid)));

                            source.sendFeedback(() -> feedbackLine, false);
                            WelcomeToMyWorld.LOGGER.info("  {} ({}) op={} value={} uuid={}", attrName, attrIdStr,
                                    opName, value, uuid);
                            foundAny = true;
                        }
                    }

                    if (!foundAny) {
                        source.sendFeedback(() -> Text.literal("§eNo attribute modifiers found on this item."), false);
                        WelcomeToMyWorld.LOGGER.info("  No attribute modifiers found.");
                    }

                    WelcomeToMyWorld.LOGGER.info("=== End of weapon stats ===");
                    source.sendFeedback(() -> Text.literal("§8(Full dump also written to server log)"), false);
                    return 1;
                });
    }
}
