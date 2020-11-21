package uk.ac.ed.inf.aqmaps;

public class WordsAddress {
	
	Coordinates coordinates;

	public WordsAddress(Coordinates coordinates) {
		this.coordinates = coordinates;
	}
	
	public static class Coordinates {
		double lng;
		double lat;
		
		public Coordinates(double lng, double lat) {
			super();
			this.lng = lng;
			this.lat = lat;
		}
		
		public double getLng() {
			return lng;
		}
		public double getLat() {
			return lat;
		}
	}
	
	public Coordinates getCoordinates() {
		return coordinates;
	}

}
