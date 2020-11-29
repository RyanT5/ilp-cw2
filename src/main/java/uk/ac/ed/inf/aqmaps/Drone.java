package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.turf.TurfJoins;
import com.mapbox.turf.models.LineIntersectsResult;

public class Drone {
	
	private static double x1 = -3.192473;
	private static double x2 = -3.184319;
	private static double y1 = 55.946233;
	private static double y2 = 55.942617;
	
	private Point startPoint;
	private Point currentLoc;
	
	private static int maxMoves = 150;
	
	private boolean terminated;

	private ArrayList<DroneMove> moveList = new ArrayList<DroneMove>();
	
	private List<Feature> unvisitedSensors = new ArrayList<Feature>();
	private List<Feature> visitedSensors = new ArrayList<Feature>();
	private Feature targetSensor;
	private List<Feature> noFlyZones = new ArrayList<Feature>();
	
	public Drone(double startLat, double startLng, List<Feature> sensors, List<Feature> buildings) {
		this.startPoint = Point.fromLngLat(startLat, startLng);
		this.currentLoc = startPoint;
//		System.out.println("currentLoc: " + currentLoc.latitude() + ", " + currentLoc.longitude());
		this.terminated = false;
		this.unvisitedSensors = sensors;
		this.noFlyZones = buildings;
	}
	
	public void nextMove() {
		
//		System.out.println(currentLoc.latitude());
		
		// System.out.println("Move function called!");
//		System.out.println("Number of sensors to visit:" + unvisitedSensors.size());
		
		if (moveList.size() < maxMoves) {
			
			// find move which takes you closest + add it to list
			List<DroneMove> validNextMoves = new ArrayList<DroneMove>();
			validNextMoves = getValidMoves();
			closestMove(validNextMoves);
			
					
			// take reading if in range + (targetSensor = null)
			if (terminated == false) {
				double targetDistance = calcDistance(moveList.get(moveList.size()-1).getLandPoint(), (Point)targetSensor.geometry());
				if (targetDistance < 0.0002) {
					// read sensor
//					System.out.println("Reached sesnor " + targetSensor.getStringProperty("location") + " on move " + moveList.size());
//					unvisitedSensors.remove(unvisitedSensors.indexOf(targetSensor));
					
//					feature.addStringProperty("rgb-string", "#00ff00");
//		        	feature.addStringProperty("marker-color", "#00ff00");
//					double battery = 
					
//					targetSensor.addStringProperty("marker-symbol", "lighthouse");
					unvisitedSensors.remove(unvisitedSensors.indexOf(targetSensor));
					visitedSensors.add(targetSensor);
					if (unvisitedSensors.isEmpty() == false) {
						setTargetSensor();
					} else {
						terminated = true;
					}
				}
			}
			
		} else {
			// now return to starting point
			terminated = true;
		}
		
	}

	private Feature closestSensor(List<Feature> sensors) {
		Feature closest = sensors.get(0);
		double minDistance = calcDistance((Point)closest.geometry(), currentLoc);
		for (Feature s : sensors) {
			Point sensorPoint = (Point)s.geometry();
			double distance = calcDistance(sensorPoint, currentLoc);
//			System.out.println(distance);
//			System.out.println(s.getStringProperty("location"));
			if (distance < minDistance) {
//				System.out.println(s.getStringProperty("location"));
//				System.out.println(distance);
				closest = s;
				minDistance = distance;
			}
		}
		return closest;
	}
	
	private List<DroneMove> getValidMoves() {
		
		int direction;
		Point destination;
		List<DroneMove> validMovies = new ArrayList<DroneMove>();
		
		for (int i = 0; i < 36; i++) {
			
			direction = i*10;
			destination = calcLandPoint(direction);
//			System.out.println(destination.latitude());
//			System.out.println("Looking for valid moves...");
			
			if (isValidMove(destination)) {
				DroneMove possibleMove = new DroneMove(direction, destination);
				validMovies.add(possibleMove);
			}
		}
//		System.out.println("Found " + validMovies.size() + " valid moves!");
		
		if (validMovies.size() == 0) {
			terminated = true;
		}
		
		return validMovies;
	}
	
	private void closestMove(List<DroneMove> validMoves) {
		
		if (validMoves.isEmpty() == false) {
		DroneMove closerMove = validMoves.get(0);
		double lowestDistanceToTarget = calcDistance(closerMove.getLandPoint(), (Point)targetSensor.geometry());
		double distanceToTarget;
//		Point destination;
		for (DroneMove d : validMoves) {
//			destination = d.getLandPoint()
//			System.out.println(d.getLandPoint().latitude());
			distanceToTarget = calcDistance(d.getLandPoint(), (Point)targetSensor.geometry());
			
			if (distanceToTarget < lowestDistanceToTarget) {
				closerMove = d;
				lowestDistanceToTarget = distanceToTarget;
			}
		}

//		System.out.println(closerMove.getLandPoint().latitude());
			moveList.add(closerMove);
			currentLoc = closerMove.getLandPoint();
		}
	}
	
	private boolean isValidMove(Point p) {
		// point in boundary
		boolean valid = true;
//		System.out.println(p.longitude() + " > " + x1);
		if (p.longitude() <= x1 || p.longitude() >= x2) {
			valid = false;
		}
		if (p.latitude() >= y1 || p.latitude() <= y2) {
			valid = false;
		}
		// point not in building - redundant by point 3?
//		for (Feature f : noFlyZones) {
//			if (pointInPoly(p, (Polygon)f.geometry())) {
//				valid = false;
//			}
//		}
		if (lineCrossPoly(p) == true) {
//			System.out.println("Avoiding building");
			valid = false;
		}
		
		if (twiceVisited(p)) {
			valid = false;
		}
		
		return valid;
	}
	
	private boolean twiceVisited(Point p) {
		int counter = 0;
		for (DroneMove move : moveList) {
			if (p.longitude() == move.getLandPoint().longitude() && p.latitude() == move.getLandPoint().latitude()) {
				counter++;
			}
		}
		if (counter == 2) {
			return true;
		} else {
			return false;
		}
	}
	
	private Point calcLandPoint(int direction) {
//		System.out.println("calcLandPoint");
//		System.out.println("currentLoc: " + currentLoc.latitude() + ", " + currentLoc.longitude());
		double inRadians = Math.toRadians(direction);
		double lng = (0.0003 * Math.cos(inRadians));
		double lat = (0.0003 * Math.sin(inRadians));
		lat += currentLoc.latitude();
		lng += currentLoc.longitude();
		Point landing = Point.fromLngLat(lng, lat);
//		System.out.println("currentLoc: " + currentLoc.longitude() + ", " + currentLoc.latitude() + " -> landPoint: " + landing.longitude() + ", " + landing.longitude());
		return landing;
	}
	
	// distance between two points
    
    private double calcDistance(Point p1, Point p2) {
    	
    	double distance = Math.sqrt((Math.pow((p1.latitude() - p2.latitude()), 2) + Math.pow((p1.longitude() - p2.longitude()), 2)));
    	
		return distance;
    }
    
    // collision between point and polygon - is this redundant?
    
    private boolean pointInPoly(Point point, Polygon poly) {
    	
    	boolean intersect = TurfJoins.inside(point, poly);
    	
    	return intersect;
    }
    
    private boolean lineCrossPoly(Point proposedMove) {
    	boolean cross = false;
    	Point lastMove;
    	if (moveList.size() > 0) {
    		lastMove = moveList.get(moveList.size()-1).getLandPoint();
    	} else {
    		lastMove = startPoint;
    	}
    	Line2D pathLine = new Line2D.Double(lastMove.longitude(), lastMove.latitude(), proposedMove.longitude(), proposedMove.latitude());
    	for (Feature f : noFlyZones) {
//    		System.out.println("Checking " + f.getStringProperty("name"));
    		Polygon poly = (Polygon)f.geometry();
    		List<Point> buildingCorners = new ArrayList<Point>();
    		buildingCorners = poly.coordinates().get(0);
//    		System.out.println(f.getStringProperty("name") + " " + buildingCorners.size());
    		for (int i = 0; i < buildingCorners.size()-1; i++) {
    				Point corner1 = buildingCorners.get(i);
    				Point corner2 = buildingCorners.get(i+1);
        			boolean intersect = pathLine.intersectsLine(corner1.longitude(), corner1.latitude(), corner2.longitude(), corner2.latitude());
        			if (intersect == true) {
        				cross = true;
        			}
    		}
    	}
    	return cross;
    }
	
	public Feature getPath() {
		List<Point> points = new ArrayList<Point>();
		points.add(startPoint);
		// System.out.println("Getting linestring");
//		System.out.println(moveList.isEmpty());
		for (DroneMove m : moveList) {
			points.add(m.getLandPoint());
//			System.out.println("Added point to drone path");
		}
		System.out.println("Added " + moveList.size() + " points to drone path");
		Feature dronePath = Feature.fromGeometry(LineString.fromLngLats(points));
		return dronePath;
	}
	
	public void setTargetSensor() {
		targetSensor = closestSensor(unvisitedSensors);
//		System.out.println();
//		System.out.println("Targeting sensor " + targetSensor.getStringProperty("location"));
	}
	
	public List<Feature> getVisitedSensors() {
		return visitedSensors;
	}
	
	// private void terminateMoves() {
	// 	terminated = true;
	// }
	
	public boolean getTerminated() {
		return terminated;
	}

}
