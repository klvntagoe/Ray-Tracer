package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Class for a plane at y=0.
 * 
 * This surface can have two materials.  If both are defined, a 1x1 tile checker 
 * board pattern should be generated on the plane using the two materials.
 */
public class Plane extends Intersectable {
    
	/** The second material, if non-null is used to produce a checker board pattern. */
	Material material2;
	
	/** The plane normal is the y direction */
	public static final Vector3d n = new Vector3d( 0, 1, 0 );
    
    /**
     * Default constructor
     */
    public Plane() {
    	super();
    }
    
        
    @Override
    public void intersect( Ray ray, IntersectResult result ) {
    
        // TODO: Objective 4: intersection of ray with plane
    	//CGPP Ch 7.8.1
		Point3d origin = new Point3d(0,0,0);		//Point on the ground plane
    	Point3d p = new Point3d(ray.eyePoint);
    	Vector3d d = new Vector3d(ray.viewDirection);
    	d.normalize();
    	
    	if (this.n.dot(d) != 0) {
    		Vector3d lookAtOrigin = new Vector3d();
    		lookAtOrigin.sub(origin, p);
    		double t = this.n.dot(lookAtOrigin)/this.n.dot(d);
    		if (result.epsilon < t && t < result.t) {
    			result.t = t;
    			result.p.scaleAdd(t, d, p);
    			result.n.set(this.n);
    			if (material2 != null) {
    				int x = (int) Math.abs(Math.floor(result.p.x));
    				int z = (int) Math.abs(Math.floor(result.p.z));
    				
                    if(x % 2 == z % 2) result.material = this.material;
                    else result.material = this.material2;
                    
    			}else result.material = this.material;
    			
    		}
    	}
    }
    
}
