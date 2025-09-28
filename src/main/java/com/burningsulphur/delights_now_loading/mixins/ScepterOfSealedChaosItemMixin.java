package com.burningsulphur.delights_now_loading.mixins;


import dev.hexnowloading.dungeonnowloading.item.ScepterOfSealedChaosItem;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.yirmiri.dungeonsdelight.core.registry.DDParticles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ScepterOfSealedChaosItem.class)
public class ScepterOfSealedChaosItemMixin {
    @ModifyArg(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
    private ParticleOptions changeFlameParticle(ParticleOptions oldParticle) {
        if (oldParticle == ParticleTypes.FLAME) {
            return DDParticles.LIVING_FLAME.get();
        }
        return oldParticle;
    }
}
