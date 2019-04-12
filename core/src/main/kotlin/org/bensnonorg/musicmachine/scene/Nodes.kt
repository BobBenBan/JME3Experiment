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
 * Indicates something can be enabled and disabled. Actual behavior depends on implementation
 */
interface Enableable {

    /** If this object is enabled or not */
    var isEnabled: Boolean
}

/**
 * Special enabling extension that distinguishes _effective enabling_ from this node's individual enable setting.
 * The standard is that effective enabling only occurs if all parents of this node are enabled and the current node is
 * also enabled. In this way individual sections can be both enabled and disabled.
 *
 * Also, effectiveness notification can be done both Pre-order and Post-order on a update on the scene graph, using
 * [preEffective] and [postEffective]
 */
interface NodeEnableable : Enableable {

    fun preEffective(isEnabled: Boolean)
    fun postEffective(isEnabled: Boolean)
}

/**
 * A node that includes other useful features.
 *
 * This node keeps track of when it is attached to or not attached to a scene, to perform possible refreshing options.
 *   - Overridable functions [onAttached], [onDetached], and [onChanged] will be called when, respectively:
 *     - A node was attached when not previously,
 *     - A node is detached when previously was attached;
 *     - A node is still attached but has changed parents.
 *   - A child of this node that is an `AugmentedNode` will also be notified on being attached or detached.
 *   - A parent will always have [onAttached] before its children, and children [onDetached] before [onAttached].
 *   - The source parameter will inform whether if the update was caused by the immediate parent change or not; in this
 *     way certain updates can be selectively filtered out.
 *
 *  This node implements [NodeEnableable], and all immediate parents or children that implement NodeEnableable or have
 *  a NodeEnableControl will continue `NodeEnableable` as described. Otherwise, nodes are assumed to be always enabled.
 */
abstract class AugmentedNode protected constructor(name: String?) : Node(name), NodeEnableable {

    override var isEnabled: Boolean = false
    /**
     * True if an only if this enabled and parents enabled.
     */
    private var canBeEffective: Boolean = false
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
            is AugmentedNode -> parent.isEffectivelyEnabled
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