package net.guythatcooks.sparkledustmod.item;

import com.mojang.logging.LogUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.List;

public class CustomWandEffect extends Item {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int COOLDOWN_TICKS = 40; // 2 seconds
    private static final double RANGE = 10.0; // Range in blocks

    public CustomWandEffect(Properties properties) {
        super(properties);
    }

    // Right-click on entity
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player,
                                                  LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) player.level();

        // Particle beam from player to target
        spawnParticleBeam(serverLevel, player, target);

        // Apply sparkle effect
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
        target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 1));

        // Play sound
        serverLevel.playSound(null, target.blockPosition(),
                SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS, 1.0f, 1.5f);

        // Particle burst at target
        spawnParticleBurst(serverLevel, target.position().add(0, target.getBbHeight() / 2, 0), 30);

        // Message
        player.displayClientMessage(Component.literal("✨ Sparkle Magic Applied!"), true);

        // Damage and cooldown
        stack.hurtAndBreak(1, player, player.getEquipmentSlotForItem(stack));
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResult.SUCCESS;
    }

    // Right-click in air (area effect)
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        ServerLevel serverLevel = (ServerLevel) level;

        // Find nearby entities
        AABB searchBox = player.getBoundingBox().inflate(RANGE);
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, searchBox,
                entity -> entity != player && entity.isAlive());

        if (nearbyEntities.isEmpty()) {
            // No targets - spawn sparkle explosion from hand
            Vec3 handPos = player.getEyePosition().add(player.getLookAngle().scale(0.5));
            spawnSparkleExplosion(serverLevel, handPos);

            player.displayClientMessage(Component.literal("✨ Sparkle Burst!"), true);
        } else {
            // Hit all nearby entities
            for (LivingEntity target : nearbyEntities) {
                // Particle beam
                spawnParticleBeam(serverLevel, player, target);

                // Effects
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));

                // Small burst
                spawnParticleBurst(serverLevel,
                        target.position().add(0, target.getBbHeight() / 2, 0), 15);
            }

            player.displayClientMessage(
                    Component.literal("✨ Hit " + nearbyEntities.size() + " entities!"), true);
        }

        // Sound
        serverLevel.playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_LEVELUP,
                SoundSource.PLAYERS, 0.8f, 1.8f);

        // Damage and cooldown
        stack.hurtAndBreak(1, player, player.getEquipmentSlotForItem(stack));
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        return InteractionResultHolder.success(stack);
    }

    // Helper: Particle beam from player to target
    private void spawnParticleBeam(ServerLevel level, Player player, LivingEntity target) {
        Vec3 start = player.getEyePosition();
        Vec3 end = target.position().add(0, target.getBbHeight() / 2, 0);
        Vec3 direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);

        int particleCount = (int) (distance * 15);
        for (int i = 0; i < particleCount; i++) {
            double progress = i / (double) particleCount;
            Vec3 pos = start.add(direction.scale(distance * progress));

            level.sendParticles(
                    ParticleTypes.ENCHANT,
                    pos.x, pos.y, pos.z,
                    1,
                    0.05, 0.05, 0.05,
                    0.02
            );
        }
    }

    // Helper: Particle burst at location
    private void spawnParticleBurst(ServerLevel level, Vec3 pos, int count) {
        for (int i = 0; i < count; i++) {
            double xSpeed = (level.random.nextDouble() - 0.5) * 0.3;
            double ySpeed = (level.random.nextDouble() - 0.5) * 0.3;
            double zSpeed = (level.random.nextDouble() - 0.5) * 0.3;

            level.sendParticles(
                    ParticleTypes.FIREWORK,
                    pos.x, pos.y, pos.z,
                    1,
                    xSpeed, ySpeed, zSpeed,
                    0.15
            );
        }
    }

    // Helper: Sparkle explosion outward
    private void spawnSparkleExplosion(ServerLevel level, Vec3 center) {
        // Ring particles
        for (int i = 0; i < 40; i++) {
            double angle = (i / 40.0) * Math.PI * 2;
            double radius = 1.5;
            double xSpeed = Math.cos(angle) * 0.3;
            double zSpeed = Math.sin(angle) * 0.3;

            level.sendParticles(
                    ParticleTypes.ENCHANT,
                    center.x, center.y, center.z,
                    1,
                    xSpeed, 0.0, zSpeed,
                    0.2
            );
        }

        // Upward sparkles
        for (int i = 0; i < 20; i++) {
            double xSpeed = (level.random.nextDouble() - 0.5) * 0.4;
            double ySpeed = level.random.nextDouble() * 0.5;
            double zSpeed = (level.random.nextDouble() - 0.5) * 0.4;

            level.sendParticles(
                    ParticleTypes.FIREWORK,
                    center.x, center.y, center.z,
                    1,
                    xSpeed, ySpeed, zSpeed,
                    0.2
            );
        }
    }

    // Show cooldown bar
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - (float)stack.getDamageValue() * 13.0F / (float)stack.getMaxDamage());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFF00FF; // Magenta color
    }
}
