/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Baseball_Sack;

import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.animation.SkeletonControl;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.MotionPathListener;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Spline.SplineType;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import de.lessvoid.nifty.Nifty;
import java.util.Collection;

/**
 *
 * @author practicing01
 */
public class GameplayState extends AbstractAppState {

    private static SimpleApplication app;
    private Camera cam;
    private Node rootNode;
    private AssetManager assetManager;
    private AppStateManager stateManager;
    private AppSettings settings;
    private BulletAppState bulletAppState;
    private InputManager inputManager;
    private Nifty nifty;
    private Node SceneNode;
    private Node BleachersNode;
    private int SceneTraversalPhase;
    private SceneGraphVisitor visitor;
    private Vector2f click2d;
    private Vector3f click3d;
    private Vector3f rayDir;
    private Vector3f contactPoint;
    private Ray ray;
    private CollisionResults collisionResults;
    
    //Hardcoded batter spatial, need to design a better solution.
    private Spatial Batter;
    public static Spatial Ball;
    public static Spatial Pitcher;
    public static Vector3f ballOrigin;
    public static Spatial HomeBaseNode,FirstBaseNode,SecondBaseNode,ThirdBaseNode;
    private Spatial BatOffsetNode;
    public static Spatial Runner;
    private static MotionPath path;
    private static MotionEvent motionEvent;
    public static int basePhase = 0;
    public static BitmapText bmptxtScore;
    public static int iScore = 0;
    public static AudioNode audioThrowBall;
    public static AudioNode audioSwing;
    public static AudioNode audioBatHit;
    public static Boolean canBatHitSfx = true;
    public static AudioNode audioNetHit;
    public static AudioNode audioCheer;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        //TODO: initialize your AppState, e.g. attach spatials to rootNode
        //this is called on the OpenGL thread after the AppState has been attached

        this.app = (SimpleApplication) app;
        this.stateManager = stateManager;
        this.cam = this.app.getCamera();
        this.rootNode = this.app.getRootNode();
        this.assetManager = this.app.getAssetManager();
        this.settings = app.getContext().getSettings();
        this.bulletAppState = app.getStateManager().getState(BulletAppState.class);
        this.inputManager = app.getInputManager();
        
        //Debug.
        //bulletAppState.getPhysicsSpace().enableDebug(assetManager);
        //this.app.getFlyByCamera().setDragToRotate(true);
        //this.app.getInputManager().setCursorVisible(true);
        
        bulletAppState.getPhysicsSpace().setAccuracy(0.01f);

        SceneNode = (Node) assetManager.loadModel(
                "Scenes/BaseballScene/Baseball_Scene.j3o");

        rootNode.attachChild(SceneNode);

        BleachersNode = (Node) assetManager.loadModel(
                "Scenes/Bleachers/bleachers.j3o");

        rootNode.attachChild(BleachersNode);

        SceneNode.scale(0.1f);

        BleachersNode.scale(0.1f);
        
        cam.setFrustumPerspective(62f, (float) cam.getWidth() / (float) cam.getHeight(), 0.01f, 1000f);

        cam.setLocation(new Vector3f(0f, 1f, -6f));

        cam.lookAtDirection(new Vector3f(0f, -0.3f, 1f), Vector3f.UNIT_Y);

        visitor = new SceneGraphVisitor() {
            @Override
            public void visit(Spatial spat) {

                Collection spatKeys = spat.getUserDataKeys();

                //addControl phase.
                if (SceneTraversalPhase == 0) {
                    if (spatKeys.contains("isSceneObject") == true) {
                        Boolean isSceneObject = spat.getUserData("isSceneObject");
                        if (isSceneObject == true) {
                            SceneObjectControl sceneObjCtrl = new SceneObjectControl();
                            sceneObjCtrl.app = GameplayState.this.app;
                            spat.addControl(sceneObjCtrl);
                        }
                    }
                }//setAnim phase.
                else if (SceneTraversalPhase == 1) {
                    if (spatKeys.contains("isAnimated") == true) {
                        Boolean isAnimated = spat.getUserData("isAnimated");
                        if (isAnimated == true) {
                            String Animation = spat.getUserData("Animation");
                            SceneObjectControl sceneObjCtrl = spat.getParent().getControl(SceneObjectControl.class);
                            if (sceneObjCtrl != null) {
                                sceneObjCtrl.animcontrol = spat.getControl(AnimControl.class);
                                sceneObjCtrl.animcontrol.addListener(sceneObjCtrl);
                                sceneObjCtrl.animchannel = sceneObjCtrl.animcontrol.createChannel();
                                sceneObjCtrl.animchannel.setAnim(Animation);
                                if (spatKeys.contains("AnimLoopMode") == true) {
                                    String AnimLoopMode = spat.getUserData("AnimLoopMode");
                                    if (AnimLoopMode.equals("DontLoop") == true) {
                                        sceneObjCtrl.animchannel.setLoopMode(LoopMode.DontLoop);
                                    } else if (AnimLoopMode.equals("Loop") == true) {
                                        sceneObjCtrl.animchannel.setLoopMode(LoopMode.Loop);
                                    } else if (AnimLoopMode.equals("Cycle") == true) {
                                        sceneObjCtrl.animchannel.setLoopMode(LoopMode.Cycle);
                                    }
                                } else {
                                    sceneObjCtrl.animchannel.setLoopMode(LoopMode.DontLoop);
                                }
                                if (spatKeys.contains("AnimSpeed") == true) {
                                    float AnimSpeed = spat.getUserData("AnimSpeed");
                                    sceneObjCtrl.animchannel.setSpeed(AnimSpeed);
                                } else {
                                    sceneObjCtrl.animchannel.setSpeed(1.0f);
                                }
                            }
                        }
                    }
                }//attachChild phase.
                else if (SceneTraversalPhase == 2) {
                    if (spatKeys.contains("AttachedMesh") == true) {
                        String meshName = spat.getUserData("AttachedMesh");
                        Spatial attachedMesh = rootNode.getChild(meshName);
                        SkeletonControl skeletonControl = attachedMesh.getControl(SkeletonControl.class);
                        if (spatKeys.contains("AttachedBone") == true) {
                            String AttachedBone = spat.getUserData("AttachedBone");
                            Node boner = skeletonControl.getAttachmentsNode(AttachedBone);
                            boner.attachChild(spat);
                        }
                        if (spatKeys.contains("AttachedScale") == true) {
                            float AttachedScale = spat.getUserData("AttachedScale");
                            //spat.scale(AttachedScale);
                            spat.setLocalScale(AttachedScale);
                        }
                        if (spatKeys.contains("AttachedRotX") == true) {
                            float AttachedRotX = spat.getUserData("AttachedRotX");
                            //spat.rotate(AttachedRotX, 0.0f, 0.0f);
                            spat.setLocalRotation(spat.getLocalRotation().fromAngleAxis(AttachedRotX, Vector3f.UNIT_X));
                        }
                        if (spatKeys.contains("AttachedRotY") == true) {
                            float AttachedRotY = spat.getUserData("AttachedRotY");
                            //spat.rotate(0.0f, AttachedRotY, 0.0f);
                            spat.setLocalRotation(spat.getLocalRotation().fromAngleAxis(AttachedRotY, Vector3f.UNIT_Y));
                        }
                        if (spatKeys.contains("AttachedRotZ") == true) {
                            float AttachedRotZ = spat.getUserData("AttachedRotZ");
                            //spat.rotate(0.0f, 0.0f, AttachedRotZ);
                            spat.setLocalRotation(spat.getLocalRotation().fromAngleAxis(AttachedRotZ, Vector3f.UNIT_Z));
                        }
                        if (spatKeys.contains("AttachedOffX") == true) {
                            float AttachedOffX = spat.getUserData("AttachedOffX");
                            //spat.move(AttachedOffX, 0.0f, 0.0f);
                            spat.setLocalTranslation(AttachedOffX, 0.0f, 0.0f);
                        }
                        if (spatKeys.contains("AttachedOffY") == true) {
                            float AttachedOffY = spat.getUserData("AttachedOffY");
                            //spat.move(0.0f, AttachedOffY, 0.0f);
                            spat.setLocalTranslation(0.0f, AttachedOffY, 0.0f);
                        }
                        if (spatKeys.contains("AttachedOffZ") == true) {
                            float AttachedOffZ = spat.getUserData("AttachedOffZ");
                            //spat.move(0.0f, 0.0f, AttachedOffZ);
                            spat.setLocalTranslation(0.0f, 0.0f, AttachedOffZ);
                        }
                    }
                }//RigidBodyControl phase.
                else if (SceneTraversalPhase == 3) {
                    RigidBodyControl body = spat.getControl(RigidBodyControl.class);
                    if (body != null) {
                        //body.setMass(1.0f);//crash
                        Vector3f extent = ((BoundingBox) spat.getWorldBound()).getExtent(new Vector3f());
                        //extent.multLocal(1.7f);//Hack untill I figure out how to offset rigidbodies.
                        /*Quaternion rot = spat.getWorldRotation();
                        Vector3f loc = spat.getWorldTranslation();
                        Vector3f dir = spat.getWorldRotation().mult(Vector3f.UNIT_Z);//.multLocal(Vector3f.UNIT_Z);
                        dir.normalizeLocal();
                        dir.multLocal(extent);
                        loc.addLocal(dir);*/
                        //BoxCollisionShape box = new BoxCollisionShape(extent.mult(spat.getWorldScale()));*/
                        BoxCollisionShape box = new BoxCollisionShape(extent);
                        body.setCollisionShape(box);
                        bulletAppState.getPhysicsSpace().add(body);
                        /*body.setApplyPhysicsLocal(true);
                        body.setPhysicsLocation(loc);
                        body.setPhysicsRotation(rot);
                        body.setApplyPhysicsLocal(false);*/
                    }
                    //Since this phase isn't used we'll use it as the BatOffsetNode getter phase.
                    if (spat.getName().equals("BatOffsetNode") == true) {
                        BatOffsetNode = spat;
                    }
                }//Hardcoded batter-getter phase.  Need to design a better solution.
                else if (SceneTraversalPhase == 4) {
                    if (spatKeys.contains("isBatter") == true) {
                        Batter = spat;
                        RigidBodyControl body = new RigidBodyControl(new BoxCollisionShape(spat.getWorldScale()),10f);
                        Batter.addControl(body);
                        body.setCollideWithGroups(RigidBodyControl.COLLISION_GROUP_NONE);
                        body.setCollisionGroup(2);
                        bulletAppState.getPhysicsSpace().add(body);
                        body.setGravity(Vector3f.ZERO);
                        body.setAngularDamping(0.70f);
                        SceneObjectControl sceneObjCtrl = Batter.getControl(SceneObjectControl.class);
                        sceneObjCtrl.rigidbodycontrol = body;
                        
                        RigidBodyControl anchor = new RigidBodyControl(new BoxCollisionShape(spat.getWorldScale()),1f);
                        Batter.getParent().addControl(anchor);
                        anchor.setCollideWithGroups(RigidBodyControl.COLLISION_GROUP_NONE);
                        anchor.setCollisionGroup(2);
                        bulletAppState.getPhysicsSpace().add(anchor);
                        anchor.setGravity(Vector3f.ZERO);
                        anchor.setKinematic(true);
                        
                        HingeJoint ganja;
                        ganja = new HingeJoint(anchor,
                                body,
                                new Vector3f(0f, 0f, 0f),
                                new Vector3f(0f, 0f, 0f),
                                Vector3f.UNIT_Y,
                                Vector3f.UNIT_Y);
                        bulletAppState.getPhysicsSpace().add(ganja);
                        
                        RigidBodyControl Bat = new RigidBodyControl(new BoxCollisionShape(spat.getWorldScale().multLocal(1.0f, 4.0f, 4.0f)), 1f);
                        BatOffsetNode.addControl(Bat);
                        bulletAppState.getPhysicsSpace().add(Bat);
                        Bat.setGravity(Vector3f.ZERO);
                        Bat.setKinematic(true);
                        Bat.setRestitution(0.5f);
                        Bat.setCollideWithGroups(RigidBodyControl.COLLISION_GROUP_03);
                        Bat.setCollisionGroup(RigidBodyControl.COLLISION_GROUP_04);
                        
                        sceneObjCtrl.ghostControl = new GhostControl(Bat.getCollisionShape());
                        BatOffsetNode.addControl(sceneObjCtrl.ghostControl);
                        sceneObjCtrl.ghostControl.setCollideWithGroups(RigidBodyControl.COLLISION_GROUP_03);
                        sceneObjCtrl.ghostControl.setCollisionGroup(RigidBodyControl.COLLISION_GROUP_04);
                        bulletAppState.getPhysicsSpace().add(sceneObjCtrl.ghostControl);
                        bulletAppState.getPhysicsSpace().addTickListener(sceneObjCtrl);
                    }
                }//Hard-coded ball phase.
                else if (SceneTraversalPhase == 5) {
                    if (spat.getName().equals("baseballmat") == true) {
                        spat.scale(4.0f);
                        Ball = spat;
                        AxisConstrainedRigidBody body = new AxisConstrainedRigidBody (new SphereCollisionShape(spat.getWorldScale().x),0.1f);
                        //AxisConstrainedRigidBody body = new AxisConstrainedRigidBody (new BoxCollisionShape(spat.getWorldScale()),1f);
                        body.constrainedAxes.y = 1f;
                        body.constrainVector.y = Ball.getWorldTranslation().y;
                        Ball.addControl(body);
                        bulletAppState.getPhysicsSpace().add(body);
                        body.setGravity(Vector3f.ZERO);
                        body.setDamping(0f, 0f);
                        body.setSleepingThresholds(0f, 0f);
                        body.setFriction(0f);
                        body.setRestitution(0.5f);
                        body.setCollisionGroup(RigidBodyControl.COLLISION_GROUP_03);
                        body.setCollideWithGroups(RigidBodyControl.COLLISION_GROUP_04);
                        //body.setCcdMotionThreshold(0.1f);
                        SceneObjectControl sceneObjCtrl = new SceneObjectControl();
                        sceneObjCtrl.app = GameplayState.this.app;
                        spat.addControl(sceneObjCtrl);
                        sceneObjCtrl.rigidbodycontrol = body;
                        ballOrigin = body.getPhysicsLocation();
                    }
                }//Base nodes phase.
                else if (SceneTraversalPhase == 6) {
                    if (spat.getName().equals("HomeBaseNode") == true) {
                        HomeBaseNode = spat;
                    }
                    else if (spat.getName().equals("FirstBaseNode") == true) {
                        FirstBaseNode = spat;
                    }
                    else if (spat.getName().equals("SecondBaseNode") == true) {
                        SecondBaseNode = spat;
                    }
                    else if (spat.getName().equals("ThirdBaseNode") == true) {
                        ThirdBaseNode = spat;
                    }
                }//Net nodes phase.
                else if (SceneTraversalPhase == 7) {
                    if (spatKeys.contains("isNet") == true) {
                        BallNetControl ballNetControl = new BallNetControl();
                        spat.addControl(ballNetControl);
                        ballNetControl.ghostControl = new GhostControl(new BoxCollisionShape(spat.getWorldScale()));
                        spat.addControl(ballNetControl.ghostControl);
                        ballNetControl.ghostControl.setCollideWithGroups(RigidBodyControl.COLLISION_GROUP_03);
                        ballNetControl.ghostControl.setCollisionGroup(RigidBodyControl.COLLISION_GROUP_04);
                        bulletAppState.getPhysicsSpace().add(ballNetControl.ghostControl);
                        //bulletAppState.getPhysicsSpace().addCollisionListener(ballNetControl);
                        bulletAppState.getPhysicsSpace().addTickListener(ballNetControl);
                    }
                }//Pitcher getter phase.
                else if (SceneTraversalPhase == 8) {
                    if (spatKeys.contains("isPitcher") == true) {
                        Pitcher = spat;
                    }
                }//Runner getter phase.
                else if (SceneTraversalPhase == 9) {
                    if (spatKeys.contains("isRunner") == true) {
                        Runner = spat.getParent();
                        SceneObjectControl sceneObjCtrl = spat.getParent().getControl(SceneObjectControl.class);
                        //RigidBodyControl body = new RigidBodyControl(new BoxCollisionShape(spat.getWorldScale().mult(new Vector3f(1f,4f,1f))),1f);
                        RigidBodyControl body = new RigidBodyControl(new BoxCollisionShape(spat.getWorldScale().mult(new Vector3f(0.5f,0.5f,0.5f))),1f);
                        Runner.addControl(body);
                        body.setCollisionGroup(RigidBodyControl.COLLISION_GROUP_05);
                        body.setCollideWithGroups(RigidBodyControl.COLLISION_GROUP_NONE);
                        bulletAppState.getPhysicsSpace().add(body);
                        body.setGravity(Vector3f.ZERO);
                        body.setDamping(0f, 0f);
                        body.setSleepingThresholds(0f, 0f);
                        body.setFriction(0f);
                        sceneObjCtrl.rigidbodycontrol = body;
                        
                        GhostRunnerControl ghostRunnerControl = new GhostRunnerControl();
                        spat.addControl(ghostRunnerControl);
                        ghostRunnerControl.ghostControl = new GhostControl(new BoxCollisionShape(spat.getWorldScale().mult(new Vector3f(4f,4f,4f))));
                        spat.addControl(ghostRunnerControl.ghostControl);
                        ghostRunnerControl.ghostControl.setCollideWithGroups(RigidBodyControl.COLLISION_GROUP_03);
                        ghostRunnerControl.ghostControl.setCollisionGroup(RigidBodyControl.COLLISION_GROUP_04);
                        bulletAppState.getPhysicsSpace().add(ghostRunnerControl.ghostControl);
                        bulletAppState.getPhysicsSpace().addTickListener(ghostRunnerControl);
                    }
                }
            }
        };

        SceneTraversalPhase = 0;

        rootNode.depthFirstTraversal(visitor);

        SceneTraversalPhase = 1;

        rootNode.depthFirstTraversal(visitor);

        SceneTraversalPhase = 2;

        rootNode.depthFirstTraversal(visitor);

        SceneTraversalPhase = 3;

        rootNode.depthFirstTraversal(visitor);

        SceneTraversalPhase = 4;

        rootNode.depthFirstTraversal(visitor);

        SceneTraversalPhase = 5;

        rootNode.depthFirstTraversal(visitor);

        SceneTraversalPhase = 6;

        rootNode.depthFirstTraversal(visitor);

        SceneTraversalPhase = 7;

        rootNode.depthFirstTraversal(visitor);

        SceneTraversalPhase = 8;

        rootNode.depthFirstTraversal(visitor);

        SceneTraversalPhase = 9;

        rootNode.depthFirstTraversal(visitor);

        ray = new Ray();
        
        collisionResults = new CollisionResults();

        mapKeys();
        
        Ball.getControl(SceneObjectControl.class).moveTo(HomeBaseNode.getWorldTranslation().setY(Ball.getWorldTranslation().y), 2f, false);
        
        Runner.lookAt(SecondBaseNode.getWorldTranslation().setY(Runner.getWorldTranslation().y), Vector3f.UNIT_Y);
        Vector3f v3Banana = new Vector3f(SecondBaseNode.getWorldTranslation());
        v3Banana.y = Runner.getWorldTranslation().y;
        Runner.getControl(SceneObjectControl.class).moveTo(v3Banana, 2f, true);
        //Runner.getControl(SceneObjectControl.class).moveTo(HomeBaseNode.getWorldTranslation().setY(Runner.getWorldTranslation().y), 2f, true);
        
        
        /*Runner.getControl(SceneObjectControl.class).rigidbodycontrol.setEnabled(false);
        path = new MotionPath();
        path.setCycle(true);
        //path.setPathSplineType(SplineType.Linear);
        path.addWayPoint(FirstBaseNode.getWorldTranslation());
        path.addWayPoint(SecondBaseNode.getWorldTranslation());
        path.addWayPoint(ThirdBaseNode.getWorldTranslation());
        path.addWayPoint(HomeBaseNode.getWorldTranslation());

        motionEvent = new MotionEvent(Runner, path);
        motionEvent.setLoopMode(LoopMode.Loop);
        motionEvent.setSpeed(1f);
        motionEvent.setDirectionType(MotionEvent.Direction.PathAndRotation);
        motionEvent.setRotation(new Quaternion().fromAngleNormalAxis(-FastMath.HALF_PI, Vector3f.UNIT_Y));
        motionEvent.setInitialDuration(10f);

        path.addListener(new MotionPathListener() {
            public void onWayPointReach(MotionEvent control, int wayPointIndex) {
                if (path.getNbWayPoints() == wayPointIndex + 1) {
                    System.out.println(control.getSpatial().getName() + "Finished!!! ");
                } else {
                    //System.out.println(control.getSpatial().getName() + " Reached way point " + wayPointIndex);
                    System.out.println(control.getSpatial().getWorldTranslation() + " " + control.getPath().getWayPoint(wayPointIndex));
                }
            }
        });

        path.enableDebugShape(assetManager, rootNode);

        motionEvent.play();
*/
        
        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        bmptxtScore = new BitmapText(guiFont);
        bmptxtScore.setSize(guiFont.getCharSet().getRenderedSize());
        bmptxtScore.move(
                settings.getWidth() / 2,
                bmptxtScore.getLineHeight(),
                0);
        bmptxtScore.setText("Score: 0");

        this.app.getGuiNode().attachChild(bmptxtScore);
        
        audioThrowBall = new AudioNode(assetManager, "Sounds/throwBall.ogg", false);
        
        audioThrowBall.setVolume(0.5f);
        
        audioSwing = new AudioNode(assetManager, "Sounds/swing.ogg", false);
        
        audioSwing.setVolume(0.5f);
        
        audioBatHit = new AudioNode(assetManager, "Sounds/batHit.ogg", false);
        
        audioBatHit.setVolume(0.5f);
        
        audioNetHit = new AudioNode(assetManager, "Sounds/netHit.ogg", false);
        
        audioNetHit.setVolume(0.5f);
        
        audioCheer = new AudioNode(assetManager, "Sounds/applause.ogg", false);
        
        audioCheer.setVolume(0.5f);
        
    }

    @Override
    public void update(float tpf) {
        //TODO: implement behavior during runtime
        //System.out.println("loc" + cam.getLocation() + "dir" + cam.getDirection());
    }

    @Override
    public void cleanup() {
        super.cleanup();
        //TODO: clean up what you initialized in the initialize method,
        //e.g. remove all spatials from rootNode
        //this is called on the OpenGL thread after the AppState has been detached
    }
    private ActionListener actionListener = new ActionListener() {
        public void onAction(String name, boolean keyPressed, float tpf) {
            //System.out.println(name + " " + keyPressed);
            if (keyPressed == true) {
                click2d = inputManager.getCursorPosition();
                click3d = cam.getWorldCoordinates(click2d, 0f);
                rayDir = cam.getWorldCoordinates(click2d, 1f).subtractLocal(click3d);
                ray.origin = click3d;
                ray.direction = rayDir;
                collisionResults.clear();
                rootNode.collideWith(ray, collisionResults);
                
                if (collisionResults.size() > 0) {

                    contactPoint = collisionResults.getCollision(0).getContactPoint();
                    
                    Batter.getControl(SceneObjectControl.class).rotateTo(contactPoint, 15f);

                }

            }

        }
    };

    public void mapKeys() {
        app.getFlyByCamera().setEnabled(false);
        app.getInputManager().addMapping("Touch", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        app.getInputManager().addListener(actionListener, "Touch");
    }
    
    public static void throwBal() {
        canBatHitSfx = true;
        audioThrowBall.playInstance();
        Ball.getControl(SceneObjectControl.class).onMoveToComplete();
        Ball.getControl(SceneObjectControl.class).rigidbodycontrol.setLinearVelocity(Vector3f.ZERO);
        Ball.getControl(SceneObjectControl.class).rigidbodycontrol.setAngularVelocity(Vector3f.ZERO);
        Ball.getControl(SceneObjectControl.class).rigidbodycontrol.setPhysicsLocation(ballOrigin);
        Ball.getControl(SceneObjectControl.class).moveTo(HomeBaseNode.getWorldTranslation().setY(Ball.getWorldTranslation().y), 2f, false);
        Pitcher.getParent().getControl(SceneObjectControl.class).animchannel.setAnim("Attack");
        Pitcher.getParent().getControl(SceneObjectControl.class).animchannel.setLoopMode(LoopMode.DontLoop);
    }
}
