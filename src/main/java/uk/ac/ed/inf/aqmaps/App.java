package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

public class App {

	private static List<Feature> sensors = new ArrayList<Feature>();
	private static List<Feature> buildings = new ArrayList<Feature>();
	private static List<Feature> allFeatures = new ArrayList<Feature>();

	public static void main(String[] args) {

		// Arg 0: day
		// Arg 1: month
		// Arg 2: year
		// Arg 3: starting point lng
		// Arg 4: starting point lat
		// Arg 5: seed
		// Arg 6: port

		// Initialise the map - see Map class
		// Map map = new Map(arg[0], arg[1], arg[2], arg[6]);
		Map map = new Map("15", "06", "2021", "80");

		// Get the sesnor and building features from the map
		sensors = map.getFeaturedSensors();
		buildings = map.getNoFlyZones();

		// Initialise the drone - see Drone class
		// Drone drone = new Drone(arg[3], arg[4], sensors, buildings);
		Drone drone = new Drone(-3.1878, 55.9444, sensors, buildings);

		// Begin the drone algorithm - see Drone class
		drone.setTargetSensor();
		while (drone.getTerminated() == false) {
			drone.nextMove();
		}

		// Drone algorithm has terminated
		// Get the features to write geojson output

		allFeatures.add(drone.getPath());

		for (Feature f : drone.getVisitedSensors()) {
			double battery = map.getSensorBattery(f.getStringProperty("location"));
			String reading = map.getSensorReading(f.getStringProperty("location"));

			if (battery >= 10) {
				f.addStringProperty("rgb-string", getColor(Double.parseDouble(reading)));
				f.addStringProperty("marker-color", getColor(Double.parseDouble(reading)));
				if (Double.parseDouble(reading) <= 128) {
					f.addStringProperty("marker-symbol", "lighthouse");
				} else {
					f.addStringProperty("marker-symbol", "danger");
				}
			} else {
				f.addStringProperty("rgb-string", "#000000");
				f.addStringProperty("marker-color", "#000000");
				f.addStringProperty("marker-symbol", "cross");
			}
			allFeatures.add(f);
		}
		for (Feature f : drone.getUnvisitedSensors()) {
			allFeatures.add(f);
		}
		System.out.println(allFeatures.size());
		renderGeojson();
	}

	// Check color classification as per specification
	public static String getColor(double reading) {
		if (reading >= 0 && reading < 32) {
			return "#00ff00";
		} else if (reading >= 32 && reading < 64) {
			return "#40ff00";
		} else if (reading >= 64 && reading < 96) {
			return "#80ff00";
		} else if (reading >= 96 && reading < 128) {
			return "#c0ff00";
		} else if (reading >= 128 && reading < 160) {
			return "#ffc000";
		} else if (reading >= 160 && reading < 192) {
			return "#ff8000";
		} else if (reading >= 192 && reading < 224) {
			return "#ff4000";
		} else if (reading >= 224 && reading < 256) {
			return "#ff0000";
		}
		return null;
	}

	// Render features
	private static void renderGeojson() {
		// Create feature collection from list of features
		FeatureCollection fc = FeatureCollection.fromFeatures(allFeatures);
		// Convert to json string
		String geojson = fc.toJson();

		System.out.println(geojson);
	}

}
