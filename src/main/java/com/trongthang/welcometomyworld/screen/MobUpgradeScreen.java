package com.trongthang.welcometomyworld.screen;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.classes.tameablePacket.TameableEntityInterface;
import com.trongthang.welcometomyworld.classes.tameablePacket.UpdateMobStatPacket;
import io.netty.buffer.Unpooled;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.NotNull;

public class MobUpgradeScreen extends BaseOwoScreen<FlowLayout> {
    public final TameableEntity tameableEntity;
    private final TameableEntityInterface entityInterface;
    private float rotationAngle = 0;

    private LabelComponent pointsLabel;

    private LabelComponent damageLabel;
    private LabelComponent damageLevelLabel;
    private ButtonComponent damageBtn;

    private LabelComponent healthLabel;
    private LabelComponent healthLevelLabel;
    private ButtonComponent healthBtn;

    private LabelComponent defenseLabel;
    private LabelComponent defenseLevelLabel;
    private ButtonComponent defenseBtn;

    private LabelComponent speedLabel;
    private LabelComponent speedLevelLabel;
    private ButtonComponent speedBtn;

    private LabelComponent xpLabel;
    private FlowLayout xpBarFill;

    // Discord-style Dark Theme colors
    private static final int DISCORD_BG = 0xFF2B2D31; // Dark gray background
    private static final int DISCORD_BORDER = 0xFF1E1F22; // Darker border
    private static final int DISCORD_TEXT = 0xFFFFFFFF; // White text
    private static final int DISCORD_LEVEL_TEXT = 0xFFFEE75C; // Yellowish for levels

    public MobUpgradeScreen(TameableEntity tameableEntity) {
        super(Text.of("Let's Upgrade Your Companion!"));
        this.tameableEntity = tameableEntity;
        this.entityInterface = (TameableEntityInterface) tameableEntity;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        // A thick outer box to hold everything
        FlowLayout outerBox = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        outerBox.surface(Surface.flat(DISCORD_BG).and(Surface.outline(DISCORD_BORDER)))
                .padding(Insets.of(5))
                .verticalAlignment(VerticalAlignment.CENTER);

        // Header Title (We can put it on top of the outerbox)
        rootComponent.child(
                Components.label(tameableEntity.getName())
                        .shadow(true)
                        .margins(Insets.bottom(15)));

        // Left Panel - Mob Preview area
        FlowLayout leftPanel = Containers.verticalFlow(Sizing.fixed(160), Sizing.fixed(200));
        leftPanel.surface(Surface.flat(DISCORD_BG).and(Surface.outline(DISCORD_BORDER)))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.BOTTOM)
                .margins(Insets.right(5));

        pointsLabel = Components.label(Text.literal("Points: 0")).color(Color.ofRgb(DISCORD_TEXT)).shadow(true);

        // XP Bar directly under the mob in the left panel according to sketch
        FlowLayout xpBarContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());
        xpBarContainer.horizontalAlignment(HorizontalAlignment.CENTER).margins(Insets.vertical(10));

        xpLabel = Components.label(Text.literal("0 / 0")).color(Color.ofRgb(DISCORD_TEXT)).shadow(true);
        xpBarContainer.child(xpLabel.margins(Insets.bottom(2)));

        FlowLayout xpBarBackground = Containers.horizontalFlow(Sizing.fixed(140), Sizing.fixed(10));
        // Add padding of 1 so the fill respects the outline border!
        xpBarBackground.surface(Surface.flat(0xFF111111).and(Surface.outline(DISCORD_BORDER)))
                .padding(Insets.of(1));

        xpBarFill = Containers.horizontalFlow(Sizing.fixed(0), Sizing.fill(100));
        xpBarFill.surface(Surface.flat(0xFF2ECC71)); // Discord green

        xpBarBackground.child(xpBarFill);
        xpBarContainer.child(xpBarBackground);

        leftPanel.child(pointsLabel.margins(Insets.bottom(5)));
        leftPanel.child(xpBarContainer);

        // Right Panel - Stats Box
        FlowLayout rightPanel = Containers.verticalFlow(Sizing.fixed(220), Sizing.fixed(200));
        rightPanel.surface(Surface.flat(DISCORD_BG).and(Surface.outline(DISCORD_BORDER)))
                .padding(Insets.of(15))
                .verticalAlignment(VerticalAlignment.CENTER);

        // Damage Row (Attack)
        damageLabel = Components.label(Text.literal("")).color(Color.ofRgb(DISCORD_TEXT)).shadow(true);
        damageLevelLabel = Components.label(Text.literal("")).color(Color.ofRgb(DISCORD_LEVEL_TEXT)).shadow(true);
        damageBtn = Components.button(Text.literal("+"), button -> {
            UpdateMobStatPacket packet = new UpdateMobStatPacket(tameableEntity.getId(), "damage", 1);
            ClientPlayNetworking.send(WelcomeToMyWorld.UPDATE_MOB_STAT,
                    packet.encode(new PacketByteBuf(Unpooled.buffer())));
        });
        rightPanel.child(createStatRow("attack", damageLabel, damageLevelLabel, damageBtn));

        // Health Row
        healthLabel = Components.label(Text.literal("")).color(Color.ofRgb(DISCORD_TEXT)).shadow(true);
        healthLevelLabel = Components.label(Text.literal("")).color(Color.ofRgb(DISCORD_LEVEL_TEXT)).shadow(true);
        healthBtn = Components.button(Text.literal("+"), button -> {
            UpdateMobStatPacket packet = new UpdateMobStatPacket(tameableEntity.getId(), "health", 1);
            ClientPlayNetworking.send(WelcomeToMyWorld.UPDATE_MOB_STAT,
                    packet.encode(new PacketByteBuf(Unpooled.buffer())));
        });
        rightPanel.child(createStatRow("health", healthLabel, healthLevelLabel, healthBtn));

        // Defense Row
        defenseLabel = Components.label(Text.literal("")).color(Color.ofRgb(DISCORD_TEXT)).shadow(true);
        defenseLevelLabel = Components.label(Text.literal("")).color(Color.ofRgb(DISCORD_LEVEL_TEXT)).shadow(true);
        defenseBtn = Components.button(Text.literal("+"), button -> {
            UpdateMobStatPacket packet = new UpdateMobStatPacket(tameableEntity.getId(), "defense", 1);
            ClientPlayNetworking.send(WelcomeToMyWorld.UPDATE_MOB_STAT,
                    packet.encode(new PacketByteBuf(Unpooled.buffer())));
        });
        rightPanel.child(createStatRow("defense", defenseLabel, defenseLevelLabel, defenseBtn));

        // Speed Row
        speedLabel = Components.label(Text.literal("")).color(Color.ofRgb(DISCORD_TEXT)).shadow(true);
        speedLevelLabel = Components.label(Text.literal("")).color(Color.ofRgb(DISCORD_LEVEL_TEXT)).shadow(true);
        speedBtn = Components.button(Text.literal("+"), button -> {
            UpdateMobStatPacket packet = new UpdateMobStatPacket(tameableEntity.getId(), "speed", 1);
            ClientPlayNetworking.send(WelcomeToMyWorld.UPDATE_MOB_STAT,
                    packet.encode(new PacketByteBuf(Unpooled.buffer())));
        });
        rightPanel.child(createStatRow("speed", speedLabel, speedLevelLabel, speedBtn));

        outerBox.child(leftPanel);
        outerBox.child(rightPanel);
        rootComponent.child(outerBox);

        updateDynamicUI(); // Initialize values
    }

    private FlowLayout createStatRow(String iconName, LabelComponent nameLabel, LabelComponent levelLabel,
            ButtonComponent btn) {
        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.margins(Insets.bottom(15)); // spacing between rows

        // Icon Box (Placeholder for the PNGs)
        FlowLayout iconWrapper = Containers.horizontalFlow(Sizing.fixed(24), Sizing.fixed(24));
        iconWrapper.surface(Surface.flat(DISCORD_BG).and(Surface.outline(DISCORD_BORDER)));

        Identifier iconId = new Identifier(WelcomeToMyWorld.MOD_ID, "textures/gui/" + iconName + ".png");
        // Using texture component. Note: user needs to put raw 16x16 PNGs there!
        iconWrapper.child(Components.texture(iconId, 0, 0, 16, 16, 16, 16).sizing(Sizing.fixed(16), Sizing.fixed(16))
                .margins(Insets.of(4)));

        // Fixed sizing to prevent overflowing layout and clipping the button
        FlowLayout labelContainer = Containers.horizontalFlow(Sizing.fixed(125), Sizing.content());
        labelContainer.margins(Insets.left(10));
        labelContainer.child(nameLabel.margins(Insets.right(5)));
        labelContainer.child(levelLabel);

        row.child(iconWrapper);
        row.child(labelContainer);

        btn.sizing(Sizing.fixed(24), Sizing.fixed(24)); // Squarish button like bedrock
        row.child(btn);

        return row;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        updateDynamicUI();
        super.render(context, mouseX, mouseY, delta);

        rotationAngle += delta * 2;
        if (rotationAngle >= 360) {
            rotationAngle -= 360;
        }

        drawMobModel(context);
    }

    private void updateDynamicUI() {
        if (pointsLabel == null)
            return; // Before build is fully complete

        int points = entityInterface.getPointAvailalble();
        pointsLabel.text(Text.literal("Points: " + points));

        // Highlight green if points available, otherwise dark text
        if (points > 0) {
            pointsLabel.color(Color.ofRgb(0x2ECC71)); // Discord green
        } else {
            pointsLabel.color(Color.ofRgb(DISCORD_TEXT));
        }

        boolean hasPoints = points > 0;
        damageBtn.active = hasPoints;
        healthBtn.active = hasPoints;
        defenseBtn.active = hasPoints;
        speedBtn.active = hasPoints;

        // check if there's no generic attribute
        // NOTE: getAttributeValue(...) will throw if the attribute doesn't exist on
        // this entity,
        // so only query the value when hasAttribute(...) == true.

        if (tameableEntity.getAttributes().hasAttribute(EntityAttributes.GENERIC_ATTACK_DAMAGE)) {
            String damageValue = String.format("%.1f",
                    tameableEntity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE));
            damageLabel.text(Text.literal("Damage [" + damageValue + "]:"));
            damageLevelLabel.text(Text.literal("Lv" + entityInterface.getDamageLevel()));
        } else {
            damageLabel.text(Text.literal("Damage [N/A]:"));
            damageLevelLabel.text(Text.literal("Lv0"));
        }

        if (tameableEntity.getAttributes().hasAttribute(EntityAttributes.GENERIC_MAX_HEALTH)) {
            String healthValue = String.format("%.1f",
                    tameableEntity.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH));
            healthLabel.text(Text.literal("Health [" + healthValue + "]:"));
            healthLevelLabel.text(Text.literal("Lv" + entityInterface.getHealthLevel()));
        } else {
            healthLabel.text(Text.literal("Health [N/A]:"));
            healthLevelLabel.text(Text.literal("Lv0"));
        }

        if (tameableEntity.getAttributes().hasAttribute(EntityAttributes.GENERIC_ARMOR)) {
            String defenseValue = String.format("%.1f",
                    tameableEntity.getAttributeBaseValue(EntityAttributes.GENERIC_ARMOR));
            defenseLabel.text(Text.literal("Defense [" + defenseValue + "]:"));
            defenseLevelLabel.text(Text.literal("Lv" + entityInterface.getDefenseLevel()));
        } else {
            defenseLabel.text(Text.literal("Defense [N/A]:"));
            defenseLevelLabel.text(Text.literal("Lv0"));
        }

        if (tameableEntity.getAttributes().hasAttribute(EntityAttributes.GENERIC_MOVEMENT_SPEED)) {
            String speedValue = String.format("%.2f",
                    tameableEntity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
            speedLabel.text(Text.literal("Speed [" + speedValue + "]:"));
            speedLevelLabel.text(Text.literal("Lv" + entityInterface.getSpeedLevel()));
        } else {
            speedLabel.text(Text.literal("Speed [N/A]:"));
            speedLevelLabel.text(Text.literal("Lv0"));
        }

        float currentExp = entityInterface.getCurrentLevelExp();
        float requiredExp = entityInterface.getNextLevelRequireExp();
        float fillPercentage = requiredExp <= 0 ? 0 : MathHelper.clamp(currentExp / requiredExp, 0, 1);

        xpLabel.text(Text.literal((int) currentExp + " / " + (int) requiredExp));

        // Use 138 scaled width because we added padding 1 to the background 140 pixel
        // container
        xpBarFill.sizing(Sizing.fixed((int) (138 * fillPercentage)), Sizing.fill(100));
    }

    private void drawMobModel(DrawContext context) {
        EntityRenderDispatcher dispatcher = this.client.getEntityRenderDispatcher();
        float mobWidth = tameableEntity.getDimensions(tameableEntity.getPose()).width;
        float mobHeight = tameableEntity.getDimensions(tameableEntity.getPose()).height;

        float maxWidth = 80;
        float maxHeight = 100;
        float scaleX = maxWidth / mobWidth;
        float scaleY = maxHeight / mobHeight;
        float scale = Math.min(scaleX, scaleY);
        scale = MathHelper.clamp(scale, 20, 40);

        int modelX = this.width / 2 - 110;
        int modelY = this.height / 2 + 30; // Just slightly above points/xp

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(modelX, modelY, 100);
        matrices.scale(scale, scale, scale);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationAngle));

        dispatcher.render(tameableEntity, 0, 0, 0, 0, 1, matrices, context.getVertexConsumers(), 15728880);
        matrices.pop();
    }
}