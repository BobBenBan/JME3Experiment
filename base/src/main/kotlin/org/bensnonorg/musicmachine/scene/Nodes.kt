package org.bensnonorg.musicmachine.scene

import com.jme3.asset.AssetManager
import com.jme3.audio.AudioData
import com.jme3.audio.AudioNode
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.collision.PhysicsCollisionEvent
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import org.bensnonorg.musicmachine.kotlin.CallSuper
import org.bensnonorg.musicmachine.kotlin.StrictCallSuper
import org.bensnonorg.musicmachine.kotlin.SuperCalled
import org.bensnonorg.musicmachine.kotlin.TopClass
import org.bensnonorg.musicmachine.physics.PhysicalObject
import org.bensnonorg.musicmachine.physics.SpatialCollisionListener
import org.bensnonorg.musicmachine.scene.AugmentedNode.Update.*
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
	private var isAttached = false
	protected open fun onEnable(): StrictCallSuper {
		return TopClass
	}

	protected open fun onDisable(): StrictCallSuper {
		return TopClass
	}

	protected enum class Update {
		Attached, Detached, Changed
	}

	override fun setParent(newParent: Node?) {
		super.setParent(newParent)
		val update: Update =
			if (isAttached) {
				if (newParent.isAttached()) Changed else Detached
			} else {
				if (newParent.isAttached()) Attached else return
			}
		onUpdate(update)
	}

	private fun Node?.isAttached(): Boolean {
		return when (this) {
			null -> false
			is AugmentedNode -> this.isAttached
			else -> getUserData<Boolean>("isRoot") ?: parent.isAttached()
		}
	}

	protected fun onUpdate(update: Update, source: Boolean = true) {
		when (update) {
			Attached -> onAttached(source)
			Detached -> onDetached(source)
			Changed -> onChanged(source)
		}
		propagateUpdate(update)
	}

	private fun propagateUpdate(update: Update) {
		for (c in children) {
			if (c is AugmentedNode) c.onUpdate(update, false)
		}
	}

	protected open fun onAttached(source: Boolean): StrictCallSuper {
		isAttached = true
		return TopClass
	}

	protected open fun onDetached(source: Boolean): StrictCallSuper {
		isEnabled = false//do not try to run if not attached
		isAttached = false
		return TopClass
	}

	protected open fun onChanged(source: Boolean): CallSuper = TopClass
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
	override fun onDetached(source: Boolean): StrictCallSuper {
		super.onDetached(source)
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