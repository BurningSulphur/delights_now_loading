package com.burningsulphur.delights_now_loading.items;




import com.burningsulphur.delights_now_loading.DelightsNowLoading;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.yirmiri.dungeonsdelight.common.entity.misc.CleaverEntity;
import net.yirmiri.dungeonsdelight.common.item.CleaverItem;
import net.yirmiri.dungeonsdelight.core.registry.DDEnchantments;
import net.yirmiri.dungeonsdelight.core.registry.DDEntities;
import net.yirmiri.dungeonsdelight.core.registry.DDSounds;

public class OuroborosCleaverItem extends CleaverItem {
    private static final ResourceLocation VERTEX_ID = new ResourceLocation("dungeonnowloading", "vertex_transmission");

    public OuroborosCleaverItem(Properties props) {
        super(1.5F, Tiers.IRON, 1.5F, -3.0F, props);
    }                                                //means 4



    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int timeLeft) {
        if (living instanceof Player player) {
            if (this.getUseDuration(stack) - timeLeft >= 6 && !player.getCooldowns().isOnCooldown(this)) {
                if (!level.isClientSide) {
                    stack.hurtAndBreak(1, player, (p) -> {
                        p.broadcastBreakEvent(living.getUsedItemHand());
                    });

                    CleaverEntity cleaver = new CleaverEntity((EntityType) DelightsNowLoading.VERTEX_CLEAVER.get(), level, player, stack.copy());
                    cleaver.setItem(stack.copy());
                    this.applyEnchantments(stack, cleaver);
                    cleaver.setBaseDamage(cleaver.getBaseDamage() + (double)this.getAttackDamage());
                    cleaver.shootFromRotation(player, player.getXRot(), (player.getYRot()-10), 0.0F, this.range, 1.0F);
                    if (player.getAbilities().instabuild) {
                        cleaver.pickup = AbstractArrow.Pickup.DISALLOWED;
                    }

                    CleaverEntity cleaver2 = new CleaverEntity((EntityType) DelightsNowLoading.VERTEX_CLEAVER.get(), level, player, stack.copy());
                    cleaver2.setItem(stack.copy());
                    this.applyEnchantments(stack, cleaver2);
                    cleaver2.setBaseDamage(cleaver2.getBaseDamage() + (double)this.getAttackDamage());
                    cleaver2.shootFromRotation(player, player.getXRot(), (player.getYRot()+10), 0.0F, this.range, 1.0F);
                    if (player.getAbilities().instabuild) {
                        cleaver2.pickup = AbstractArrow.Pickup.DISALLOWED;
                    }

                    level.addFreshEntity(cleaver);
                    level.addFreshEntity(cleaver2);


                    level.playSound((Player)null, cleaver, (SoundEvent) DDSounds.CLEAVER_THROW.get(), SoundSource.PLAYERS, 2.0F, 1.0F);

                }

                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);

        MobEffect vertexEffect = ForgeRegistries.MOB_EFFECTS.getValue(VERTEX_ID);

        if (vertexEffect != null) {
            int duration = 100;
            int amplifier = 1;
            boolean ambient = false;
            boolean showParticles = true;

            target.addEffect(new MobEffectInstance(vertexEffect, duration, amplifier, ambient, showParticles));
        }

        return result;
    }

    void applyEnchantments(ItemStack stack, CleaverEntity cleaver) {
        int sharpness = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, stack);
        if (sharpness > 0) {
            cleaver.setBaseDamage(cleaver.getBaseDamage() + (double)sharpness * 0.5 + 0.5);
        }

        int fireAspect = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, stack);
        if (fireAspect > 0) {
            cleaver.setSecondsOnFire(100 * fireAspect);
        }

        int ricochet = EnchantmentHelper.getItemEnchantmentLevel((Enchantment) DDEnchantments.RICOCHET.get(), stack);
        if (ricochet > 0) {
            cleaver.ricochetsLeft += ricochet;
        }

        int serrated = EnchantmentHelper.getItemEnchantmentLevel((Enchantment)DDEnchantments.SERRATED_STRIKE.get(), stack);
        if (serrated > 0) {
            cleaver.setSerratedLevel(serrated);
        }

    }
}