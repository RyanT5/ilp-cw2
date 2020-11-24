package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.turf.TurfJoins;

public class App {
	
	public static List<Feature> sensors = new ArrayList<Feature>();
	public static List<Feature> allFeatures = new ArrayList<Feature>();
		
    public static void main( String[] args ) {
        
//        Arg 0: day
//        Arg 1: month
//        Arg 2: year
//        Arg 3: starting point x
//        Arg 4: starting point y
//        Arg 5: seed
//        Arg 6: port
    	
//    	Map map = new Map(arg[0], arg[1], arg[2], arg[6]);
    	Map map = new Map("15", "06", "2021", "80");
//    	Drone drone = new Drone(arg[3], arg[4]);
    	Drone drone = new Drone(55.9444, -3.1878);
    	
    	//
//    	sensors = map.getFeaturedSensors();
//    	while (drone.getTerminated() == false) {
//    		drone.nextMove(sensors);
//    	}
    	//
    	
    	allFeatures = map.getMapFeatures();
    	renderGeojson();
    	
    }
    
    // collision between linestring and polygon
    
    // point within boundary
    
//	 Render 'features'
    
   private static void renderGeojson() {
//   	Create feature collection from list of features
   	FeatureCollection fc = FeatureCollection.fromFeatures(allFeatures);
//   	Convert to json string
   	String geojson = fc.toJson();
   	
   	System.out.println(geojson);
   }
   
}
