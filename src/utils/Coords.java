package utils;

import zones.AZone;


/**
 * Coords represents an event's geographical coordinates in degrees
 * The latitude value has to be between -90 and 90
 * The longitude value has to be between -180 and 180
 *
 * @author Nicolas Noel
 */

public class Coords {

	protected double latitude; //between -90 and 90
	protected double longitude; //between -180 and 180
	public static final int MIN_LONG = -180;
	public static final int MAX_LONG = 180;
	public static final int MIN_LAT = -90;
	public static final int MAX_LAT = 90;

	/**
	 * Empty Constructor of the Coords object
	 */
	public Coords() {
	}

	/**
	 * Constructor of an object Coords with two coordinates
	 * @param latitude parameter to affect as the latitude of the coordinates
	 * @param longitude parameter to affect as the longitude of the coordinates
	 */
	public Coords(double latitude, double longitude) {

		if ((latitude>-90)&&(latitude<90)&&(longitude>-180)&&(longitude<180)) {
			this.latitude = latitude;
			this.longitude = longitude;
		}
		else {
			System.out.println("inadequates coordinates");
			this.latitude = latitude;
			this.longitude = longitude;
		}
	}

	//Copy constructor
	public Coords(Coords coords) {
		this.latitude = coords.latitude;
		this.longitude = coords.longitude;
	}

	@Override
	public String toString() {
		return "Coords [latitude=" + this.latitude + ", longitude=" + this.longitude+ "]";
	}

	public boolean equals(Coords coords) {
		return ((Math.abs(coords.getLat()-this.latitude)<AZone.epsilonTest)
				&& (Math.abs(coords.getLon()-this.longitude)<AZone.epsilonTest));
	}

	/**
	 * Distance orthodromique de la classe appelante a B
	 * (la plus courte distance en spherique)
	 * Result in meters
	 * @param B
	 * @return
	 */
	//Methode testee, elle marche !
	public double distance (Coords B) {
		double latA = this.getLat()*Math.PI/180;
		double lonA = this.getLon()*Math.PI/180;
		double latB = B.getLat()*Math.PI/180;
		double lonB = B.getLon()*Math.PI/180;

		double result =(Math.sin(latA)*Math.sin(latB)) + (Math.cos(latA)*Math.cos(latB)*Math.cos(lonB-lonA));
		result = Math.acos(result)*180/Math.PI; // des radians aux degres
		result = 60*result; // distance in nautical miles
		result = result * 1852; // distance in meters
		return result;

	}

	/* Getters and Setters */
	public double getLat() {
		return this.latitude;
	}
	public void setLat(double latitude) {
		this.latitude = latitude;
	}
	public double getLon() {
		return this.longitude;
	}
	public void setLon(double longitude) {
		this.longitude = longitude;
	}

}
