package notMyStuff;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.font.BitmapText;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

/**
 * Example 12 - how to give objects physical properties so they bounce and fall.
 *
 * @author base code by double1984, updated by zathras
 */
public class TestWorld extends SimpleApplication {
	private CapsuleCollisionShape shape;
	
	public static void main(String[] args) {
		TestWorld app = new TestWorld();
		app.start();
	}
	
	/**
	 * Prepare the Physics Application State (jBullet)
	 */
	private BulletAppState bulletAppState;
	/**
	 * Prepare Materials
	 */
	private Material wall_mat;
	private Material stone_mat;
	private Material floor_mat;
	private static final Box box;
	private static final Sphere sphere;
	private static final Box floor;
	/**
	 * dimensions used `class` bricks and wall
	 */
	private static final float brickLength = 0.48f;
	private static final float brickWidth = 0.24f;
	private static final float brickHeight = 0.12f;
	static {
		/* Initialize the cannon ball geometry */
		sphere = new Sphere(32, 32, 0.4f, true, false);
		sphere.setTextureMode(TextureMode.Projected);
		/* Initialize the brick geometry */
		box = new Box(brickLength, brickHeight, brickWidth);
		box.scaleTextureCoordinates(new Vector2f(1f, .5f));
		/* Initialize the floor geometry */
		floor = new Box(400f, 0.1f, 400f);
		floor.scaleTextureCoordinates(new Vector2f(3, 6));
	}
	@Override
	public void simpleUpdate(float tpf) {
	}
	
	@Override
	public void simpleInitApp() {
		flyCam.setMoveSpeed(70);
		/* Set up Physics Game */
		bulletAppState = new BulletAppState() {
			@Override
			public void prePhysicsTick(PhysicsSpace space, float f) {
				for (PhysicsRigidBody physicsRigidBody : space.getRigidBodyList()) {
					physicsRigidBody.setGravity(physicsRigidBody.getPhysicsLocation().normalize().multLocal(-10
					));
				}
			}
		};
		shape = new CapsuleCollisionShape(1, 1);
		
		bulletAppState.getPhysicsSpace().add(shape);
		stateManager.attach(bulletAppState);
		//bulletAppState.getPhysicsSpace().enableDebug(assetManager);
		
		/* Configure cam to look at scene */
		cam.setLocation(new Vector3f(0, 4f, 6f));
		cam.lookAt(new Vector3f(2, 2, 0), Vector3f.UNIT_Y);
		/* Add InputManager action: Left click triggers shooting. */
		inputManager.addMapping("shoot",
				new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addListener(actionListener, "shoot");
		/* Initialize the scene, materials, and physics space */
		initMaterials();
		initWall();
		//initFloor();
		initCrossHairs();
	}
	
	/**
	 * Every time the shoot action is triggered, a new cannon ball is produced.
	 * The ball is set up to fly from the camera position in the camera direction.
	 */
	private ActionListener actionListener = (name, keyPressed, tpf) -> {
		if (name.equals("shoot") && !keyPressed) {
			makeCannonBall();
		}
	};
	
	/**
	 * Initialize the materials used in this scene.
	 */
	private void initMaterials() {
		wall_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		TextureKey key = new TextureKey("Textures/Terrain/BrickWall/BrickWall.jpg");
		key.setGenerateMips(true);
		Texture tex = assetManager.loadTexture(key);
		wall_mat.setTexture("ColorMap", tex);
		
		stone_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		TextureKey key2 = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
		key2.setGenerateMips(true);
		Texture tex2 = assetManager.loadTexture(key2);
		stone_mat.setTexture("ColorMap", tex2);
		
		floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		TextureKey key3 = new TextureKey("Textures/Terrain/Pond/Pond.jpg");
		key3.setGenerateMips(true);
		Texture tex3 = assetManager.loadTexture(key3);
		tex3.setWrap(WrapMode.Repeat);
		floor_mat.setTexture("ColorMap", tex3);
	}
	
	/**
	 * Make a solid floor and add it to the scene.
	 */
	private void initFloor() {
		Geometry floor_geo = new Geometry("Floor", floor);
		floor_geo.setMaterial(floor_mat);
		floor_geo.setLocalTranslation(0, -0.1f, 0);
		this.rootNode.attachChild(floor_geo);
		/* Make the floor physical with mass 0.0f! */
		RigidBodyControl floor_phy = new RigidBodyControl(0.0f);
		floor_geo.addControl(floor_phy);
		bulletAppState.getPhysicsSpace().add(floor_phy);
		floor_phy.setRestitution(1);
	}
	
	/**
	 * This loop builds a wall out of individual bricks.
	 */
	private void initWall() {
		float startpt = brickLength / 4;
		float height = 0;
		for (int j = 0; j < 15; j++) {
			for (int i = 0; i < 6; i++) {
				Vector3f vt =
						new Vector3f(i * brickLength * 2 + startpt, brickHeight + height, 0);
				makeBrick(vt);
			}
			startpt = -startpt;
			height += 2 * brickHeight;
		}
	}
	
	/**
	 * This method creates one individual physical brick.
	 */
	private void makeBrick(Vector3f loc) {
		/* Create a brick geometry and attach to scene graph. */
		Geometry brick_geo = new Geometry("brick", box);
		brick_geo.setMaterial(wall_mat);
		rootNode.attachChild(brick_geo);
		/* Position the brick geometry  */
		brick_geo.setLocalTranslation(loc);
		/* Make brick physical with a mass > 0.0f. */
		/*
		  Prepare geometries and physical nodes `class` bricks and cannon balls.
		 */
		RigidBodyControl brick_phy = new RigidBodyControl(0.1f);
		/* Add physical brick to physics space. */
		brick_geo.addControl(brick_phy);
		bulletAppState.getPhysicsSpace().add(brick_phy);
		brick_phy.setRestitution(1.1f);
	}
	
	/**
	 * This method creates one individual physical cannon ball.
	 * By default, the ball is accelerated and flies
	 * from the camera position in the camera direction.
	 */
	private void makeCannonBall() {
		/* Create a cannon ball geometry and attach to scene graph. */
		Geometry ball_geo = new Geometry("cannon ball", sphere);
		ball_geo.setMaterial(stone_mat);
		rootNode.attachChild(ball_geo);
		/* Position the cannon ball  */
		ball_geo.setLocalTranslation(cam.getLocation());
		/* Make the ball physcial with a mass > 0.0f */
		RigidBodyControl ball_phy = new RigidBodyControl(4000f);
		/* Add physical ball to physics space. */
		ball_geo.addControl(ball_phy);
		bulletAppState.getPhysicsSpace().add(ball_phy);
		/* Accelerate the physcial ball to shoot it. */
		ball_phy.setLinearVelocity(cam.getDirection().mult(25));
		ball_phy.setRestitution(1);
	}
	
	/**
	 * A plus sign used as crosshairs to help the player with aiming.
	 */
	private void initCrossHairs() {
		guiNode.detachAllChildren();
		guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
		BitmapText ch = new BitmapText(guiFont, false);
		ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
		ch.setText("+");        // fake crosshairs :)
		ch.setLocalTranslation( // center
				settings.getWidth() / 2f - guiFont.getCharSet().getRenderedSize() / 3f * 2,
				settings.getHeight() / 2f + ch.getLineHeight() / 2, 0);
		guiNode.attachChild(ch);
	}
}