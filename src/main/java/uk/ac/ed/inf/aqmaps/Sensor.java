package uk.ac.ed.inf.aqmaps;

public class Sensor {
	
	private String location;
	private double battery;
	private String reading;
	
	public Sensor(String location, double battery, String reading) {
		super();
		this.location = location;
		this.battery = battery;
		this.reading = reading;
	}
	
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public double getBattery() {
		return battery;
	}

	public void setBattery(double battery) {
		this.battery = battery;
	}

	public String getReading() {
		return reading;
	}

	public void setReading(String reading) {
		this.reading = reading;
	}
	
}
