package io.github.amerebagatelle.fabricskyboxes.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.amerebagatelle.fabricskyboxes.SkyBoxes;
import java.util.Optional;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class JsonObjectWrapper {
    private JsonObject focusedObject;

    public JsonElement get(String memberName) {
        return this.focusedObject.get(memberName);
    }

    public Identifier getJsonStringAsId(String key) {
        if (!this.contains(key)) {
            SkyBoxes.getLogger().warn("Could not find Identifier with key \"" + key + "\"");
            SkyBoxes.getLogger().debug(new Throwable());
            SkyBoxes.getLogger().debug(this.getFocusedObject().toString());
            return null;
        }
        return new Identifier(this.focusedObject.get(key).getAsString());
    }

    public Optional<JsonElement> getOptionalValue(String key) {
        if (!this.contains(key)) {
            SkyBoxes.getLogger().debug(String.format("Optional value %s not set.", key));
        }
        return Optional.ofNullable(this.focusedObject.get(key));
    }

    public float getOptionalFloat(String key, float defaultValue) {
        if (this.getOptionalValue(key).isEmpty()) {
            return defaultValue;
        }
        JsonElement element = this.getOptionalValue(key).get();
        return JsonHelper.isNumber(element) ? element.getAsFloat() : defaultValue;
    }

    public boolean getOptionalBoolean(String key, boolean defaultValue) {
        if (this.getOptionalValue(key).isEmpty()) {
            return defaultValue;
        }
        JsonElement element = this.getOptionalValue(key).get();
        return element.getAsJsonPrimitive().isBoolean() ? element.getAsBoolean() : defaultValue;
    }

    public float getOptionalArrayFloat(String key, int index, float defaultValue) {
        if (this.getOptionalValue(key).isEmpty()) {
            return defaultValue;
        }
        JsonElement element = this.getOptionalValue(key).get();
        return element.isJsonArray() ? element.getAsJsonArray().get(index).getAsFloat() : defaultValue;
    }

    public boolean contains(String key) {
        return this.focusedObject.has(key);
    }

    public JsonObject getFocusedObject() {
        return this.focusedObject;
    }

    public void setFocusedObject(JsonObject focusedObject) {
        this.focusedObject = focusedObject;
    }
}
