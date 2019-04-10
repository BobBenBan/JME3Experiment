package org.bensnonorg.musicmachine.core

import java.util.*

class DelayedExecutor {
	var maxUpdates = 300
	private var numUpdates = 0
	val queue: Queue<Runnable> = LinkedList()
	fun execute(block: Runnable) {
		queue.add(block)
	}

	fun run(maxUpdates: Int = this.maxUpdates) {
		var num = 0
		while (queue.isNotEmpty()) {
			if (num++ < maxUpdates) queue.remove()
		}
	}

	fun run(calcUpdates: (Int) -> Int) {
		run(calcUpdates(queue.size))
	}
}