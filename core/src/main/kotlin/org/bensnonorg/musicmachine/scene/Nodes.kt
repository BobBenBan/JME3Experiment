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

/**
 * A node that includes other useful features.
 *
 * ***Most of these features only work properly when there is a chain of augmented nodes from any augmented node to the root***
 *
 * This node keeps track of when it is attached to or not attached to a scene, to perform possible refreshing options.
 *   - Overridable functions [onAttached], [onDetached], and [onChanged] will be called when, respectively:
 *     - A node was attached when not previously,
 *     - A node is detached when previously was attached;
 *     - A node is still attached but has changed parents.
 *   - A child of this node that is an `AugmentedNode` will also be notified on being attached or detached.
 *
 *  Nodes can be individually disabled or enabled. Any node can be individually set to be disabled.
 *   - However, a node can only be effectively enabled if:
 *     - it is Attached; and
 *     - all its parents (up to the root node, or first non-augmented node) is enabled
 *   - In this way, individual nodes or entire sections can be individually enabled or disabled.
 *   - Overridable functions [onEnable] and [onDisable] will be called when a node is _effectively_ enabled or disabled.
 *   - Actual behavior of enabling/disabling depends on subclass behavior.
 *
 *  When created, by default nodes are both detached and disabled.

 */
abstract class AugmentedNode protected constructor(name: String?) : Node(name) {

	/**
	 * @param action the action to be performed on children, and will recurse on the children's children
	 *               if return value is true.
	 */
	private inline fun forAugChildren(action: (AugmentedNode) -> Unit) {
		for (c in children) if (c is AugmentedNode) {
			action(c)
		}
	}

	/**
	 * If this node is individually enabled or not.
	 * _Effective_ enabling will only occur if all parents are enabled and the node is attached.
	 * @see [isEffective]
	 */
	var isEnabled: Boolean = false
		set(enabled) {
			if (field == enabled) return //no change.
			field = enabled
			//notify from the top down.
			if (enabled) {
				if (isParentsEnabled) {
					//continue the parents enabled chain
					forAugChildren { it.isParentsEnabled = true }
				} else {
				} //still not parents enabled.
			} else { //set not enabled
				if (isParentsEnabled) {
					forAugChildren { it.isParentsEnabled = false }
				}
			}
			checkIsEffective()
		}
	/**
	 * if this node is effective. If so, [onEnable] has been called at some point, else [onDisable] has been called, or the
	 * node was just initialized.
	 */
	var isEffective: Boolean = false
		private set

	private fun checkIsEffective() {
		val wasEffective = isEffective
		isEffective = isAttached && isEnabled && isParentsEnabled
		if (wasEffective != isEffective) if (isEffective) onEnable() else onDisable()
	}

	/**
	 * If this node is attached to a scene. This means that one of this node's ancestors has a
	 * userData boolean "isRoot" of true.
	 *
	 */
	/*
	 * True if: parent isAttached
	 */
	var isAttached = false
		private set

	private fun setIsAttached(isAttached: Boolean, source: Boolean = true) {
		val wasAttached = this.isAttached
		this.isAttached = isAttached
		val update: Update =
			if (wasAttached) {
				if (isAttached) Changed else Detached
			} else {
				if (isAttached) Attached else return
			}
		onUpdate(update, source)
		forAugChildren { it.setIsAttached(isAttached, false) }
		//attached updated first recursively
		//then possibly effectiveness
		checkIsEffective()
	}

	/**
	 * True if
	 *      parent isEnabled
	 *  and parent isParentsEnabled
	 *
	 *  if one changes, other must be true for other to change.
	 */
	private var isParentsEnabled: Boolean = true
		set(parEnabled) {
			if (field == parEnabled) return //the same thing.
			field = parEnabled
			checkIsEffective()
			if (isEnabled) {
				forAugChildren { it.isParentsEnabled = parEnabled }
			}
		}

	protected enum class Update {
		Attached, Detached, Changed
	}

	override fun setParent(newParent: Node?) {
		super.setParent(newParent)
		if (parent == newParent) return
		val parentAttached = newParent.findIsAttached()
		val wasAttached = isAttached
		isAttached = parentAttached
		val update: Update =
			if (wasAttached) {
				if (isAttached) Changed else Detached
			} else {
				if (isAttached) Attached else return
			}
		onUpdate(update, false)
	}

	private tailrec fun Node?.findIsAttached(): Boolean {
		return when (this@findIsAttached) {
			null -> false
			is AugmentedNode -> if (this@findIsAttached === this@AugmentedNode) false else this@findIsAttached.isAttached
			else -> getUserData<Boolean>("isRoot")
				?: parent.findIsAttached()
		}
	}

	private fun onUpdate(update: Update, source: Boolean) {
		when (update) {
			Attached -> onAttached(source)
			Detached -> onDetached(source)
			Changed -> onChanged(source)
		}
	}

	private fun propagateUpdate(update: Update) {
	}

	protected open fun onAttached(source: Boolean): StrictCallSuper {
		isAttached = true
		return TopClass
	}

	protected open fun onDetached(source: Boolean): StrictCallSuper {
		isAttached = false
		return TopClass
	}

	protected open fun onChanged(source: Boolean): CallSuper = TopClass
	protected open fun onEnable(): StrictCallSuper = TopClass
	protected open fun onDisable(): StrictCallSuper = TopClass
	final override fun attachChild(child: Spatial) = super.attachChild(child)
	final override fun attachChildAt(child: Spatial, index: Int) = super.attachChildAt(child, index)
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