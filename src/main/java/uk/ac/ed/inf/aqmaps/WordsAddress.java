package uk.ac.ed.inf.aqmaps;

public class WordsAddress {

	// Maps to data in air-quality-data.json
	private Coordinates coordinates;

	// Constructor
	public WordsAddress(Coordinates coordinates) {
		this.coordinates = coordinates;
	}

	// Nested static class
	public static class Coordinates {
		private double lng;
		private double lat;

		// Constructor for nexted class
		public Coordinates(double lng, double lat) {
			this.lng = lng;
			this.lat = lat;
		}

		// Public getters for nested class

		public double getLng() {
			return lng;
		}

		public double getLat() {
			return lat;
		}
	}

	// Public getters

	public Coordinates getCoordinates() {
		return coordinates;
	}

}
