package org.bensnonorg.musicmachine.kotlin

@Deprecated("Use @CallSuper instead", replaceWith = ReplaceWith("Unit"))
/**
 * Utility marker class for "enforcing" call super.
 * Use this as return value to remind implementers to call super
 */
sealed class CallSuper

@Deprecated("Use @CallSuper instead", replaceWith = ReplaceWith("Unit"))
/**
 * A stricter [CallSuper] that does not allow [SkipCall] to be returned
 */
sealed class StrictCallSuper : Unit()

@Deprecated("Use @CallSuper instead", replaceWith = ReplaceWith("Unit"))
/**
 * Used to indicate this is the top class and therefore no super call needed.
 * @see CallSuper
 */
object TopClass : Unit()

@Deprecated("Use @CallSuper instead", replaceWith = ReplaceWith("Unit"))
/**
 * Used to indicate that super has already been called somewhere else.
 * If not, spank the programmer who did this
 * @see CallSuper
 */
object SuperCalled : Unit()

@Deprecated("Use @CallSuper instead", replaceWith = ReplaceWith("Unit"))
/**
 * Used to indicate super will not be called.
 * Not applicable to [StrictCallSuper]
 * @see CallSuper
 */
object SkipCall : Unit()