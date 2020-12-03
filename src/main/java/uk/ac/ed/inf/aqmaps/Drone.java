package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class Drone {

	private static double x1 = -3.192473;
	private static double x2 = -3.184319;
	private static double y1 = 55.946233;
	private static double y2 = 55.942617;

	private Point startPoint;
	private Point currentLoc;

	private boolean obstacle;
	private int directionToTarget;
	private boolean avoidable;
	private int avoidanceDirection;

	private static int maxMoves = 150;

	private boolean terminated;

	private ArrayList<DroneMove> moveList = new ArrayList<DroneMove>();

	private List<Feature> unvisitedSensors = new ArrayList<Feature>();
	private List<Feature> visitedSensors = new ArrayList<Feature>();
	private Feature targetSensor;
	private List<Feature> noFlyZones = new ArrayList<Feature>();

	public Drone(double startLng, double startLat, List<Feature> sensors, List<Feature> buildings) {
		this.startPoint = Point.fromLngLat(startLng, startLat);
		this.currentLoc = startPoint;
		// System.out.println("currentLoc: " + currentLoc.latitude() + ", " +
		// currentLoc.longitude());
		this.terminated = false;
		this.unvisitedSensors = sensors;
		this.noFlyZones = buildings;

		this.obstacle = false;
		this.avoidable = false;
	}

	public void nextMove() {

		// System.out.println(currentLoc.longitude() + ", " + currentLoc.latitude());

		// System.out.println("Move function called!");
		// System.out.println("Number of sensors to visit:" + unvisitedSensors.size());

		if (moveList.size() < maxMoves) {
			// find move which takes you closest + add it to list
			List<DroneMove> validNextMoves = new ArrayList<DroneMove>();
			validNextMoves = getValidMoves();
			closestMove(validNextMoves);

			// take reading if in range + (targetSensor = null)
			if (terminated == false) {
				double targetDistance = calcDistance(moveList.get(moveList.size() - 1).getLandPoint(),
						(Point) targetSensor.geometry());
				if (targetDistance < 0.0002) {
					// System.out.println("Reached sesnor " +
					// targetSensor.getStringProperty("location") + " on move "
					// + moveList.size());
					if (targetSensor.getStringProperty("location") != "home") {
						unvisitedSensors.remove(unvisitedSensors.indexOf(targetSensor));
						visitedSensors.add(targetSensor);
					} else {
						terminated = true;
					}
					// System.out.println("target next sensor? " + !unvisitedSensors.isEmpty());
					if (unvisitedSensors.isEmpty() == false) {
						setTargetSensor();
					} else {
						// now return to starting point
						Feature home = Feature.fromGeometry(startPoint);
						home.addStringProperty("location", "home");
						targetSensor = home;
						directionToTarget = getDirectionToTarget();
						// terminated = true;
					}
				}
			}
		} else {
			terminated = true;
		}

	}

	private Feature closestSensor(List<Feature> sensors) {
		Feature closest = sensors.get(0);
		double minDistance = calcDistance((Point) closest.geometry(), currentLoc);
		for (Feature s : sensors) {
			Point sensorPoint = (Point) s.geometry();
			double distance = calcDistance(sensorPoint, currentLoc);
			// System.out.println(distance);
			// System.out.println(s.getStringProperty("location"));
			if (distance < minDistance) {
				// System.out.println(s.getStringProperty("location"));
				// System.out.println(distance);
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

			direction = i * 10;
			destination = calcLandPoint(direction);
			DroneMove possibleMove = new DroneMove(direction, destination);

			if (isValidMove(possibleMove)) {
				validMovies.add(possibleMove);
			}
		}
		// System.out.println("Found " + validMovies.size() + " valid moves!");

		if (validMovies.size() == 0) {
			terminated = true;
		}

		return validMovies;
	}

	private void closestMove(List<DroneMove> validMoves) {

		if (validMoves.isEmpty() == false) {
			if (obstacle == false) {
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
				// System.out.println("Move made in direction " + closerMove.getDirection());
			} else {
				ArrayList<DroneMove> edgeMoves = new ArrayList<DroneMove>();
				// System.out.println("When obsticle == true");
				for (int i = 0; i < validMoves.size(); i++) {
					int directionI = validMoves.get(i).getDirection();
					int directionI1 = validMoves.get((i + validMoves.size() - 1) % validMoves.size()).getDirection();
					if ((directionI % 360) != ((directionI1 + 10) % 360)) {
						// System.out.println("made avoidence move");
						// System.out.println(directionI);
						// System.out.println(directionI1);
						edgeMoves.add(validMoves.get((i + validMoves.size() - 1) % validMoves.size()));
						edgeMoves.add(validMoves.get(i));
						// System.out.println("Edge options: " + validMoves.get(i).getDirection() + " vs
						// " + directionToTarget);
					}
				}
				// System.out.println("Hello?????");
				DroneMove closestMove;
				if (avoidable == false) {
					closestMove = findAvoidenceMove(directionToTarget, edgeMoves);

					avoidable = true;
					avoidanceDirection = closestMove.getDirection();
				} else {
					closestMove = findAvoidenceMove(avoidanceDirection, edgeMoves);
				}
				moveList.add(closestMove);
				currentLoc = closestMove.getLandPoint();
			}
		}
	}

	private DroneMove findAvoidenceMove(int direction, ArrayList<DroneMove> edgeMoves) {
		int smallestDifference = gapBetweenAngles(direction, edgeMoves.get(0).getDirection());
		DroneMove closestMove = edgeMoves.get(0);
		for (DroneMove move : edgeMoves) {
			int difference = gapBetweenAngles(direction, move.getDirection());
			// System.out.println("difference of " + difference + " at " +
			// move.getDirection());
			if (difference < smallestDifference) {
				smallestDifference = difference;
				closestMove = move;
			}
		}
		return closestMove;
	}

	private boolean isValidMove(DroneMove move) {
		// point in boundary
		boolean valid = true;
		// System.out.println(p.longitude() + " > " + x1);
		if (move.getLandPoint().longitude() <= x1 || move.getLandPoint().longitude() >= x2) {
			valid = false;
		}
		if (move.getLandPoint().latitude() >= y1 || move.getLandPoint().latitude() <= y2) {
			valid = false;
		}
		if (lineCrossPoly(move.getLandPoint()) == true) {
			// System.out.println("Avoiding building at " + move.getDirection());
			valid = false;
			if (move.getDirection() == directionToTarget && obstacle == false) {
				obstacle = true;
				directionToTarget = getDirectionToTarget();
				// System.out.println(" - started obsticle avoidence");
			}
		}
		if (move.getDirection() == directionToTarget && valid == true && obstacle == true) {
			obstacle = false;
			avoidable = false;
			// System.out.println("ended obsticle avoidence");
		}
		if (twiceVisited(move.getLandPoint())) {
			valid = false;
		}
		return valid;
	}

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
		// System.out.println("calcLandPoint");
		// System.out.println("currentLoc: " + currentLoc.latitude() + ", " +
		// currentLoc.longitude());
		double inRadians = Math.toRadians(direction);
		double lng = (0.0003 * Math.cos(inRadians));
		double lat = (0.0003 * Math.sin(inRadians));
		lat += currentLoc.latitude();
		lng += currentLoc.longitude();
		Point landing = Point.fromLngLat(lng, lat);
		// System.out.println("currentLoc: " + currentLoc.longitude() + ", " +
		// currentLoc.latitude() + " -> landPoint: " + landing.longitude() + ", " +
		// landing.longitude());
		return landing;
	}

	// distance between two points
	private double calcDistance(Point p1, Point p2) {
		double distance = Math
				.sqrt((Math.pow((p1.latitude() - p2.latitude()), 2) + Math.pow((p1.longitude() - p2.longitude()), 2)));
		return distance;
	}

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
			// System.out.println("Checking " + f.getStringProperty("name"));
			Polygon poly = (Polygon) f.geometry();
			List<Point> buildingCorners = new ArrayList<Point>();
			buildingCorners = poly.coordinates().get(0);
			// System.out.println(f.getStringProperty("name") + " " +
			// buildingCorners.size());
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

	public Feature getPath() {
		List<Point> points = new ArrayList<Point>();
		points.add(startPoint);
		// System.out.println("Getting linestring");
		// System.out.println(moveList.isEmpty());
		for (DroneMove m : moveList) {
			points.add(m.getLandPoint());
			// System.out.println("Added point to drone path");
		}
		// System.out.println("Added " + moveList.size() + " points to drone path");
		Feature dronePath = Feature.fromGeometry(LineString.fromLngLats(points));
		return dronePath;
	}

	public void setTargetSensor() {
		targetSensor = closestSensor(unvisitedSensors);
		directionToTarget = getDirectionToTarget();
		// System.out.println("directionToTarget set to " + directionToTarget);
		// System.out.println("Targeting sensor " +
		// targetSensor.getStringProperty("location") + " which is "
		// + calcDistance(currentLoc, (Point) targetSensor.geometry()));
	}

	public List<Feature> getVisitedSensors() {
		return visitedSensors;
	}

	public boolean getTerminated() {
		return terminated;
	}

	public int getNumMoves() {
		return moveList.size();
	}

}
