package io.github.amerebagatelle.fabricskyboxes.skyboxes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.architectury.registry.registries.Registrar;
import io.github.amerebagatelle.fabricskyboxes.SkyBoxes;
import io.github.amerebagatelle.fabricskyboxes.skyboxes.textured.SquareTexturedSkybox;
import io.github.amerebagatelle.fabricskyboxes.util.JsonObjectWrapper;
import io.github.amerebagatelle.fabricskyboxes.util.object.Blend;
import io.github.amerebagatelle.fabricskyboxes.util.object.Decorations;
import io.github.amerebagatelle.fabricskyboxes.util.object.Fade;
import io.github.amerebagatelle.fabricskyboxes.util.object.MinMaxEntry;
import io.github.amerebagatelle.fabricskyboxes.util.object.RGBA;
import io.github.amerebagatelle.fabricskyboxes.util.object.Rotation;
import io.github.amerebagatelle.fabricskyboxes.util.object.Texture;
import io.github.amerebagatelle.fabricskyboxes.util.object.Textures;
import java.lang.reflect.Array;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

public class LegacyDeserializer<T extends AbstractSkybox> {
    public static final RegistryKey<Registry<LegacyDeserializer<? extends AbstractSkybox>>> REGISTRY_KEY;
    public static final Registrar<LegacyDeserializer<? extends AbstractSkybox>> REGISTRY;
    public static final Supplier<LegacyDeserializer<MonoColorSkybox>> MONO_COLOR_SKYBOX_DESERIALIZER;
    public static final Supplier<LegacyDeserializer<SquareTexturedSkybox>> SQUARE_TEXTURED_SKYBOX_DESERIALIZER;
    private final BiConsumer<JsonObjectWrapper, AbstractSkybox> deserializer;

    static {
        Identifier registryId = new Identifier(SkyBoxes.MODID, "legacy_skybox_deserializer");
        REGISTRY_KEY = RegistryKey.ofRegistry(registryId);
        REGISTRY = SkyBoxes.REGISTRIES.get().builder(registryId,
            (LegacyDeserializer<? extends AbstractSkybox>[]) Array.newInstance(LegacyDeserializer.class, 0)).build();

        MONO_COLOR_SKYBOX_DESERIALIZER = register(new LegacyDeserializer<>(LegacyDeserializer::decodeMonoColor, MonoColorSkybox.class), "mono_color_skybox_legacy_deserializer");
        SQUARE_TEXTURED_SKYBOX_DESERIALIZER = register(new LegacyDeserializer<>(LegacyDeserializer::decodeSquareTextured, SquareTexturedSkybox.class), "square_textured_skybox_legacy_deserializer");
    }

    private LegacyDeserializer(BiConsumer<JsonObjectWrapper, AbstractSkybox> deserializer, Class<T> clazz) {
        this.deserializer = deserializer;
    }

    public BiConsumer<JsonObjectWrapper, AbstractSkybox> getDeserializer() {
        return this.deserializer;
    }

    private static void decodeSquareTextured(JsonObjectWrapper wrapper, AbstractSkybox skybox) {
        decodeSharedData(wrapper, skybox);
        ((SquareTexturedSkybox) skybox).rotation = new Rotation(new Vec3f(0f, 0f, 0f), new Vec3f(wrapper.getOptionalArrayFloat("axis", 0, 0), wrapper.getOptionalArrayFloat("axis", 1, 0), wrapper.getOptionalArrayFloat("axis", 2, 0)), 1);
        ((SquareTexturedSkybox) skybox).blend = new Blend(wrapper.getOptionalBoolean("shouldBlend", false) ? "add" : "", 0, 0, 0);
        ((SquareTexturedSkybox) skybox).textures = new Textures(
                new Texture(wrapper.getJsonStringAsId("texture_north")),
                new Texture(wrapper.getJsonStringAsId("texture_south")),
                new Texture(wrapper.getJsonStringAsId("texture_east")),
                new Texture(wrapper.getJsonStringAsId("texture_west")),
                new Texture(wrapper.getJsonStringAsId("texture_top")),
                new Texture(wrapper.getJsonStringAsId("texture_bottom"))
        );
    }

    private static void decodeMonoColor(JsonObjectWrapper wrapper, AbstractSkybox skybox) {
        decodeSharedData(wrapper, skybox);
        ((MonoColorSkybox) skybox).color = new RGBA(wrapper.get("red").getAsFloat(), wrapper.get("blue").getAsFloat(), wrapper.get("green").getAsFloat());
    }

    private static void decodeSharedData(JsonObjectWrapper wrapper, AbstractSkybox skybox) {
        skybox.fade = new Fade(
                wrapper.get("startFadeIn").getAsInt(),
                wrapper.get("endFadeIn").getAsInt(),
                wrapper.get("startFadeOut").getAsInt(),
                wrapper.get("endFadeOut").getAsInt(),
                false
        );
        // alpha changing
        skybox.maxAlpha = wrapper.getOptionalFloat("maxAlpha", 1f);
        skybox.transitionSpeed = wrapper.getOptionalFloat("transitionSpeed", 1f);
        // rotation
        skybox.shouldRotate = wrapper.getOptionalBoolean("shouldRotate", false);
        // decorations
        skybox.decorations = Decorations.DEFAULT;
        // fog
        skybox.changeFog = wrapper.getOptionalBoolean("changeFog", false);
        skybox.fogColors = new RGBA(
                wrapper.getOptionalFloat("fogRed", 0f),
                wrapper.getOptionalFloat("fogGreen", 0f),
                wrapper.getOptionalFloat("fogBlue", 0f)
        );
        // environment specifications
        JsonElement element;
        element = wrapper.getOptionalValue("weather").orElse(null);
        if (element != null) {
            if (element.isJsonArray()) {
                for (JsonElement jsonElement : element.getAsJsonArray()) {
                    skybox.weather.add(jsonElement.getAsString());
                }
            } else if (JsonHelper.isString(element)) {
                skybox.weather.add(element.getAsString());
            }
        }
        element = wrapper.getOptionalValue("biomes").orElse(null);
        processIds(element, skybox.biomes);
        element = wrapper.getOptionalValue("dimensions").orElse(null);
        processIds(element, skybox.worlds);
        element = wrapper.getOptionalValue("heightRanges").orElse(null);
        if (element != null) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement jsonElement : array) {
                JsonArray insideArray = jsonElement.getAsJsonArray();
                float low = insideArray.get(0).getAsFloat();
                float high = insideArray.get(1).getAsFloat();
                skybox.yRanges.add(new MinMaxEntry(low, high));
            }
        }
    }

    private static void processIds(JsonElement element, List<Identifier> list) {
        if (element != null) {
            if (element.isJsonArray()) {
                for (JsonElement jsonElement : element.getAsJsonArray()) {
                    list.add(new Identifier(jsonElement.getAsString()));
                }
            } else if (JsonHelper.isString(element)) {
                list.add(new Identifier(element.getAsString()));
            }
        }
    }

    private static <T extends AbstractSkybox> Supplier<LegacyDeserializer<T>> register(LegacyDeserializer<T> deserializer, String name) {
        return REGISTRY.register(new Identifier(SkyBoxes.MODID, name), () -> deserializer);
    }
}
