package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

public class DroneMove {
	
	private int direction;
	private Point landPoint;
	
	public DroneMove(int direction, Point landPoint) {
		this.direction = direction;
		this.landPoint = landPoint;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public Point getLandPoint() {
		return landPoint;
	}

	public void setLandPoint(Point landPoint) {
		this.landPoint = landPoint;
	}

}
