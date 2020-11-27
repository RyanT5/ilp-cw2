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
	private List<Feature> noFlyZones = new ArrayList<Feature>();
	
	public Drone(double startLng, double startLat, List<Feature> sensors, List<Feature> buildings) {
		this.startPoint = Point.fromLngLat(startLng, startLat);
		this.currentLoc = startPoint;
		this.terminated = false;
		this.unvisitedSensors = sensors;
		this.noFlyZones = buildings;
	}
	
	public void nextMove() {
		
		System.out.println("Move function called!");
		System.out.println("Number of sensors to visit:" + unvisitedSensors.size());
		
		if (moveList.size() < maxMoves && unvisitedSensors.isEmpty() == false) {
			
			if (targetSensor == null) {
				targetSensor = closestSensor(unvisitedSensors);	
			}
			
			// find move which takes you closest + add it to list
			closestMove();
			
					
			// take reading if in range + (targetSensor = null)
			if (terminated == false) {
				double targetDistance = calcDistance(moveList.get(moveList.size()).getLandPoint(), (Point)targetSensor.geometry());
				if (targetDistance < 0.0003) {
					// read sensor
					System.out.println("Reached a sesnor!");
					targetSensor = null;
				}
			}
			
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
		unvisitedSensors.remove(unvisitedSensors.indexOf(closest));
		return closest;
	}
	
	private void closestMove() {
		// got currentLoc
		// got targetSensor
		// 36 possible (but not all valid) moves
		
		int direction;// = 0;
		Point destination;// = calcLandPoint(direction);
		double distanceToTarget;// = calcDistance(destination, (Point)targetSensor.geometry());
		DroneMove closerMove = null;// = new DroneMove(direction, destination);
		DroneMove possibleMove = null;
		double lowestDistanceToTarget = 0;
		
//		moveList.add(e);
		for (int i = 0; i < 36; i++) {
			
			// create move
			direction = i*10;
			destination = calcLandPoint(direction);
			distanceToTarget = calcDistance(destination, (Point)targetSensor.geometry());
//			System.out.println(possibleMove == null);
			possibleMove = new DroneMove(direction, destination);
//			System.out.println(possibleMove == null);
			
			if (isValidMove(destination)) {
				
				if (closerMove == null) {
					
					closerMove = possibleMove;
					lowestDistanceToTarget = distanceToTarget;
					System.out.println(closerMove == null);
					
				} else {
					
					if (distanceToTarget < lowestDistanceToTarget) {
						closerMove = possibleMove;
						lowestDistanceToTarget = distanceToTarget;
					}
				}
			}
		}
		// by end have a move added to list
		if (closerMove != null) {
//		updateDroneMoves(closerMove.getDirection(), closerMove.getLandPoint());
			moveList.add(closerMove);
			currentLoc =  closerMove.getLandPoint();
		} else {
			terminateMoves();
			System.out.println("Didn't fine a move");
		}
	}
	
	private boolean isValidMove(Point p) {
		// point in boundary
		boolean valid = true;
		if (p.longitude() < Map.x1 || p.longitude() > Map.x2) {
			valid = false;
		}
		if (p.latitude() < Map.y1 || p.latitude() > Map.y2) {
			valid = false;
		}
		// point not in building - redundant by point 3?
		for (Feature f : noFlyZones)
		if (pointInPoly(p, (Polygon)f.geometry())) {
			valid = false;
		}
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
		System.out.println("Getting linestring");
		System.out.println(moveList.isEmpty());
		for (DroneMove m : moveList) {
			points.add(m.getLandPoint());
			System.out.println("Added point to drone path");
		}
		Feature dronePath = Feature.fromGeometry(LineString.fromLngLats(points));
		return dronePath;
	}
	
	private void terminateMoves() {
		terminated = true; ////////////////////////// ???????
	}
	
	public boolean getTerminated() {
		return terminated;
	}

}
