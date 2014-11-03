/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Baseball_Sack;

import com.jme3.animation.AnimControl;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.jme3.util.SkyFactory;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 *
 * @author practicing01 & others
 */
public class SplashesState extends AbstractAppState implements Runnable {

    private static SimpleApplication app;
    private Camera cam;
    private Node rootNode;
    private AssetManager assetManager;
    private AppStateManager stateManager;
    private AppSettings settings;
    private BulletAppState bulletAppState;
    private ScheduledFuture<?> Schedule_Handle = null;
    private SceneObjectControl sceneobjectcontrol;
    private Node Scene_MourningDoveSoft;
    private int Splash_Phase = 0;
    private Vector3f LookAt_Loc;
    private Vector3f Cam_Loc;
    private Vector3f PathEndVector;
    private Vector3f PathStartVector;
    private MotionPath path;
    private MotionEvent motionEvent;
    private BitmapText Text;
    private AudioNode oga;
    private AudioNode jme;
    private SceneGraphVisitor visitor;

    @Override
    public void update(float tpf) {
    }

    @Override
    public void cleanup() {

        if (Schedule_Handle != null) {
            Schedule_Handle.cancel(false);
        }

        Main.executor.remove(this);

    }

    @Override
    public void initialize(AppStateManager stateManager,
            Application app) {

        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;
        this.stateManager = stateManager;
        this.cam = this.app.getCamera();
        this.rootNode = this.app.getRootNode();
        this.assetManager = this.app.getAssetManager();
        this.settings = app.getContext().getSettings();
        this.bulletAppState = app.getStateManager().getState(BulletAppState.class);

        this.app.getFlyByCamera().setDragToRotate(true);
        this.app.getInputManager().setCursorVisible(true);

        AudioNode mourningdove = new AudioNode(assetManager, "Sounds/mourningdove.ogg", false);

        oga = new AudioNode(assetManager, "Sounds/OGA.ogg", false);
        jme = new AudioNode(assetManager, "Sounds/ammo_loop1.ogg", false);

        sceneobjectcontrol = new SceneObjectControl();
        sceneobjectcontrol.app = (SimpleApplication) app;

        visitor = new SceneGraphVisitor() {
            @Override
            public void visit(Spatial spat) {

                //System.out.println(spat.getName());
                //System.out.println(spat.getClass().getName());

                if (spat.getName().equals("Node_LookAt")) {
                    LookAt_Loc = spat.getWorldTranslation();
                } else if (spat.getName().equals("Node_Camera")) {
                    Cam_Loc = spat.getWorldTranslation();
                } else if (spat.getName().equals("Bird-ogremesh")) {
                    sceneobjectcontrol.animcontrol = spat.getControl(AnimControl.class);
                    sceneobjectcontrol.animchannel = sceneobjectcontrol.animcontrol.createChannel();
                    sceneobjectcontrol.animchannel.setAnim("Fly");
                } else if (spat.getName().equals("Bird")) {
                    if (spat instanceof Geometry) {
                        sceneobjectcontrol.rigidbodycontrol = new RigidBodyControl(1f);
                        spat.addControl(sceneobjectcontrol.rigidbodycontrol);
                        bulletAppState.getPhysicsSpace().add(sceneobjectcontrol.rigidbodycontrol);
                        sceneobjectcontrol.rigidbodycontrol.setGravity(new Vector3f(0f, 0f, 0f));
                        sceneobjectcontrol.rigidbodycontrol.setFriction(0f);
                        sceneobjectcontrol.rigidbodycontrol.setSleepingThresholds(0f, 0f);
                        sceneobjectcontrol.rigidbodycontrol.setDamping(0f,0f);
                    } else if (spat instanceof Node) {
                        spat.addControl(sceneobjectcontrol);
                    }
                } else if (spat.getName().equals("Node_EndVec")) {
                    PathEndVector = spat.getWorldTranslation();
                    rootNode.detachChild(spat);
                } else if (spat.getName().equals("Node_StartVec")) {
                    PathStartVector = spat.getWorldTranslation();
                    rootNode.detachChild(spat);
                }
            }
        };

        Scene_MourningDoveSoft = (Node) assetManager.loadModel(
                "Models/Scene_MourningDoveSoft/Scene_MourningDoveSoft.j3o");

        rootNode.attachChild(Scene_MourningDoveSoft);

        //dds unsupported by android
        /*rootNode.attachChild(SkyFactory.createSky(
                assetManager, "Textures/Skybox/Sky0.dds", false));
*/
        rootNode.depthFirstTraversal(visitor);//rootNode.breadthFirstTraversal(visitor);

        cam.setLocation(Cam_Loc);

        cam.lookAt(LookAt_Loc, Vector3f.UNIT_Y);

        cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.01f, 1000f);

        mourningdove.play();//mourningdove.setLooping(true);

        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        Text = new BitmapText(guiFont);
        Text.setSize(guiFont.getCharSet().getRenderedSize());
        Text.move(
                settings.getWidth() / 2,
                Text.getLineHeight(),
                0);
        Text.setText("MourningDoveSoft");

        this.app.getGuiNode().attachChild(Text);

        sceneobjectcontrol.moveTo(PathEndVector, 5f, true);
        /*
         path = new MotionPath();
         path.setPathSplineType(SplineType.Linear);
         path.addWayPoint(PathStartVector);
         path.addWayPoint(PathEndVector);
         //path.setCycle(true);

         motionEvent = new MotionEvent(Bird, path);
         motionEvent.setLoopMode(LoopMode.Loop);
         //motionEvent.setDirectionType(MotionEvent.Direction.PathAndRotation);
         //motionEvent.setDirectionType(MotionEvent.Direction.Path);
         //motionEvent.setDirectionType(MotionEvent.Direction.LookAt);
         //motionEvent.setRotation(new Quaternion().fromAngleNormalAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y));
         //motionEvent.setRotation(new Quaternion().fromAngleNormalAxis(180 / 180 * FastMath.PI, Vector3f.UNIT_Y));
         //motionEvent.setInitialDuration(10f);
         motionEvent.setSpeed(10f);
        
         path.addListener(new MotionPathListener() {
         public void onWayPointReach(MotionEvent control, int wayPointIndex) {
         if (path.getNbWayPoints() == wayPointIndex + 1) {
         System.out.println(control.getSpatial().getName() + "Finished!!! ");
         } else {
         System.out.println(control.getSpatial().getName() + " Reached way point " + wayPointIndex);
         }
         }
         });
        
         motionEvent.play();
         */
        //path.enableDebugShape(assetManager,rootNode);

        //bulletAppState.getPhysicsSpace().enableDebug(assetManager);

        //Schedule_Handle = Main.executor.scheduleAtFixedRate(this, 0, 5, SECONDS);

        Schedule_Handle = Main.executor.schedule(this, 5, SECONDS);

    }

    public void Splash_OGA() {

        oga.play();
        Box box = new Box(settings.getWidth() / 2, settings.getHeight() / 2, 1f);
        Spatial wall = new Geometry("Box", box);
        Material mat_brick = new Material(
                assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat_brick.setTexture("ColorMap",
                assetManager.loadTexture("Textures/oga.png"));
        wall.setMaterial(mat_brick);

        wall.setLocalTranslation(settings.getWidth() / 2,
                settings.getHeight() / 2, 0);

        app.getGuiNode().attachChild(wall);

    }

    public void Splash_JME() {

        jme.play();
        Box box = new Box(settings.getWidth() / 2, settings.getHeight() / 2, 1f);
        Spatial wall = new Geometry("Box", box);
        Material mat_brick = new Material(
                assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat_brick.setTexture("ColorMap",
                assetManager.loadTexture("Textures/Monkey.jpg"));
        wall.setMaterial(mat_brick);

        wall.setLocalTranslation(settings.getWidth() / 2,
                settings.getHeight() / 2, 0);

        app.getGuiNode().attachChild(wall);

    }

    public void run() {

        Splash_Phase++;

        if (Splash_Phase == 1) {

            //clear scene
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    Main.Clear_Scene(app, rootNode, bulletAppState);
                    Splash_OGA();
                    SplashesState.this.Schedule_Handle = Main.executor.schedule(SplashesState.this, 5, SECONDS);
                    return null;
                }
            });

        } else if (Splash_Phase == 2) {

            //clear scene
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    Main.Clear_Scene(app, rootNode, bulletAppState);
                    Splash_JME();
                    SplashesState.this.Schedule_Handle = Main.executor.schedule(SplashesState.this, 3, SECONDS);
                    return null;
                }
            });

        } else if (Splash_Phase >= 3) {

            //clear scene
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    Main.Clear_Scene(app, rootNode, bulletAppState);
                    return null;
                }
            });

            stateManager.detach(this);

            MainMenu mm = new MainMenu();

            stateManager.attach(mm);

            Main.appstate_delete(this);

        }

    }
}
