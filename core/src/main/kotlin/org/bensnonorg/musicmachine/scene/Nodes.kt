package org.bensnonorg.musicmachine.scene

import com.jme3.asset.AssetManager
import com.jme3.audio.AudioData
import com.jme3.audio.AudioNode
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.collision.PhysicsCollisionEvent
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import com.jme3.scene.control.Control
import org.bensnonorg.musicmachine.physics.PhysicalObject
import org.bensnonorg.musicmachine.physics.SpatialCollisionListener
import org.bensnonorg.musicmachine.scene.AttachmentAware.Update
import org.bensnonorg.musicmachine.scene.AttachmentAware.Update.*
import java.util.logging.Logger

/**
 * Iterates through this spatial's controls, and runs the block on all of them.
 */
inline fun forControlsIn(spatial: Spatial, block: (Control) -> Unit) {
	for (i in 0 until spatial.numControls) {
		block(spatial.getControl(i))
	}
}

/**
 * Indicates something can be enabled and disabled. Actual behavior depends on implementation.
 */
interface Enableable {

	/** If this is enabled or not */
	var isEnabled: Boolean
}

fun Node.enableAndAttach(child: Spatial) {
	if (child is Enableable) child.isEnabled = true
	this.attachChild(child)
}

fun Node.attachAndEnable(child: Spatial) {
	this.attachChild(child)
	if (child is Enableable) child.isEnabled = true
}

/**
 * Indicates something can be hidden or revealed; makes sense only on [Spatial]s or spatial-related objects.
 * If a parent _node_ is hidden, all children that implement  `Hideable` or have a Control that implement `Hideable` should
 * be notified, and this notification should recurse down through the graph, (but do not have to change their [isHidden] value).
 * Hiding update can both preorder and postorder, using [preHideUpdate] and [postHideUpdate] respectively.
 *
 * Typically hiding can be used to make a node invisible/ineffective as opposed to removing it form the scene, if we
 * can be sure it will be used again.
 * Hiding can result in other behavior depending on implementation.
 */
interface Hideable {

	var isHidden: Boolean
	fun preHideUpdate(isHidden: Boolean)
	fun postHideUpdate(isHidden: Boolean)
}

/**
 * Indicates that a spatial, control, etc. wants to be notified when it is attached or detached from the scene.
 * This is a bridge for NotifyingNode to provide updates.
 *
 * This node keeps track of when it is attached to or not attached to a scene, to perform possible refreshing options.
 *   [attachmentUpdate] will be called when when attachment status change, with its [Update] parameter
 *   representing the type of update
 *
 *   - It is recommended that children's children also be notified of attachment updates if they implement
 *   `AttachmentAware`
 *   - Parents should be attached or changed before its children, and children detached
 *      before the parent.
 *   - The source parameter will inform whether if the update was caused by this node changing parents; otherwise
 *     it was caused by an not-immediate ancestor. In this way certain updates may be selectively filtered out.
 *   - [isAttached] should be used to store info about whether or not this is attached.
 */
interface AttachmentAware {

	val isAttached: Boolean
	fun attachmentUpdate(update: Update)
	enum class Update {
		/**This node has been attached to a root node through a parent or through self */
		Attach,
		/** A node is detached from a root node. */
		Detach,
		/** A node is still attached but has changed parents.*/
		Change;

		companion object {
			/**
			 * Finds the corresponding update type based on previous attachment state.
			 */
			@JvmStatic
			fun getUpdateType(wasAttached: Boolean, isAttached: Boolean): Update? {
				return if (wasAttached) {
					if (isAttached) Change else Detach
				} else if (isAttached) Attach else null
			}
		}
	}
}

private val aaLogger = Logger.getLogger(AttachmentAware::class.java.name)!!

/**
 * This node provides a base implementation of [Hideable] and [AttachmentAware] for nodes
 * and notifies children at appropriate times when [attachmentUpdate] is called, then
 * functions are instead delegated to [onAttach] [onDetach] and [onChange]
 *
 *  This node [Hideable], and [AttachmentAware].
 *  All children or controls that implement [AttachmentAware] will be on attachment changes, as well as [Hideable]s
 */
open class NotifyingNode(name: String?) : Node(name), Hideable, AttachmentAware {

	final override var isAttached: Boolean = false
		private set
	override var isHidden: Boolean = false
	var isEffectivelyHidden: Boolean = false //true if either parent is hidden or I am hidden.
		private set

	private fun controlAttachUpdate(spatial: Spatial, update: Update) {
		if (spatial.getUserData<Any>("NotifyControls") != null) {
			forControlsIn(spatial) {
				if (it is AttachmentAware) it.attachmentUpdate(update)
			}
		}
	}

	override fun attachmentUpdate(update: Update) {
		when (update) {
			Detach -> {
				//children first
				for (c in children.array) {
					if (c is AttachmentAware) c.attachmentUpdate(update)
					controlAttachUpdate(c, update)
				}
				onDetach()
			}
			Attach -> {
				onAttach()
				for (c in children.array) {
					controlAttachUpdate(c, update)
					if (c is AttachmentAware) c.attachmentUpdate(update)
				}
			}
			Change -> TODO()
		}
	}

	override fun setParent(newParent: Node?) {
		val wasAttached = isAttached
		isAttached = newParent.also { super.setParent(it) }.findIsAttached()
		val update = Update.getUpdateType(wasAttached, isAttached) ?: return
		attachmentUpdate(update)
	}

	override fun preHideUpdate(isHidden: Boolean) {
		TODO()
	}

	override fun postHideUpdate(isHidden: Boolean) {
		TODO()
	}

	final override fun attachChild(child: Spatial) = super.attachChild(child)
	final override fun attachChildAt(child: Spatial, index: Int) = super.attachChildAt(child, index)
	/**
	 * Finds if this node is attached, either through itself or ancestor, searching for
	 * [AttachmentAware]s
	 */
	tailrec fun Node?.findIsAttached(): Boolean {
		return when (this) {
			is AttachmentAware ->
				if (this === this@NotifyingNode)
					false.also { aaLogger.warning("Circular ancestry detected!") }
				else isAttached
			null -> false
			else -> this.getUserData<Boolean>("isRoot") ?: parent.findIsAttached()
		}
	}

	companion object {
		@JvmStatic
		private val logger = Logger.getLogger(NotifyingNode::class.java.name)!!
	}
}

fun parentFirstif(parentFirst: Boolean) = if (parentFirst) Spatial.DFSMode.PRE_ORDER else Spatial.DFSMode.POST_ORDER
/**
 * TODO: REMOVE/INLINE/STANDARDIZE
 */
abstract class BangingObject(
	name: String?, spatial: Spatial, physicsSpace: PhysicsSpace, mass: Float, assetManager: AssetManager,
	audioName: String
) : PhysicalObject(name, spatial, physicsSpace, mass),
    SpatialCollisionListener {

	val audioNode get() = getChild<AudioNode>()!!

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

	override fun onCollision(event: PhysicsCollisionEvent, other: Spatial) {
		onStrike(
			event.appliedImpulse,
			hardnessOf(other)
		)
	}

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