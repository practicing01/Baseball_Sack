/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Baseball_Sack;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioSource;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * @author practicing01
 */
public class SceneObjectControl extends AbstractControl implements AnimEventListener, PhysicsTickListener {
    //Any local variables should be encapsulated by getters/setters so they
    //appear in the SDK properties window and can be edited.
    //Right-click a local variable to encapsulate it with getters and setters.

    public SimpleApplication app;
    public AnimControl animcontrol;
    public AnimChannel animchannel;
    public RigidBodyControl rigidbodycontrol;
    public GhostControl ghostControl;
    public Vector3f movedirection, location, destination;
    public boolean moving = false;
    public boolean rotating = false;
    public boolean mStopOnCompletion = false;
    public float traveltime, elapsedTime, tpfOffset;
    public long curTime, prevTime, systemTimeElapsed;

    public void onMoveToComplete() {
        if (moving == true) {
            moving = false;
            if (mStopOnCompletion == true) {
                //System.out.println(elapsedTime-tpfOffset + " " + traveltime + " " + destination + " " + rigidbodycontrol.getPhysicsLocation());
                app.enqueue(new Callable() {
                    public Object call() throws Exception {
                        rigidbodycontrol.setLinearVelocity(Vector3f.ZERO);
                        if (spatial == GameplayState.Runner) {//Grotesque hack cus I'm getting sick of this app & need to learn java "function pointers".
                            rigidbodycontrol.setEnabled(false);
                            GameplayState.basePhase++;
                            if (GameplayState.basePhase > 3) {
                                GameplayState.basePhase = 0;
                            }
                            if (GameplayState.basePhase == 1) {
                                spatial.lookAt(GameplayState.ThirdBaseNode.getWorldTranslation().setY(spatial.getWorldTranslation().y), Vector3f.UNIT_Y);
                                spatial.getControl(SceneObjectControl.class).moveTo(GameplayState.ThirdBaseNode.getWorldTranslation().setY(spatial.getWorldTranslation().y), 2f, true);
                            } else if (GameplayState.basePhase == 2) {
                                spatial.lookAt(GameplayState.HomeBaseNode.getWorldTranslation().setY(spatial.getWorldTranslation().y), Vector3f.UNIT_Y);
                                spatial.getControl(SceneObjectControl.class).moveTo(GameplayState.HomeBaseNode.getWorldTranslation().setY(spatial.getWorldTranslation().y), 2f, true);
                            } else if (GameplayState.basePhase == 3) {
                                spatial.lookAt(GameplayState.FirstBaseNode.getWorldTranslation().setY(spatial.getWorldTranslation().y), Vector3f.UNIT_Y);
                                spatial.getControl(SceneObjectControl.class).moveTo(GameplayState.FirstBaseNode.getWorldTranslation().setY(spatial.getWorldTranslation().y), 2f, true);
                            } else if (GameplayState.basePhase == 0) {
                                spatial.lookAt(GameplayState.SecondBaseNode.getWorldTranslation().setY(spatial.getWorldTranslation().y), Vector3f.UNIT_Y);
                                spatial.getControl(SceneObjectControl.class).moveTo(GameplayState.SecondBaseNode.getWorldTranslation().setY(spatial.getWorldTranslation().y), 2f, true);
                            }
                            rigidbodycontrol.setPhysicsRotation(spatial.getWorldRotation());
                            rigidbodycontrol.setEnabled(true);
                            rigidbodycontrol.setGravity(Vector3f.ZERO);
                            rigidbodycontrol.setDamping(0f, 0f);
                            rigidbodycontrol.setSleepingThresholds(0f, 0f);
                            rigidbodycontrol.setFriction(0f);
                        }
                        return null;
                    }
                });
            }
        }
    }

    public void moveTo(final Vector3f dest, final float speed, final boolean stopOnCompletion) {
        app.enqueue(new Callable() {
            public Object call() throws Exception {
                destination = dest;
                //location = spatial.getWorldTranslation();//Serious set/get synch problems with this engine.
                location = rigidbodycontrol.getPhysicsLocation();//Works cus this particular app calls this within a phys tick
                movedirection = dest.subtract(location);
                movedirection.normalizeLocal();
                traveltime = destination.distance(location) / speed;
                //prevTime = -1;
                //systemTimeElapsed = 0;
                elapsedTime = -1f;
                mStopOnCompletion = stopOnCompletion;
                moving = true;
                rigidbodycontrol.setLinearVelocity(movedirection.mult(speed));
                return null;
            }
        });
    }

    public void onRotateToComplete() {
        rotating = false;
        app.enqueue(new Callable() {
            public Object call() throws Exception {
                rigidbodycontrol.setAngularVelocity(Vector3f.ZERO);
                return null;
            }
        });
    }

    public void rotateTo(final Vector3f destination, final float speed) {
        app.enqueue(new Callable() {
            public Object call() throws Exception {
                GameplayState.audioSwing.playInstance();
                movedirection = spatial.getWorldTranslation().subtract(destination);
                movedirection.y = 0f;
                location = spatial.getWorldRotation().multLocal(new Vector3f(0f, 0f, 1f));
                location.y = 0f;
                movedirection.crossLocal(location);
                if (movedirection.y > 0) {
                    rigidbodycontrol.setAngularVelocity(new Vector3f(0f, speed, 0f));
                } else if (movedirection.y < 0) {
                    rigidbodycontrol.setAngularVelocity(new Vector3f(0f, -speed, 0f));
                }
                return null;
            }
        });
    }

    @Override
    protected void controlUpdate(float tpf) {
        //TODO: add code that controls Spatial,
        //e.g. spatial.rotate(tpf,tpf,tpf);
        if (moving == true) {
            if (elapsedTime == -1f) {
                tpfOffset = tpf;
            }
            /*curTime = System.currentTimeMillis();
             if (prevTime != -1) {
             systemTimeElapsed += curTime-prevTime;
             }
             prevTime = curTime;*/
            elapsedTime += tpf;
            //System.out.println(elapsedTime + " " + (elapsedTime-tpfOffset));
            //if (systemTimeElapsed/1000f >= traveltime) {
            if (elapsedTime - tpfOffset >= traveltime) {
                onMoveToComplete();
            }
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //Only needed for rendering-related operations,
        //not called when spatial is culled.
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        SceneObjectControl control = new SceneObjectControl();
        //TODO: copy parameters to new Control
        return control;
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule in = im.getCapsule(this);
        //TODO: load properties of this Control, e.g.
        //this.value = in.readFloat("name", defaultValue);
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule out = ex.getCapsule(this);
        //TODO: save properties of this Control, e.g.
        //out.write(this.value, "name", defaultValue);
    }

    @Override
    public void onAnimCycleDone(AnimControl animcontrol, AnimChannel animchannel, String animname) {
        //System.out.println("anim done " + animname);
        if (animname.equals("Attack")) {
            animchannel.setAnim("Idle");
            animchannel.setLoopMode(LoopMode.Loop);
        }
    }

    @Override
    public void onAnimChange(AnimControl animcontrol, AnimChannel AnimChannel, String animname) {
        //System.out.println("anim change " + animname);
    }

    @Override
    public void prePhysicsTick(PhysicsSpace space, float tpf) {
    }

    @Override
    public void physicsTick(PhysicsSpace space, float tpf) {
        if (ghostControl.getOverlappingCount() > 0) {
            List<PhysicsCollisionObject> overlappingObjects;
            overlappingObjects = ghostControl.getOverlappingObjects();
            for (Iterator<PhysicsCollisionObject> it = overlappingObjects.iterator(); it.hasNext();) {
                PhysicsCollisionObject physicsCollisionObject = it.next();
                if (physicsCollisionObject.getUserObject().getClass().equals(Geometry.class) == true) {
                    Geometry natGeo = (Geometry) physicsCollisionObject.getUserObject();
                    if (natGeo.getName().equals("baseballmat") == true) {//more hardcoded nonsense
                        if (GameplayState.canBatHitSfx == true) {//GameplayState.audioBatHit.getStatus() != AudioSource.Status.Playing) {
                            GameplayState.canBatHitSfx = false;
                            GameplayState.audioBatHit.play();
                        }
                    }
                }
            }
        }
    }
}
