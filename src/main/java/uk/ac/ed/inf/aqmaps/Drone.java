package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.turf.TurfJoins;

public class Drone {
	
	private Point startPoint;
	private Point currentLoc;
	
	private static int maxMoves = 150;
	
	private boolean terminated;

	private ArrayList<DroneMove> moveList = new ArrayList<DroneMove>();
	
	public Drone(double startLng, double startLat) {
		this.startPoint = Point.fromLngLat(startLng, startLat);
		this.currentLoc = startPoint;
		this.terminated = false;
	}
	
	public void nextMove(List<Feature> sensors) {
		
		if (moveList.size() < maxMoves) {
			
		} else {
			terminated = true;
		}
		
	}

	private Feature closestSensor(List<Feature> sensors) {
		Feature closest = sensors.get(0);
		for (Feature s : sensors) {
//			Point sensorPoint = s.geometry();
//			double distance = calcDistance();
		}
		
		return closest;
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
	
	private void updateDroneMoves(String direction, Point landPoint) {
		DroneMove nextMove = new DroneMove(direction, landPoint);
		moveList.add(nextMove);
		currentLoc =  landPoint;
//		currentLoc =  moveList.get(moveList.size()).getLandPoint();
	}
	
	public Feature getPath() {
		List<Point> points = new ArrayList<Point>();
		points.add(startPoint);
		for (DroneMove m : moveList) {
			points.add(m.getLandPoint());
		}
		Feature dronePath = Feature.fromGeometry(LineString.fromLngLats(points));
		return dronePath;
	}
	
	private void terminateMoves() {
		terminated = true;
	}
	
	public boolean getTerminated() {
		return terminated;
	}

}
