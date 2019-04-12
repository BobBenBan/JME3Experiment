package org.bensnonorg.musicmachine.core

import java.util.*

inline fun run(crossinline block: () -> Unit): Runnable {
	return object : Runnable {
		override fun run() {
			block()
		}
	}
}

interface QueuedExecutor {
	fun queue(runnable: Runnable)
	fun queue(runnable: () -> Unit)/* {
		queue(run(runnable))
	}*/
	fun execute()
}

interface MappingQueuedExecutor : QueuedExecutor
open class ScalableBatchExecutor : QueuedExecutor {
	override fun queue(runnable: () -> Unit) {
		queue(run(runnable))
	}

	private var numUpdates = 0
	private val queue: Queue<Runnable> = LinkedList()
	override fun queue(runnable: Runnable) {
		queue.add(runnable)
	}

	override fun execute() {
		var num = 0
		while (queue.isNotEmpty()) {
			if (num++ < numUpdates) {
				queue.remove().run()
			}
		}
	}
}