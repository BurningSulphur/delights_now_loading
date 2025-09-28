package com.burningsulphur.delights_now_loading.mixins;


import dev.hexnowloading.dungeonnowloading.block.DungeonWallTorch;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.yirmiri.dungeonsdelight.core.registry.DDParticles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(DungeonWallTorch.class)
public class DungeonWallTorchMixin {
    @ModifyArg(method = "animateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private ParticleOptions changeFlameParticle(ParticleOptions oldParticle) {
        if (oldParticle == ParticleTypes.FLAME) {
            return DDParticles.LIVING_FLAME.get();
        }
        return oldParticle;
    }
}
