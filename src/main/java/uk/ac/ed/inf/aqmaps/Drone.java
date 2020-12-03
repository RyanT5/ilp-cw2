package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class Drone {

	// Set the fly boundry
	private static final double x1 = -3.192473;
	private static final double x2 = -3.184319;
	private static final double y1 = 55.946233;
	private static final double y2 = 55.942617;

	// Set the maximum number of drone moves
	private static final int maxMoves = 150;

	private Point startPoint;
	private Point currentLoc;

	private boolean obstacle;
	private int directionToTarget;
	private boolean avoidable;
	private int avoidanceDirection;

	private boolean terminated;

	private ArrayList<DroneMove> moveList = new ArrayList<DroneMove>();

	private List<Feature> unvisitedSensors = new ArrayList<Feature>();
	private List<Feature> visitedSensors = new ArrayList<Feature>();
	private Feature targetSensor;
	private List<Feature> noFlyZones = new ArrayList<Feature>();

	// Constructor
	public Drone(double startLng, double startLat, List<Feature> sensors, List<Feature> buildings) {
		this.startPoint = Point.fromLngLat(startLng, startLat);
		this.currentLoc = startPoint;

		this.terminated = false;
		this.unvisitedSensors = sensors;
		this.noFlyZones = buildings;

		this.obstacle = false;
		this.avoidable = false;
	}

	// Main function that controls the move
	public void nextMove() {
		if (moveList.size() < maxMoves) {
			// Find next move which takes you closest to target
			List<DroneMove> validNextMoves = new ArrayList<DroneMove>();
			validNextMoves = getValidMoves();
			closestMove(validNextMoves);

			// Check range to target
			if (terminated == false) {
				double targetDistance = calcDistance(moveList.get(moveList.size() - 1).getLandPoint(),
						(Point) targetSensor.geometry());
				if (targetDistance < 0.0002) {
					// Drone is in range of a target
					if (targetSensor.getStringProperty("location") != "home") {
						// Target is a sensor
						unvisitedSensors.remove(unvisitedSensors.indexOf(targetSensor));
						visitedSensors.add(targetSensor);
					} else {
						// Drone has returned to starting point
						terminated = true;
					}
					if (unvisitedSensors.isEmpty() == false) {
						setTargetSensor();
					} else {
						// All sensors have been read
						// Now return to starting point
						Feature home = Feature.fromGeometry(startPoint);
						home.addStringProperty("location", "home");
						targetSensor = home;
						directionToTarget = getDirectionToTarget();
					}
				}
			}
		} else {
			// Have reached maximum allowed moves
			terminated = true;
		}

	}

	// Get sensor closest to drone
	private Feature closestSensor(List<Feature> sensors) {
		Feature closest = sensors.get(0);
		double minDistance = calcDistance((Point) closest.geometry(), currentLoc);
		for (Feature s : sensors) {
			Point sensorPoint = (Point) s.geometry();
			double distance = calcDistance(sensorPoint, currentLoc);
			if (distance < minDistance) {
				closest = s;
				minDistance = distance;
			}
		}
		return closest;
	}

	// Get possible moves from drone location
	private List<DroneMove> getValidMoves() {
		// int direction;
		// Point destination;
		List<DroneMove> validMovies = new ArrayList<DroneMove>();

		for (int i = 0; i < 36; i++) {
			int direction = i * 10;
			Point destination = calcLandPoint(direction);
			DroneMove possibleMove = new DroneMove(direction, destination);

			if (isValidMove(possibleMove)) {
				validMovies.add(possibleMove);
			}
		}

		if (validMovies.size() == 0) {
			terminated = true;
		}

		return validMovies;
	}

	// Get valid move that takes drone closest to target
	private void closestMove(List<DroneMove> validMoves) {
		if (validMoves.isEmpty() == false) {
			if (obstacle == false) {
				// There is no obstacle in the drone's way
				DroneMove closerMove = validMoves.get(0);
				double lowestDistanceToTarget = calcDistance(closerMove.getLandPoint(),
						(Point) targetSensor.geometry());
				double distanceToTarget;

				for (DroneMove d : validMoves) {
					distanceToTarget = calcDistance(d.getLandPoint(), (Point) targetSensor.geometry());
					if (distanceToTarget < lowestDistanceToTarget) {
						closerMove = d;
						lowestDistanceToTarget = distanceToTarget;
					}
				}

				moveList.add(closerMove);
				currentLoc = closerMove.getLandPoint();
			} else {
				// The drone is avoiding an obstacle
				ArrayList<DroneMove> edgeMoves = new ArrayList<DroneMove>();
				// Get moves that would avoid the obstacle
				for (int i = 0; i < validMoves.size(); i++) {
					int directionI = validMoves.get(i).getDirection();
					int directionI1 = validMoves.get((i + validMoves.size() - 1) % validMoves.size()).getDirection();
					if ((directionI % 360) != ((directionI1 + 10) % 360)) {
						edgeMoves.add(validMoves.get((i + validMoves.size() - 1) % validMoves.size()));
						edgeMoves.add(validMoves.get(i));
					}
				}
				// Get the best of the avoiding moves
				DroneMove closestMove;
				if (avoidable == false) {
					// The drone has not picked an avoiding move direction
					closestMove = findAvoidenceMove(directionToTarget, edgeMoves);

					avoidable = true;
					avoidanceDirection = closestMove.getDirection();
				} else {
					// The drone has picked an avoiding move direction it should follow
					closestMove = findAvoidenceMove(avoidanceDirection, edgeMoves);
				}
				moveList.add(closestMove);
				currentLoc = closestMove.getLandPoint();
			}
		}
	}

	// Calculate next move based on avoidence move direction
	private DroneMove findAvoidenceMove(int direction, ArrayList<DroneMove> edgeMoves) {
		int smallestDifference = gapBetweenAngles(direction, edgeMoves.get(0).getDirection());
		DroneMove closestMove = edgeMoves.get(0);
		for (DroneMove move : edgeMoves) {
			int difference = gapBetweenAngles(direction, move.getDirection());
			if (difference < smallestDifference) {
				smallestDifference = difference;
				closestMove = move;
			}
		}
		return closestMove;
	}

	// Check a move is valid
	private boolean isValidMove(DroneMove move) {
		// Check move point is inside boundary
		boolean valid = true;
		if (move.getLandPoint().longitude() <= x1 || move.getLandPoint().longitude() >= x2) {
			valid = false;
		}
		if (move.getLandPoint().latitude() >= y1 || move.getLandPoint().latitude() <= y2) {
			valid = false;
		}
		// Check move does not cross building
		if (lineCrossPoly(move.getLandPoint()) == true) {
			valid = false;
			if (move.getDirection() == directionToTarget && obstacle == false) {
				obstacle = true;
				directionToTarget = getDirectionToTarget();
			}
		}
		if (move.getDirection() == directionToTarget && valid == true && obstacle == true) {
			obstacle = false;
			avoidable = false;
		}
		// Avoid drone falling into repeating loop
		if (twiceVisited(move.getLandPoint())) {
			valid = false;
		}
		return valid;
	}

	// Get direction from drone to target point
	private int getDirectionToTarget() {
		int testDirection = 0;
		int targetDirection = testDirection;
		Point destination = calcLandPoint(testDirection);
		double distanceToTarget = calcDistance(destination, (Point) targetSensor.geometry());
		double minDistance = distanceToTarget;
		for (int i = 0; i < 36; i++) {
			testDirection = i * 10;
			destination = calcLandPoint(testDirection);
			distanceToTarget = calcDistance(destination, (Point) targetSensor.geometry());
			if (distanceToTarget < minDistance) {
				minDistance = distanceToTarget;
				targetDirection = testDirection;
			}
		}
		return targetDirection;
	}

	// Determine if a point has been visited by drone twice
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

	// Calculate the point location a move would take the drone to
	private Point calcLandPoint(int direction) {
		double inRadians = Math.toRadians(direction);
		double lng = (0.0003 * Math.cos(inRadians));
		double lat = (0.0003 * Math.sin(inRadians));
		lat += currentLoc.latitude();
		lng += currentLoc.longitude();
		Point landing = Point.fromLngLat(lng, lat);
		return landing;
	}

	// Calculate the distance between two points
	private double calcDistance(Point p1, Point p2) {
		double distance = Math
				.sqrt((Math.pow((p1.latitude() - p2.latitude()), 2) + Math.pow((p1.longitude() - p2.longitude()), 2)));
		return distance;
	}

	// Determine if the drone path has crossed a building
	private boolean lineCrossPoly(Point proposedMove) {
		boolean cross = false;
		Point lastMove;
		if (moveList.size() > 0) {
			lastMove = moveList.get(moveList.size() - 1).getLandPoint();
		} else {
			lastMove = startPoint;
		}
		Line2D pathLine = new Line2D.Double(lastMove.longitude(), lastMove.latitude(), proposedMove.longitude(),
				proposedMove.latitude());
		for (Feature f : noFlyZones) {
			Polygon poly = (Polygon) f.geometry();
			List<Point> buildingCorners = new ArrayList<Point>();
			buildingCorners = poly.coordinates().get(0);
			for (int i = 0; i < buildingCorners.size() - 1; i++) {
				Point corner1 = buildingCorners.get(i);
				Point corner2 = buildingCorners.get(i + 1);
				boolean intersect = pathLine.intersectsLine(corner1.longitude(), corner1.latitude(),
						corner2.longitude(), corner2.latitude());
				if (intersect == true) {
					cross = true;
				}
			}
		}
		return cross;
	}

	// Calculate the smallest angle between two directions
	private int gapBetweenAngles(int a, int b) {
		int ans;
		if (a < b) {
			ans = (b - a);
		} else {
			ans = (a - b);
		}

		if (ans > 180) {
			return (360 - ans);
		} else {
			return ans;
		}
	}

	// Set the target to the next closest sensor
	public void setTargetSensor() {
		targetSensor = closestSensor(unvisitedSensors);
		directionToTarget = getDirectionToTarget();
	}

	// Public getters

	// Get the drone path
	public Feature getPath() {
		List<Point> points = new ArrayList<Point>();
		points.add(startPoint);
		for (DroneMove m : moveList) {
			points.add(m.getLandPoint());
		}
		Feature dronePath = Feature.fromGeometry(LineString.fromLngLats(points));
		return dronePath;
	}

	public List<Feature> getVisitedSensors() {
		return visitedSensors;
	}

	public List<Feature> getUnvisitedSensors() {
		return unvisitedSensors;
	}

	public boolean getTerminated() {
		return terminated;
	}

	public int getNumMoves() {
		return moveList.size();
	}

}
