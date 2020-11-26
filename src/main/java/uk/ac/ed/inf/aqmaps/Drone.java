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
	
	private List<Feature> unvisitedSensors = new ArrayList<Feature>();
	private Feature targetSensor = null;
	
	public Drone(double startLng, double startLat, List<Feature> sensors) {
		this.startPoint = Point.fromLngLat(startLng, startLat);
		this.currentLoc = startPoint;
		this.terminated = false;
		this.unvisitedSensors = sensors;
	}
	
	public void nextMove() {
		
		if (moveList.size() < maxMoves) {
			
			if (targetSensor == null) {
				targetSensor = closestSensor(unvisitedSensors);	
			}
			
			// find move which takes you closest + add it to list
			// take reading if in range + (targetSensor = null)
			
		} else {
			terminated = true;
		}
		
	}

	private Feature closestSensor(List<Feature> sensors) {
		Feature closest = sensors.get(0);
		double minDistance = calcDistance((Point)closest.geometry(), currentLoc);
		for (Feature s : sensors) {
			Point sensorPoint = (Point)s.geometry();
			double distance = calcDistance(sensorPoint, currentLoc);
			if (distance < minDistance) {
				closest = s;
			}
		}
		return closest;
	}
	
	private void closestMove() {
		// got currentLoc
		// got targetSensor
		// 36 possible (but not all valid) moves
		
//		int direction = 0;
//		Point destination = calcLandPoint(direction);
//		double distanceToTarget = calcDistance(destination, (Point)targetSensor.geometry());
//		DroneMove closerMove = new DroneMove(direction, destination);
		
//		moveList.add(e);
		for (int i = 0; i < 36; i++) {
			// check if one is close - not added to list
			
//			direction = i*10;
//			destination = calcLandPoint(direction);
//			double newDistanceToTarget = calcDistance(destination, (Point)targetSensor.geometry());
//			if (newDistanceToTarget < distanceToTarget) {
//				
//			}
			
//			DroneMove possibleMove = new DroneMove(direction, destination); - not needed yet
			// then check if its valid - added to list
		}
		// by end have a move added to list
	}
	
	private boolean isValidMove(Point p) {
		// point in boundary
		boolean valid = true;
		if (p.longitude() < Map.x1) {
			
		}
		// point not in building - redundant by point 3?
		// flightpath not cross building - hardest?
		return valid;
	}
	
	private Point calcLandPoint(int direction) {
		double inRadians = Math.toRadians(direction);
		double lng = (0.0003 * Math.cos(inRadians));
		double lat = (0.0003 * Math.sin(inRadians));
		Point landing = Point.fromLngLat(lng, lat);
		return landing;
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
	
	private void updateDroneMoves(int direction, Point landPoint) {
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
