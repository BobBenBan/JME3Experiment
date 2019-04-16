package org.bensnonorg.musicmachine.scene

import com.jme3.scene.Node
import com.jme3.scene.Spatial
import org.bensnonorg.musicmachine.scene.AttachmentAware.Companion.doChildrenUpdate
import org.bensnonorg.musicmachine.scene.AttachmentAware.Companion.doControlUpdate
import java.util.logging.Logger

/**
 * This node implementation of [Hideable] and [AttachmentAware] for nodes
 * and can notify children at appropriate times when [attachUpdate] is called, then
 * functions are instead delegated to [onAttach] [onDetach] and [onParentChange].
 *
 *  All children that implement [AttachmentAware] will be on attachment changes, as well as [Hideable]s
 * If userdata "ControlUpdateAttachment" of a child spatial is anything except null, controls that implement `AttachmentAware`
 * will be updated.
 * ***This does not apply if the NODE already implements [AttachmentAware].*** If control updates are wanted, specify said
 * behavior in [attachUpdate]
 *
 */
open class NotifyingNode(name: String?) : Node(name), Hideable,
                                          AttachmentAware {

    final override var isAttached: Boolean = false
        private set
    override var isHidden: Boolean = false
    final override var parentsAreHidden: Boolean = false
        private set

    init {
        doChildrenUpdate = true
        doControlUpdate = true
    }

    override fun setParent(newParent: Node?) {
        val wasAttached = isAttached
        super.setParent(newParent)
        isAttached = newParent.findIsAttached()
        attachUpdate(AttachmentAware.Update.getUpdate(wasAttached, isAttached) ?: return)
    }

    override fun attachUpdate(update: AttachmentAware.Update) {
        if (update === AttachmentAware.Update.Detach) {
            this.childrenAttachUpdate(update)
            this.controlAttachUpdate(update)
            onDetach()
        } else {
            if (update === AttachmentAware.Update.Attach) onAttach() else if (update == AttachmentAware.Update.Detach) onParentChange() else throw NotImplementedError()
            this.controlAttachUpdate(update)
            this.childrenAttachUpdate(update)
        }
    }

    protected open fun onDetach() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    protected open fun onParentChange() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    protected open fun onAttach() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
                    false.also { logger.warning("Circular ancestry detected!") }
                else isAttached
            null -> false
            else -> this.getUserData<Boolean>("isRoot") ?: parent.findIsAttached()
        }
    }

    companion object {
        @JvmStatic
        private val logger = Logger.getLogger(NotifyingNode::class.java.name)!!

        @JvmStatic
        fun Spatial.controlAttachUpdate(update: AttachmentAware.Update) {
            if (doControlUpdate)
                if (getUserData<Any>("DoControlAttachmentUpdate") !== null) {
                    controlAttachUpdate(update)
                }
        }

        private fun Node.childrenAttachUpdate(update: AttachmentAware.Update) {
            if (doChildrenUpdate)
                this.children.forEach {
                    if (it is AttachmentAware) it.attachUpdate(update)
                    else it.controlAttachUpdate(update)
                }
        }
    }
}