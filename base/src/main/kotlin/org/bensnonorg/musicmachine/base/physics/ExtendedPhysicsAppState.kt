package org.bensnonorg.musicmachine.base.physics

import com.jme3.bullet.BulletAppState

class ExtendedPhysicsAppState(isParallel: Boolean) : BulletAppState() {
	init {
		threadingType = if (isParallel) ThreadingType.PARALLEL else ThreadingType.SEQUENTIAL
	}
}