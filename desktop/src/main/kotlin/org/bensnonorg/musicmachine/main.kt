@file:JvmName("Main")

package org.bensnonorg.musicmachine

import org.bensnonorg.musicmachine.exp.BeepingTeapots
import org.bensnonorg.musicmachine.scene.AugmentedNode

fun main(args: Array<String>) {
	var node: AugmentedNode? = null

	BeepingTeapots().start()
}