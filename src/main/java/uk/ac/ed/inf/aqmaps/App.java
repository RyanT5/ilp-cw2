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
import com.mapbox.geojson.Point;

/**
 * Hello world!
 *
 */
public class App 
{
	
	private static final HttpClient client = HttpClient.newHttpClient();
	
	private static String day;
	private static String month;
	private static String year;
	private static String port;
	
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
       
//        Set boundry
//    	Get no-fly zones (in buildings/no-fly-zones.geojson)
//    	Get date's sensor list (in /maps/year/month/day/air-quality-data.json) -> sensor class list (always 33)
    	
//    	When necessary get words from words/first/second/third/details.json on server

//        System.out.println(serverRequest(80));
        String urlString = "http://localhost:" + port + "/maps/" + year + "/" + month + "/" + day + "/air-quality-data.json";
//        System.out.println(serverRequest(urlString));
        String sensorJson = serverRequest(urlString);
        
        Type listType = new TypeToken<ArrayList<Sensor>>() {}.getType();
        ArrayList<Sensor> sensorList = new Gson().fromJson(sensorJson, listType);
        System.out.println(sensorList.get(0).getLocation());
        
//        Create geojson features of that list
        
        List<Point> points = new ArrayList<Point>();
        
        wordsToLoc(sensorList.get(1).getLocation());
        
//        for (int i = 0; i < 33; i++) {
//        	points.add(Point.fromLngLat(topleftx, toplefty));
//        }
        
//        Shit I need wordsToLoc translation
        
        System.out.println("Done :)");

    }
    
//    createUrl
    
    private static String serverRequest(String urlString) {
    	
    	var request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
    	
    	try {
        	var response = client.send(request, BodyHandlers.ofString());
//        	System.out.println( response.statusCode() );
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
    
    private static String wordsToLoc(String words) {
    	
//    	When necessary get words from words/first/second/third/details.json on server
    	
    	String[] wordsList = words.split("\\.");
    	String urlString = "http://localhost:" + port + "/words/" + wordsList[0] + "/" + wordsList[1] + "/" + wordsList[2] + "/details.json";
//    	System.out.println(urlString);
    	
    	String wordsJson = serverRequest(urlString);
    	
    	System.out.println(wordsJson);
    	
    	return null;
    }
    
//    unpackFeatures
    
//    packageFeatures
    
    
    
    
    
    
    
    
}
