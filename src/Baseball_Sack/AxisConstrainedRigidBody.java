/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Baseball_Sack;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;

/**
 *
 * @author practicing01
 */
public class AxisConstrainedRigidBody extends RigidBodyControl implements PhysicsTickListener{

    public Vector3f constrainedAxes;//Dumb to use floats to represent bools, need to fix this.
    public Vector3f constrainVector;
    public Vector3f loc;
    public Vector3f velocity;
    
    public AxisConstrainedRigidBody(BoxCollisionShape cshape, float mass) {
        super(cshape,mass);
        constrainedAxes = new Vector3f(Vector3f.ZERO);
        constrainVector = new Vector3f(Vector3f.ZERO);
    }
    
    public AxisConstrainedRigidBody(SphereCollisionShape cshape, float mass) {
        super(cshape,mass);
        constrainedAxes = new Vector3f(Vector3f.ZERO);
        constrainVector = new Vector3f(Vector3f.ZERO);
    }

    public AxisConstrainedRigidBody() {
        constrainedAxes = new Vector3f(Vector3f.ZERO);
        constrainVector = new Vector3f(Vector3f.ZERO);
    }

    @Override
    public void setPhysicsSpace(PhysicsSpace space) {
        super.setPhysicsSpace(space);
        space.addTickListener(this);
    }
 
    @Override
    public void prePhysicsTick(PhysicsSpace space, float tpf) {
        
        loc = getPhysicsLocation();
        
        velocity = getLinearVelocity();
        
        if (constrainedAxes.x != 0f) {
            loc.x = constrainVector.x;
            velocity.x = 0;
        }
        if (constrainedAxes.y != 0f) {
            loc.y = constrainVector.y;
            velocity.y = 0;
        }
        if (constrainedAxes.z != 0f) {
            loc.z = constrainVector.z;
            velocity.z = 0;
        }
        
        setPhysicsLocation(loc);
        
        setLinearVelocity(velocity);
    }

    @Override
    public void physicsTick(PhysicsSpace space, float tpf) {
        
    }
    
}
