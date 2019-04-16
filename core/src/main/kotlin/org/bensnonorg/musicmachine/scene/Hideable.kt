package org.bensnonorg.musicmachine.scene

import com.jme3.scene.Spatial
import com.jme3.scene.control.Control

/**
 * This allows a something can be hidden or revealed, through setting [isHidden]. This is intended as an alternative to
 * frequently detaching and reattaching nodes to a scene, and so on makes sense when related to [Spatial]s.
 * This can be implemented through the node directly, or through [HideableControl]. see [hideableImpl]
 *
 * Hiding can mean simply changing culling, removing physics objects, or possibly other actions.
 *
 * _Effective_ hiding should occur if _either_ this node's [isHidden] value is true or any continuous chain of
 * Hideable parents are hidden (using [parentsHidden]), which can be independent of this node's `isHidden` value.
 * Hiding should be checked whenever either of these two values change. see [isEffectivelyHidden]
 * [parentsHidden] should be updated the following way: if any node has their _effective_ hiding changed,
 * they should notify children's parentsHidden values, looking for a `Hideable`s implementation in the following way:
 *   - If the child implements Hideable, update them.
 *   - _Otherwise_, if the child has a control that implements Hideable, update them too.
 *   - Otherwise, do nothing.
 *
 * Hiding Update can happen in any order as long as parents are updated before children
 * The root node's parentsHidden value should always be `false`.
 *
 * ***Important***
 * If a node wants to guarantee it is notified of hide updates, it should make sure every ancestor either implements
 * Hideable or have a control that implements it, and attach a control if nonexistent. Otherwise hideable updates may not
 * propagate.
 *
 * Also, Depending on specific use, attaching and detaching nodes may cause broken behavior, so it is suggested that
 * Hideable implementations also implement [AttachmentAware] to refresh hidden state using [refreshParentsHidden], and
 * possibly do the Hiding Implementation check as described above.
 *
 * @see isEffectivelyHidden
 */
interface Hideable {

    /** If this node is hidden or not. Can be set to _simulate_ detachment from the scene graph.*/
    var isHidden: Boolean
    /**Used to cache if any parents in the scene graph are hidden. Can get, but do not set directly */
    var parentsHidden: Boolean
}



/** Returns if this node is effectively hidden and should hide, i.e., either it is hidden or parents are hidden. */
val Hideable.isEffectivelyHidden
    get() = isHidden || parentsHidden
/**
 * Returns the spatial if it implements Hideable, else _the first_ control that implements hideable, else null.
 */
val Spatial.hideableImpl: Hideable?
    get() {
        return implOf<Hideable>()
    }

fun Hideable.hide() {
    isHidden = true
}

fun Hideable.reveal() {
    isHidden = false
}

fun Spatial.refreshParentsHidden() {
    val hideableImpl = hideableImpl ?: throw IllegalStateException("This spatial is not Hideable")
    hideableImpl.parentsHidden = parent?.hideableImpl?.isEffectivelyHidden ?: false
}