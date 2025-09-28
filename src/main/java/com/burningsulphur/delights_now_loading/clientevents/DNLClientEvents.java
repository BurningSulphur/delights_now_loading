package com.burningsulphur.delights_now_loading.clientevents;

import com.burningsulphur.delights_now_loading.DelightsNowLoading;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import  com.burningsulphur.delights_now_loading.entity.VertexCleaverRenderer;


@Mod.EventBusSubscriber(modid = DelightsNowLoading.MOD_ID)
public class DNLClientEvents {

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(DelightsNowLoading.VERTEX_CLEAVER_ENTITY.get(), VertexCleaverRenderer::new);
    }
}