package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.turf.TurfJoins;

public class App {

	private static List<Feature> sensors = new ArrayList<Feature>();
	private static List<Feature> buildings = new ArrayList<Feature>();
	private static List<Feature> allFeatures = new ArrayList<Feature>();

	public static void main(String[] args) {

//        Arg 0: day
//        Arg 1: month
//        Arg 2: year
//        Arg 3: starting point x
//        Arg 4: starting point y
//        Arg 5: seed
//        Arg 6: port

		for (int k = 2020; k < 2022; k++) {
			for (int j = 1; j < 13; j++) {
				for (int i = 1; i < 29; i++) {

//			    	Map map = new Map(arg[0], arg[1], arg[2], arg[6]);
//					Map map = new Map("15", "06", "2021", "80");
					
					String day = Integer.toString(i);
					String month = Integer.toString(j);
					String year = Integer.toString(k);
					
					if (day.length() == 1) {
						day = "0" + Integer.toString(i);
					}
					if (month.length() == 1) {
						month = "0" + Integer.toString(j);
					}
					
					Map map = new Map(day, month, year, "80");

					sensors = map.getFeaturedSensors();
					buildings = map.getNoFlyZones();

//			    	Drone drone = new Drone(arg[3], arg[4]);
					Drone drone = new Drone(-3.1878, 55.9444, sensors, buildings);

					//

					drone.setTargetSensor();
					while (drone.getTerminated() == false) {
						drone.nextMove();
					}
//			    	Feature dronePath = drone.getPath();
					//
					
					if (drone.getNumMoves() == 150) {
						System.out.println("Possible fail on " + i + j + k);
					}
					
				}
			}
		}



//    	allFeatures = map.getMapFeatures();
//		allFeatures = map.getMapFeatures();
//		allFeatures.add(drone.getPath());
//		for (Feature f : drone.getVisitedSensors()) {
//			double battery = map.getSensorBattery(f.getStringProperty("location"));
//			String reading = map.getSensorReading(f.getStringProperty("location"));
//
//			if (battery >= 10) {
//				f.addStringProperty("rgb-string", getColor(Double.parseDouble(reading)));
//				f.addStringProperty("marker-color", getColor(Double.parseDouble(reading)));
//				if (Double.parseDouble(reading) <= 128) {
//					f.addStringProperty("marker-symbol", "lighthouse");
//				} else {
//					f.addStringProperty("marker-symbol", "danger");
//				}
//			} else {
//				f.addStringProperty("rgb-string", "#000000");
//				f.addStringProperty("marker-color", "#000000");
//				f.addStringProperty("marker-symbol", "cross");
//			}
//			allFeatures.add(f);
//		}
//		renderGeojson();

		System.out.println("Test complete :)");
		
	}

	public static String getColor(double reading) {
//    	Check color classification as per specification
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

//	 Render 'features'

	private static void renderGeojson() {
//   	Create feature collection from list of features
		FeatureCollection fc = FeatureCollection.fromFeatures(allFeatures);
//   	Convert to json string
		String geojson = fc.toJson();

		System.out.println(geojson);
	}

}
