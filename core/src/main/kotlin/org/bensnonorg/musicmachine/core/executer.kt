package org.bensnonorg.musicmachine.core

import org.bensnonorg.musicmachine.core.util.logIf
import java.util.*
import java.util.concurrent.Executor
import java.util.function.IntUnaryOperator
import java.util.logging.Level
import java.util.logging.Logger

/**
 * An executor that can accumulate and store runnable tasks then run them at a later specified time.
 * Runnables will be executed in the order they were submitted.
 */
interface BufferedExecutor : Executor {

    override fun execute(runnable: Runnable)

    fun executeNow()

    fun cancel(runnable: Runnable)
}

open class ExecutionException : RuntimeException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(
        message, cause, enableSuppression, writableStackTrace
    )
}

class BatchExecutor(private val getBatchSize: IntUnaryOperator) : BufferedExecutor {

    private val queue = LinkedHashSet<Runnable>()
    override fun execute(runnable: Runnable) {
        logger.log(Level.FINEST, "Adding a runnable")
        queue.add(runnable)
    }

    override fun cancel(runnable: Runnable) {
        queue.remove(runnable)
    }

    override fun executeNow() {
        val batchSize = getBatchSize(queue.size)
        var numExecuted = 0
        logger.logIf(Level.FINER) { "Execution batch of size $batchSize from queue size ${queue.size}" }
        val iterator = queue.iterator()
        while (iterator.hasNext() || numExecuted++ < batchSize) {
            try {
                iterator.next().run()
            } finally {
                iterator.remove()
            }
        }
        logger.logIf(Level.FINEST) { "Finished actually executing $numExecuted" }
    }

    private companion object {
        @JvmStatic
        private val logger = Logger.getLogger(BatchExecutor::class.java.name)!!
    }
}

