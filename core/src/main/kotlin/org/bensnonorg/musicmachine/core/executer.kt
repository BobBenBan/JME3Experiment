package org.bensnonorg.musicmachine.core

import java.util.*
import java.util.concurrent.Executor

interface KotlinExecutor : Executor {
	fun execute(runnable: () -> Unit) {
		execute(Runnable(runnable))
	}
}

class BatchExecutor : KotlinExecutor {

	var maxUpdates = 300
	private var numUpdates = 0
	private val queue: Queue<Runnable> = LinkedList()
	override fun execute(command: Runnable?) {
		queue.add(command!!)
	}

	@JvmOverloads
	fun run(maxUpdates: Int = this.maxUpdates) {
		var num = 0
		while (queue.isNotEmpty()) {
			if (num++ < maxUpdates) {
				queue.remove().run()
			}
		}
	}

	fun run(calcUpdates: (Int) -> Int) {
		run(calcUpdates(queue.size))
	}
}