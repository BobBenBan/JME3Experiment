@file:JvmName("Main")

package org.bensnonorg.musicmachine

import org.bensnonorg.musicmachine.exp.BeepingTeapots
import org.bensnonorg.musicmachine.scene.NotifyingNode

fun main(args: Array<String>) {
    var node: NotifyingNode? = null

    BeepingTeapots().start()
}