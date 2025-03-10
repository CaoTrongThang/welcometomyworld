package com.trongthang.welcometomyworld.screen;

import com.trongthang.welcometomyworld.WelcomeToMyWorld;
import com.trongthang.welcometomyworld.classes.tameablePacket.TameableEntityInterface;
import com.trongthang.welcometomyworld.classes.tameablePacket.UpdateMobStatPacket;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class MobUpgradeScreen extends Screen {
    public final TameableEntity tameableEntity;
    private final TameableEntityInterface entityInterface;
    private float rotationAngle = 0; // Current rotation angle of the mob

    private int statsBoxWidth = 220; // Width of the stats box
    private int statsBoxHeight = 140; // Height of the stats box

    private int buttonsOffset = -25;

    public MobUpgradeScreen(TameableEntity tameableEntity) {
        super(Text.of("Mob Upgrade Screen"));
        this.tameableEntity = tameableEntity;
        this.entityInterface = (TameableEntityInterface) tameableEntity;
    }

    @Override
    protected void init() {
        super.init();

        // Calculate the box dimensions
        int boxX = this.width / 2 + 40; // Stats box on the right side
        int boxY = this.height / 2 - statsBoxHeight / 2;

        int buttonSpacing = 25; // Space between buttons and stats

        // Add buttons inside the box
        addButton("+", "damage", 1, boxX + buttonsOffset, boxY + 20);
        addButton("+", "health", 1, boxX + buttonsOffset, boxY + 20 + buttonSpacing);
        addButton("+", "defense", 1, boxX + buttonsOffset, boxY + 20 + 2 * buttonSpacing);
        addButton("+", "speed", 1, boxX + buttonsOffset, boxY + 20 + 3 * buttonSpacing);


    }

    private void addButton(String symbol, String statName, int amount, int x, int y) {
        this.addDrawableChild(ButtonWidget.builder(
                        Text.of(symbol),
                        button -> {
                            // Send the packet to the server
                            UpdateMobStatPacket packet = new UpdateMobStatPacket(tameableEntity.getId(), statName, amount);
                            ClientPlayNetworking.send(WelcomeToMyWorld.UPDATE_MOB_STAT, packet.encode(new PacketByteBuf(Unpooled.buffer())));
                        })
                .position(x, y) // Position inside the box
                .size(20, 20) // Smaller square buttons
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Clear the screen with a background color
        this.renderBackground(context);

        rotationAngle += delta * 2; // Rotate 45 degrees per second
        if (rotationAngle >= 360) {
            rotationAngle -= 360; // Reset angle to avoid overflow
        }

        // Draw the mob model on the left
        drawMobModel(context);

        // Draw the mob name and available points at the top center
        drawTitleAndPoints(context);

        // Draw the stats box on the right
        drawStatsBox(context);

        // Draw experience and current level at the bottom center
        drawExperienceBar(context);

        // Call the super method to render other elements (like buttons)
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawMobModel(DrawContext context) {
        // Get the entity renderer for the mob
        EntityRenderDispatcher dispatcher = this.client.getEntityRenderDispatcher();

        // Get the mob's dimensions (size)
        float mobWidth = tameableEntity.getDimensions(tameableEntity.getPose()).width;
        float mobHeight = tameableEntity.getDimensions(tameableEntity.getPose()).height;

        // Define maximum dimensions for the mob on the screen
        float maxWidth = 80; // Max width in pixels
        float maxHeight = 120; // Max height in pixels

        // Calculate the scaling factor to fit within the max dimensions
        float scaleX = maxWidth / mobWidth;
        float scaleY = maxHeight / mobHeight;
        float scale = Math.min(scaleX, scaleY); // Use the smaller scale to maintain aspect ratio

        // Clamp the scale to ensure the mob isn't too small or too big
        scale = MathHelper.clamp(scale, 20, 40); // Minimum scale: 5, Maximum scale: 15

        // Calculate the position of the mob model relative to the stats box
        int statsBoxX = this.width / 2 + 40; // X position of the stats box
        int statsBoxY = this.height / 2 - statsBoxHeight / 2; // Y position of the stats box

        // Position the model horizontally based on its scaled width
        int modelX = statsBoxX - (int) (mobWidth * scale + 100); // Add padding based on scaled width

        // Position the model vertically based on its scaled height
        int modelY = statsBoxY + statsBoxHeight / 2 + (int) (mobHeight * scale / 2); // Center the model vertically

        // Push the matrix stack to isolate transformations
        MatrixStack matrices = context.getMatrices();
        matrices.push();

        // Translate the model to the desired position
        matrices.translate(modelX, modelY, 100); // Move the model to the desired position

        // Scale the model (maintain aspect ratio)
        matrices.scale(scale, scale, scale);

        // Rotate the model to face the camera
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180)); // Rotate horizontally to face the camera
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180)); // Flip the model vertically if needed

        // Apply continuous rotation around the Y-axis
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationAngle));

        // Render the mob model
        dispatcher.render(tameableEntity, 0, 0, 0, 0, 1, matrices, context.getVertexConsumers(), 15728880);

        // Pop the matrix stack to restore the original state
        matrices.pop();
    }

    private void drawTitleAndPoints(DrawContext context) {
        // Get the mob's name and available points
        String title = tameableEntity.getName().getString();
        int points = entityInterface.getPointAvailalble();
        String pointsLabel = " - Points: "; // Label for the points
        String pointsValue = String.valueOf(points); // The actual number of points

        // Calculate the position of the title and points
        int titleWidth = this.textRenderer.getWidth(title);
        int pointsLabelWidth = this.textRenderer.getWidth(pointsLabel);
        int pointsValueWidth = this.textRenderer.getWidth(pointsValue);

        // Total width of the combined text
        int totalWidth = titleWidth + pointsLabelWidth + pointsValueWidth;

        // Center the entire text horizontally
        int titleX = this.width / 2 - totalWidth / 2; // Start X for the title
        int titleY = this.height / 2 - statsBoxHeight / 2 - 40; // Above the stats box

        // Draw the mob's name in white
        context.drawTextWithShadow(this.textRenderer, title, titleX, titleY, 0xFFFFFF);

        // Draw the " - Points: " label in white
        int pointsLabelX = titleX + titleWidth; // Start X for the points label
        context.drawTextWithShadow(this.textRenderer, pointsLabel, pointsLabelX, titleY, 0xFFFFFF);

        // Determine the color for the points value
        int pointsColor = points > 0 ? 0xFF00FF00 : 0xFFFFFF; // Green if > 0, white otherwise

        // Draw the points value in the determined color
        int pointsValueX = pointsLabelX + pointsLabelWidth; // Start X for the points value
        context.drawTextWithShadow(this.textRenderer, pointsValue, pointsValueX, titleY, pointsColor);
    }

    private void drawStatsBox(DrawContext context) {
        int boxX = this.width / 2; // Stats box on the right side
        int boxY = this.height / 2 - statsBoxHeight / 2;

        // Draw the background box
        context.fill(boxX, boxY, boxX + statsBoxWidth, boxY + statsBoxHeight, 0xFF222222); // Dark gray background
        context.drawBorder(boxX, boxY, statsBoxWidth, statsBoxHeight, 0xFFFFFFFF); // White border

        // Format the attribute values to one decimal place


        String damageValue = String.format("%.1f", tameableEntity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE));
        String healthValue = String.format("%.1f", tameableEntity.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH));
        String defenseValue = String.format("%.1f", tameableEntity.getAttributeBaseValue(EntityAttributes.GENERIC_ARMOR));
        String speedValue = String.format("%.2f", tameableEntity.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));

        // Draw stats inside the box
        drawStatInBox(context, "Damage Level [" + damageValue + "]: ", entityInterface.getDamageLevel(), boxX + 40, boxY + 25);
        drawStatInBox(context, "Health Level [" + healthValue + "]: ", entityInterface.getHealthLevel(), boxX + 40, boxY + 50);
        drawStatInBox(context, "Defense Level [" + defenseValue + "]: ", entityInterface.getDefenseLevel(), boxX + 40, boxY + 75);
        drawStatInBox(context, "Speed Level [" + speedValue + "]: ", entityInterface.getSpeedLevel(), boxX + 40, boxY + 100);
    }

    private void drawStatInBox(DrawContext context, String label, Object value, int x, int y) {
        int labelColor = 0xFFFFFFFF; // Light gray for labels
        int valueColor = 0xFFFFD700; // Gold for values

        // Draw the label
        context.drawTextWithShadow(this.textRenderer, label, x, y, labelColor);

        // Draw the value
        String valueString = value.toString();
        int valueWidth = this.textRenderer.getWidth(valueString);
        context.drawTextWithShadow(this.textRenderer, valueString, x + this.textRenderer.getWidth(label), y, valueColor);
    }

    private void drawExperienceBar(DrawContext context) {
        // Calculate the position of the experience bar
        int barX = this.width / 2 - 100; // Center the bar horizontally
        int barY = this.height / 2 + statsBoxHeight / 2 + 40; // Below the stats box
        int barWidth = 200; // Total width of the bar
        int barHeight = 10; // Height of the bar

        // Get the current and required experience
        float currentExp = entityInterface.getCurrentLevelExp();
        float requiredExp = entityInterface.getNextLevelRequireExp();

        // Calculate the fill percentage (clamp between 0 and 1)
        float fillPercentage = MathHelper.clamp(currentExp / requiredExp, 0, 1);

        // Draw the background of the bar (dark gray)
        context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF222222); // Dark gray background

        // Draw the filled portion of the bar (green)
        int fillWidth = (int) (barWidth * fillPercentage); // Width of the filled portion
        context.fill(barX, barY, barX + fillWidth, barY + barHeight, 0xFF00FF00); // Green fill

        // Draw the experience text on top of the bar
        String expText = (int) currentExp + " / " + (int) requiredExp;
        int textWidth = this.textRenderer.getWidth(expText);
        int textX = barX + (barWidth - textWidth) / 2; // Center the text horizontally
        int textY = barY - 12; // Position the text above the bar

        context.drawTextWithShadow(this.textRenderer, expText, textX, textY, 0xFFFFFF); // White text
    }
}