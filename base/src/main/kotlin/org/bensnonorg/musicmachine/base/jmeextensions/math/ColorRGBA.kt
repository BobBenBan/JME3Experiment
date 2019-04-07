package org.bensnonorg.musicmachine.base.jmeextensions.math

import com.jme3.math.ColorRGBA

operator fun ColorRGBA.times(d: Float) = this.mult(d)
