package uk.ac.ed.inf.aqmaps;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

public class Map {
	
	private static final HttpClient client = HttpClient.newHttpClient();
	
	private String day;
	private String month;
	private String year;
	private String port;
	
	private static double x1 = -3.192473;
	private static double x2 = -3.184319;
	private static double y1 = 55.946233;
	private static double y2 = 55.942617;
	
	public ArrayList<Sensor> sensorList = new ArrayList<Sensor>();
	
	public List<Feature> noFlyZones = new ArrayList<Feature>();
	
	public List<Feature> features = new ArrayList<Feature>();
	
	public Feature boundary;
	
//	 Constructor
	
	public Map(String day, String month, String year, String port) {
		this.day = day;
		this.month = month;
		this.year = year;
		this.port = port;
		
		sensorList = getStations();
		noFlyZones = getBuildings();
		boundary = getBoundary();

		for (Sensor s: sensorList) {
			WordsAddress wordsAddress = wordsToLoc(s.getLocation());
        	Feature feature = Feature.fromGeometry(Point.fromLngLat(wordsAddress.getCoordinates().getLng(), wordsAddress.getCoordinates().getLat()));
        	feature.addStringProperty("location", s.getLocation());
//        	feature.addStringProperty("rgb-string", "#00ff00");
//        	feature.addStringProperty("marker-color", "#00ff00");
//        	feature.addStringProperty("marker-symbol", "lighthouse");
        	features.add(feature);
		}
		
		noFlyZones = getBuildings();
        for(Feature f : noFlyZones) {
        	features.add(f);
        }
        
        features.add(getBoundary());
	}
	
//	 Server request
	
    private static String serverRequest(String urlString) {
    	
    	var request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
    	
    	try {
        	var response = client.send(request, BodyHandlers.ofString());
        	if (response.statusCode() == 200) {
        		System.out.println("Server responded 200: file recieved");
        		return response.body();
        	} else {
        		if (response.statusCode() == 404) {
        			System.out.println("Error 404: file not found");
        		} else {
        			System.out.println("Unknown error: server responded " + response.statusCode());
        		}
        	}
        } catch (IOException | InterruptedException e) {
        	System.out.println("Error connecting to server");
			e.printStackTrace();
		}
    	return null;
    }
    
//     Get stations
    
    private ArrayList<Sensor> getStations() {
    	String urlString = "http://localhost:" + port + "/maps/" + year + "/" + month + "/" + day + "/air-quality-data.json";
        String sensorJson = serverRequest(urlString);
        Type listType = new TypeToken<ArrayList<Sensor>>() {}.getType();
        ArrayList<Sensor> sensorList = new Gson().fromJson(sensorJson, listType);
        return sensorList;
    }
    
//     Get coordinates from words
    
    private WordsAddress wordsToLoc(String words) {
    	String[] wordsList = words.split("\\.");
    	String urlString = "http://localhost:" + port + "/words/" + wordsList[0] + "/" + wordsList[1] + "/" + wordsList[2] + "/details.json";
    	String wordsJson = serverRequest(urlString);
    	var details = new Gson().fromJson(wordsJson, WordsAddress.class);
    	return details;
    }
	
//	 Get noflyzones
    
    private List<Feature> getBuildings() {
    	String urlString = "http://localhost:" + port + "/buildings/no-fly-zones.geojson";
        String buildingGeojson = serverRequest(urlString);
    	FeatureCollection buildingGroup = FeatureCollection.fromJson(buildingGeojson);
    	List<Feature> buildings = new ArrayList<Feature>();
    	buildings = buildingGroup.features();
    	return buildings;
    }
	
//	 Get boundary
    
    private static Feature getBoundary() {
    	
    	List<Point> points = new ArrayList<Point>();
    	
    	points.add(Point.fromLngLat(x1, y1));
		points.add(Point.fromLngLat(x2, y1));
		points.add(Point.fromLngLat(x2, y2));
		points.add(Point.fromLngLat(x1, y2));
		points.add(Point.fromLngLat(x1, y1));
		
		Feature boundary = Feature.fromGeometry(LineString.fromLngLats(points));
    	
    	return boundary;
    }
	
//	 Render 'features'
    
    public void renderGeojson() {
//    	Create feature collection from list of features
    	FeatureCollection fc = FeatureCollection.fromFeatures(features);
//    	Convert to json string
    	String geojson = fc.toJson();
    	
    	System.out.println(geojson);
    }

}
