

package com.burningsulphur.delights_now_loading.entity;

import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexArrowProjectileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.yirmiri.dungeonsdelight.common.item.StainedCleaverItem;
import net.yirmiri.dungeonsdelight.core.init.DDDamageTypes;
import net.yirmiri.dungeonsdelight.core.registry.*;

//vertex part
import dev.hexnowloading.dungeonnowloading.components.VertexNode;
import dev.hexnowloading.dungeonnowloading.potion.VertexTransmissionEffect;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import dev.hexnowloading.dungeonnowloading.registry.DNLMobEffects;
import dev.hexnowloading.dungeonnowloading.registry.DNLSounds;
import dev.hexnowloading.dungeonnowloading.util.DNLMath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;


public class VertexCleaverEntity extends AbstractArrow {
    public static final EntityDataAccessor<Boolean> ID_FOIL = SynchedEntityData.defineId(VertexCleaverEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(VertexCleaverEntity.class, EntityDataSerializers.ITEM_STACK);
    public ItemStack cleaverItem;
    private double damage = 0;
    public boolean canBypassCooldowns = false;
    public int despawnTime = 300;
    public boolean spinning = true;
    public boolean hasSetCooldown = false;
    public float ricochetsPitch = 1.0F;
    public int ricochetsLeft = 0;
    public int serratedLevel = 0;
    public int retractionLevel = 0;
    public int persistenceLevel = 0;
    public int soundTickCounter = 0;

    //vertex part---------------------------------------
    private int powerLevel = 0;
    private VertexNode vertexNode = new VertexNode(this);
    private int powerIncrementTimer = 0;
    private int life;

    private static final EntityDataAccessor<Integer> POWER_LEVEL = SynchedEntityData.defineId(VertexCleaverEntity.class, EntityDataSerializers.INT);
    private static final int ENTITY_DIRECT_HIT_EFFECT_DURATION_TICKS = 120 ;
    private static final int ADVANCE_POWER_LEVEL_THRESHOLD_TICKS = 8;
    private static final int MAX_POWER_LEVEL = 3;
    private static final int DESPAWN_TIME_TICKS = 300;

    private static final HashMap<UUID, VertexNode> entityVertexNodeMap = new HashMap();

    private static final int MAX_CONNECTION_COUNT = 5;
    //----------------------------

    public VertexCleaverEntity(EntityType<? extends VertexCleaverEntity> type, Level level) {
        super(type, level);
    }

    public VertexCleaverEntity(EntityType<? extends VertexCleaverEntity> type, Level level, Player living, ItemStack stack) {
        super(type, living, level);
        cleaverItem = getItem();
        cleaverItem = getItem().copy();
        this.entityData.set(ID_FOIL, stack.hasFoil());
    }

    public VertexCleaverEntity(EntityType<? extends VertexCleaverEntity> type, Level level, Position pos, ItemStack stack) {
        super(type, level);
        cleaverItem = getItem();
        cleaverItem = getItem().copy();
        this.entityData.set(ID_FOIL, stack.hasFoil());
    }

    public VertexCleaverEntity(EntityType<? extends VertexCleaverEntity> type, Level level, double x, double y, double z) {
        super(type, x, y, z, level);
    }

    //----------------------------
    public int getPowerLevel() {
        return (Integer)this.entityData.get(POWER_LEVEL);
    }

    public boolean isFullyPowered() { return this.powerLevel == MAX_POWER_LEVEL; }

    public VertexNode getVertexNode() {
        return this.vertexNode;
    }
    //--------------------------

    public void setItem(ItemStack stack) {
        if (stack.hasTag()) {
            this.getEntityData().set(DATA_ITEM_STACK, stack.copyWithCount(1));
        }
    }

    protected ItemStack getItemRaw() {
        return this.getEntityData().get(DATA_ITEM_STACK);
    }

    public ItemStack getItem() {
        ItemStack stack = this.getItemRaw();
        return stack.isEmpty() ? new ItemStack(DDItems.FLINT_CLEAVER.get()) : stack;
    }

    @Override
    public ItemStack getPickupItem() {
        return getItem();
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ID_FOIL, false);
        this.getEntityData().define(DATA_ITEM_STACK, ItemStack.EMPTY);
        //---------
        this.entityData.define(POWER_LEVEL, 0);
        //--------------
    }

    @Override
    protected void updateRotation() {
        this.setXRot(0);
    }

    public boolean isFoil() { //TODO: RENDER ENCHANT
        return this.entityData.get(ID_FOIL);
    }

    @Override
    public void playerTouch(Player player) {
        if (persistenceLevel > 0 && this.inGround && this.ownedBy(player) || this.getOwner() == null && (player.getCooldowns().isOnCooldown(getItem().getItem()))) {
            player.playSound(SoundEvents.ARMOR_EQUIP_GENERIC, 1.0F, 1.0F);
            player.getCooldowns().removeCooldown(getItem().getItem());
            this.vertexNode.disconnect_all();
            this.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        vertexNode.tick(this);

        if (!this.level().isClientSide) {
            soundTickCounter++;
            if (soundTickCounter >= 4 && !this.inGround) {
                this.level().playSound(null, this, DDSounds.CLEAVER_FLYING.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                soundTickCounter = 0;
            }
        }

        if (this.inGroundTime > despawnTime) {
            this.vertexNode.disconnect_all();
            this.discard();
        }

        if (this.shakeTime > 0) {
            --this.shakeTime;
        }

        if (!isInGround()) {
            this.setXRot(this.xRotO - 45);
        }

        if (this.powerLevel == 0 && this.powerIncrementTimer == 0) {
            this.level().playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    DNLSounds.VERTEX_ARROW_BOOTUP.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.2F / (DNLMath.randomRange(0.0f, 1.0f) * 0.2F + 0.9F)
            );
        }
        if (this.powerLevel < MAX_POWER_LEVEL) {
            this.powerIncrementTimer++;
            if (this.powerIncrementTimer >= ADVANCE_POWER_LEVEL_THRESHOLD_TICKS) {
                this.powerLevel++;
                this.entityData.set(POWER_LEVEL, this.powerLevel);
                this.powerIncrementTimer = 0;
            }
        }else if (!this.level().isClientSide) { //&& !this.vertexNode.attemptedConnection()  && this.life != DESPAWN_TIME_TICKS
            this.vertexNode.connectToNearbyNodes(this );
        }


    }

    public boolean isInGround() {
        return this.inGround && ricochetsLeft <= 0;
    }

    @Override
    public void setBaseDamage(double addedDamage) {
        damage = addedDamage * 1.66;
    }

    @Override
    public double getBaseDamage() {
        return damage;
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        if (ricochetsLeft <= 0) {
            Vec3 vec3 = hitResult.getLocation().subtract(this.getX(), this.getY(), this.getZ());
            this.setDeltaMovement(vec3);
            hasImpulse = true;
            Vec3 vec31 = vec3.normalize().scale(0.05);
            this.setPos(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);

            if (ricochetsLeft == 0) {
                this.inGround = true;
                this.shakeTime = 24;
                playSound(DDSounds.CLEAVER_HIT_BLOCK.get(), 2.0F, 1.0F);
            }
        }

        if (getOwner() instanceof Player player) {
            if (!player.getAbilities().instabuild && !canBypassCooldowns && !hasSetCooldown) {
                player.getCooldowns().addCooldown(getItem().getItem(), 50);
                if (ricochetsLeft == 0) {
                    hasSetCooldown = true;
                }
            }

            if (ricochetsLeft > 0) {
                Vec3 reflected = new Vec3(getDeltaMovement().toVector3f().reflect(hitResult.getDirection().step())).scale(0.8F);
                setDeltaMovement(reflected);
                this.setPos(this.getX() + reflected.x, this.getY() + reflected.y, this.getZ() + reflected.z);
                hasImpulse = true;
                ((ServerLevel) level()).getChunkSource().broadcast(this, new ClientboundSetEntityMotionPacket(this.getId(), getDeltaMovement()));
                ricochetsLeft--;
                damage = damage * 1.25;
                playSound(DDSounds.CLEAVER_RICOCHET.get(), 1.0F, ricochetsPitch);
                ricochetsPitch = ricochetsPitch + 0.25F;
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        Entity entity = hitResult.getEntity();
        Entity owner = getOwner();

        if (!(entity instanceof ItemEntity) && entity.hurt(new DamageSource(this.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DDDamageTypes.CLEAVER), this, owner == null ? this : owner), (float) damage)) {
            if (entity.getType() == EntityType.ENDERMAN) {
                return;
            }

            if (entity instanceof LivingEntity living) {
                if (owner instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(living, owner);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) owner, living);


                    //-------------------------------------------------------------------------
                    if (hitResult.getEntity() instanceof LivingEntity entity2) {
                        int slownessDurationTicks = ENTITY_DIRECT_HIT_EFFECT_DURATION_TICKS;
                        int slownessAmplifier = this.vertexNode.getConnectionCount();
                        int vertexTransDurationTicks = ENTITY_DIRECT_HIT_EFFECT_DURATION_TICKS;
                        int vertexTransAmplifier = 0;

                        // Slowness application
                        entity2.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slownessDurationTicks, slownessAmplifier));

                        // Vertex Transmission application
                        boolean entityHasEffect = entity2.hasEffect(DNLMobEffects.VERTEX_TRANSMISSION.get());
                        if (!entityHasEffect) {
                            entity2.addEffect(new MobEffectInstance(DNLMobEffects.VERTEX_TRANSMISSION.get(), vertexTransDurationTicks, vertexTransAmplifier));
                        } else {
                            entity2.addEffect(new MobEffectInstance(DNLMobEffects.VERTEX_TRANSMISSION.get(), vertexTransDurationTicks, vertexTransAmplifier));
                            VertexTransmissionEffect vertexTransmissionEffect = (VertexTransmissionEffect) entity2.getEffect(DNLMobEffects.VERTEX_TRANSMISSION.get()).getEffect();
                            vertexTransmissionEffect.markAsReconnectionCase(entity2.getUUID());
                        }
                    }

                    //---------------------------------------------------------------------------


                    if (this.isOnFire()) {
                        entity.setSecondsOnFire(this.getRemainingFireTicks());
                    }

                    if (getSerratedLevel() > 0 && !entity.isInvulnerable()) {
                        int duration = 40 + (getSerratedLevel() * 20);

                        if (living.hasEffect(DDEffects.SERRATED.get())) {
                            duration = duration / 2;
                            duration += living.getEffect(DDEffects.SERRATED.get()).getDuration();
                        }
                        living.addEffect(new MobEffectInstance(DDEffects.SERRATED.get(), duration, 0));
                        living.playSound(DDSounds.CLEAVER_SERRATED_STRIKE.get(), 2.0F, 1.0F);
                    }

                    if (getPersistenceLevel() > 0) {
                        if (!living.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 + (getPersistenceLevel() * 20), 0));
                        }
                    }
                    damage = damage * 0.8; //20% of damage is lost upon pierces into another entity
                }
                doPostHurtEffects(living);

                if (retractionLevel > 0 && getOwner() != null) {
                    if (!(entity instanceof Ghast)) {
                        pullEntity(entity, 1.5F);
                    } else {
                        pullEntity(entity, 2.0F);
                    }
                }
            }

            if (getSerratedLevel() <= 0 && !entity.isInvulnerable()) {
                entity.playSound(DDSounds.CLEAVER_HIT_ENTITY.get(), 2.5F, 1.0F);
            }
        }

        if (owner instanceof Player player && entity != owner) {
            canBypassCooldowns = true;
            player.getCooldowns().removeCooldown(getItem().getItem()); //remove cooldown when entity is hit with cleaver
        }

        if (retractionLevel > 0 && getOwner() != null) {
            if (entity instanceof ItemEntity) {
                pullEntity(entity, 2.0F);
            }
        }
    }

    public void pullEntity(Entity entity, float maxDistance) {
        if (retractionLevel > 0 && getOwner() != null) {
            Vec3 direction = getOwner().position().subtract(entity.position());
            double distance = direction.length();

            if (entity instanceof LivingEntity && distance <= 4.5) {
                return;
            }

            if (distance > 0.01) {
                Vec3 velocity = direction.normalize().scale(Math.min(maxDistance, distance * 0.25));
                entity.setDeltaMovement(entity.getDeltaMovement().add(velocity));
                entity.playSound(DDSounds.CLEAVER_FLYING.get(), 0.75F, -1.0F);
            }
            entity.hurtMarked = true;
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) || entity.isAlive() && entity instanceof ItemEntity;
    }

    public boolean isInCeiling() {
        if (this.noPhysics) {
            return false;
        } else {
            float f = 0.25F * 0.8F;
            BlockPos pos = BlockPos.containing(this.getEyePosition().add(0, 1.0E-6D, 0));
            BlockState blockstate = this.level().getBlockState(pos);
            return
                    !blockstate.isAir() && blockstate.isSuffocating(this.level(), pos) && Shapes.joinIsNotEmpty(blockstate.getCollisionShape(this.level(), pos).move(pos.getX(), pos.getY(), pos.getZ()), Shapes.create(AABB.ofSize(this.getEyePosition(), 0.1, 0.1, 0.1)), BooleanOp.AND
                    );
        }
    }

    public int getPersistenceLevel() {
        return persistenceLevel;
    }

    public void setPersistenceLevel(int additionalPersistenceLevel) {
        persistenceLevel = persistenceLevel + additionalPersistenceLevel;
    }

    public int getRetractionLevel() {
        return retractionLevel;
    }

    public void setRetractionLevel(int additionalRetractionLevel) {
        retractionLevel = retractionLevel + additionalRetractionLevel;
    }

    public int getSerratedLevel() {
        return serratedLevel;
    }

    public void setSerratedLevel(int additionalSerratedLevel) {
        serratedLevel = serratedLevel + additionalSerratedLevel;
    }

    @Override
    protected float getWaterInertia() {
        return 0.75F;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Cleaver", 10)) {
            this.cleaverItem = ItemStack.of(tag.getCompound("Cleaver"));
        }
        this.setItem(ItemStack.of(tag.getCompound("Item")));
        //---------------
        this.entityData.set(POWER_LEVEL, tag.getInt("powerLevel"));
        //---------------
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Cleaver", this.cleaverItem.save(new CompoundTag()));
        //-------------------------
        tag.putInt("powerLevel", this.entityData.get(POWER_LEVEL));
        //------------------------
        if (!this.getItemRaw().isEmpty()) {
            tag.put("Item", this.getItemRaw().save(new CompoundTag()));
        }
    }

    @Override
    public boolean shouldRender(double pX, double pY, double pZ) {
        return true;
    }

    /* replaced with despawn ticks or something idk
    @Override
    public void tickDespawn() {
        if (this.pickup != Pickup.ALLOWED) {
            super.tickDespawn();
        }
    }
     */

    @Override
    protected void tickDespawn() {
        this.life++;
        if (this.life >= DESPAWN_TIME_TICKS) {
            this.vertexNode.disconnect_all();
            this.discard();
        }
    }

    //--------------

}