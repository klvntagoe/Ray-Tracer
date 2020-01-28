package comp557.a4;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Simple scene loader based on XML file format.
 */
public class Scene {
    
    /** List of surfaces in the scene */
    public List<Intersectable> surfaceList = new ArrayList<Intersectable>();
	
	/** All scene lights */
	public Map<String,Light> lights = new HashMap<String,Light>();

    /** Contains information about how to render the scene */
    public Render render;
    
    /** The ambient light colour */
    public Color3f ambient = new Color3f();

    public final int maxResursionDepth = 10;
    /** 
     * Default constructor.
     */
    public Scene() {
    	this.render = new Render();
    }
    
    /**
     * renders the scene
     */
    public void render(boolean showPanel) {
 
        Camera cam = render.camera; 
        int w = cam.imageSize.width;
        int h = cam.imageSize.height;
        int numThreads = 8;
        int width = w/numThreads;
        
        render.init(w, h, showPanel);
        
        //setPixels(0, w, 0, h);
        ArrayList<Thread> listOfThreads = new ArrayList<Thread>();
        for (int k = 0; k < numThreads; k++) {
        	listOfThreads.add(new Thread(new RenderAccelerator(k * width, (k+1) * width, 0, h)));
        }
        for (int k = 0; k < numThreads; k++) {
        	listOfThreads.get(k).start();
        }
        try {
        	for (int k = 0; k < numThreads; k++) {
            	listOfThreads.get(k).join();
            }
        }catch(Exception e) {
        	System.out.append(e.getMessage());
        }
        
        // save the final render image
        render.save();
        // wait for render viewer to close
        render.waitDone();
        
    }
    
    public void TraceRay(int iMin, int iMax, int jMin, int jMax) {
    	 int samples = Math.max(render.samples, 1);
         boolean jitter = render.jitter;
         
         for ( int j = jMin; j < jMax && !render.isDone(); j++ ) {
             for ( int i = iMin; i < iMax && !render.isDone(); i++ ) {
             	//Initializations
             	double[] offset = {0.5,0.5};
             	Color3f lighting = new Color3f(0,0,0);
             	
             	//TODO: Objective 8: anti-aliasing (Stochastic Super-sampling)
             	if (!jitter) {	//If no jitter then shoot first ray to center else shoot ray with random offset
                 	Color3f temp = getPixelColour(i, j, offset);
                     lighting.add(temp);
             	}else {
             		offset[0] = Math.random() - 0.5;
             		offset[1] = Math.random() - 0.5;
             		Color3f temp = getPixelColour(i, j, offset);
                     lighting.add(temp);
             	}
             	//Shoot remaining (sample - 1) rays with random offset
             	for (int k = 1; k < samples; k++) {
             		offset[0] = Math.random() - 0.5;
             		offset[1] = Math.random() - 0.5;
             		Color3f temp = getPixelColour(i, j, offset);
                     lighting.add(temp);
             	}
             	lighting.x /= (float) samples;
     			lighting.y /= (float) samples;
     			lighting.z /= (float) samples;
     			
             	// Here is an example of how to calculate the pixel value.
             	Color3f c = new Color3f(lighting);
             	int r = (int)(255*c.x);
                 int g = (int)(255*c.y);
                 int b = (int)(255*c.z);
                 int a = 255;
                 int argb = (a<<24 | r<<16 | g<<8 | b);    
                 
                 // update the render image
                 render.setPixel(i, j, argb);
             }
         }
    }
    
    /**
     * Generate a ray through a pixel (i,j) and compute shading result with respect to the scene
     * 
     * @param i The pixel row.
     * @param j The pixel column.
     * @param offset The offset from the center of the pixel, in the range [-0.5,+0.5] for each coordinate. 
     */
    public Color3f getPixelColour(int i, int j, double[] offset) {

        // TODO: Objective 1: generate a ray (use the generateRay method)
    	Ray ray = new Ray();
    	generateRay(i, j, offset, render.camera, ray);
    	
        // TODO: Objective 2: test for intersection with scene surfaces
		IntersectResult closestResult = new IntersectResult();
    	for (Intersectable surface : this.surfaceList) {
    		IntersectResult result = new IntersectResult();
    		surface.intersect(ray, result);
    		if (closestResult.t > result.t) closestResult = new IntersectResult(result);
    	}
    	
        // TODO: Objective 3: compute the shaded result for the intersection point (perhaps requiring shadow rays)
        Color3f lighting = new Color3f(0,0,0);
        if (closestResult.material != null && 0 < closestResult.t && closestResult.t < Double.POSITIVE_INFINITY) {
        	Color3f shading = new Color3f(0,0,0);
        	
        	shading = computeShading(closestResult);
        	
    		if (0 < closestResult.material.reflectiveness && closestResult.material.reflectiveness <= 1) {
    			shading.scale(1 - closestResult.material.reflectiveness);
    			shading.add(applyReflection(ray, closestResult, 0));
    		}
    		
    		lighting.add(shading);
        }else lighting.add(this.render.bgcolor);
        
        lighting.clamp(0, 1);
        
        return lighting;
    }
    
    /**
     * Generate a ray through pixel (i,j).
     * 
     * @param i The pixel row.
     * @param j The pixel column.
     * @param offset The offset from the center of the pixel, in the range [-0.5,+0.5] for each coordinate. 
     * @param cam The camera.
     * @param ray Contains the generated ray.
     */
	public static void generateRay(final int i, final int j, final double[] offset, final Camera cam, Ray ray) {
		// TODO: Objective 1: generate rays given the provided parmeters
		
		double distance = 1;
		double top = Math.tan(Math.toRadians(cam.fovy)/2) * distance;
		double bottom = -top;
		double aspectRatio = cam.imageSize.width / cam.imageSize.height;
		double right = top * aspectRatio;
		double left = -right;
		
		/*
		double top = cam.imageSize.height/2;
		double bottom = -top;
		double aspectRatio = cam.imageSize.width / cam.imageSize.height;
		double right = top*aspectRatio;
		double left = -right;
		double distance = top/Math.tan(Math.toRadians(cam.fovy/2));
		*/
		
		double uCoordinate = left + (right - left) * (i + offset[0])/cam.imageSize.width;
		double vCoordinate = bottom + (top - bottom) * (j + offset[1])/cam.imageSize.height;
		
		Vector3d u = new Vector3d(), v = new Vector3d(), w = new Vector3d();
		w.sub(cam.from, cam.to);
		w.normalize();
		u.cross(cam.up, w);
		u.normalize();
		v.cross(u, w);
		v.normalize();
		
		Point3d p = new Point3d();
		p.set(cam.from);
		
		Vector3d d = new Vector3d();
		d.scale(-distance, w);
		d.scaleAdd(vCoordinate, v, d);
		d.scaleAdd(uCoordinate, u, d);
		d.normalize();
		
		ray.set(p, d);
	}

	/**
     * Compute shading of a material.
     * 
     * @param result The result of a given intersection.
     * @param light A given light.
     */
	public Color3f computeShading(IntersectResult result) {
		Color3f shading = new Color3f();
		for (Light light : this.lights.values()) {
			Color4f sourceColour = new Color4f(light.color);
			
			Vector3d shadowD = new Vector3d();
			shadowD.sub(light.from, result.p);
			
			Point3d shadowP = new Point3d();
			shadowP.scaleAdd(result.epsilon, shadowD, result.p);
			
			Ray shadowRay = new Ray();
			shadowRay.set(shadowP, shadowD);
			
			IntersectResult shadowResult = new IntersectResult();
			
			if (!inShadow(result, light, shadowResult, shadowRay)) {
				Vector3d v = new Vector3d();
				v.sub(render.camera.from, result.p);
				v.normalize();
				
	        	Vector3d l = new Vector3d();
	    		l.set(shadowD);
				l.normalize();
				
				Color3f lambertian = new Color3f(result.material.diffuse.x*sourceColour.x, result.material.diffuse.y*sourceColour.y, result.material.diffuse.z*sourceColour.z);
				lambertian.scale((float) Math.max(0, result.n.dot(l)));
				shading.add(lambertian);
				
	    		Vector3d bisector = new Vector3d();
				bisector.add(v, l);
				bisector.normalize();
				
				Color3f specular = new Color3f(result.material.specular.x*sourceColour.x, result.material.specular.y*sourceColour.y, result.material.specular.z*sourceColour.z);
				specular.scale((float) Math.pow(Math.max(0, result.n.dot(bisector)), result.material.shinyness));
				shading.add(specular);
			}
			shading.x += result.material.diffuse.x*ambient.x*sourceColour.x;
			shading.y += result.material.diffuse.y*ambient.y*sourceColour.y;
			shading.z += result.material.diffuse.z*ambient.z*sourceColour.z;
		}
		return shading;
	}
	/**
	 * Shoot a shadow ray in the scene and get the result.
	 * 
	 * @param result Intersection result from raytracing. 
	 * @param light The light to check for visibility.
	 * @param root The scene node.
	 * @param shadowResult Contains the result of a shadow ray test.
	 * @param shadowRay Contains the shadow ray used to test for visibility.
	 * 
	 * @return True if a point is in shadow, false otherwise. 
	 */
	public boolean inShadow(final IntersectResult result, final Light light, IntersectResult shadowResult, Ray shadowRay) {

		//For computing distance from light source to point of interest
    	Vector3d lightVector = new Vector3d();
    	lightVector.sub(light.from, shadowRay.eyePoint);
    	
		// TODO: Objective 5: check for shadows and use it in your lighting computation
		for (Intersectable surface : surfaceList) {
			surface.intersect(shadowRay, shadowResult);
            if(shadowResult.epsilon < shadowResult.t && shadowResult.t < Double.POSITIVE_INFINITY) {
            	Vector3d shadowVector = new Vector3d();
            	shadowVector.sub(shadowResult.p, shadowRay.eyePoint);
            	if (shadowResult.material != null && shadowVector.length() < lightVector.length()) return true;
            }
        }
		return false;
	}
	
	/**
	 * Compute the shading of the reflection of a ray.
	 * 
	 * @param ray Ray that was shot at object
	 * @param result Intersection result of interected reflective object.
	 * @param currentDepth current depth of recursive stack frame
	 * 
	 * @return Shading of reflection. 
	 */
	public Color3f applyReflection( Ray ray, IntersectResult result, int currentDepth ) {
		Color3f shading = new Color3f(0,0,0);
		
		if (currentDepth > maxResursionDepth) return shading;
		
		//Compute reflected ray
		Vector3d v = new Vector3d(ray.viewDirection);
		v.scale(-1.0);
		
		Vector3d n = new Vector3d(result.n);
		double d = v.dot(n) * 2.0;
		n.scale(d);

		Vector3d reflectedDirection = new Vector3d();
		reflectedDirection.sub(n,v);
		reflectedDirection.normalize();
		
		Point3d shiftedPoint = new Point3d();
		shiftedPoint.scaleAdd(result.epsilon, result.n, result.p);
		
		Ray reflectedRay = new Ray();
		reflectedRay.set(shiftedPoint, reflectedDirection);
		
		//Intersect Reflected array
		IntersectResult reflectedRayIntersectionResult = new IntersectResult();
        for(Intersectable surface : surfaceList){
            IntersectResult temp = new IntersectResult();
            surface.intersect(reflectedRay, temp);
            if(result.epsilon < temp.t && temp.t < reflectedRayIntersectionResult.t){
            	reflectedRayIntersectionResult = new IntersectResult(temp);
            }
        }
        
        //We either recurse (if intersected object is reflecive), return shading (if intersected objects is not reflective), or return background color(no intersection)
        if (result.epsilon < reflectedRayIntersectionResult.t && reflectedRayIntersectionResult.t < Double.POSITIVE_INFINITY) {
        	if (reflectedRayIntersectionResult.material.reflectiveness > 0) {
        		shading = applyReflection(reflectedRay, reflectedRayIntersectionResult, currentDepth++);
        		shading.scale(reflectedRayIntersectionResult.material.reflectiveness);
        	}else {
        		shading = computeShading(reflectedRayIntersectionResult);
        	}
        }else shading = new Color3f(render.bgcolor);
        
		return shading;
    }
	
	private class RenderAccelerator implements Runnable{
		
		private int iMin, iMax, jMin, jMax;
		
		public RenderAccelerator(int iMin, int iMax, int jMin, int jMax) {
			this.iMin = iMin;
			this.iMax = iMax;
			this.jMin = jMin;
			this.jMax = jMax;
		}
		
		@Override
		public void run() {
			TraceRay(this.iMin, this.iMax, this.jMin, this.jMax);
		}
	}
}