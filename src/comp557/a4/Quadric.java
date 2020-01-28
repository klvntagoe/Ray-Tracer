package comp557.a4;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;


public class Quadric extends Intersectable {
    
	/**
	 * Radius of the sphere.
	 */
	public Matrix4d Q = new Matrix4d();
	public Matrix3d A = new Matrix3d();
	public Vector3d B = new Vector3d();
	public double C;
	
	public int min = -10, max = 10;
	
	/**
	 * The second material, e.g., for front and back?
	 */
	Material material2 = null;
	
	public Quadric() {
	
	}
	
	@Override
	public void intersect(Ray ray, IntersectResult result) {
		//TODO: Quadric Intersection
		Vector3d p = new Vector3d(ray.eyePoint);
		Vector3d d = new Vector3d(ray.viewDirection);
    	
		Vector3d Arow0 = new Vector3d(), Arow1 = new Vector3d(), Arow2 = new Vector3d();
    	A.getRow(0, Arow0);
    	A.getRow(1, Arow1);
    	A.getRow(2, Arow2);
    	
    	Vector3d Ad = new Vector3d();
    	Ad.x = Arow0.dot(d);
    	Ad.y = Arow1.dot(d);
    	Ad.z = Arow2.dot(d);
    	
    	Vector3d Ap = new Vector3d();
    	Ap.x = Arow0.dot(p);
    	Ap.y = Arow1.dot(p);
    	Ap.z = Arow2.dot(p);
    	
    	double a = d.dot(Ad);
    	double b = (2*p.dot(Ad)) - (2*this.B.dot(d));
    	double c = p.dot(Ap) - (2*this.B.dot(p)) + this.C;

    	double discriminant = Math.pow(b, 2) - (4*a*c);
    	if (discriminant >= 0) {
    		double t1 = (0 - b - Math.sqrt(discriminant))/(2*a);
    		double t2 = (0 - b + Math.sqrt(discriminant))/(2*a);
    		double t = Math.min(t1, t2);
    		
    		Point3d x = new Point3d();
    		x.scaleAdd(t, d, p);
    		
    		if (min <= x.x && x.x <= max 
    				&& min <= x.y && x.y <= max 
    				&& min <= x.z && x.z <= max) {
    			Vector3d n = new Vector3d();
            	Vector3d temp = new Vector3d(x);
            	n.x = Arow0.dot(temp) - (2*B.x);
            	n.y = Arow1.dot(temp) - (2*B.x);
            	n.z = Arow2.dot(temp) - (2*B.x);
        		
        		if (result.epsilon < t && t < result.t) {
        			result.t = t;
        			result.p.set(x);
        			result.n.set(n);
        			result.material = this.material2 != null ? this.material2 : this.material;
        		}
    		}
    	}
	}
	
}
