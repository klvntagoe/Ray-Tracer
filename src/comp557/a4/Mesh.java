package comp557.a4;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class Mesh extends Intersectable {
	
	/** Static map storing all meshes by name */
	public static Map<String,Mesh> meshMap = new HashMap<String,Mesh>();
	
	/**  Name for this mesh, to allow re-use of a polygon soup across Mesh objects */
	public String name = "";
	
	/**
	 * The polygon soup.
	 */
	public PolygonSoup soup;

	public Mesh() {
		super();
		this.soup = null;
	}			
		
	@Override
	public void intersect(Ray ray, IntersectResult result) {
		
		// TODO: Objective 9: ray triangle intersection for meshes
		for (int[] face : soup.faceList) {
			Point3d a = soup.vertexList.get(face[0]).p, b = soup.vertexList.get(face[1]).p, c = soup.vertexList.get(face[2]).p;
			
			Point3d p = new Point3d(ray.eyePoint);
			Vector3d d = new Vector3d(ray.viewDirection);
			
			//Ray-plane intersection
			Vector3d ab = new Vector3d(), bc = new Vector3d(), ca = new Vector3d();
			ab.sub(b, a);
			bc.sub(c, b);
			ca.sub(a, c);
			
			Vector3d lookAtPointA = new Vector3d();
			lookAtPointA.sub(a,p);
			
			Vector3d n = new Vector3d();
			n.cross(ab, ca);	//Opposite direction of ab x ac
			n.scale(-1);		//Flip direction
			n.normalize();
			
			double t = n.dot(lookAtPointA)/n.dot(d);
			
			//Inside-edge test
			if (result.epsilon < t && t < result.t) {
				Point3d x = new Point3d();
				x.scaleAdd(t, d, p);
				Vector3d ax = new Vector3d(), bx = new Vector3d(), cx = new Vector3d();
				ax.sub(x, a);
				bx.sub(x, b);
				cx.sub(x, c);
				
				Vector3d abaxNormal = new Vector3d(), bcbxNormal = new Vector3d(), cacxNormal = new Vector3d();
				abaxNormal.cross(ab, ax);
				bcbxNormal.cross(bc, bx);
				cacxNormal.cross(ca, cx);
				if (result.epsilon < abaxNormal.dot(n) && result.epsilon < bcbxNormal.dot(n) && result.epsilon < cacxNormal.dot(n)) {
					result.t = t;
	    			result.p.scaleAdd(t, d, p);
	    			result.n.set(n);
	    			result.material = material;
				}
			}
		}
		
	}

}
