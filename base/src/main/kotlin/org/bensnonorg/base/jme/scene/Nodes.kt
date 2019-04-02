package org.bensnonorg.base.jme.scene

import com.jme3.asset.AssetManager
import com.jme3.audio.AudioData
import com.jme3.audio.AudioNode
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.collision.PhysicsCollisionEvent
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.renderer.queue.RenderQueue
import com.jme3.scene.Node
import com.jme3.scene.Spatial
import org.bensnonorg.base.kotlin.StrictCallSuper
import org.bensnonorg.base.kotlin.TopClass
import org.bensnonorg.base.jme.scene.AugmentedNode.Update.*
import java.util.logging.Logger

/**
 * A node that includes many other useful features, including:
 * - Being able to recursively call to notify if a node has been detached from the scene
 * - Possibly More
 */
abstract class AugmentedNode constructor(name: String?) : Node(name) {

	/**
	 * Additional Initialization to be called AFTER initialization.
	 * Things to do here, NOT in init blocks:
	 * - Add controls
	 * - Do things BEFORE controls are added, such as translate nodes
	 * - Initialize Renderer, Filters, etc.
	 */
	private var initialized = false

	protected open fun init(): StrictCallSuper {
		if (initialized) throw IllegalStateException("Already Initialized")
		initialized = true
		return TopClass
	}

	fun ensureInit() {
		if (!initialized) init()
	}

	protected enum class Update {
		Attached, Detached, Changed
	}

	override fun setParent(newParent: Node?) {
		val oldParent = parent
		super.setParent(newParent)
		val update: Update = if (oldParent === null) {
			if (newParent !== null) Attached else return
		} else {
			if (newParent === null) Detached else Changed
		}
		onUpdate(update)
	}

	protected fun onUpdate(update: Update) {
		val b = when (update) {
			Attached -> onAttached()
			Detached -> onDetached()
			Changed -> onChanged()
		}
		if (b) for (c in children) {
			if (c is AugmentedNode) c.onUpdate(update)
		}
	}

	protected open fun onAttached(): Boolean {
		ensureInit()
		return true
	}

	protected open fun onDetached() = true
	protected open fun onChanged() = true
	final override fun attachChild(child: Spatial) = super.attachChild(child)
	final override fun attachChildAt(child: Spatial, index: Int) = super.attachChildAt(child, index)

	companion object {
		@JvmStatic
		val logger: Logger = Logger.getLogger(AugmentedNode::class.simpleName)
	}
}

open class PhysicalObject protected constructor(
		name: String?, spatial: Spatial, private val physicsSpace: PhysicsSpace, private val mass: Float
) : AugmentedNode(name) {

	override fun init(): StrictCallSuper {
		shadowMode = RenderQueue.ShadowMode.CastAndReceive
		addControl(RigidBodyControl(mass))
		return super.init()
	}

	var hardness = DEFAULT_HARDNESS
	val rigidBodyControl get() = getControl(RigidBodyControl::class.java)!!
	val spatial: Spatial get() = getChild(0)

	init {
		attachChild(spatial)
	}

	override fun onAttached(): Boolean {
		super.onAttached()
		physicsSpace.add(this)
		return true
	}

	override fun onDetached(): Boolean {
		physicsSpace.remove(this)
		return true
	}

	companion object {
		const val DEFAULT_HARDNESS = 1f
		fun hardnessOf(other: Spatial) = if (other is PhysicalObject) other.hardness else DEFAULT_HARDNESS
	}
}

abstract class BangingObject(
		name: String?, spatial: Spatial, physicsSpace: PhysicsSpace, mass: Float, assetManager: AssetManager,
		audioName: String
) : PhysicalObject(name, spatial, physicsSpace, mass), SpatialCollisionListener {

	override fun onCollision(event: PhysicsCollisionEvent, other: Spatial) {
		onStrike(event.appliedImpulse, hardnessOf(other))
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
	override fun onDetached(): Boolean {
		audioNode.stop()
		return super.onDetached()
	}

	override fun clone(useMaterials: Boolean): BangingObject = super.clone(useMaterials) as BangingObject
	abstract fun onStrike(force: Float, hardness: Float)
}
