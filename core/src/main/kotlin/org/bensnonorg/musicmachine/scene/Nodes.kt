package org.bensnonorg.musicmachine.scene

import com.jme3.asset.AssetManager
import com.jme3.audio.AudioData
import com.jme3.audio.AudioNode
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.collision.PhysicsCollisionEvent
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import org.bensnonorg.musicmachine.physics.PhysicalObject
import org.bensnonorg.musicmachine.physics.SpatialCollisionListener
import org.bensnonorg.musicmachine.scene.AugmentedNode.Update.*

/**
 * A node that includes other useful features.
 *
 * ***Most of these features only work properly when there is a chain of augmented nodes from any augmented node to the root***
 * ***Requires Augmented App***
 * This node keeps track of when it is attached to or not attached to a scene, to perform possible refreshing options.
 *   - Overridable functions [onAttached], [onDetached], and [onChanged] will be called when, respectively:
 *     - A node was attached when not previously,
 *     - A node is detached when previously was attached;
 *     - A node is still attached but has changed parents.
 *   - A child of this node that is an `AugmentedNode` will also be notified on being attached or detached.
 *
 *  Nodes can be individually disabled or enabled. Any node can be individually set to be disabled.
 *   - However, a node can only be effectively enabled if:
 *     - it is attached; and
 *     - all its parents (up to the root node, or first non-augmented node) are enabled
 *   - In this way, individual nodes or entire sections can be individually enabled or disabled.
 *   - Overridable functions [onEnable] and [onDisable] will be called when a node is _effectively_ enabled or disabled.
 *   - Actual behavior of enabling/disabling depends on subclass behavior.
 *   - Parents will be enabled before any children, and children will be disabled before any parents.
 *
 *   - Disable will always occur before Detached, Attached before Enable.
 */
/*TODO
 *       To prevent massive one frame drops when one node with many children is enabled or disabled, a limited
 *       (configurable) number of nodes can be enabled/disabled per frame, queued up and executed.
 *  When created, by default nodes are both detached and disabled.
 */
abstract class AugmentedNode protected constructor(name: String?) : Node(name) {

	/**
	 * If this node is individually enabled or not.
	 * _Effective_ enabling will only occur if all parents are enabled and the node is attached.
	 * @see [isEffective]
	 */
	var isEnabled: Boolean = false
		set(enabled) {
			if (field == enabled) return //no change.
			field = enabled
			//XXX TODO
			forAugNodeChildren(dfsMode = parentFirstif(enabled), recurse = { isParentEffective() }) {
				checkIsEffective()
			}
		}
	/**
	 * if this node is effective. If so, [onEnable] has been called at some point, else [onDisable] has been called, or the
	 * node was just initialized.
	 *
	 * True if enabled and parent is effective (meaning attached).
	 */
	var isEffective: Boolean = false
		private set

	private fun checkIsEffective(source: Boolean = true) {
		val wasEffective = this.isEffective
		this.isEffective = isEnabled && isAttached && isParentEffective()
		if (wasEffective != this.isEffective) if (this.isEffective) onEnable(source) else onDisable(source)
	}

	/**
	 * If this node is attached to a scene. This means that one of this node's ancestors has a
	 * userData boolean "isRoot" of true.
	 *
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
		when (update) {
			Attached -> onAttached(source)
			Detached -> onDetached(source)
			Changed -> onChanged(source)
		}
		//		forAugChildren { it.setIsAttached(isAttached, false) }
		//attached updated first recursively
		//then possibly effectiveness
		checkIsEffective(source) //XXX maybe not right?
	}

	protected enum class Update {
		Attached, Detached, Changed
	}

	override fun setParent(newParent: Node?) {
		super.setParent(newParent)
		if (parent === newParent || parent === this) return
		val nowAttached = newParent.findIsAttached()
		val wasAttached = isAttached
		isAttached = nowAttached
		val update: Update =
			if (wasAttached) {
				if (nowAttached) Changed else Detached
			} else {
				if (nowAttached) Attached else return
			}
		when (update) {
			Attached -> onAttached(true)
			Detached -> onDetached(true)
			Changed -> onChanged(true)
		}
	}

	private fun Node?.findIsAttached(): Boolean {
		return when (this@findIsAttached) {
			is AugmentedNode -> if (this@findIsAttached === this@AugmentedNode) false else this@findIsAttached.isAttached
			null -> false
			else -> getUserData<Boolean>("isRoot")
				?: false
		}
	}

	/**
	 * ONLY TO BE CALLED WHEN isAttached
	 */
	private fun isParentEffective(): Boolean {
		assert(isAttached)
		val parent = parent ?: throw AssertionError()
		return when (parent) {
			is AugmentedNode -> parent.isEffective
			else -> getUserData<Boolean>("isRoot")
				?: throw AssertionError()
		}
	}

	/**
	 * Searches the scene graph possibly recursively through this node on _only [AugmentedNode]_ children.
	 * First runs on current node, then possibly any children.
	 *
	 * @param dfsMode the [Spatial.DFSMode]
	 * @param recurse A function that returns if we should traverse down the children of this node. First called on _this_ node.
	 * @param source information to be passed on about if this the source of the update, default false
	 * @param visit the function to run at the visited node. First called on _this_ node.
	 */
	@JvmOverloads
	fun forAugNodeChildren(
		dfsMode: DFSMode = DFSMode.PRE_ORDER,
		recurse: AugmentedNode.() -> Boolean = { true },
		source: Boolean = false,
		visit: AugmentedNode.(Boolean) -> Unit
	) {
		when (dfsMode) {
			DFSMode.PRE_ORDER -> {
				this.visit(source)
				forAugChildren {
					if (it.recurse())
						it.forAugNodeChildren(dfsMode, recurse, false, visit)
				}
			}
			DFSMode.POST_ORDER -> {
				forAugChildren {
					if (it.recurse())
						it.forAugNodeChildren(dfsMode, recurse, false, visit)
				}
				this.visit(source)
			}
		}
	}

	/**
	 * @param action the action to be performed on children, and will recurse on the children's children
	 *               if return value is true.
	 */
	private inline fun forAugChildren(action: (AugmentedNode) -> Unit) {
		for (c in children.array) if (c is AugmentedNode) action(c)
	}

	protected abstract fun onAttached(source: Boolean)
	protected abstract fun onDetached(source: Boolean)
	protected abstract fun onChanged(source: Boolean)
	protected abstract fun onEnable(source: Boolean)
	protected abstract fun onDisable(source: Boolean)
	final override fun attachChild(child: Spatial) = super.attachChild(child)
	final override fun attachChildAt(child: Spatial, index: Int) = super.attachChildAt(child, index)
}

fun parentFirstif(parentFirst: Boolean) = if (parentFirst) Spatial.DFSMode.PRE_ORDER else Spatial.DFSMode.POST_ORDER
//extention
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
	override fun onDetached(source: Boolean) {
		super.onDetached(source)
		audioNode.stop()
		return Unit
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