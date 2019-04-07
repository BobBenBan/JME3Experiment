package org.bensnonorg.musicmachine.base.kotlin

/**
 * Utility marker class for "enforcing" call super.
 * Use this as return value to remind implementers to call super
 */
sealed class CallSuper

/**
 * A stricter [CallSuper] that does not allow [SkipCall] to be returned
 */
sealed class StrictCallSuper : CallSuper()

/**
 * Used to indicate this is the top class and therefore no super call needed.
 * @see CallSuper
 */
object TopClass : StrictCallSuper()

/**
 * Used to indicate that super has already been called somewhere else.
 * If not, spank the programmer who did this
 * @see CallSuper
 */
object SuperCalled : StrictCallSuper()

/**
 * Used to indicate super will not be called.
 * Not applicable to [StrictCallSuper]
 * @see CallSuper
 */
object SkipCall : CallSuper()