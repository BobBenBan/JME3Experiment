package org.bensnonorg.musicmachine.physics

import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.renderer.queue.RenderQueue
import com.jme3.scene.Spatial
import org.bensnonorg.musicmachine.scene.AugmentedNode
import org.bensnonorg.musicmachine.kotlin.StrictCallSuper
import org.bensnonorg.musicmachine.kotlin.SuperCalled

/**
 * A node that wraps around a spatial and represents a physical object in space.
 */
open class PhysicalObject protected constructor(
	name: String?, spatial: Spatial, private val physicsSpace: PhysicsSpace, private val mass: Float
) : AugmentedNode(name) {

	override fun init(): StrictCallSuper {
		shadowMode = RenderQueue.ShadowMode.CastAndReceive
		val control = RigidBodyControl(mass)
		addControl(control)
		control.isEnabled = false
		control.physicsSpace = physicsSpace
		return super.init()
	}

	var hardness = DEFAULT_HARDNESS
	val rigidBodyControl get() = getControl(RigidBodyControl::class.java)!!
	val spatial: Spatial get() = getChild(0)

	init {
		attachChild(spatial)
	}

	override fun onAttached(source: Boolean): StrictCallSuper {
		super.onAttached(source)
		rigidBodyControl.isEnabled = true
		return SuperCalled
	}

	override fun onDetached(source: Boolean): StrictCallSuper {
		super.onDetached(source)
		rigidBodyControl.isEnabled = false
		return SuperCalled
	}

	companion object {
		const val DEFAULT_HARDNESS = 1f
		fun hardnessOf(other: Spatial) = if (other is PhysicalObject) other.hardness else DEFAULT_HARDNESS
	}
}