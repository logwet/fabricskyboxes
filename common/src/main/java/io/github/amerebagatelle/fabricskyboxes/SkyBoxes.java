package io.github.amerebagatelle.fabricskyboxes;

import com.google.common.base.Suppliers;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.registries.Registries;
import io.github.amerebagatelle.fabricskyboxes.resource.SkyboxResourceListener;
import java.util.function.Supplier;
import net.minecraft.resource.ResourceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SkyBoxes {
    public static final String MODID = "fabricskyboxes";
    private static Logger LOGGER;

    public static final Supplier<Registries> REGISTRIES = Suppliers.memoize(() -> Registries.get(MODID));

    public static void init() {
        ReloadListenerRegistry.register(ResourceType.CLIENT_RESOURCES, new SkyboxResourceListener());
    }

    public static Logger getLogger() {
        if (LOGGER == null) {
            LOGGER = LogManager.getLogger("FabricSkyboxes");
        }
        return LOGGER;
    }
}
