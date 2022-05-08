package io.github.amerebagatelle.fabricskyboxes.fabric;

import io.github.amerebagatelle.fabricskyboxes.SkyBoxes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class FabricSkyBoxesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SkyBoxes.init();
    }
}
