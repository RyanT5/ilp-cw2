package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

public class DroneMove {

	// Moves have a direction and point where they land
	private int direction;
	private Point landPoint;

	// Constructor
	public DroneMove(int direction, Point landPoint) {
		this.direction = direction;
		this.landPoint = landPoint;
	}

	// Public getters

	public int getDirection() {
		return direction;
	}

	public Point getLandPoint() {
		return landPoint;
	}

}
