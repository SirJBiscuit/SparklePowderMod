package net.guythatcooks.sparkledustmod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber
public class LevitationEventHandler {

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        // Only process LivingEntities
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (entity.level().isClientSide) return;

        CompoundTag nbt = entity.getPersistentData();

        // Check if entity is sparkle levitating
        if (nbt.getBoolean("SparkleLevitating")) {

            // Check if entity is leashed
            if (entity instanceof Mob mob && mob.isLeashed()) {
                // Remove levitation when leashed
                entity.removeEffect(MobEffects.LEVITATION);
                nbt.putBoolean("SparkleLevitating", false);
                nbt.remove("SparkleStartY");
                nbt.remove("SparkleTargetY");
                return;
            }

            // Check if reached target height
            double targetY = nbt.getDouble("SparkleTargetY");
            if (entity.getY() >= targetY) {
                // Stop levitation at target height
                entity.removeEffect(MobEffects.LEVITATION);

                // Apply slow falling to keep them there
                entity.addEffect(new MobEffectInstance(
                        MobEffects.SLOW_FALLING,
                        Integer.MAX_VALUE,
                        0,
                        false, false, true
                ));
            }
        }
    }
}
