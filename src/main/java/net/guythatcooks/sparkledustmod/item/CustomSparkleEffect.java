package net.guythatcooks.sparkledustmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class CustomSparkleEffect extends Item {
    private static final double TARGET_HEIGHT = 35.0;

    public CustomSparkleEffect(Properties properties) {
        super(properties);
    }


    private boolean hasCustomName(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_NAME);
    }

    private boolean isTamedOrFed(LivingEntity entity) {
        if (entity instanceof TamableAnimal tameable) {
            return tameable.isTame();
        }

        if (entity instanceof Animal animal) {
            CompoundTag nbt = animal.getPersistentData();
            if (nbt.getBoolean("HasBeenFed")) {
                return true;
            }

            if (animal.isInLove()) {
                nbt.putBoolean("HasBeenFed", true);
                return true;
            }

            return animal.getAge() == 0 && animal.canFallInLove();
        }

        return false;
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, Player player,
                                                           @NotNull LivingEntity target, @NotNull InteractionHand hand) {
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) player.level();
        spawnSparkleEffect(serverLevel, target);

        CompoundTag nbt = target.getPersistentData();
        if (nbt.getBoolean("SparkleBlessedPet")) {
            removeBlessing(target, player, serverLevel);
            applyControlledLevitation(serverLevel, target, player);

            if (!player.isCreative()) {
                stack.shrink(1);
            }

            return InteractionResult.SUCCESS;
        }

        if (target instanceof Player) {
            player.displayClientMessage(
                    Component.literal("Cannot use on players!")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return InteractionResult.FAIL;
        }

        boolean isAnimal = target instanceof Animal || target instanceof TamableAnimal;

        if (!isAnimal) {
            player.displayClientMessage(
                    Component.literal("This creature floats to the sky!")
                            .withStyle(ChatFormatting.LIGHT_PURPLE),
                    true
            );

            applyControlledLevitation(serverLevel, target, player);

            if (!player.isCreative()) {
                stack.shrink(1);
            }

            return InteractionResult.SUCCESS;
        }

        if (!hasCustomName(stack)) {
            player.displayClientMessage(
                    Component.literal("Unnamed dust! Sending to the sky...")
                            .withStyle(ChatFormatting.YELLOW),
                    true
            );

            applyControlledLevitation(serverLevel, target, player);

            if (!player.isCreative()) {
                stack.shrink(1);
            }

            return InteractionResult.SUCCESS;
        }

        if (!isTamedOrFed(target)) {
            player.displayClientMessage(
                    Component.literal("This animal hasn't been tamed or fed yet!")
                            .withStyle(ChatFormatting.RED)
                            .append(Component.literal(" Sending to the sky...")
                                    .withStyle(ChatFormatting.LIGHT_PURPLE)),
                    true
            );

            applyControlledLevitation(serverLevel, target, player);

            if (!player.isCreative()) {
                stack.shrink(1);
            }

            return InteractionResult.SUCCESS;
        }

        applyBlessing(stack, target, player, serverLevel);

        if (!player.isCreative()) {
            stack.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }

    private void applyBlessing(ItemStack stack, LivingEntity target, Player player, ServerLevel level) {
        String dustName = stack.getHoverName().getString();

        Component fancyName = Component.literal("✨ ")
                .withStyle(ChatFormatting.WHITE)
                .append(Component.literal(dustName)
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .append(Component.literal(" ✨")
                        .withStyle(ChatFormatting.WHITE));

        target.setCustomName(fancyName);
        target.setCustomNameVisible(true);
        target.setInvulnerable(true);

        target.addEffect(new MobEffectInstance(
                MobEffects.GLOWING,
                Integer.MAX_VALUE,
                0, false, false, true
        ));

        target.addEffect(new MobEffectInstance(
                MobEffects.REGENERATION,
                Integer.MAX_VALUE,
                4,
                false, false, true
        ));

        target.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                Integer.MAX_VALUE,
                4,
                false, false, true
        ));

        target.addEffect(new MobEffectInstance(
                MobEffects.FIRE_RESISTANCE,
                Integer.MAX_VALUE,
                0,
                false, false, true
        ));

        CompoundTag nbt = target.getPersistentData();
        nbt.putBoolean("SparkleBlessedPet", true);
        nbt.putString("SparkleGivenName", dustName);

        spawnBlessedParticles(level, target);

        level.playSound(null, target.blockPosition(),
                SoundEvents.PLAYER_LEVELUP,
                SoundSource.PLAYERS, 1.5f, 1.2f);

        level.playSound(null, target.blockPosition(),
                SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS, 1.0f, 1.8f);

        player.displayClientMessage(
                Component.literal("✨ Blessed ")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(dustName)
                                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                        .append(Component.literal(" with eternal protection! ✨")
                                .withStyle(ChatFormatting.GOLD)),
                false
        );
    }

    private void removeBlessing(LivingEntity target, Player player, ServerLevel level) {
        String oldName = target.getPersistentData().getString("SparkleGivenName");

        target.removeEffect(MobEffects.GLOWING);
        target.removeEffect(MobEffects.REGENERATION);
        target.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        target.removeEffect(MobEffects.FIRE_RESISTANCE);

        target.setInvulnerable(false);

        target.setCustomName(null);
        target.setCustomNameVisible(false);

        CompoundTag nbt = target.getPersistentData();
        nbt.putBoolean("SparkleBlessedPet", false);
        nbt.remove("SparkleGivenName");

        spawnUnblessedParticles(level, target);

        level.playSound(null, target.blockPosition(),
                SoundEvents.WITHER_SPAWN,
                SoundSource.PLAYERS, 0.5f, 2.0f);

        player.displayClientMessage(
                Component.literal("💔 Removed blessing from ")
                        .withStyle(ChatFormatting.RED)
                        .append(Component.literal(oldName)
                                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
                        .append(Component.literal("! Now floating away... 💔")
                                .withStyle(ChatFormatting.RED)),
                false
        );
    }

    private void applyControlledLevitation(ServerLevel level, LivingEntity target, Player player) {
        CompoundTag nbt = target.getPersistentData();
        nbt.putDouble("SparkleStartY", target.getY());
        nbt.putDouble("SparkleTargetY", target.getY() + TARGET_HEIGHT);
        nbt.putBoolean("SparkleLevitating", true);

        target.addEffect(new MobEffectInstance(
                MobEffects.LEVITATION,
                20 * 60 * 10,
                0,
                false, true, true
        ));

        target.addEffect(new MobEffectInstance(
                MobEffects.GLOWING,
                Integer.MAX_VALUE,
                0,
                false, false, true
        ));

        Vec3 pos = target.position().add(0, target.getBbHeight() / 2, 0);
        for (int i = 0; i < 50; i++) {
            double angle = (i / 50.0) * Math.PI * 4;
            double radius = 0.5;
            double height = i * 0.1;

            double x = pos.x + Math.cos(angle) * radius;
            double y = pos.y + height;
            double z = pos.z + Math.sin(angle) * radius;

            level.sendParticles(
                    ParticleTypes.END_ROD,
                    x, y, z, 1,
                    0, 0.1, 0, 0.02
            );
        }

        level.playSound(null, target.blockPosition(),
                SoundEvents.ENDER_DRAGON_FLAP,
                SoundSource.PLAYERS, 0.8f, 1.5f);

        player.displayClientMessage(
                Component.literal("✨ Floating to the sky... ✨")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC),
                true
        );
    }

    private void spawnSparkleEffect(ServerLevel level, LivingEntity target) {
        Vec3 center = target.position().add(0, target.getBbHeight() / 2, 0);

        for (int i = 0; i < 20; i++) {
            double angle = (i / 20.0) * Math.PI * 2;
            double radius = target.getBbWidth() * 0.6;

            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;

            level.sendParticles(
                    ParticleTypes.ENCHANT,
                    x, center.y, z, 1,
                    0, 0.2, 0, 0.05
            );
        }
    }

    private void spawnBlessedParticles(ServerLevel level, LivingEntity target) {
        Vec3 center = target.position().add(0, target.getBbHeight() / 2, 0);

        for (int ring = 0; ring < 3; ring++) {
            double ringHeight = center.y + (ring - 1) * 0.5;

            for (int i = 0; i < 30; i++) {
                double angle = (i / 30.0) * Math.PI * 2;
                double radius = (ring + 1) * 0.5;

                double x = center.x + Math.cos(angle) * radius;
                double z = center.z + Math.sin(angle) * radius;

                level.sendParticles(
                        ParticleTypes.FIREWORK,
                        x, ringHeight, z, 1,
                        0, 0.3, 0, 0.1
                );

                level.sendParticles(
                        ParticleTypes.ENCHANT,
                        x, ringHeight, z, 2,
                        0.1, 0.1, 0.1, 0.05
                );
            }
        }

        for (int i = 0; i < 40; i++) {
            double xSpeed = (level.random.nextDouble() - 0.5) * 0.3;
            double ySpeed = level.random.nextDouble() * 0.5 + 0.2;
            double zSpeed = (level.random.nextDouble() - 0.5) * 0.3;

            level.sendParticles(
                    ParticleTypes.TOTEM_OF_UNDYING,
                    center.x, center.y, center.z, 1,
                    xSpeed, ySpeed, zSpeed, 0.2
            );
        }
    }

    private void spawnUnblessedParticles(ServerLevel level, LivingEntity target) {
        Vec3 center = target.position().add(0, target.getBbHeight() / 2, 0);

        for (int i = 0; i < 30; i++) {
            double xSpeed = (level.random.nextDouble() - 0.5) * 0.2;
            double ySpeed = level.random.nextDouble() * 0.3;
            double zSpeed = (level.random.nextDouble() - 0.5) * 0.2;

            level.sendParticles(
                    ParticleTypes.SMOKE,
                    center.x, center.y, center.z, 1,
                    xSpeed, ySpeed, zSpeed, 0.1
            );
        }

        for (int i = 0; i < 20; i++) {
            double xSpeed = (level.random.nextDouble() - 0.5) * 0.3;
            double ySpeed = -level.random.nextDouble() * 0.2;
            double zSpeed = (level.random.nextDouble() - 0.5) * 0.3;

            level.sendParticles(
                    ParticleTypes.SOUL,
                    center.x, center.y, center.z, 1,
                    xSpeed, ySpeed, zSpeed, 0.1
            );
        }
    }
}
