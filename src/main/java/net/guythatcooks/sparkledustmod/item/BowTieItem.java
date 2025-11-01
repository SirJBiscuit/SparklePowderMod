package net.guythatcooks.sparkledustmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class BowTieItem extends Item {

    public BowTieItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel level = (ServerLevel) player.level();
        CompoundTag nbt = target.getPersistentData();

        // Check if target is blessed
        if (!nbt.getBoolean("SparkleBlessedPet")) {
            player.displayClientMessage(
                    Component.literal("Only blessed pets can wear the magical bow tie!")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return InteractionResult.FAIL;
        }

        String petName = nbt.getString("SparkleGivenName");

        // Check if already wearing bow tie
        if (nbt.getBoolean("WearingBowTie")) {
            // Remove bow tie
            nbt.putBoolean("WearingBowTie", false);
            nbt.putBoolean("BowTieFloating", false);
            nbt.remove("BowTieOwnerUUID");

            // Give bow tie back
            if (!player.getInventory().add(new ItemStack(ModItems.BOWTIE.get()))) {
                player.drop(new ItemStack(ModItems.BOWTIE.get()), false);
            }

            // Sparkle particles
            Vec3 pos = target.position().add(0, target.getBbHeight() / 2, 0);
            for (int i = 0; i < 20; i++) {
                level.sendParticles(
                        ParticleTypes.POOF,
                        pos.x, pos.y, pos.z, 1,
                        (level.random.nextDouble() - 0.5) * 0.2,
                        level.random.nextDouble() * 0.2,
                        (level.random.nextDouble() - 0.5) * 0.2,
                        0.1
                );
            }

            level.playSound(null, target.blockPosition(),
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    SoundSource.PLAYERS, 1.0f, 0.8f);

            player.displayClientMessage(
                    Component.literal("Removed bow tie from ")
                            .withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal(petName)
                                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                            .append(Component.literal("! They will gently float down.")
                                    .withStyle(ChatFormatting.YELLOW)),
                    false
            );

            return InteractionResult.SUCCESS;
        } else {
            // Put bow tie on
            nbt.putBoolean("WearingBowTie", true);
            nbt.putBoolean("BowTieFloating", true);
            nbt.putString("BowTieOwnerUUID", player.getStringUUID());

            // Golden sparkle particles
            Vec3 pos = target.position().add(0, target.getBbHeight() / 2, 0);
            for (int i = 0; i < 30; i++) {
                double angle = (i / 30.0) * Math.PI * 2;
                double radius = 0.6;

                double x = pos.x + Math.cos(angle) * radius;
                double z = pos.z + Math.sin(angle) * radius;

                level.sendParticles(
                        ParticleTypes.END_ROD,
                        x, pos.y, z, 2,
                        0, 0.3, 0, 0.05
                );

                level.sendParticles(
                        ParticleTypes.FIREWORK,
                        x, pos.y, z, 1,
                        0, 0.2, 0, 0.05
                );
            }

            level.playSound(null, target.blockPosition(),
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    SoundSource.PLAYERS, 1.0f, 1.5f);

            level.playSound(null, target.blockPosition(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS, 1.0f, 1.8f);

            player.displayClientMessage(
                    Component.literal("✨ ")
                            .withStyle(ChatFormatting.GOLD)
                            .append(Component.literal(petName)
                                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                            .append(Component.literal(" is now wearing a magical bow tie! They'll float beside you! ✨")
                                    .withStyle(ChatFormatting.GOLD)),
                    false
            );

            if (!player.isCreative()) {
                stack.shrink(1);
            }

            return InteractionResult.SUCCESS;
        }
    }
}
