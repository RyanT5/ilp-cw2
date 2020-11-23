package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.turf.TurfJoins;

public class App 
{
		
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
    	map.renderGeojson();
    	
    }
    
    // distance between two points
    
    private double calcDistance(Point p1, Point p2) {
    	
    	double distance = Math.sqrt((Math.pow((p1.longitude() - p2.longitude()), 2) + Math.pow((p1.latitude() - p2.latitude()), 2)));
    	
		return distance;
    }
    
    // collision between point and polygon - is this redundant?
    
    private boolean pointInPoly(Point point, Polygon poly) {
    	
    	boolean intersect = TurfJoins.inside(point, poly);
    	
    	return intersect;
    }
    
    // collision between linestring and polygon
    
    // point within boundary
}
