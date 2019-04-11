package org.bensnonorg.musicmachine.core

import java.util.*

class DelayedExecutor {
	var maxUpdates = 300
	private var numUpdates = 0
	private val queue: Queue<() -> Unit> = LinkedList()
	fun queue(runnable: Runnable) {
		queue.add { runnable.run() }
	}

	fun queue(block: () -> Unit) {
		queue.add(block)
	}
	@JvmOverloads
	fun run(maxUpdates: Int = this.maxUpdates) {
		var num = 0
		while (queue.isNotEmpty()) {
			if (num++ < maxUpdates) queue.remove()()
		}
	}

	fun run(calcUpdates: (Int) -> Int) {
		run(calcUpdates(queue.size))
	}
}