package org.bensnonorg.musicmachine.base.jmeextensions.scene

import com.jme3.asset.AssetManager
import com.jme3.material.Material
import com.jme3.texture.Texture

class SplatMaterial(contentMan: AssetManager) : Material(contentMan, "Common/MatDefs/Terrain/TerrainLighting.j3md") {
	fun setDiffuse(n: Int, tex: Texture, scale: Float) {
		tex.setWrap(Texture.WrapMode.Repeat)
		setTexture("DiffuseMap${if (n != 0) "_$n" else ""}", tex)
		setFloat("DiffuseMap_${n}_scale", scale)
	}

	fun setNormal(n: Int, tex: Texture) {
		tex.setWrap(Texture.WrapMode.Repeat)
		setTexture("NormalMap${if (n != 0) "_$n" else ""}", tex)
	}
}

