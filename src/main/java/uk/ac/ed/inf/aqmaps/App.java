package uk.ac.ed.inf.aqmaps;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;

public class App 
{
	
	private static final HttpClient client = HttpClient.newHttpClient();
	
	private static String day;
	private static String month;
	private static String year;
	private static String port;
	
	public static List<Feature> features = new ArrayList<Feature>();
	
    public static void main( String[] args ) {
        
//        Arg 0: day
//        Arg 1: month
//        Arg 2: year
//        Arg 3: starting point x
//        Arg 4: starting point y
//        Arg 5: seed
//        Arg 6: port
        
//    	day = arg[0];
//    	month = arg[1];
//    	year = arg[2];
    	
//    	port = arg[6];
    	
    	day = "15";
    	month = "06";
    	year = "2021";
    	
    	port = "80";
       
//      Set boundary
//    	Get no-fly zones (in buildings/no-fly-zones.geojson)
//    	Get date's sensor list (in /maps/year/month/day/air-quality-data.json) -> sensor class list (always 33)
//    	Get words from words/first/second/third/details.json on server

        String urlString = "http://localhost:" + port + "/maps/" + year + "/" + month + "/" + day + "/air-quality-data.json";
        String sensorJson = serverRequest(urlString);
        
        Type listType = new TypeToken<ArrayList<Sensor>>() {}.getType();
        ArrayList<Sensor> sensorList = new Gson().fromJson(sensorJson, listType);
        
        for (int i = 0; i < 33; i++) {
        	WordsAddress wordsAddress = wordsToLoc(sensorList.get(i).getLocation());
        	Feature feature = Feature.fromGeometry(Point.fromLngLat(wordsAddress.getCoordinates().getLng(), wordsAddress.getCoordinates().getLat()));
        	feature.addStringProperty("location", sensorList.get(i).getLocation());
        	feature.addStringProperty("rgb-string", "#00ff00");
        	feature.addStringProperty("marker-color", "#00ff00");
        	feature.addStringProperty("marker-symbol", "lighthouse");
        	features.add(feature);
        }
        
//    	Create feature collection from list of features
    	FeatureCollection fc = FeatureCollection.fromFeatures(features);
//    	Convert to json string
    	String geojson = fc.toJson();
    	
    	System.out.println(geojson);
    }
    
    private static String serverRequest(String urlString) {
    	
    	var request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
    	
    	try {
        	var response = client.send(request, BodyHandlers.ofString());
//        	System.out.println( response.statusCode() );
        	if (response.statusCode() == 200) {
//        		System.out.println("Server responded 200: file recieved");
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
    
    private static WordsAddress wordsToLoc(String words) {
    	String[] wordsList = words.split("\\.");
    	String urlString = "http://localhost:" + port + "/words/" + wordsList[0] + "/" + wordsList[1] + "/" + wordsList[2] + "/details.json";
    	
    	String wordsJson = serverRequest(urlString);
    	
    	var details = new Gson().fromJson(wordsJson, WordsAddress.class);
    	
    	return details;
    }
    
//    unpackFeatures
    
//    packageFeatures
    
    
    
}
