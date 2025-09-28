package com.burningsulphur.delights_now_loading.entity;

import com.burningsulphur.delights_now_loading.DelightsNowLoading;
import dev.hexnowloading.dungeonnowloading.components.VertexNode;
import dev.hexnowloading.dungeonnowloading.potion.VertexTransmissionEffect;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLMobEffects;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import dev.hexnowloading.dungeonnowloading.util.DNLMath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.yirmiri.dungeonsdelight.common.entity.misc.CleaverEntity;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;


public class VertexCleaver extends CleaverEntity {
    private VertexNode vertexNode = new VertexNode(this);
    private boolean firstTick = true;

    private static final HashMap<UUID, VertexNode> entityVertexNodeMap = new HashMap();

    private static final HashMap<UUID, Boolean> isReconnectionCaseMap = new HashMap();

    private int powerLevel = 0;
    private int powerIncrementTimer = 0;
    private int life;
    private static final int ENTITY_DIRECT_HIT_EFFECT_DURATION_TICKS = 120;
    private static final int ADVANCE_POWER_LEVEL_THRESHOLD_TICKS = 8;
    private static final int MAX_POWER_LEVEL = 3;
    private static final int DESPAWN_TIME_TICKS = 400;


    public VertexCleaver(EntityType<? extends CleaverEntity> type, Level level) {
        super(type, level);
    }

    public VertexNode getVertexNode(UUID uuid) {
        return (VertexNode)entityVertexNodeMap.get(uuid);
    }

    public void markAsReconnectionCase(UUID uuid) {
        isReconnectionCaseMap.put(uuid, true);
    }

    public VertexCleaver(Level level) {
        super((EntityType) DelightsNowLoading.VERTEX_CLEAVER.get(), level);
    }

    public VertexNode getVertexNode() {
        return this.vertexNode;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (firstTick) {
                vertexNode.connectToNearbyNodes(this);
                firstTick = false;
            }

            // keep the vertex node alive
            vertexNode.tick(this);
        }
    }

    //------------


    public void tickDespawn() {
        ++this.life;
        if (this.life >= 400) {
            this.vertexNode.disconnect_all();
            this.discard();
        }

    }
    //--------



    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);

        if (entityHitResult.getEntity() instanceof LivingEntity entity) {
            int slownessDurationTicks = ENTITY_DIRECT_HIT_EFFECT_DURATION_TICKS;
            int slownessAmplifier = this.vertexNode.getConnectionCount();
            int vertexTransDurationTicks = ENTITY_DIRECT_HIT_EFFECT_DURATION_TICKS;
            int vertexTransAmplifier = 0;

            // Slowness application
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slownessDurationTicks, slownessAmplifier));

            // Vertex Transmission application
            boolean entityHasEffect = entity.hasEffect(DNLMobEffects.VERTEX_TRANSMISSION.get());
            if (!entityHasEffect) {
                entity.addEffect(new MobEffectInstance(DNLMobEffects.VERTEX_TRANSMISSION.get(), vertexTransDurationTicks, vertexTransAmplifier));
            } else {
                entity.addEffect(new MobEffectInstance(DNLMobEffects.VERTEX_TRANSMISSION.get(), vertexTransDurationTicks, vertexTransAmplifier));
                VertexTransmissionEffect vertexTransmissionEffect = (VertexTransmissionEffect) entity.getEffect(DNLMobEffects.VERTEX_TRANSMISSION.get()).getEffect();
                vertexTransmissionEffect.markAsReconnectionCase(entity.getUUID());
            }
        }
    }

}
