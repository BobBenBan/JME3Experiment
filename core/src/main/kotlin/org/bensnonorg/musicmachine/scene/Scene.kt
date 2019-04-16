package org.bensnonorg.musicmachine.scene

import com.jme3.scene.Node
import com.jme3.scene.Spatial
import com.jme3.scene.control.Control
import kotlinx.coroutines.awaitAll

/**
 * Iterate through all this Spatial's controls.
 */
inline fun Spatial.forEachControl(block: (Control?) -> Unit) {
    for (i in 0 until numControls) {
        block(getControl(i))
    }
}

/**
 * Get the first control of type T
 */
inline fun <reified T> Spatial.getControl(): T? {
    forEachControl { if (it is T) return it }
    return null
}
inline fun <reified T> Spatial.implOf(): T?{
    if(this is T) return this
    return getControl<T>()
}
/**
 * Get the first control of type T
 */
@Suppress("UNCHECKED_CAST")
fun <T> Spatial.getControl(clazz: Class<T>): T? {
    forEachControl { if (clazz.isInstance(it)) return it as T }
    return null
}
/**
 * Get a child from a node by class. Kotlin inline function
 */
inline fun <reified T : Spatial> Node.getChild(): T? {
    for (c in children) if (c is T) return c
    return null
}

/**
 * Get a child from a node by class.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Spatial?> Node.getChild(clazz: Class<T>): T? {
    for (c in children) if (clazz.isInstance(c)) return c as T
    return null
}
//
///**
// * TODO: REMOVE/INLINE/STANDARDIZE
// */
//abstract class BangingObject(
//    name: String?, spatial: Spatial, physicsSpace: PhysicsSpace, mass: Float, assetManager: AssetManager,
//    audioName: String
//) : PhysicalObject(name, spatial, physicsSpace, mass),
//    SpatialCollisionListener {
//
//    val audioNode get() = getChild<AudioNode>()!!
//
//    init {
//        val audioNode = AudioNode(assetManager, audioName, AudioData.DataType.Buffer)
//        attachChild(audioNode)
//        with(audioNode) {
//            this.name = "audioNode"
//            isPositional = true
//            isVelocityFromTranslation = true
//            isReverbEnabled = true
//        }
//    }
//
//    override fun onCollision(event: PhysicsCollisionEvent, other: Spatial) {
//        onStrike(
//            event.appliedImpulse,
//            hardnessOf(other)
//        )
//    }
//
//    override fun onDetached(source: Boolean) {
//        super.onDetached(source)
//        audioNode.stop()
//    }
//
//    override fun clone(useMaterials: Boolean): BangingObject = super.clone(useMaterials) as BangingObject
//    abstract fun onStrike(force: Float, hardness: Float)
//}
//extensions