package zones;

import utils.Coords;

/**
 * Cartesian Coordinates of a point on the sphere/Earth
 * Also understandable as the vector from center of the Earth to the point on the surface of the sphere
 * With methods to convert from latitude+longitude
 * and methods to operate vectors.
 * @author Nicolas Noel
 *
 */
public class CartesianCoords {
	
	protected double x;
	protected double y;
	protected double z;

	public CartesianCoords(double x, double y, double z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public CartesianCoords (Coords arg) {
		double Lon=arg.getLon()*Math.PI/180;
		double Lat=arg.getLat()*Math.PI/180;
		this.x = 6371000.0*Math.cos(Lon)*Math.cos(Lat);
		this.y = 6371000.0*Math.sin(Lon)*Math.cos(Lat);
		this.z = 6371000.0*Math.sin(Lat);
	}
	
	/**
	 * if u is the calling instance and v the argument this method returns u vectorial v
	 * @param v
	 * @return
	 */
	public CartesianCoords vectorialProduct (CartesianCoords v) {
		double uX = this.getX();
		double uY = this.getY();
		double uZ = this.getZ();
		double vX = v.getX();
		double vY = v.getY();
		double vZ = v.getZ();
		return new CartesianCoords((uY*vZ)-(uZ*vY),(uZ*vX)-(uX*vZ),(uX*vY)-(uY*vX));
	}
	
	public double scalarProduct (CartesianCoords v) {
		double uX = this.getX();
		double uY = this.getY();
		double uZ = this.getZ();
		double vX = v.getX();
		double vY = v.getY();
		double vZ = v.getZ();
		return ((uX*vX)+(uY*vY)+(uZ*vZ));
	}
	
	public double norm () {
		return this.scalarProduct(this);
	}
	
	public void normalize () {
		double norm = this.norm();
		this.x = this.x/norm;
		this.y = this.y/norm;
		this.z = this.z/norm;
	}
	
	double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public double getZ() {
		return z;
	}
	public void setZ(double z) {
		this.z = z;
	}
	
	public String toString() {
		return("CartesianCoords : x="+x+" ; y="+y+" ; z="+z);
	}

}
