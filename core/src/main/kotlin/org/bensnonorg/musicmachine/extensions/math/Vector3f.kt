package org.bensnonorg.musicmachine.extensions.math

import com.jme3.math.Vector3f

operator fun Vector3f.minus(it: Vector3f): Vector3f = this.subtract(it)
operator fun Vector3f.plus(it: Vector3f): Vector3f = this.add(it)
operator fun Vector3f.times(it: Float): Vector3f = this.mult(it)
operator fun Vector3f.div(it: Float): Vector3f = this.mult(1 / it)
infix fun Vector3f.set(value: Vector3f): Vector3f = this.set(value)
operator fun Vector3f.unaryMinus(): Vector3f = this.negate()
operator fun Vector3f.unaryPlus(): Vector3f = this.clone()
operator fun Vector3f.minusAssign(it: Vector3f) {
    this.subtractLocal(it)
}

operator fun Vector3f.plusAssign(it: Vector3f) {
    this.addLocal(it)
}

operator fun Vector3f.timesAssign(it: Float) {
    this.multLocal(it)
}

operator fun Vector3f.divAssign(it: Float) {
    this.multLocal(1 / it)
}

