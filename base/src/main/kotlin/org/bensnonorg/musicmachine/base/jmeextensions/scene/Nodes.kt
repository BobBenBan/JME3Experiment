package org.bensnonorg.musicmachine.base.jmeextensions.scene

import com.jme3.asset.AssetManager
import com.jme3.audio.AudioData
import com.jme3.audio.AudioNode
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.collision.PhysicsCollisionEvent
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import org.bensnonorg.musicmachine.base.jmeextensions.scene.AugmentedNode.Update.*
import org.bensnonorg.musicmachine.base.kotlin.CallSuper
import org.bensnonorg.musicmachine.base.kotlin.StrictCallSuper
import org.bensnonorg.musicmachine.base.kotlin.SuperCalled
import org.bensnonorg.musicmachine.base.kotlin.TopClass
import org.bensnonorg.musicmachine.base.physics.PhysicalObject
import org.bensnonorg.musicmachine.base.physics.SpatialCollisionListener
import java.util.logging.Logger

/**
 * A node that includes many other useful features, including:
 * - Being able to recursively call to notify if a node has been detached from the scene
 * - Late initialization after construction
 *
 * XXX TODO FIX INIT AND CLONE, STANDARDIZE
 */
open class AugmentedNode protected constructor(name: String?) : Node(name) {

	var isEnabled: Boolean = false
		set(enabled) {
			if (field == enabled) return
			field = enabled
			if (enabled) onEnable() else onDisable()
		}

	protected open fun onEnable(): StrictCallSuper = TopClass
	protected open fun onDisable(): StrictCallSuper = TopClass
	private var isAttached = false

	protected enum class Update {
		Attached, Detached, Changed
	}

	override fun setParent(newParent: Node?) {
		val oldParent = parent
		super.setParent(newParent)
		val update: Update = if (oldParent === null) {
			if (newParent.isAttached()) Attached else return
		} else {
			if (newParent.isAttached()) Changed else Detached
		}
		onUpdate(update)
	}

	private fun Node?.isAttached(): Boolean {
		when (this) {
			null -> return false
			is AugmentedNode -> return this.isAttached
			else -> return getUserData<Boolean>("isRoot") ?: return parent.isAttached()
		}
	}

	protected fun onUpdate(update: Update) {
		when (update) {
			Attached -> onAttached()
			Detached -> onDetached()
			Changed -> onChanged()
		}
		for (c in children) {
			if (c is AugmentedNode) c.onUpdate(update)
		}
	}

	protected open fun onAttached(): StrictCallSuper {
		isAttached = true
		return TopClass
	}

	protected open fun onDetached(): StrictCallSuper {
		isEnabled = false//do not try to run if not attached
		isAttached = false
		return TopClass
	}

	protected open fun onChanged(): CallSuper =
		TopClass

	final override fun attachChild(child: Spatial) = super.attachChild(child)
	final override fun attachChildAt(child: Spatial, index: Int) = super.attachChildAt(child, index)

	companion object {
		@JvmStatic
		val logger: Logger = Logger.getLogger(AugmentedNode::class.simpleName)
	}
}

//extensnion
fun Node.attachAndEnable(node: AugmentedNode) {
	node.isEnabled = true
	this.attachChild(node)
}

/**
 * TODO: REMOVE/INLINE/STANDARDIZE
 */
abstract class BangingObject(
	name: String?, spatial: Spatial, physicsSpace: PhysicsSpace, mass: Float, assetManager: AssetManager,
	audioName: String
) : PhysicalObject(name, spatial, physicsSpace, mass),
    SpatialCollisionListener {

	override fun onCollision(event: PhysicsCollisionEvent, other: Spatial) {
		onStrike(
			event.appliedImpulse,
			hardnessOf(other)
		)
	}

	init {
		val audioNode = AudioNode(assetManager, audioName, AudioData.DataType.Buffer)
		attachChild(audioNode)
		with(audioNode) {
			this.name = "audioNode"
			isPositional = true
			isVelocityFromTranslation = true
			isReverbEnabled = true
		}
	}

	val audioNode get() = getChild<AudioNode>()!!
	override fun onDetached(): StrictCallSuper {
		super.onDetached()
		audioNode.stop()
		return SuperCalled
	}

	override fun clone(useMaterials: Boolean): BangingObject = super.clone(useMaterials) as BangingObject
	abstract fun onStrike(force: Float, hardness: Float)
}

//extensions
@Suppress("UNCHECKED_CAST")
fun <T : Spatial> Node.getChild(clazz: Class<T>): T? {
	for (c in children) if (clazz.isInstance(c)) return c as T
	return null
}

inline fun <reified T : Spatial> Node.getChild(): T? {
	for (c in children) if (c is T) return c
	return null
}