package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A simple box class. A box is defined by it's lower (@see min) and upper (@see max) corner. 
 */
public class Box extends Intersectable {

	public Point3d max;
	public Point3d min;
	
    /**
     * Default constructor. Creates a 2x2x2 box centered at (0,0,0)
     */
    public Box() {
    	super();
    	this.max = new Point3d( 1, 1, 1 );
    	this.min = new Point3d( -1, -1, -1 );
    }	

	@Override
	public void intersect(Ray ray, IntersectResult result) {
		// TODO: Objective 6: intersection of Ray with axis aligned box
		Point3d p = new Point3d(ray.eyePoint);
		Vector3d d = new Vector3d(ray.viewDirection);
		
		double txmin = (this.min.x - p.x)/d.x;
		double tymin = (this.min.y - p.y)/d.y;
		double tzmin = (this.min.z - p.z)/d.z;
		double txmax = (this.max.x - p.x)/d.x;
		double tymax = (this.max.y - p.y)/d.y;
		double tzmax = (this.max.z - p.z)/d.z;
		
		double txlow = Math.min(txmin, txmax);
		double tylow = Math.min(tymin, tymax);
		double tzlow = Math.min(tzmin, tzmax);
		double txhigh = Math.max(txmin, txmax);
		double tyhigh = Math.max(tymin, tymax);
		double tzhigh = Math.max(tzmin, tzmax);
		
		double tmin = Math.max(txlow, Math.max(tylow, tzlow));
		double tmax = Math.min(txhigh, Math.min(tyhigh, tzhigh));
		
		if (result.epsilon < tmin && tmin < result.t && tmin < tmax){
			result.t = tmin;
			result.p.scaleAdd(tmin, d, p);
			result.material = this.material;
			if (max.x - result.epsilon < result.p.x && result.p.x < max.x + result.epsilon) result.n = new Vector3d(1, 0, 0);
	        else if(max.y - result.epsilon < result.p.y && result.p.y < max.y + result.epsilon) result.n = new Vector3d(0, 1,0);
	        else if(max.z - result.epsilon < result.p.z && result.p.z < max.z + result.epsilon) result.n = new Vector3d(0, 0,1);
	        else if(min.x - result.epsilon < result.p.x && result.p.x < min.x + result.epsilon) result.n = new Vector3d(-1, 0,0);
	        else if(min.y - result.epsilon < result.p.y && result.p.y < min.y + result.epsilon) result.n = new Vector3d(0, -1,0);
	        else if(min.z - result.epsilon < result.p.z && result.p.z < min.z + result.epsilon) result.n = new Vector3d(0, 0,-1);
		}
	}

}
