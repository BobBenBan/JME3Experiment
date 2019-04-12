package org.bensnonorg.musicmachine.control

import com.jme3.input.InputManager
import com.jme3.input.controls.InputListener
import com.jme3.input.controls.Trigger

@Deprecated("To be replaced with InputManager extention.")
inline fun <reified Val> InputManager.allInOneMultiple(
    actionListener: InputListener, valToTrigger: (Val) -> Trigger, vararg values: Any
) {
    var name: String? = null
    for (value in values)
        when (value) {
            is String -> {
                if (name != null) this.addListener(actionListener, name)
                name = value
            }
            is Val -> this.addMapping(
                name ?: throw IllegalArgumentException(),
                valToTrigger(value)
            )
            else -> throw IllegalArgumentException()
        }
    this.addListener(actionListener, name ?: throw IllegalArgumentException())
}