package notMyStuff;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.TangentBinormalGenerator;

public class TestMain extends SimpleApplication {
	private Geometry player;
	private boolean isRunning = true;
	private Geometry sphereGeo;
	private Spatial teapot;
	private DirectionalLight sun;
	private final ActionListener actionListener = (name, keyPressed, tpf) -> {
		if (name.equals("Pause") && !keyPressed) {
			isRunning = !isRunning;
		}
	};
	private final AnalogListener analogListener = new AnalogListener() {
		@Override
		public void onAnalog(String name, float value, float tpf) {
			if (isRunning) {
				switch (name) {
					case "Rotate":
						player.rotate(0, value * speed, 0);
						break;
					case "Right": {
						Vector3f v = player.getLocalTranslation();
						player.setLocalTranslation(v.x + value * speed, v.y, v.z);
						break;
					}
					case "Left": {
						Vector3f v = player.getLocalTranslation();
						player.setLocalTranslation(v.x - value * speed, v.y, v.z);
						break;
					}
					case "Teapot": {
						teapot.rotate(value * 2, value * 4, value * 20);
						sun.setDirection(sun.getDirection().add(value / 3, value, value / 2));
					}
				}
			} else {
				System.out.println("Press P to unpause.");
			}
		}
	};
	private float time;
	
	public TestMain() {
	}
	
	public static void main(String[] args) {
		TestMain main = new TestMain();
		main.start();
	}
	
	@Override
	public void simpleInitApp() {
		
		// Create a wall with a simple texture from test_data
		Box box = new Box(2.5f, 2.5f, 1.0f);
		player = new Geometry("Box", box);
		Material mat_brick = new Material(
				assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat_brick.setTexture("ColorMap",
				assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg"));
		player.setMaterial(mat_brick);
		player.setLocalTranslation(2.0f, -2.5f, 0.0f);
		rootNode.attachChild(player);
		//
		/* A bumpy rock with a shiny light effect.*/
		Sphere sphereMesh = new Sphere(32, 32, 1f);
		sphereMesh.setTextureMode(Sphere.TextureMode.Projected); // better quality on spheres
		TangentBinormalGenerator.generate(sphereMesh);           // `class` lighting effect
		Material sphereMat = new Material(assetManager,
				"Common/MatDefs/Light/Lighting.j3md");
		sphereMat.setTexture("DiffuseMap",
				assetManager.loadTexture("Textures/Terrain/splat/grass.jpg"));
		sphereMat.setTexture("NormalMap",
				assetManager.loadTexture("Textures/Terrain/splat/grass_normal.jpg"));
		sphereMat.setBoolean("UseMaterialColors", true);
		sphereMat.setColor("Diffuse", ColorRGBA.White);
		sphereMat.setColor("Specular", ColorRGBA.White);
		sphereMat.setFloat("Shininess", 20f);  // [0,128]
		sphereGeo = new Geometry("Shiny rock", sphereMesh);
		sphereGeo.setMaterial(sphereMat);
		sphereGeo.setLocalTranslation(0, 2, -2); // Move it a bit
		sphereGeo.rotate(1.6f, 0, 0);          // Rotate it a bit
		rootNode.attachChild(sphereGeo);
		//teapot
		teapot = assetManager.loadModel("Models/Teapot/Teapot.obj");
		Material teapotMat = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
		teapot.setMaterial(teapotMat);
		rootNode.attachChild(teapot);
		// Display a line of text with a default font
		guiNode.detachAllChildren();
		guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
		BitmapText helloText = new BitmapText(guiFont, false);
		helloText.setSize(guiFont.getCharSet().getRenderedSize());
		helloText.setText("Hello World");
		helloText.setLocalTranslation(300, helloText.getLineHeight(), 0);
		guiNode.attachChild(helloText);
		//
		Box cube2Mesh = new Box(1f, 1f, 0.01f);
		Geometry cube2Geo = new Geometry("window frame", cube2Mesh);
		Material cube2Mat = new Material(assetManager,
				"Common/MatDefs/Misc/Unshaded.j3md");
		cube2Mat.setTexture("ColorMap",
				assetManager.loadTexture("Textures/ColoredTex/Monkey.png"));
		cube2Mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
		cube2Geo.setQueueBucket(RenderQueue.Bucket.Transparent);
		cube2Geo.setMaterial(cube2Mat);
		rootNode.attachChild(cube2Geo);
		// You must
		// add a light to make the model visible
		sun = new DirectionalLight();
		sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
		sun.setColor(ColorRGBA.randomColor());
		rootNode.addLight(sun);
		initKeys();
	}
	
	@Override
	public void simpleUpdate(float tpf) {
		sphereGeo.rotate(tpf, 2 * tpf, -3 * tpf);
		time += tpf;
		if (time > .1) {
			time -= .1;
			sun.setColor(ColorRGBA.randomColor());
		}
	}
	
	private void initKeys() {
		// You can map one or several inputs to one named action
		inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
		inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_J));
		inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_K));
		inputManager.addMapping("Rotate", new KeyTrigger(KeyInput.KEY_SPACE),
				new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addMapping("Teapot", new KeyTrigger(KeyInput.KEY_T));
		// Add the names to the action listener.
		inputManager.addListener(actionListener, "Pause", "Teapot");
		inputManager.addListener(analogListener, "Left", "Right", "Rotate", "Teapot");
	}
}
