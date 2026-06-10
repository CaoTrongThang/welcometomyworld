package com.trongthang.welcometomyworld.mixin.accessor;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClampedEntityAttribute.class)
public interface ClampedEntityAttributeAccessor {
    @Accessor("maxValue")
    double getMaxValue();

    @Accessor("maxValue")
    void setMaxValue(double value);
}
