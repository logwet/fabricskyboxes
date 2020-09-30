package io.github.amerebagatelle.fabricskyboxes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import io.github.amerebagatelle.fabricskyboxes.skyboxes.MonoColorSkybox;
import io.github.amerebagatelle.fabricskyboxes.skyboxes.textured.SquareTexturedSkybox;
import io.github.amerebagatelle.fabricskyboxes.util.object.Fade;
import io.github.amerebagatelle.fabricskyboxes.util.object.RGBA;
import io.github.amerebagatelle.fabricskyboxes.util.object.Textures;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;

import net.minecraft.screen.PlayerScreenHandler;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class Test implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            return;
        }
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Path monoPath = FabricLoader.getInstance().getGameDir().resolve("mono_test.json");
            if (!Files.exists(monoPath)) {
                Files.createFile(monoPath);
            }
            MonoColorSkybox monoColorSkybox = new MonoColorSkybox(
                    new Fade(1000, 2000, 3000, 4000),
                    .5F,
                    .9F,
                    true,
                    new RGBA(.2F, .7F, .5F),
                    false,
                    true,
                    Lists.newArrayList(),
                    Lists.newArrayList(),
                    Lists.newArrayList(),
                    Lists.newArrayList(),
                    new RGBA(.2F, .6F, .2F)
            );
            JsonObject monoColorObject = (JsonObject) MonoColorSkybox.CODEC.encodeStart(JsonOps.INSTANCE, monoColorSkybox).getOrThrow(false, System.err::println);
            monoColorObject.add("schemaVersion", new JsonPrimitive(2));
            monoColorObject.add("type", new JsonPrimitive(monoColorSkybox.getType()));
            Files.write(monoPath, gson.toJson(monoColorObject).getBytes(StandardCharsets.UTF_8));

            Path texPath = FabricLoader.getInstance().getGameDir().resolve("texture_test.json");
            if (!Files.exists(texPath)) {
                Files.createFile(texPath);
            }
            SquareTexturedSkybox squareTexturedSkybox = new SquareTexturedSkybox(
                    new Fade(1000, 2000, 3000, 4000),
                    1,
                    1,
                    true,
                    new RGBA(.5F, .3F, .2F),
                    false,
                    true,
                    Lists.newArrayList(),
                    Lists.newArrayList(),
                    Lists.newArrayList(),
                    Lists.newArrayList(),
                    new Textures(
                            PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
                            PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
                            PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
                            PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
                            PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
                            PlayerScreenHandler.BLOCK_ATLAS_TEXTURE
                    ),
                    Lists.newArrayList(.0F, .0F, .0F),
                    true
            );
            JsonObject squareTexturedObject = (JsonObject) SquareTexturedSkybox.CODEC.encodeStart(JsonOps.INSTANCE, squareTexturedSkybox).getOrThrow(false, System.err::println);
            squareTexturedObject.add("schemaVersion", new JsonPrimitive(2));
            squareTexturedObject.add("type", new JsonPrimitive(monoColorSkybox.getType()));
            Files.write(texPath, gson.toJson(squareTexturedObject).getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }
    }
}