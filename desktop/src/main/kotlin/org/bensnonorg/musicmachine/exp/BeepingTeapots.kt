package org.bensnonorg.musicmachine.exp

import com.jme3.light.AmbientLight
import com.jme3.light.DirectionalLight
import com.jme3.light.PointLight
import com.jme3.light.SpotLight
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.math.FastMath
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.control.LightControl
import com.jme3.shadow.DirectionalLightShadowRenderer
import org.bensnonorg.musicmachine.extensions.LazyCloning
import org.bensnonorg.musicmachine.extensions.math.plus
import org.bensnonorg.musicmachine.extensions.math.times
import org.bensnonorg.musicmachine.kotlin.Factory
import org.bensnonorg.musicmachine.scene.BangingObject
import org.bensnonorg.musicmachine.scene.attachAndEnable
import kotlin.math.exp

private const val FORCE_MIN = 2

class BeepingTeapots : TestApp() {

    private val material by lazy {
        Material(assetManager, "Common/MatDefs/Light/Lighting.j3md").apply {
            setFloat("Shininess", 100f)
            setBoolean("UseMaterialColors", true)
        }
    }
    //	private val tankMaterial by lazy {
//		assetManager.loadMaterial("Models/Tank/tank.j3m")
//	}
    private val teapotSpatial by LazyCloning(true) {
        assetManager.loadModel("Models/Teapot/Teapot.obj").apply {
            //		assetManager.loadModel("Models/Tree/Tree.mesh.j3o").apply {
            //		assetManager.loadModel("Models/Test/CornellBox.j3o").apply {
//	assetManager.loadModel("Models/Sponza/Sponza.j3o").apply {
            setMaterial(material)
        }
    }
    private val teapot: BangingObject by Factory {
        object : BangingObject(
            "teapot", teapotSpatial, physicsSpace, 3f, assetManager,
            "Sound/Effects/Bang.wav"
        ) {
            override fun onStrike(force: Float, hardness: Float) {
                if (force < FORCE_MIN) return
                audioNode.apply {
                    volume = force / 20
                    pitch = (2 - 1.2 * exp(-force / 200)).toFloat()
                    playInstance()
                }
            }

            //			private var shadowRenderer by initOnce<SpotLightShadowRenderer>()
            override fun onAttached(source: Boolean): StrictCallSuper {
                super.onAttached(source)
                with(rigidBodyControl) {
                    restitution = 0.7f
                    friction = 0.8f
                    angularDamping = 0.4f
                    linearDamping = 0.3f
                    setCamDirs()
                    physicsLocation = camLocation + camDir * 10f
                    linearVelocity = camDir * 100f
                    angularVelocity = Vector3f(
                        -1 + 2 * Math.random().toFloat(), -1 + 2 * Math.random().toFloat(),
                        -2 + 2 * Math.random().toFloat()
                    ).multLocal(10f)
                }
                rootNode.addLight(pointLight)
                rootNode.addLight(spotLight)
//				viewPort.addProcessor(shadowRenderer)
                return SuperCalled
            }

            //
//			override fun onDetached(): Boolean {
//				val control = lightControl
//				control.isEnabled = false
//				rootNode.removeLight(control.light)
//				viewPort.removeProcessor(shadowRenderer)
//				return super.onDetached()
//			}
            private val color = ColorRGBA.randomColor().multLocal(8f)
            private val pointLight = PointLight(Vector3f.ZERO.clone(), ColorRGBA.Cyan * 0.4f, 40f)
            private val spotLight: SpotLight = SpotLight(
                Vector3f.ZERO.clone(), Vector3f.UNIT_XYZ.clone(), 100f, color,
                FastMath.QUARTER_PI / 4,
                FastMath.QUARTER_PI
            )

            override fun onEnable(): StrictCallSuper {
                spatial.rotate(0f, 0f, -FastMath.QUARTER_PI / 2)
                super.onEnable()
                addControl(LightControl(spotLight))
                addControl(LightControl(pointLight))
//				shadowRenderer = SpotLightShadowRenderer(assetManager, 1024)
//				shadowRenderer.light = light
                return SuperCalled
            }

            init {
                (spatial as Geometry).material.setColor("Diffuse", color)
                with(audioNode) {
                    refDistance = 2f
                }
            }
        }
    }

    override fun initLight() {
        val directionalLight = DirectionalLight(Vector3f(-0.3f, -0.4f, 1.0f), ColorRGBA.White * 0.7f)
        rootNode.addLight(directionalLight)
        val shadowRenderer = DirectionalLightShadowRenderer(assetManager, 1024, 4).apply {
            light = directionalLight
            lambda = 0.8f
        }
        viewPort.addProcessor(shadowRenderer)
        rootNode.addLight(AmbientLight(ColorRGBA.White.mult(0.1f)))
//		rootNode.addLight(AmbientLight(ColorRGBA.White.mult(0.005f)))
    }

    override fun action() {
        rootNode.attachAndEnable(teapot)
    }
}

