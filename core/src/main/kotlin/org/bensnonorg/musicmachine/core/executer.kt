package org.bensnonorg.musicmachine.core

import org.bensnonorg.musicmachine.core.util.logIf
import java.util.*
import java.util.function.IntUnaryOperator
import java.util.logging.Level
import java.util.logging.Logger

/**
 * An executor that can accumulate and store runnable tasks then run them at a later specified time.
 * Runnables shall be executed in the order they were submitted.
 */
interface BufferedExecutor {

	fun queue(runnable: Runnable)
	fun executeNow()
}

abstract class BaseBufferedExecutor : BufferedExecutor {
	@JvmField
	protected val queue = ArrayDeque<Runnable>()
	override fun queue(runnable: Runnable) {
		queue.add(runnable)
	}
	protected fun executeUpTo(number: Int) {
		var numExecuted = 0
		logger.logIf(Level.FINER) { "Executing up to $number out of ${queue.size}" }
		val iterator: MutableIterator<Runnable> = queue.iterator()
		while (iterator.hasNext() || numExecuted++ < number) {
			try {
				iterator.next().run()
			} finally {
				iterator.remove()
			}
		}
		logger.logIf(Level.FINEST) { "Successfully executed $numExecuted" }
	}

	private companion object {
		@JvmStatic
		private val logger = Logger.getLogger(BatchBufferedExecutor::class.java.name)!!
	}
}

class BatchBufferedExecutor(private val getBatchSize: IntUnaryOperator) : BaseBufferedExecutor() {
	override fun executeNow() {
		executeUpTo(getBatchSize(queue.size))
	}
}

class CompleteBufferedExecutor : BaseBufferedExecutor() {
	override fun executeNow() {
		executeUpTo(queue.size)
	}
}
class SingleExecutor: BaseBufferedExecutor(){
	override fun executeNow() {
		executeUpTo(1)
	}
}
