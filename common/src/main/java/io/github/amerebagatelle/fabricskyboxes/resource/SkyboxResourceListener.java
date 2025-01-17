package io.github.amerebagatelle.fabricskyboxes.resource;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.github.amerebagatelle.fabricskyboxes.SkyBoxes;
import io.github.amerebagatelle.fabricskyboxes.SkyboxManager;
import io.github.amerebagatelle.fabricskyboxes.skyboxes.AbstractSkybox;
import io.github.amerebagatelle.fabricskyboxes.skyboxes.SkyboxType;
import io.github.amerebagatelle.fabricskyboxes.util.JsonObjectWrapper;
import io.github.amerebagatelle.fabricskyboxes.util.object.internal.Metadata;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;

public class SkyboxResourceListener implements SynchronousResourceReloader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().setLenient().create();
    private static final JsonObjectWrapper objectWrapper = new JsonObjectWrapper();

    @Override
    public void reload(ResourceManager manager) {
        SkyboxManager skyboxManager = SkyboxManager.getInstance();

        // clear registered skyboxes on reload
        skyboxManager.clearSkyboxes();

        // load new skyboxes
        Collection<Identifier> resources = manager.findResources("sky", (string) -> string.endsWith(".json"));

        for (Identifier id : resources) {
            Resource resource;
            try {
                resource = manager.getResource(id);
                try {
                    JsonObject json = GSON.fromJson(new InputStreamReader(resource.getInputStream()), JsonObject.class);
                    objectWrapper.setFocusedObject(json);
                    AbstractSkybox skybox = this.parseSkyboxJson(id);
                    if (skybox != null) {
                        skyboxManager.addSkybox(skybox);
                    }
                } finally {
                    try {
                        resource.close();
                    } catch (IOException e) {
                        SkyBoxes.getLogger().error("Error closing resource " + id.toString());
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                SkyBoxes.getLogger().error("Error reading skybox " + id.toString());
                e.printStackTrace();
            }
        }
    }

    private AbstractSkybox parseSkyboxJson(Identifier id) {
        AbstractSkybox skybox;
        Metadata metadata;

        try {
            metadata = Metadata.CODEC.decode(JsonOps.INSTANCE, objectWrapper.getFocusedObject()).getOrThrow(false, System.err::println).getFirst();
        } catch (RuntimeException e) {
            SkyBoxes.getLogger().warn("Skipping invalid skybox " + id.toString(), e);
            SkyBoxes.getLogger().warn(objectWrapper.toString());
            return null;
        }

        SkyboxType<? extends AbstractSkybox> type = SkyboxType.REGISTRY.get(metadata.getType());
        Preconditions.checkNotNull(type, "Unknown skybox type: " + metadata.getType().getPath().replace('_', '-'));
        if (metadata.getSchemaVersion() == 1) {
            Preconditions.checkArgument(type.isLegacySupported(), "Unsupported schema version '1' for skybox type " + type.getName());
            SkyBoxes.getLogger().debug("Using legacy deserializer for skybox " + id.toString());
            skybox = type.instantiate();
            //noinspection ConstantConditions
            type.getDeserializer().getDeserializer().accept(objectWrapper, skybox);
        } else {
            skybox = type.getCodec(metadata.getSchemaVersion()).decode(JsonOps.INSTANCE, objectWrapper.getFocusedObject()).getOrThrow(false, System.err::println).getFirst();
        }
        return skybox;
    }
}
