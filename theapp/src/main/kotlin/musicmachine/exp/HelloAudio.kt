package musicmachine.exp

import com.jme3.app.SimpleApplication
import com.jme3.audio.AudioData.DataType
import com.jme3.audio.AudioNode
import com.jme3.input.MouseInput
import com.jme3.input.controls.ActionListener
import com.jme3.input.controls.MouseButtonTrigger
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Box

/** Sample 11 - playing 3D audio.  */
class HelloAudio : SimpleApplication() {

	private lateinit var audioGun: AudioNode
	private lateinit var audioNature: AudioNode
	private lateinit var player: Geometry
	/** Defining the "Shoot" action: Play a gun sound.  */
	private val actionListener = ActionListener { name, keyPressed, _ ->
		if (name == "Shoot" && !keyPressed) {
			audioGun.playInstance() // play each instance once!
		}
	}

	override fun simpleInitApp() {
		flyCam.moveSpeed = 40f
		/** just a blue box floating in space  */
		val box1 = Box(1f, 1f, 1f)
		player = Geometry("Player", box1)
		val mat1 = Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
		mat1.setColor("Color", ColorRGBA.Blue)
		player.material = mat1

		rootNode.attachChild(player)
		/** custom init methods, see below  */
		initKeys()
		initAudio()
	}

	/** We create two audio nodes.  */
	private fun initAudio() {
		/* gun shot sound is to be triggered by a mouse click. */
		audioGun = AudioNode(
			assetManager,
			"Sound/Effects/Gun.wav", DataType.Buffer
		)
		audioGun.isPositional = false
		audioGun.isLooping = false
		audioGun.volume = 2f
		rootNode.attachChild(audioGun)
		/* nature sound - keeps playing in a loop. */
		audioNature = AudioNode(
			assetManager,
			"Sound/Environment/Ocean Waves.ogg", DataType.Stream
		)
		audioNature.isLooping = true  // activate continuous playing
		audioNature.isPositional = true
		audioNature.volume = 3f
		rootNode.attachChild(audioNature)
		audioNature.play() // play continuously!
	}

	/** Declaring "Shoot" action, mapping it to a trigger (mouse left click).  */
	private fun initKeys() {
		inputManager.addMapping("Shoot", MouseButtonTrigger(MouseInput.BUTTON_LEFT))
		inputManager.addListener(actionListener, "Shoot")
	}

	/** Move the listener with the a camera - for 3D audio.  */
	override fun simpleUpdate(tpf: Float) {
		listener.location = cam.location
		listener.rotation = cam.rotation
	}
}

fun main() {
	HelloAudio().start()
}