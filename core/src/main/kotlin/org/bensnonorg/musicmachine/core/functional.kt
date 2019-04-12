package org.bensnonorg.musicmachine.core

import java.util.function.*
import java.util.function.Function

operator fun Runnable.invoke() = run()
operator fun IntUnaryOperator.invoke(i: Int) = applyAsInt(i)
operator fun DoubleUnaryOperator.invoke(d: Double) = applyAsDouble(d)
operator fun <T, R> Function<T, R>.invoke(t: T): R = apply(t)
operator fun <T> Consumer<T>.invoke(t:T) {accept(t)}
operator fun <T> Supplier<T>.invoke() = get()