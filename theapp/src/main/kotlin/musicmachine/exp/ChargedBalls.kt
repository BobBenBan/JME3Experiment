package musicmachine.exp

import com.jme3.app.Application
import com.jme3.app.state.BaseAppState
import com.jme3.asset.AssetManager
import com.jme3.bullet.PhysicsSpace
import com.jme3.bullet.PhysicsTickListener
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.export.JmeExporter
import com.jme3.export.JmeImporter
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.math.Vector3f
import com.jme3.scene.Geometry
import com.jme3.scene.Node
import com.jme3.scene.shape.Sphere
import org.bensnonorg.musicmachine.base.jmeextensions.math.plus
import org.bensnonorg.musicmachine.base.jmeextensions.math.times
import kotlin.math.absoluteValue
import kotlin.math.sign

private const val K = 2e6f
private const val MAX_FORCE = 500f

class ChargedBall private constructor(mass: Float, private var charge: Float) : Geometry() {

	private val control = RigidBodyControl(mass)

	constructor(size: Float = 1f, mass: Float = 5f, charge: Float, assetManager: AssetManager) : this(mass, charge) {
		val sphere = Sphere(32, 32, size)
		sphere.textureMode = Sphere.TextureMode.Projected
		this.setMesh(sphere)
		val color = if (charge > 0) ColorRGBA.Green else ColorRGBA.Red
//		val material = Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md").apply {
//			setColor("Color", color)
//		}
//*
		val material = Material(assetManager, "Common/MatDefs/Light/Lighting.j3md").apply {
			setColor("Ambient", color)
			setColor("Diffuse", color)
			setColor("Specular", color)
			setColor("GlowColor", color)
			setBoolean("UseMaterialColors", true)
		}// */
		this.setMaterial(material)
		this.addControl(control)
	}

	fun doPhysicsOn(other: ChargedBall) {
		if (this.control.physicsSpace != other.control.physicsSpace || this === other) return
		val thisPos = this.control.physicsLocation
		val otherPos = other.control.physicsLocation
		val diff = thisPos.subtract(otherPos)
		var force = this.charge * other.charge * K / diff.lengthSquared()
		force = Math.min(force.absoluteValue, MAX_FORCE) * force.sign
		diff.normalizeLocal().multLocal(force)
		this.control.applyCentralForce(diff)
		other.control.applyCentralForce(diff.mult(-1f))
	}

	override fun write(ex: JmeExporter) {
		super.write(ex)
		val capsule = ex.getCapsule(this)
		capsule.write(charge, "charge", 0f)
	}

	override fun read(im: JmeImporter) {
		super.read(im)
		val capsule = im.getCapsule(this)
		charge = capsule.readFloat("charge", 0f)
	}
}

class ChargedBallsAppState(private val physicsSpace: PhysicsSpace, private val ballsRootNode: Node) : BaseAppState() {
	override fun initialize(app: Application?) {
	}

	override fun onDisable() {
		physicsSpace.removeTickListener(tickListener)
		for (child in ballsRootNode.children) {
			physicsSpace.remove(child)
		}
	}

	override fun onEnable() {
		physicsSpace.addTickListener(tickListener)
		for (child in ballsRootNode.children) {
			physicsSpace.add(child)
		}
	}

	override fun cleanup(app: Application?) {
		ballsRootNode.detachAllChildren()
	}

	fun addChargedBall(
		ball: ChargedBall, location: Vector3f, direction: Vector3f = Vector3f.ZERO, offset: Float, speed: Float = 0f
	) {
		ballsRootNode.attachChild(ball)
		physicsSpace.add(ball)
		ball.getControl(RigidBodyControl::class.java).apply {
			physicsLocation = location + direction * offset
			linearVelocity = direction * speed
			restitution = 1.1f
			friction = 0.7f
		}
	}

	fun removedChargedBall(ball: ChargedBall) {
		ballsRootNode.detachChild(ball)
		physicsSpace.remove(ball)
	}

	private val tickListener = object : PhysicsTickListener {
		override fun prePhysicsTick(space: PhysicsSpace, tpf: Float) {
			for (i in 0 until ballsRootNode.children.size) {
				for (j in i + 1 until ballsRootNode.children.size) {
					(ballsRootNode.children[i] as ChargedBall).doPhysicsOn(ballsRootNode.children[j] as ChargedBall)
				}
			}
		}

		override fun physicsTick(space: PhysicsSpace, tpf: Float) {
		}
	}
}
