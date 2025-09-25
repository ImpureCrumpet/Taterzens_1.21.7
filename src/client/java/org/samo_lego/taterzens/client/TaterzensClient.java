package org.samo_lego.taterzens.client;

import net.fabricmc.api.ClientModInitializer;

public class TaterzensClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// We just do this to avoid client crashes
		/*
		EntityRendererRegistry.register(Taterzens.TATERZEN_TYPE.get(), context -> new MobRenderer<>(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 1.0F) {
			@Override
			public ResourceLocation getTextureLocation(TaterzenNPC entity) {
				return DefaultPlayerSkin.getDefaultTexture();
			}
		});
		*/
	}
}
