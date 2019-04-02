package org.bensnonorg.base.jme.scene

import com.jme3.bullet.control.RigidBodyControl
import com.jme3.material.Material
import com.jme3.renderer.Camera
import com.jme3.renderer.queue.RenderQueue
import com.jme3.terrain.geomipmap.TerrainLodControl
import com.jme3.terrain.geomipmap.TerrainQuad
import com.jme3.terrain.heightmap.HeightMap
import com.jme3.terrain.heightmap.ImageBasedHeightMap
import com.jme3.texture.Texture

class SimpleTerrainQuad internal constructor(heightMap: HeightMap, patchSize: Int) : TerrainQuad("SimpleTerrainQuad", patchSize, heightMap.size + 1, heightMap.heightMap) {

    fun addLODControl(camera: Camera) {
        this.addControl(TerrainLodControl(this, camera))
    }

    fun addRigidBodyControl(): RigidBodyControl {
        val rigidBodyControl = RigidBodyControl(0f)
        this.addControl(rigidBodyControl)
        return rigidBodyControl
    }
}

fun Texture.createSimpleTerrainQuad(patchSize: Int, material: Material): SimpleTerrainQuad {
    val heightMap = ImageBasedHeightMap(this.image)
    heightMap.load()
    return SimpleTerrainQuad(heightMap, patchSize).apply {
        this.material = material
        shadowMode = RenderQueue.ShadowMode.Receive
    }

}

