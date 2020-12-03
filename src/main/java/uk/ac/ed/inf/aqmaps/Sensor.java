package uk.ac.ed.inf.aqmaps;

public class Sensor {

	// Maps to data in air-quality-data.json
	private String location;
	private double battery;
	private String reading;

	// Constructor
	public Sensor(String location, double battery, String reading) {
		this.location = location;
		this.battery = battery;
		this.reading = reading;
	}

	// Public getters

	public String getLocation() {
		return location;
	}

	public double getBattery() {
		return battery;
	}

	public String getReading() {
		return reading;
	}

}
