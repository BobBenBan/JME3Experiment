package org.bensnonorg.musicmachine.exp

import com.jme3.app.DebugKeysAppState
import com.jme3.app.FlyCamAppState
import com.jme3.app.SimpleApplication
import com.jme3.app.StatsAppState
import com.jme3.audio.AudioListenerState
import com.jme3.bullet.BulletAppState
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape
import com.jme3.bullet.control.CharacterControl
import com.jme3.input.KeyInput
import com.jme3.input.MouseInput
import com.jme3.input.controls.ActionListener
import com.jme3.input.controls.KeyTrigger
import com.jme3.input.controls.MouseButtonTrigger
import com.jme3.light.AmbientLight
import com.jme3.light.DirectionalLight
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.scene.Node
import com.jme3.texture.Texture.WrapMode
import kotlinx.coroutines.runBlocking
import org.bensnonorg.musicmachine.control.allInOneMultiple
import org.bensnonorg.musicmachine.extensions.math.minusAssign
import org.bensnonorg.musicmachine.extensions.math.plusAssign
import org.bensnonorg.musicmachine.extensions.math.timesAssign
import org.bensnonorg.musicmachine.physics.SpatialCollisionListener
import org.bensnonorg.musicmachine.scene.SplatMaterial
import org.bensnonorg.musicmachine.scene.createSimpleTerrainQuad

abstract class TestApp :
	SimpleApplication(StatsAppState(), FlyCamAppState(), AudioListenerState(), DebugKeysAppState()),
	ActionListener {

	protected lateinit var physicsSpace: PhysicsSpace
	private lateinit var playerControl: CharacterControl
	private var left: Boolean = false
	private var right: Boolean = false
	private var up: Boolean = false
	private var down: Boolean = false
	protected val camDir = Vector3f()
	private val camLeft = Vector3f()
	private val walkDirection = Vector3f()
	private val playerNode = Node()
	protected val camLocation = Vector3f()
	override fun simpleInitApp() {
		preloadAssets()
		initPhysics()
		initTerrain()
		initPlayer()
		initKeys()
		initLight()
	}

	private fun initPhysics() {
		val bulletAppState = BulletAppState()
		bulletAppState.threadingType = BulletAppState.ThreadingType.PARALLEL
		bulletAppState.isDebugEnabled = false
		stateManager.attach(bulletAppState)
		this.physicsSpace = bulletAppState.physicsSpace
		physicsSpace.setGravity(Vector3f(0f, -20f, 0f))
		physicsSpace.addCollisionListener(SpatialCollisionListener.MasterListener)
	}

	private fun initTerrain() {
		viewPort.backgroundColor = ColorRGBA.LightGray
		val terrainMaterial = SplatMaterial(assetManager)
			.apply {
				setTexture("AlphaMap", assetManager.loadTexture("Textures/Terrain/splat/alpha1.png"))
				setTexture("AlphaMap_1", assetManager.loadTexture("Textures/Terrain/splat/alpha2.png"))
				setDiffuse(0, assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg"), 16f)
				setDiffuse(1, assetManager.loadTexture("Textures/Terrain/Rock2/rock.jpg"), 32f)
				setDiffuse(2, assetManager.loadTexture("Textures/Terrain/Rock/Rock.PNG"), 32f)
				setDiffuse(3, assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg"), 80f)
				setDiffuse(4, assetManager.loadTexture("Textures/Terrain/splat/grass.jpg"), 32f)
				setDiffuse(5, assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg"), 128f)
				setDiffuse(6, assetManager.loadTexture("Textures/Terrain/splat/road.jpg"), 200f)
				setNormal(0, assetManager.loadTexture("Textures/Terrain/splat/dirt_normal.png"))
				val rockNormal = assetManager.loadTexture("Textures/Terrain/Rock/Rock_normal.png")
					.apply { setWrap(WrapMode.Repeat) }
				setNormal(1, rockNormal)
				setNormal(2, rockNormal)
				setNormal(4, assetManager.loadTexture("Textures/Terrain/splat/grass_normal.jpg").apply {
					setWrap(WrapMode.Repeat)
				})
				setNormal(6, assetManager.loadTexture("Textures/Terrain/splat/road_normal.png"))
				setFloat("Shininess", 70f)
			}
		val terrain = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png")
			.createSimpleTerrainQuad(65, terrainMaterial)
			.apply {
				setLocalTranslation(0f, -50f, 0f)
				setLocalScale(2f, 0.2f, 2f)
				addLODControl(cam)
				addRigidBodyControl().apply {
					restitution = 1.5f
					friction = 1f
				}
			}
		physicsSpace.add(terrain)
		rootNode.attachChild(terrain)
	}

	private suspend fun test() {
		runBlocking {
		}
	}

	private fun initPlayer() {
		playerControl = CharacterControl(CapsuleCollisionShape(1.5f, 6f), 0.05f)
			.apply {
				jumpSpeed = 60f
				fallSpeed = 30f
				setGravity(Vector3f(0f, -50f, 0f))
				jumpSpeed = 30f
//					gravity = -50f
				physicsLocation = Vector3f(0f, 30f, 0f)
			}
		playerNode.addControl(playerControl)
		physicsSpace.add(playerNode)
	}

	private fun initKeys() {
		inputManager.allInOneMultiple(
			this, ::KeyTrigger,
			"Left", KeyInput.KEY_A,
			"Right", KeyInput.KEY_D,
			"Up", KeyInput.KEY_W,
			"Down", KeyInput.KEY_S,
			"Jump", KeyInput.KEY_SPACE
		)
		inputManager.allInOneMultiple(
			this, ::MouseButtonTrigger,
			"Action", MouseInput.BUTTON_LEFT
		)
	}

	protected open fun initLight() {
		rootNode.addLight(DirectionalLight(Vector3f(-0.3f, -0.4f, 1.0f), ColorRGBA.White))
		rootNode.addLight(AmbientLight(ColorRGBA.White.mult(0.05f)))
	}

	override fun simpleUpdate(tpf: Float) {
		setCamDirs()
		walkDirection.set(Vector3f.ZERO)
		if (left) walkDirection += camLeft
		if (right) walkDirection -= camLeft
		if (up) walkDirection += camDir
		if (down) walkDirection -= camDir
		playerControl.walkDirection = walkDirection
		cam.location = playerControl.physicsLocation
	}

	private fun preloadAssets() {
		assetManager.loadModel("Models/Teapot/Teapot.obj")
		assetManager.loadAsset("Common/MatDefs/Light/Lighting.j3md")
	}

	protected fun setCamDirs() {
		camDir.set(cam.direction) *= .6f
		camLeft.set(cam.left) *= .4f
		camLocation.set(cam.location)
	}

	override fun onAction(name: String, isPressed: Boolean, tpf: Float) {
		when (name) {
			"Left" -> left = isPressed
			"Right" -> right = isPressed
			"Up" -> up = isPressed
			"Down" -> down = isPressed
			"Jump" -> if (isPressed) playerControl.jump(Vector3f(0f, 30f, 0f))
			"Action" -> if (isPressed) action()
		}
	}

	protected abstract fun action()
}