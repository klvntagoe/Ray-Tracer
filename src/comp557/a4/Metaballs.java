package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;

public class Metaballs extends Intersectable {

    public double boundRadius = 1;
    public double threshhold = 1;
    public ArrayList<Sphere> boundingSpheres = new ArrayList<>();
    public int maxDistance = 6;

    public double marchIncrement = 0.003;

    public ArrayList<Point3d> centers = new ArrayList<>();
    public Metaballs(){super();}

    public Metaballs(double threshhold, ArrayList<Point3d> centers, Material material){
        super();
        this.threshhold = threshhold;
        this.centers = centers;
        this.material = material;


    }

    public void setBoundingSpheres(){
        this.boundRadius = 1/(Math.sqrt(threshhold/3));
        for(Point3d center : centers){
            this.boundingSpheres.add(new Sphere(boundRadius, center, null));
        }
    }

    /**Check if a point is inside or outside the threshhold.*/
    private double sumF(Point3d p){
        double sum = 0;
        for(Point3d pi : centers){
            sum += (1/(Math.pow(p.x - pi.x, 2) + Math.pow(p.y - pi.y, 2) + Math.pow(p.z - pi.z, 2)));
        }
        return sum;
    }

    private Vector3d computeNormal(IntersectResult result){
        Vector3d n;
        Vector3d gradSum = new Vector3d();

        for(Point3d p : centers){
            Vector3d grad = new Vector3d();
            grad.sub(result.p, p);
            double denominator = Math.pow(grad.length(), 4);
            grad.scale(2*(1/denominator));
            gradSum.add(grad);
        }
        n = gradSum;
        n.normalize();
        return n;
    }

    private boolean checkBoundIntersection(Ray ray){
        IntersectResult tempResult = new IntersectResult();
        for(Sphere s : boundingSpheres){
            s.intersect(ray, tempResult);
            if(tempResult.t < Double.POSITIVE_INFINITY && tempResult.t > 0){
                return true;
            }
        }
        return false;
    }

    @Override
    public void intersect(Ray ray, IntersectResult result){
        if(! checkBoundIntersection(ray)) return;

        double distance = 0;
        Point3d currentPointAlongRay = new Point3d(ray.eyePoint);
        Point3d nextPointAlongRay = new Point3d(ray.viewDirection);
        nextPointAlongRay.scale(marchIncrement);

        while(distance < maxDistance){
            distance += marchIncrement;
            currentPointAlongRay.add(nextPointAlongRay);
            if(sumF(currentPointAlongRay) >= threshhold){
                // Collision!
                result.p = currentPointAlongRay;
                result.material = material;
                result.n = computeNormal(result);
                result.t = distance;
                return;
            }
        }
    }
}