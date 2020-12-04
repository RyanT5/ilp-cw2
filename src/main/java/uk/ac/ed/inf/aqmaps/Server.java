package uk.ac.ed.inf.aqmaps;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

public final class Server {

	// Establish the http client
	private static final HttpClient client = HttpClient.newHttpClient();

	// Constructor
	private Server() {
		// Nothing needed here
	}

	// Server request
	public static String serverRequest(String urlString) {
		var request = HttpRequest.newBuilder().uri(URI.create(urlString)).build();
		try {
			var response = client.send(request, BodyHandlers.ofString());
			if (response.statusCode() == 200) {
				// Successful request
				return response.body();
			} else {
				if (response.statusCode() == 404) {
					// Server active but no such file
					System.out.println("Error 404: file not found");
				} else {
					// Server connection failed
					System.out.println("Unknown error: server responded " + response.statusCode());
				}
			}
		} catch (IOException | InterruptedException e) {
			System.out.println("Error connecting to server");
			e.printStackTrace();
		}
		return null;
	}

}
