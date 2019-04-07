package org.bensnonorg.musicmachine.base.jmeextensions.controls

import com.jme3.export.JmeExporter
import com.jme3.export.JmeImporter
import com.jme3.export.Savable
import com.jme3.input.InputManager
import com.jme3.input.controls.KeyTrigger
import com.jme3.input.controls.MouseAxisTrigger
import com.jme3.input.controls.MouseButtonTrigger
import com.jme3.input.controls.Trigger
import org.bensnonorg.musicmachine.base.kotlin.initOnce
import java.io.IOException

/**
 * Manages configurable custom keybindngs.
 * This contains a constant set of mapping names, which each map to (as of now) only one Trigger.
 * this is [Savable], and hence only limited trigger types supported.
 */
@Suppress("unused")
class KeyBindingManager : Savable {

	private var bindings: Map<String, SavableTrigger> by initOnce()

	/**
	 * For Savable. Do not use.
	 */
	constructor()

	/**
	 * Creates a key binding initialized to the given [map] of mappingNames to triggers.
	 *
	 * @throws UnsupportedOperationException if any triggers types in [map] is not supported.
	 */
	constructor(map: Map<String, Trigger>) {
		bindings = map.map {
			it.key to SavableTrigger(
				it.value
			)
		}.toMap(HashMap())
	}

	/**
	 * Creates a key bindings with the given [mappingNames] and empty triggers.
	 */
	constructor(mappingNames: List<String>) {
		bindings = mappingNames.map { it to SavableTrigger() }.toMap(HashMap())
	}

	/**
	 * Sets the trigger for the [mappingName] to the [trigger].
	 * @throws IllegalArgumentException if the given [mappingName] does not exist in this key bind setup
	 * @throws UnsupportedOperationException if [trigger] type is not supported
	 */
	operator fun set(mappingName: String, trigger: Trigger) {
		val savableTrigger = bindings[mappingName] ?: throw IllegalArgumentException(
			"The given name $mappingName does not exist in this key bind setup"
		)
		savableTrigger.trigger = trigger
	}

	operator fun get(mappingName: String): Trigger? {
		return bindings[mappingName]?.trigger
	}

	/**
	 * Adds this key binding configuration onto [inputManager]
	 */
	fun addOn(inputManager: InputManager) {
		for ((name, t) in this.bindings)
			inputManager.addMapping(name, t.trigger)
	}

	/**
	 * Removes all mapping names from the [inputManager]
	 */
	fun removeOn(inputManager: InputManager) {
		for (name in bindings.keys)
			inputManager.deleteMapping(name)
	}

	/**
	 * Removes and re-adds this configuration onto [inputManager], effectively resetting it
	 */
	fun resetOn(inputManager: InputManager) {
		removeOn(inputManager)
		addOn(inputManager)
	}

	override fun write(ex: JmeExporter) {
		val capsule = ex.getCapsule(this)
		capsule.writeStringSavableMap(bindings, "keyBindings", null)
	}

	@Suppress("UNCHECKED_CAST")
	override fun read(im: JmeImporter) {
		val capsule = im.getCapsule(this)
		bindings = capsule.readStringSavableMap(
			"keyBindings", null
		) as MutableMap<String, SavableTrigger>
	}

	/**
	 * A wrapper around a [Trigger] that is savable
	 */
	class SavableTrigger : Savable {

		internal var trigger: Trigger? = null
			set(value) {
				if (value != null)
					TriggerTypes.findType(
						value
					) ?: throw UnsupportedOperationException(
						"The given trigger type ${value::class.simpleName}is not supported"
					)
				field = value
			}

		constructor()
		constructor(t: Trigger) {
			this.trigger = t
		}

		override fun write(ex: JmeExporter) {
			val trigger = this.trigger ?: return
			val type = TriggerTypes.findType(
				trigger
			) ?: throw AssertionError()
			ex.getCapsule(this).apply {
				write(type.getCode(trigger), "code", 0)
				write(type.ordinal, "typeOrdinal", -1)
			}
		}

		override fun read(im: JmeImporter) {
			val capsule = im.getCapsule(this)
			val typeOrdinal = capsule.readInt("typeOrdinal", -1)
			trigger = when (typeOrdinal) {
				-1 -> null
				!in 0 until TriggerTypes.values().size -> throw IOException()
				else -> TriggerTypes.toTrigger(
					capsule.readInt("code", 0), typeOrdinal
				)
			}
		}

		/**
		 * List of supported TriggerTypes, to assist decompose/recompose on save/load
		 */
		private enum class TriggerTypes(
			val clazz: Class<out Trigger>, val getCode: Trigger.() -> Int, val makeNew: (Int) -> Trigger
		) {

			Key(KeyTrigger::class.java, { this as KeyTrigger; keyCode }, ::KeyTrigger),
			MouseButton(
				MouseButtonTrigger::class.java, { this as MouseButtonTrigger; mouseButton }, ::MouseButtonTrigger
			),
			MouseAxis(MouseAxisTrigger::class.java,
			          { this as MouseAxisTrigger; if (isNegative) mouseAxis else -1 - mouseAxis },
			          { MouseAxisTrigger(if (it > 0) it else -1 - it, it < 0) });

			fun isType(trigger: Trigger) = clazz.isInstance(trigger)

			companion object {
				fun findType(trigger: Trigger): TriggerTypes? {
					for (type in values()) if (type.isType(trigger)) return type
					return null
				}

				fun toTrigger(code: Int, typeOrdinal: Int): Trigger {
					return values()[typeOrdinal].makeNew(code)
				}
			}
		}
	}
}
//extensions
/**
 * Adds mappings for the given [keyBindings]
 */
fun InputManager.addKeyBindings(keyBindings: KeyBindingManager) = keyBindings.addOn(this)

/**
 * Removes all mapping names in the given [keyBindings]
 */
fun InputManager.removeKeyBindings(keyBindings: KeyBindingManager) = keyBindings.removeOn(this)

/**
 * Removes and re-adds keybindings in the given [keyBindings], effectively resetting
 */
fun InputManager.resetKeyBindigs(keyBindings: KeyBindingManager) = keyBindings.resetOn(this)