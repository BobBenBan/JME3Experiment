package notMyStuff;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;

public class TestMain2 extends SimpleApplication {
	private Node shootables;
	private Node inventory;
	private Vector3f oldPosition;
	private ActionListener actionListener = new ActionListener() {
		public void onAction(String name, boolean keyPressed, float tpf) {
			if (name.equals("Shoot") && !keyPressed) {
				if (inventory.getChildren().isEmpty()) {
					CollisionResults results = new CollisionResults();
					Collidable ray = new Ray(cam.getLocation(), cam.getDirection());
					shootables.collideWith(ray, results);
					
					if (results.size() > 0) {
						CollisionResult closest = results.getClosestCollision();
						Spatial s = closest.getGeometry();
						// we cheat Model differently with simple Geometry
						// s.parent is Oto-ogremesh when s is Oto_geom-1 and that is what we need
						if (s.getName().equals("Oto-geom-1")) {
							s = s.getParent();
						}
						// It's important to get a clone or otherwise it will behave weird
						oldPosition = s.getLocalTranslation().clone();
						shootables.detachChild(s);
						inventory.attachChild(s);
						// make it bigger to see on the HUD
						s.scale(50f);
						// make it on the HUD center
						s.setLocalTranslation(settings.getWidth() / 2f, settings.getHeight() / 2f, 0);
					}
				} else {
					Spatial s1 = inventory.getChild(0);
					// scale back
					s1.scale(.02f);
					s1.setLocalTranslation(oldPosition);
					inventory.detachAllChildren();
					shootables.attachChild(s1);
				}
			}
		}
	};
	
	public static void main(String[] args) {
		TestMain2 app = new TestMain2();
		app.start();
	}
	
	@Override
	public void simpleInitApp() {
		initCrossHairs();
		initKeys();
		shootables = new Node("Shootables");
		inventory = new Node("Inventory");
		guiNode.attachChild(inventory);
		// add a light to the HUD so we can see the robot
		DirectionalLight sun = new DirectionalLight();
		sun.setDirection(new Vector3f(0, 0, -1.0f));
		guiNode.addLight(sun);
		rootNode.attachChild(shootables);
		shootables.attachChild(makeCube("a Dragon", -2f, 0f, 1f));
		shootables.attachChild(makeCube("a tin can", 1f, -2f, 0f));
		shootables.attachChild(makeCube("the Sheriff", 0f, 1f, -2f));
		shootables.attachChild(makeCube("the Deputy", 1f, 0f, -4f));
		shootables.attachChild(makeFloor());
		shootables.attachChild(makeCharacter());
	}
	
	private void initCrossHairs() {
		setDisplayStatView(false);
		guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
		BitmapText ch = new BitmapText(guiFont, false);
		ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
		ch.setText("+");
		ch.setLocalTranslation(
				settings.getWidth() / 2f - ch.getLineWidth() / 2, settings.getHeight() / 2f + ch.getLineHeight() / 2,
				0);
		guiNode.attachChild(ch);
	}
	
	private void initKeys() {
		inputManager.addMapping("Shoot",
				new KeyTrigger(KeyInput.KEY_SPACE),
				new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addListener(actionListener, "Shoot");
	}
	
	private Geometry makeCube(String name, float x, float y, float z) {
		Box box = new Box(1, 1, 1);
		Geometry cube = new Geometry(name, box);
		cube.setLocalTranslation(x, y, z);
		Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat1.setColor("Color", ColorRGBA.randomColor());
		cube.setMaterial(mat1);
		return cube;
	}
	
	private Geometry makeFloor() {
		Box box = new Box(15, .2f, 15);
		Geometry floor = new Geometry("the Floor", box);
		floor.setLocalTranslation(0, -4, -5);
		Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat1.setColor("Color", ColorRGBA.Gray);
		floor.setMaterial(mat1);
		return floor;
	}
	
	private Spatial makeCharacter() {
		Spatial golem = assetManager.loadModel("Models/Oto/Oto.mesh.xml");
		golem.scale(0.5f);
		golem.setLocalTranslation(-1.0f, -1.5f, -0.6f);
		System.out.println("golem.locaoTranslation:" + golem.getLocalTranslation());
		DirectionalLight sun = new DirectionalLight();
		sun.setDirection(new Vector3f(0, 0, -1.0f));
		golem.addLight(sun);
		return golem;
	}
}