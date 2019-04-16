package org.bensnonorg.musicmachine.scene

import com.jme3.scene.Spatial

/**
 * Indicates that a spatial, control, etc. wants to be notified when it is attached or detached from the scene.
 *
 *   [onAttach] will be called when a node _or ancestor_ is attached to the root node, [onDetach] when detached from the scene,
 *   and [onParentChange] when the node is still attached to the scene but has changed locations.
 *
 * Some node up graph must override either [Spatial.setParent] to dispatch these updates
 * Otherwise, a control can be used that checks parents, but these updates may happen one update cycle late.
 *
 *
 *   - It is recommended that children or controls also be notified of attachment updates if they implement
 *   `AttachmentAware`
 *   [isAttached] should be used to cache if the current node is attached or detached.
 */
interface AttachmentAware {

    /** This is for use as a buffer only to avoid searching through the entire scene */
    val isAttached: Boolean

    fun onAttach()
    fun onDetach()
    fun onParentChange()
    enum class Update {
        /**This node has been attached to a root node through a parent or through self */
        Attach,
        /** A node is detached from a root node. */
        Detach,
        /** A node is still attached but has changed parents.*/
        Change;
    }

    companion object {
        const val DO_CONTROL_UPDATE: String = "AttachmentAware DO_CONTROL_UPDATE"
        const val DO_CHILDREN_UPDATE: String = "AttachmentAware DO_CHILDREN_UPDATE"
        /**
         * Finds the corresponding update type based on previous attachment state.
         */
        @JvmStatic
        fun getUpdate(wasAttached: Boolean, isAttached: Boolean): Update? {
            return if (wasAttached) {
                if (isAttached) Update.Change else Update.Detach
            } else if (isAttached) Update.Attach else null
        }

        @JvmStatic
        var Spatial.doControlUpdate
            get() = getUserData<Any>(DO_CONTROL_UPDATE) != null
            set(doUpdate) = setUserData(DO_CONTROL_UPDATE, if (doUpdate) true else null)
        @JvmStatic
        var Spatial.doChildrenUpdate
            get() = getUserData<Any>(DO_CHILDREN_UPDATE) != null
            set(doUpdate) = setUserData(DO_CHILDREN_UPDATE, if (doUpdate) true else null)
    }
}