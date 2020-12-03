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

	private static String day;
	private static String month;
	private static String year;
	private static double startLng;
	private static double startLat;
	private static String port;

	public static void main(String[] args) {

		// day = args[0];
		// month = args[1];
		// year = args[2];
		// startLng = Double.parseDouble(args[3]);
		// startLat = Double.parseDouble(args[4]);
		startLng = -3.1878;
		startLat = 55.9444;
		// args[5] is a seed for controlled randomness
		// port = args[6];

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

		// System.out.println(allFeatures.size());
		writeGeojsonFile(renderGeojson());

		String moveListString = moveListToString(drone.getMoveList());
		writeMoveFile(moveListString);

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
		String geojson = fc.toJson();

		return geojson;
	}

	private static void writeGeojsonFile(String content) {
		// String dateString = day + "-" + month + "-" + year;
		String dateString = "15" + "-" + "06" + "-" + "2021";
		String fileName = "readings-" + dateString + ".geojson";
		try {
			FileWriter writer = new FileWriter(fileName);
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String moveListToString(ArrayList<DroneMove> moveList) {
		String moveListString = "";
		int counter = 1;
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
			moveListString += moveList.get(i).getSensorRead();
			if (counter != moveList.size()) {
				moveListString += "\n";
			}
			counter++;
		}
		return moveListString;
	}

	private static void writeMoveFile(String content) {
		// String dateString = day + "-" + month + "-" + year;
		String dateString = "15" + "-" + "06" + "-" + "2021";
		String fileName = "flightpath-" + dateString + ".txt";
		try {
			FileWriter writer = new FileWriter(fileName);
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
