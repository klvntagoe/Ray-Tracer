package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A simple sphere class.
 */
public class Sphere extends Intersectable {
    
	/** Radius of the sphere. */
	public double radius = 1;
    
	/** Location of the sphere center. */
	public Point3d center = new Point3d( 0, 0, 0 );
    
    /**
     * Default constructor
     */
    public Sphere() {
    	super();
    }
    
    /**
     * Creates a sphere with the request radius and center. 
     * 
     * @param radius
     * @param center
     * @param material
     */
    public Sphere( double radius, Point3d center, Material material ) {
    	super();
    	this.radius = radius;
    	this.center = center;
    	this.material = material;
    }
    
    @Override
    public void intersect( Ray ray, IntersectResult result ) {
    
        // TODO: Objective 2: intersection of ray with sphere
    	//CGPP Ch 7.8.2 - Note that 4s cancel in numerator and denominator
    	//Vector3d v = new Vector3d(ray.eyePoint);
    	Vector3d v = new Vector3d();
    	v.sub(ray.eyePoint, this.center);
    	Vector3d d = new Vector3d(ray.viewDirection);
    	double a = d.dot(d);
    	double b = 2 * d.dot(v);
    	double c = v.dot(v) - Math.pow(this.radius, 2);
    	double discriminant = Math.pow(b, 2) - (4*a*c);
    	if (discriminant >= 0) {
    		double t1 = (0 - b - Math.sqrt(discriminant))/(2*a);
    		double t2 = (0 - b + Math.sqrt(discriminant))/(2*a);
    		double t = Math.min(t1, t2);
    		if (result.epsilon < t && t < result.t) {
    			result.t = t;
            	result.p.scaleAdd(t, ray.viewDirection, ray.eyePoint);
            	result.n.sub(result.p, this.center);
            	result.n.normalize();
    			result.material = this.material;
    		}
    	}
    }
}
