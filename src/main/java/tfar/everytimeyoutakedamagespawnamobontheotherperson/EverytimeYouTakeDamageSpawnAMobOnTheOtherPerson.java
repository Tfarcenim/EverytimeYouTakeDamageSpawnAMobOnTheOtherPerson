package tfar.everytimeyoutakedamagespawnamobontheotherperson;

import net.minecraft.entity.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(EverytimeYouTakeDamageSpawnAMobOnTheOtherPerson.MODID)
public class EverytimeYouTakeDamageSpawnAMobOnTheOtherPerson {
    // Directly reference a log4j logger.

    public static final String MODID = "everytimeyoutakedamagespawnamobontheotherperson";

    public static final Tags.IOptionalNamedTag<EntityType<?>> blacklisted = EntityTypeTags.createOptional(new ResourceLocation(MODID,"blacklisted"));

    public EverytimeYouTakeDamageSpawnAMobOnTheOtherPerson() {
        MinecraftForge.EVENT_BUS.addListener(this::onDeath);
    }

    private void onDeath(final LivingDamageEvent event) {
        LivingEntity livingEntity = event.getEntityLiving();
        if (livingEntity instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity)livingEntity;
            List<ServerPlayerEntity> otherPlayers = new ArrayList<>(player.getServer().getPlayerList().getPlayers());
            otherPlayers.remove(player);
            for (ServerPlayerEntity otherPlayer : otherPlayers) {
                ServerWorld world = otherPlayer.getServerWorld();
                Entity entity = getRandomEntity(otherPlayer.getRNG(),world);
                entity.setLocationAndAngles(player.getPosX(), player.getPosY(), player.getPosZ(), player.rotationYaw, 0.0F);
                if (entity instanceof MobEntity) {
                    ((MobEntity)entity).onInitialSpawn(world, world.getDifficultyForLocation(otherPlayer.getPosition())
                            , SpawnReason.MOB_SUMMONED, null, null);
                }
                world.addEntity(entity);
            }
        }
    }

    public static Entity getRandomEntity(Random random, World world) {
        EntityType<?> entityType = null;
        Entity entity = null;
        while (entityType == null || entityType.isContained(blacklisted) || !(entity instanceof MobEntity)) {
            entityType = Registry.ENTITY_TYPE.getRandom(random);
            entity = entityType.create(world);
        }
        return entity;
    }
}
