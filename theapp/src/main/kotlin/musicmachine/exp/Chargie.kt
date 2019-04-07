package musicmachine.exp

import com.jme3.app.DebugKeysAppState
import com.jme3.app.FlyCamAppState
import com.jme3.app.SimpleApplication
import com.jme3.app.StatsAppState
import com.jme3.audio.AudioListenerState
import com.jme3.bullet.BulletAppState
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape
import com.jme3.bullet.control.CharacterControl
import com.jme3.font.BitmapText
import com.jme3.input.KeyInput
import com.jme3.input.MouseInput
import com.jme3.input.controls.ActionListener
import com.jme3.input.controls.AnalogListener
import com.jme3.input.controls.KeyTrigger
import com.jme3.input.controls.MouseButtonTrigger
import com.jme3.light.DirectionalLight
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.scene.Node
import org.bensnonorg.musicmachine.base.jmeextensions.controls.allInOneMultiple
import org.bensnonorg.musicmachine.base.jmeextensions.math.minusAssign
import org.bensnonorg.musicmachine.base.jmeextensions.math.plusAssign
import org.bensnonorg.musicmachine.base.jmeextensions.math.timesAssign
import org.bensnonorg.musicmachine.base.jmeextensions.scene.SplatMaterial
import org.bensnonorg.musicmachine.base.jmeextensions.scene.createSimpleTerrainQuad
import kotlin.properties.Delegates

class Chargie : SimpleApplication(StatsAppState(), FlyCamAppState(), AudioListenerState(), DebugKeysAppState()),
                ActionListener, AnalogListener {

	private lateinit var numberText: BitmapText
	private lateinit var ballsAppState: ChargedBallsAppState
	private lateinit var physicsSpace: PhysicsSpace
	private lateinit var playerControl: CharacterControl
	private var left: Boolean = false
	private var right: Boolean = false
	private var up: Boolean = false
	private var down: Boolean = false
	private val camDir = Vector3f()
	private val camLeft = Vector3f()
	private val walkDirection = Vector3f()
	private val ballsRootNode = Node()
	private val playerNode = Node()
	override fun simpleInitApp() {
		initPhysics()
		initTerrain()
		initPlayer()
		initGui()
		initKeys()
		initLight()
	}

	private fun initGui() {
		guiNode.detachAllChildren()
		guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt")
		numberText = BitmapText(guiFont, false)
		numberText.size = (guiFont.charSet.renderedSize * 2).toFloat()
		numberText.text = 0.toString()
		numberText.setLocalTranslation(
			10f, settings.height / 2f, 0f
		)
		guiNode.attachChild(numberText)
	}

	private fun initPhysics() {
		val bulletAppState = BulletAppState()
		bulletAppState.threadingType = BulletAppState.ThreadingType.PARALLEL
		bulletAppState.isDebugEnabled = false
		stateManager.attach(bulletAppState)
		this.physicsSpace = bulletAppState.physicsSpace

		physicsSpace.setGravity(Vector3f(0f, -20f, 0f))
		initBalls()
	}

	private fun initBalls() {
		ballsAppState = ChargedBallsAppState(physicsSpace, ballsRootNode)
		stateManager.attach(ballsAppState)
		rootNode.attachChild(ballsRootNode)
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
			}
		val terrain = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png")
			.createSimpleTerrainQuad(65, terrainMaterial)
			.apply {
				setLocalScale(2f, 0.3f, 2f)
				setLocalTranslation(0f, -100f, 0f)
				addLODControl(cam)
				addRigidBodyControl().apply {
					restitution = 0.9f
					friction = 1f
				}
			}
		physicsSpace.add(terrain)
		rootNode.attachChild(terrain)
	}

	private fun initPlayer() {
		playerControl = CharacterControl(CapsuleCollisionShape(1.5f, 6f), 0.05f)
			.apply {
				jumpSpeed = 60f
				fallSpeed = 30f
//					setGravity(Vector3f(0f, -50f, 0f))
				gravity = -50f
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
			"Jump", KeyInput.KEY_SPACE,
			"IncreaseCharge", KeyInput.KEY_I,
			"DecreaseCharge", KeyInput.KEY_K,
			"Negate", KeyInput.KEY_N
		)
		inputManager.allInOneMultiple(
			this, ::MouseButtonTrigger,
			"CreateMoving", MouseInput.BUTTON_LEFT,
			"CreateStatic", MouseInput.BUTTON_RIGHT
		)
	}

	private fun initLight() {
		val light = DirectionalLight(Vector3f(-0.3f, -0.4f, 1.0f), ColorRGBA.White)
		rootNode.addLight(light)
	}

	override fun simpleUpdate(tpf: Float) {
		camDir.set(cam.direction) *= .6f
		camLeft.set(cam.left) *= .4f
		walkDirection.set(Vector3f.ZERO)
		if (left) walkDirection += camLeft
		if (right) walkDirection -= camLeft
		if (up) walkDirection += camDir
		if (down) walkDirection -= camDir
		playerControl.walkDirection = walkDirection
		cam.location = playerControl.physicsLocation
	}

	private var charge by Delegates.observable(0f) { _, oldValue, newValue ->
		numberText.text = newValue.toString()
		if (oldValue * newValue <= 0)
			numberText.color = if (newValue > 0) ColorRGBA.Green else ColorRGBA.Red
	}

	override fun onAction(name: String, isPressed: Boolean, tpf: Float) {
		when (name) {
			"Left" -> left = isPressed
			"Right" -> right = isPressed
			"Up" -> up = isPressed
			"Down" -> down = isPressed
			"Jump" -> if (isPressed) playerControl.jump()
//			"Jump" -> if (isPressed) playerControl.jump(Vector3f(0f, 30f, 0f))
			"CreateMoving" -> if (isPressed) createMoving()
			"CreateStatic" -> if (isPressed) createStatic()
			"Negate" -> if (isPressed) charge *= -1
		}
	}

	private fun createStatic() {
		ballsAppState
			.addChargedBall(
				ChargedBall(
					mass = 0f, charge = charge, assetManager = assetManager
				), cam.location,
				cam.direction, 10f, 900f
			)
	}

	private fun createMoving() {
		ballsAppState
			.addChargedBall(
				ChargedBall(charge = charge, assetManager = assetManager), cam.location, cam.direction, 10f
			)
	}

	override fun onAnalog(name: String?, value: Float, tpf: Float) {
		when (name) {
			"IncreaseCharge" -> charge += value * 0.5f
			"DecreaseCharge" -> charge -= value * 0.5f
		}
	}
}

fun main() {
	Chargie().start()
}