package org.bensnonorg.musicmachine.extensions.math

import com.jme3.math.ColorRGBA

operator fun ColorRGBA.times(d: Float) = this.mult(d)
