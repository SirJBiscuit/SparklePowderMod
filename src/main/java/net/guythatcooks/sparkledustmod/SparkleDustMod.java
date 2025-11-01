package net.guythatcooks.sparkledustmod;

import net.guythatcooks.sparkledustmod.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.UUID;

@Mod(SparkleDustMod.MODID)
public class SparkleDustMod {
    public static final String MODID = "sparkledustmod";
    private static final double TARGET_HEIGHT = 35.0;
    private static final double FOLLOW_DISTANCE = 5.0;
    private static final double FOLLOW_HEIGHT = 5.0;
    private static final double MOVE_SPEED = 0.15;

    public SparkleDustMod(IEventBus modEventBus) {
        ModItems.register(modEventBus);
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.SPARKLEDUST);
            event.accept(ModItems.GUIDE_CRYSTAL);
            event.accept(ModItems.SPARKLE_WAND);
            event.accept(ModItems.BOWTIE);
        }
    }

    @EventBusSubscriber(modid = MODID)
    public static class LevitationEvents {

        @SubscribeEvent
        public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
            Player player = event.getEntity();
            Entity target = event.getTarget();
            ItemStack stack = event.getItemStack();

            if (!stack.is(Items.LEAD)) {
                return;
            }

            if (!(target instanceof LivingEntity living)) {
                return;
            }

            CompoundTag nbt = living.getPersistentData();

            if (!nbt.getBoolean("SparkleBlessedPet")) {
                return;
            }

            if (player.isShiftKeyDown()) {
                if (!player.level().isClientSide) {
                    toggleFloatingMode(living, player);
                }
                event.setCanceled(true);
            } else {
                boolean floatingEnabled = nbt.getBoolean("FloatingEnabled");

                if (floatingEnabled) {
                    if (!player.level().isClientSide) {
                        makeBlessedPetFloat((ServerLevel) player.level(), living, player);

                        if (!player.isCreative()) {
                            stack.shrink(1);
                        }
                    }
                    event.setCanceled(true);
                } else {
                    if (!player.level().isClientSide) {
                        player.displayClientMessage(
                                Component.literal("✨ Leashing ")
                                        .withStyle(ChatFormatting.GOLD)
                                        .append(Component.literal(nbt.getString("SparkleGivenName"))
                                                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                                        .append(Component.literal("! (Shift + Right-Click to enable floating)")
                                                .withStyle(ChatFormatting.GRAY)),
                                true
                        );
                    }
                }
            }
        }

        private static void toggleFloatingMode(LivingEntity living, Player player) {
            CompoundTag nbt = living.getPersistentData();
            boolean currentlyEnabled = nbt.getBoolean("FloatingEnabled");
            String petName = nbt.getString("SparkleGivenName");

            nbt.putBoolean("FloatingEnabled", !currentlyEnabled);

            if (!currentlyEnabled) {
                player.displayClientMessage(
                        Component.literal("✨ Floating ENABLED for ")
                                .withStyle(ChatFormatting.GREEN)
                                .append(Component.literal(petName)
                                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                                .append(Component.literal("! Right-click with lead to make them float! ✨")
                                        .withStyle(ChatFormatting.GREEN)),
                        false
                );

                if (living.level() instanceof ServerLevel level) {
                    Vec3 pos = living.position().add(0, living.getBbHeight() / 2, 0);
                    for (int i = 0; i < 30; i++) {
                        double angle = (i / 30.0) * Math.PI * 2;
                        double radius = 0.8;

                        double x = pos.x + Math.cos(angle) * radius;
                        double z = pos.z + Math.sin(angle) * radius;

                        level.sendParticles(
                                ParticleTypes.HAPPY_VILLAGER,
                                x, pos.y, z, 2,
                                0, 0.2, 0, 0.05
                        );
                    }

                    level.playSound(null, living.blockPosition(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP,
                            SoundSource.PLAYERS, 1.0f, 1.5f);
                }
            } else {
                player.displayClientMessage(
                        Component.literal("⛔ Floating DISABLED for ")
                                .withStyle(ChatFormatting.RED)
                                .append(Component.literal(petName)
                                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                                .append(Component.literal("! Can now be leashed normally. ⛔")
                                        .withStyle(ChatFormatting.RED)),
                        false
                );

                if (living.level() instanceof ServerLevel level) {
                    Vec3 pos = living.position().add(0, living.getBbHeight() / 2, 0);
                    for (int i = 0; i < 30; i++) {
                        double angle = (i / 30.0) * Math.PI * 2;
                        double radius = 0.8;

                        double x = pos.x + Math.cos(angle) * radius;
                        double z = pos.z + Math.sin(angle) * radius;

                        level.sendParticles(
                                ParticleTypes.ANGRY_VILLAGER,
                                x, pos.y, z, 2,
                                0, 0.2, 0, 0.05
                        );
                    }

                    level.playSound(null, living.blockPosition(),
                            SoundEvents.FIRE_EXTINGUISH,
                            SoundSource.PLAYERS, 0.5f, 1.5f);
                }
            }
        }

        private static void makeBlessedPetFloat(ServerLevel level, LivingEntity target, Player player) {
            String petName = target.getPersistentData().getString("SparkleGivenName");

            CompoundTag nbt = target.getPersistentData();
            nbt.putDouble("SparkleStartY", target.getY());
            nbt.putDouble("SparkleTargetY", target.getY() + TARGET_HEIGHT);
            nbt.putBoolean("SparkleLevitating", true);
            nbt.putBoolean("BlessedFloating", true);

            target.addEffect(new MobEffectInstance(
                    MobEffects.LEVITATION,
                    20 * 60 * 10,
                    0,
                    false, true, true
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
                        ParticleTypes.FIREWORK,
                        x, y, z, 1,
                        0, 0.1, 0, 0.02
                );

                level.sendParticles(
                        ParticleTypes.ENCHANT,
                        x, y, z, 1,
                        0, 0.1, 0, 0.02
                );
            }

            level.playSound(null, target.blockPosition(),
                    SoundEvents.PLAYER_LEVELUP,
                    SoundSource.PLAYERS, 1.0f, 1.8f);

            player.displayClientMessage(
                    Component.literal("✨ ")
                            .withStyle(ChatFormatting.GOLD)
                            .append(Component.literal(petName)
                                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                            .append(Component.literal(" ascends while keeping their blessing! ✨")
                                    .withStyle(ChatFormatting.GOLD)),
                    false
            );
        }

        @SubscribeEvent
        public static void onEntityDamage(LivingIncomingDamageEvent event) {
            LivingEntity entity = event.getEntity();
            CompoundTag nbt = entity.getPersistentData();

            if (nbt.getBoolean("SparkleBlessedPet")) {
                event.setCanceled(true);

                if (entity.getHealth() < entity.getMaxHealth()) {
                    entity.setHealth(entity.getMaxHealth());
                }
            }
        }

        @SubscribeEvent
        public static void onServerTick(ServerTickEvent.Post event) {
            for (ServerLevel level : event.getServer().getAllLevels()) {
                for (Entity entity : level.getAllEntities()) {
                    if (!(entity instanceof LivingEntity living)) {
                        continue;
                    }

                    CompoundTag nbt = living.getPersistentData();

                    // Blessed pet maintenance
                    if (nbt.getBoolean("SparkleBlessedPet")) {
                        if (living.getHealth() < living.getMaxHealth()) {
                            living.setHealth(living.getMaxHealth());
                        }

                        if (living.isOnFire()) {
                            living.clearFire();
                        }

                        if (living.hasEffect(MobEffects.POISON)) {
                            living.removeEffect(MobEffects.POISON);
                        }
                        if (living.hasEffect(MobEffects.WITHER)) {
                            living.removeEffect(MobEffects.WITHER);
                        }
                        if (living.hasEffect(MobEffects.HARM)) {
                            living.removeEffect(MobEffects.HARM);
                        }
                    }

                    // Bow tie floating companion logic
                    if (nbt.getBoolean("WearingBowTie") && nbt.getBoolean("BowTieFloating")) {
                        String ownerUUID = nbt.getString("BowTieOwnerUUID");

                        if (!ownerUUID.isEmpty()) {
                            try {
                                UUID uuid = UUID.fromString(ownerUUID);
                                ServerPlayer owner = level.getServer().getPlayerList().getPlayer(uuid);

                                if (owner != null && owner.level() == living.level()) {
                                    // Calculate target position behind and above player
                                    Vec3 ownerPos = owner.position();
                                    Vec3 ownerLook = owner.getLookAngle();

                                    // Position 5 blocks behind and 5 blocks up
                                    Vec3 targetPos = ownerPos
                                            .subtract(ownerLook.x * FOLLOW_DISTANCE, 0, ownerLook.z * FOLLOW_DISTANCE)
                                            .add(0, FOLLOW_HEIGHT, 0);

                                    Vec3 currentPos = living.position();
                                    Vec3 direction = targetPos.subtract(currentPos);
                                    double distance = direction.length();

                                    // Only move if further than 2 blocks from target
                                    if (distance > 2.0) {
                                        Vec3 motion = direction.normalize().scale(MOVE_SPEED);
                                        living.setDeltaMovement(motion);

                                        // Make them face the player
                                        double dx = ownerPos.x - currentPos.x;
                                        double dz = ownerPos.z - currentPos.z;
                                        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
                                        living.setYRot(yaw);
                                        living.setYHeadRot(yaw);
                                    } else {
                                        // Close enough, just hover
                                        living.setDeltaMovement(0, 0, 0);
                                    }

                                    // Cancel gravity
                                    living.setNoGravity(true);

                                    // Sparkle particles every 10 ticks
                                    if (level.getGameTime() % 10 == 0) {
                                        Vec3 particlePos = living.position().add(0, living.getBbHeight() / 2, 0);
                                        level.sendParticles(
                                                ParticleTypes.END_ROD,
                                                particlePos.x, particlePos.y, particlePos.z, 2,
                                                0.1, 0.1, 0.1, 0.01
                                        );
                                    }
                                } else {
                                    // Owner not found, gently float down
                                    startFloatingDown(living, nbt);
                                }
                            } catch (IllegalArgumentException e) {
                                // Invalid UUID, float down
                                startFloatingDown(living, nbt);
                            }
                        }
                    }

                    // Regular levitation logic
                    if (nbt.getBoolean("SparkleLevitating")) {
                        boolean isBlessedFloating = nbt.getBoolean("BlessedFloating");

                        if (!isBlessedFloating && living instanceof Mob mob && mob.isLeashed()) {
                            living.removeEffect(MobEffects.LEVITATION);
                            living.removeEffect(MobEffects.SLOW_FALLING);
                            living.removeEffect(MobEffects.GLOWING);

                            nbt.putBoolean("SparkleLevitating", false);
                            nbt.remove("SparkleStartY");
                            nbt.remove("SparkleTargetY");

                            living.addEffect(new MobEffectInstance(
                                    MobEffects.SLOW_FALLING,
                                    20 * 30,
                                    0,
                                    false, false, true
                            ));

                            continue;
                        }

                        double targetY = nbt.getDouble("SparkleTargetY");
                        if (living.getY() >= targetY) {
                            living.removeEffect(MobEffects.LEVITATION);

                            if (!living.hasEffect(MobEffects.SLOW_FALLING)) {
                                living.addEffect(new MobEffectInstance(
                                        MobEffects.SLOW_FALLING,
                                        Integer.MAX_VALUE,
                                        0,
                                        false, false, true
                                ));
                            }

                            if (isBlessedFloating && !living.hasEffect(MobEffects.GLOWING)) {
                                living.addEffect(new MobEffectInstance(
                                        MobEffects.GLOWING,
                                        Integer.MAX_VALUE,
                                        0,
                                        false, false, true
                                ));
                            }
                        }
                    }
                }
            }
        }

        private static void startFloatingDown(LivingEntity living, CompoundTag nbt) {
            nbt.putBoolean("BowTieFloating", false);
            living.setNoGravity(false);

            // Apply slow falling for gentle descent
            living.addEffect(new MobEffectInstance(
                    MobEffects.SLOW_FALLING,
                    20 * 30, // 30 seconds
                    0,
                    false, false, true
            ));
        }
    }
}
