package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

public class DroneMove {

	// Moves have a direction and point where they land
	private int direction;
	private Point landPoint;

	private String sensorRead;

	// Constructor
	public DroneMove(int direction, Point landPoint) {
		this.direction = direction;
		this.landPoint = landPoint;
	}
	
	// Note sensor read on this move
	public void setSensorRead(String sensorRead) {
		this.sensorRead = sensorRead;
	}

	// Public getters

	public String getSensorRead() {
		return sensorRead;
	}

	public int getDirection() {
		return direction;
	}

	public Point getLandPoint() {
		return landPoint;
	}

}
