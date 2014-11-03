/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Baseball_Sack;

import com.jme3.audio.AudioSource;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.GhostControl;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author practicing01
 */
public class GhostRunnerControl extends AbstractControl implements PhysicsTickListener {
    //Any local variables should be encapsulated by getters/setters so they
    //appear in the SDK properties window and can be edited.
    //Right-click a local variable to encapsulate it with getters and setters.

    public GhostControl ghostControl;

    @Override
    protected void controlUpdate(float tpf) {
        //TODO: add code that controls Spatial,
        //e.g. spatial.rotate(tpf,tpf,tpf);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //Only needed for rendering-related operations,
        //not called when spatial is culled.
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        GhostRunnerControl control = new GhostRunnerControl();
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
    public void prePhysicsTick(PhysicsSpace space, float tpf) {
    }

    @Override
    public void physicsTick(PhysicsSpace space, float tpf) {
        if (ghostControl.getOverlappingCount() > 0) {
            List<PhysicsCollisionObject> overlappingObjects;
            overlappingObjects = ghostControl.getOverlappingObjects();
            for (Iterator<PhysicsCollisionObject> it = overlappingObjects.iterator(); it.hasNext();) {
                PhysicsCollisionObject physicsCollisionObject = it.next();
                //System.out.println(physicsCollisionObject.getUserObject().getClass());
                if (physicsCollisionObject.getUserObject().getClass().equals(Geometry.class) == true) {
                    Geometry natGeo = (Geometry) physicsCollisionObject.getUserObject();
                    if (natGeo.getName().equals("baseballmat") == true) {//more hardcoded nonsense
                        //System.out.println(natGeo.getName() + "ballsack!");
                        if (GameplayState.audioCheer.getStatus() != AudioSource.Status.Playing) {
                            GameplayState.audioCheer.play();
                        }
                        GameplayState.iScore++;
                        GameplayState.bmptxtScore.setText("Score: " + GameplayState.iScore);
                        GameplayState.throwBal();
                    }
                }
            }
        }
    }
}
