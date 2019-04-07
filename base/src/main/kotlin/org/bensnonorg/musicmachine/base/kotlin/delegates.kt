package org.bensnonorg.musicmachine.base.kotlin

import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/** represents empty value */
private object EMPTY

/** Property that acts like a "lateinit val": can only be set once, and can only be used once set. */
@Suppress("UNCHECKED_CAST")
class InitOnceProperty<T> internal constructor() : ReadWriteProperty<Any, T> {

	private var theVal: Any? = EMPTY
	override fun getValue(thisRef: Any, property: KProperty<*>): T {
		if (!isInitialized) throw IllegalStateException("Value is not initialized")
		else return theVal as T
	}

	override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
		if (isInitialized) throw IllegalStateException("Value is already initialized")
		theVal = value
	}

	val isInitialized get() = theVal !== EMPTY
}

/** Creates a [InitOnceProperty] */
fun <T> initOnce() = InitOnceProperty<T>()

/**
 * A object that represents a [block] that can only be run once.
 */
open class RunOnce(private val block: () -> Unit) {

	var hasRun = false
		private set

	operator fun invoke() {
		if (!hasRun) {
			hasRun = true
			block()
		}
	}
}

/**
 * A delegate that is readOnly that returns the value returned by [producer] on every get
 */
class Factory<R, T>(private val producer: () -> T) : ReadOnlyProperty<R, T> {

	override fun getValue(thisRef: R, property: KProperty<*>): T = producer()
}