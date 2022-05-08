package io.github.amerebagatelle.fabricskyboxes.forge;

import dev.architectury.platform.forge.EventBuses;
import io.github.amerebagatelle.fabricskyboxes.SkyBoxes;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SkyBoxes.MODID)
public class ForgeSkyBoxesClient {
    public ForgeSkyBoxesClient() {
        EventBuses.registerModEventBus(
                SkyBoxes.MODID, FMLJavaModLoadingContext.get().getModEventBus());
        SkyBoxes.init();
    }
}
