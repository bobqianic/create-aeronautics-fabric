package dev.simulated_team.simulated.index;

import com.mojang.blaze3d.platform.InputConstants;
import com.zurrtum.create.client.AllKeys;
import dev.simulated_team.simulated.Simulated;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public enum SimKeys {
	ROTATE_MODE("rotate_mode", GLFW.GLFW_KEY_TAB, "Physics Staff Rotate Mode"),
	SCROLL_UP("scroll_up", InputConstants.UNKNOWN.getValue(), "Scroll Up"),
	SCROLL_DOWN("scroll_down", InputConstants.UNKNOWN.getValue(), "Scroll Down"),
	;


	private KeyMapping keybind;
	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Simulated.path("keys"));
	private final String description;
	private final String translation;
	private final int key;
	private final boolean modifiable;
	private final boolean conflictSafe;

	SimKeys(final int defaultKey) {
		this("", defaultKey, "");
	}

	SimKeys(final String description, final int defaultKey, final String translation) {
		this(description, defaultKey, translation, false);
	}

	SimKeys(final String description, final int defaultKey, final String translation, final boolean conflictSafe) {
		this.description = Simulated.MOD_ID + ".keyinfo." + description;
		this.key = defaultKey;
		this.modifiable = !description.isEmpty();
		this.translation = translation;
		this.conflictSafe = conflictSafe;
	}

	public static void registerTo(final Consumer<KeyMapping> consumer) {
		for (final SimKeys key : values()) {
			if (key.conflictSafe) {
				key.keybind = new KeyMapping(key.description, key.key, CATEGORY);
			} else {
				key.keybind = new KeyMapping(key.description, key.key, CATEGORY);
			}
			if (!key.modifiable)
				continue;

			consumer.accept(key.keybind);
		}
	}

	public KeyMapping getKeybind() {
		return this.keybind;
	}

	public boolean isPressed() {
		if (!this.modifiable)
			return AllKeys.isKeyDown(this.key);
		return this.keybind != null && this.keybind.isDown();
	}

	public String getBoundKey() {
		return this.keybind.getTranslatedKeyMessage()
			.getString()
			.toUpperCase();
	}
}
