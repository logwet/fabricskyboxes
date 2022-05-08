package io.github.amerebagatelle.fabricskyboxes.skyboxes;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.Registrar;
import io.github.amerebagatelle.fabricskyboxes.SkyBoxes;
import io.github.amerebagatelle.fabricskyboxes.skyboxes.textured.AnimatedSquareTexturedSkybox;
import io.github.amerebagatelle.fabricskyboxes.skyboxes.textured.SingleSpriteAnimatedSquareTexturedSkybox;
import io.github.amerebagatelle.fabricskyboxes.skyboxes.textured.SingleSpriteSquareTexturedSkybox;
import io.github.amerebagatelle.fabricskyboxes.skyboxes.textured.SquareTexturedSkybox;
import java.lang.reflect.Array;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SkyboxType<T extends AbstractSkybox> {
    public static final RegistryKey<Registry<SkyboxType<? extends AbstractSkybox>>> REGISTRY_KEY;
    public static final Registrar<SkyboxType<? extends AbstractSkybox>> REGISTRY;
    public static final Supplier<SkyboxType<MonoColorSkybox>> MONO_COLOR_SKYBOX;
    public static final Supplier<SkyboxType<SquareTexturedSkybox>> SQUARE_TEXTURED_SKYBOX;
    public static final Supplier<SkyboxType<SingleSpriteSquareTexturedSkybox>> SINGLE_SPRITE_SQUARE_TEXTURED_SKYBOX;
    public static final Supplier<SkyboxType<AnimatedSquareTexturedSkybox>> ANIMATED_SQUARE_TEXTURED_SKYBOX;
    public static final Supplier<SkyboxType<SingleSpriteAnimatedSquareTexturedSkybox>> SINGLE_SPRITE_ANIMATED_SQUARE_TEXTURED_SKYBOX;
    public static final Codec<Identifier> SKYBOX_ID_CODEC;

    private final BiMap<Integer, Codec<T>> codecBiMap;
    private final boolean legacySupported;
    private final String name;
    @Nullable
    private final Supplier<T> factory;
    @Nullable
    private final LegacyDeserializer<T> deserializer;

    private SkyboxType(BiMap<Integer, Codec<T>> codecBiMap, boolean legacySupported, String name, @Nullable Supplier<T> factory, @Nullable LegacyDeserializer<T> deserializer) {
        this.codecBiMap = codecBiMap;
        this.legacySupported = legacySupported;
        this.name = name;
        this.factory = factory;
        this.deserializer = deserializer;
    }

    public String getName() {
        return this.name;
    }

    public boolean isLegacySupported() {
        return this.legacySupported;
    }

    @NotNull
    public T instantiate() {
        return Objects.requireNonNull(Objects.requireNonNull(this.factory, "Can't instantiate from a null factory").get());
    }

    @Nullable
    public LegacyDeserializer<T> getDeserializer() {
        return this.deserializer;
    }

    public Identifier createId(String namespace) {
        return this.createIdFactory().apply(namespace);
    }

    public Function<String, Identifier> createIdFactory() {
        return (ns) -> new Identifier(ns, this.getName().replace('-', '_'));
    }

    public Codec<T> getCodec(int schemaVersion) {
        return Objects.requireNonNull(this.codecBiMap.get(schemaVersion), String.format("Unsupported schema version '%d' for skybox type %s", schemaVersion, this.name));
    }

    private static <T extends AbstractSkybox> Supplier<SkyboxType<T>> register(SkyboxType<T> type) {
        return SkyboxType.REGISTRY.register(type.createId(SkyBoxes.MODID), () -> type);
    }

    static {
        Identifier registryId = new Identifier(SkyBoxes.MODID, "skybox_type");
        REGISTRY_KEY = RegistryKey.ofRegistry(registryId);
        REGISTRY = SkyBoxes.REGISTRIES.get().builder(registryId,
            (SkyboxType<? extends AbstractSkybox>[]) Array.newInstance(SkyboxType.class, 0)).build();

        MONO_COLOR_SKYBOX = register(SkyboxType.Builder.create(MonoColorSkybox.class, "monocolor").legacySupported().deserializer(LegacyDeserializer.MONO_COLOR_SKYBOX_DESERIALIZER.get()).factory(MonoColorSkybox::new).add(2, MonoColorSkybox.CODEC).build());
        SQUARE_TEXTURED_SKYBOX = register(SkyboxType.Builder.create(SquareTexturedSkybox.class, "square-textured").deserializer(LegacyDeserializer.SQUARE_TEXTURED_SKYBOX_DESERIALIZER.get()).legacySupported().factory(SquareTexturedSkybox::new).add(2, SquareTexturedSkybox.CODEC).build());
        SINGLE_SPRITE_SQUARE_TEXTURED_SKYBOX = register(SkyboxType.Builder.create(SingleSpriteSquareTexturedSkybox.class, "single-sprite-square-textured").add(2, SingleSpriteSquareTexturedSkybox.CODEC).build());
        ANIMATED_SQUARE_TEXTURED_SKYBOX = register(SkyboxType.Builder.create(AnimatedSquareTexturedSkybox.class, "animated-square-textured").add(2, AnimatedSquareTexturedSkybox.CODEC).build());
        SINGLE_SPRITE_ANIMATED_SQUARE_TEXTURED_SKYBOX = register(SkyboxType.Builder.create(SingleSpriteAnimatedSquareTexturedSkybox.class, "single-sprite-animated-square-textured").add(2, SingleSpriteAnimatedSquareTexturedSkybox.CODEC).build());
        SKYBOX_ID_CODEC = Codec.STRING.xmap((s) -> {
            if (!s.contains(":")) {
                return new Identifier(SkyBoxes.MODID, s.replace('-', '_'));
            }
            return new Identifier(s.replace('-', '_'));
        }, (id) -> {
            if (id.getNamespace().equals(SkyBoxes.MODID)) {
                return id.getPath().replace('_', '-');
            }
            return id.toString().replace('_', '-');
        });
    }

    public static class Builder<T extends AbstractSkybox> {
        private String name;
        private final ImmutableBiMap.Builder<Integer, Codec<T>> builder = ImmutableBiMap.builder();
        private boolean legacySupported = false;
        private Supplier<T> factory;
        private LegacyDeserializer<T> deserializer;

        private Builder() {
        }

        public static <S extends AbstractSkybox> Builder<S> create(@SuppressWarnings("unused") Class<S> clazz, String name) {
            Builder<S> builder = new Builder<>();
            builder.name = name;
            return builder;
        }

        public static <S extends AbstractSkybox> Builder<S> create(String name) {
            Builder<S> builder = new Builder<>();
            builder.name = name;
            return builder;
        }

        protected Builder<T> legacySupported() {
            this.legacySupported = true;
            return this;
        }

        protected Builder<T> factory(Supplier<T> factory) {
            this.factory = factory;
            return this;
        }

        protected Builder<T> deserializer(LegacyDeserializer<T> deserializer) {
            this.deserializer = deserializer;
            return this;
        }

        public Builder<T> add(int schemaVersion, Codec<T> codec) {
            Preconditions.checkArgument(schemaVersion >= 2, "schema version was lesser than 2");
            Preconditions.checkNotNull(codec, "codec was null");
            this.builder.put(schemaVersion, codec);
            return this;
        }

        public SkyboxType<T> build() {
            if (this.legacySupported) {
                Preconditions.checkNotNull(this.factory, "factory was null");
                Preconditions.checkNotNull(this.deserializer, "deserializer was null");
            }
            return new SkyboxType<>(this.builder.build(), this.legacySupported, this.name, this.factory, this.deserializer);
        }
    }
}
