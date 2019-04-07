package org.bensnonorg.musicmachine.base.physics

import com.jme3.bullet.collision.PhysicsCollisionEvent
import com.jme3.bullet.collision.PhysicsCollisionListener
import com.jme3.scene.Spatial

interface SpatialCollisionListener {
	fun onCollision(event: PhysicsCollisionEvent, other: Spatial)

	object MasterListener : PhysicsCollisionListener {

		override fun collision(event: PhysicsCollisionEvent) {
			val nodeA = event.nodeA
			val nodeB = event.nodeB
			if (nodeA is SpatialCollisionListener) nodeA.onCollision(event, nodeB)
			if (nodeB is SpatialCollisionListener) nodeB.onCollision(event, nodeA)
		}
	}
}
