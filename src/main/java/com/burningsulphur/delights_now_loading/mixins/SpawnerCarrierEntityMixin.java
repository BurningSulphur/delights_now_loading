package com.burningsulphur.delights_now_loading.mixins;



import dev.hexnowloading.dungeonnowloading.entity.monster.SpawnerCarrierEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.yirmiri.dungeonsdelight.core.registry.DDParticles;
import net.minecraft.core.particles.SimpleParticleType;

@Mixin(SpawnerCarrierEntity.class)
public class SpawnerCarrierEntityMixin {
    @ModifyArg(method = "customServerAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
    private ParticleOptions changeFlameParticle(ParticleOptions oldParticle) {
        if (oldParticle == ParticleTypes.FLAME) {
            return DDParticles.LIVING_FLAME.get();
        }
        return oldParticle;
    }
}
