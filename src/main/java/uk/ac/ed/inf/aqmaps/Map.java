package uk.ac.ed.inf.aqmaps;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;

public class Map {

	private String day;
	private String month;
	private String year;
	private String port;

	private List<Feature> featuredSensors = new ArrayList<Feature>();

	private ArrayList<Sensor> sensorList = new ArrayList<Sensor>();

	private List<Feature> noFlyZones = new ArrayList<Feature>();

	// Constructor
	public Map(String day, String month, String year, String port) {
		this.day = day;
		this.month = month;
		this.year = year;
		this.port = port;

		// Get sensor data from server
		sensorList = getStations();
		// Get building data from server
		noFlyZones = getBuildings();

		// Get what3words data from server
		// Turn sesnor data into valid features
		for (Sensor s : sensorList) {
			WordsAddress wordsAddress = wordsToLoc(s.getLocation());
			Feature feature = Feature.fromGeometry(
					Point.fromLngLat(wordsAddress.getCoordinates().getLng(), wordsAddress.getCoordinates().getLat()));
			feature.addStringProperty("location", s.getLocation());
			feature.addStringProperty("rgb-string", "#aaaaaa");
			feature.addStringProperty("marker-color", "#aaaaaa");
			featuredSensors.add(feature);
		}
	}

	// Get sensors
	private ArrayList<Sensor> getStations() {
		String urlString = "http://localhost:" + port + "/maps/" + year + "/" + month + "/" + day
				+ "/air-quality-data.json";
		String sensorJson = Server.serverRequest(urlString);
		Type listType = new TypeToken<ArrayList<Sensor>>() {
		}.getType();
		ArrayList<Sensor> sensorList = new Gson().fromJson(sensorJson, listType);
		return sensorList;
	}

	// Get coordinates from words
	private WordsAddress wordsToLoc(String words) {
		String[] wordsList = words.split("\\.");
		String urlString = "http://localhost:" + port + "/words/" + wordsList[0] + "/" + wordsList[1] + "/"
				+ wordsList[2] + "/details.json";
		String wordsJson = Server.serverRequest(urlString);
		var details = new Gson().fromJson(wordsJson, WordsAddress.class);
		return details;
	}

	// Get noflyzones
	private List<Feature> getBuildings() {
		String urlString = "http://localhost:" + port + "/buildings/no-fly-zones.geojson";
		String buildingGeojson = Server.serverRequest(urlString);
		FeatureCollection buildingGroup = FeatureCollection.fromJson(buildingGeojson);
		List<Feature> buildings = new ArrayList<Feature>();
		buildings = buildingGroup.features();
		return buildings;
	}

	// Public getters

	public List<Feature> getFeaturedSensors() {
		return featuredSensors;
	}

	public List<Feature> getNoFlyZones() {
		return noFlyZones;
	}

	public double getSensorBattery(String sensorLoc) {
		double battery = 0;
		for (Sensor s : sensorList) {
			if (s.getLocation() == sensorLoc) {
				battery = s.getBattery();
			}
		}
		return battery;
	}

	public String getSensorReading(String sensorLoc) {
		String reading = "0";
		for (Sensor s : sensorList) {
			if (s.getLocation() == sensorLoc) {
				reading = s.getReading();
			}
		}
		return reading;
	}

}
