package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

public class DroneMove {
	
	private String direction;
	private Point landPoint;
	
	public DroneMove(String direction, Point landPoint) {
		this.direction = direction;
		this.landPoint = landPoint;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public Point getLandPoint() {
		return landPoint;
	}

	public void setLandPoint(Point landPoint) {
		this.landPoint = landPoint;
	}

}
