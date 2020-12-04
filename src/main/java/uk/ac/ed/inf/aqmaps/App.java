package uk.ac.ed.inf.aqmaps;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

public class App {

	private static List<Feature> sensors = new ArrayList<Feature>();
	private static List<Feature> buildings = new ArrayList<Feature>();
	private static List<Feature> allFeatures = new ArrayList<Feature>();

	public static void main(String[] args) {

		var day = args[0];
		var month = args[1];
		var year = args[2];
		var startLat = Double.parseDouble(args[3]);
		var startLng = Double.parseDouble(args[4]);
		// args[5] is a seed for controlled randomness
		var port = args[6];

		// Initialise the map - see Map class
		var map = new Map(day, month, year, port);

		// Get the sensor and building features from the map
		sensors = map.getFeaturedSensors();
		buildings = map.getNoFlyZones();

		// Initialise the drone - see Drone class
		var drone = new Drone(startLng, startLat, sensors, buildings);

		// Begin the drone algorithm - see Drone class
		drone.setTargetSensor();
		while (drone.getTerminated() == false) {
			drone.nextMove();
		}

		// Drone algorithm has terminated
		// Get the features to write geojson output
		allFeatures.add(drone.getPath());

		setGeojsonProperties(map, drone);

		writeGeojsonFile(renderGeojson(), day, month, year);

		var moveListString = moveListToString(drone.getMoveList(), startLng, startLat);
		writeMoveFile(moveListString, day, month, year);
	}

	// Set string properties to display in geojson
	private static void setGeojsonProperties(Map map, Drone drone) {
		for (Feature f : drone.getVisitedSensors()) {
			var battery = map.getSensorBattery(f.getStringProperty("location"));
			var reading = map.getSensorReading(f.getStringProperty("location"));

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
	}

	// Check color classification as per specification
	private static String getColor(double reading) {
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
	private static String renderGeojson() {
		// Create feature collection from list of features
		FeatureCollection fc = FeatureCollection.fromFeatures(allFeatures);
		// Convert to json string
		var geojson = fc.toJson();

		return geojson;
	}

	// Output geojson readings file
	private static void writeGeojsonFile(String content, String day, String month, String year) {
		var dateString = day + "-" + month + "-" + year;
		var fileName = "readings-" + dateString + ".geojson";
		try {
			FileWriter writer = new FileWriter(fileName);
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Generate flightpath from moveList
	private static String moveListToString(ArrayList<DroneMove> moveList, double startLng, double startLat) {
		var moveListString = "";
		var counter = 1;
		for (int i = 0; i < moveList.size(); i++) {
			moveListString += Integer.toString(counter) + ",";
			if (counter == 1) {
				moveListString += Double.toString(startLng) + ",";
				moveListString += Double.toString(startLat) + ",";
			} else {
				moveListString += Double.toString(moveList.get(i - 1).getLandPoint().longitude()) + ",";
				moveListString += Double.toString(moveList.get(i - 1).getLandPoint().latitude()) + ",";
			}
			moveListString += Integer.toString(moveList.get(i).getDirection()) + ",";
			moveListString += Double.toString(moveList.get(i).getLandPoint().longitude()) + ",";
			moveListString += Double.toString(moveList.get(i).getLandPoint().latitude()) + ",";
			moveListString += moveList.get(i).getSensorRead() + "\n";
			counter++;
		}
		return moveListString;
	}

	// Output txt flightpath file
	private static void writeMoveFile(String content, String day, String month, String year) {
		var dateString = day + "-" + month + "-" + year;
		var fileName = "flightpath-" + dateString + ".txt";
		try {
			FileWriter writer = new FileWriter(fileName);
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
