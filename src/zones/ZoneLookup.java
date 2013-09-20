package zones;

import java.util.ArrayList;
import utils.Coords;


/**
 * ZoneLookup is a type of AZoneSimple. It is a rectangle (representing the view in
 * OpenLayers) parallel to the axes. It is represented by coordsA (bottom-left
 * vertex) and coordsB (top-right vertex)
 * 
 * @author Nicolas Noel
 * 
 */
public class ZoneLookup extends AZoneSimple {

	protected Coords coordsA;
	protected Coords coordsB;

	/**
	 * The Constructor of an empty Zone just initialised a set of two
	 * Coordinates
	 */
	public ZoneLookup() {
		super();
		this.coordsA = new Coords();
		this.coordsB = new Coords();
		this.type = IConstantsZones.Zone_Type_Lookup;
	}

	/**
	 * The Constructor of a Zone using two coordinates
	 * 
	 * @param list
	 *            The list of Coordinates we want to use as peaks of the Zone
	 */
	public ZoneLookup(Coords coordsA, Coords coordsB) {
		super(coordsA);
		if (coordsA.getLat() < coordsB.getLat()) {
			this.coordsA = coordsA;
			this.coordsB = coordsB;
		} else {
			System.out
					.println("Warning : the two coords in the constructor of ZoneLookup had to be inverted");

			this.coordsA = coordsB;
			this.coordsB = coordsA;
		}
		this.type = IConstantsZones.Zone_Type_Lookup;
	}

	public Coords getCoordsA() {
		return coordsA;
	}

	public void setCoordsA(Coords coordsA) {
		this.coordsA = coordsA;
		this.center=coordsA;
	}

	public Coords getCoordsB() {
		return coordsB;
	}

	public void setCoordsB(Coords coordsB) {
		this.coordsB = coordsB;
	}

	@Override
	public AZone intersection(AZone zone) {
		if (zone instanceof ZoneNull) {
			return zone;
		} else if (zone instanceof ZonePoint) {
			// If its a point, if the point belongs to the lookup zone, return
			// the point
			if (this.belongs(((ZonePoint) zone).getCenter())) {
				return zone;
			} else {
				return new ZoneNull();
			}
		} else if (zone instanceof ZoneLookup) {
			// If its another ZoneLookup,calculate, the intersection is either a
			// Lookup or a Null
			Coords R1 = ((ZoneLookup) zone).getCoordsA();
			Coords R2 = ((ZoneLookup) zone).getCoordsB();
			Coords B1 = this.getCoordsA();
			Coords B2 = this.getCoordsB();
			// Test intersection null
			if ((B1.getLon() <= R1.getLon() && R1.getLon() > B2.getLon())
					|| (B1.getLon() <= R1.getLon() && R1.getLon() > B2.getLon())
					|| (B1.getLat() <= R1.getLat() && R1.getLat() > B2.getLat())
					|| (B1.getLat() <= R1.getLat() && R1.getLat() > B2.getLat())) {
				return new ZoneNull();
			}
			// If intersection not null return the zone lookup intersection
			else {
				double lat1, lat2, long1, long2;
				long1 = Math.max(B1.getLon(), R1.getLon());
				lat1 = Math.max(B1.getLat(), R1.getLat());
				long2 = Math.min(R2.getLon(), B2.getLon());
				lat2 = Math.min(R2.getLat(), B2.getLat());
				return new ZoneLookup(new Coords(lat1, long1),
						new Coords(lat2, long2));
			}
		} else if (zone instanceof ZoneRoute) {
			// If its a ZoneRoute, call ZoneRoute's method
			return zone.intersection(this);
		} else if (zone instanceof ZonePolygon) {
			// If its a ZonePolygon, call ZonePolygon's method
			return zone.intersection(this);
		} else if (zone instanceof ZoneCircle) {
			// If it is a ZoneCircle, call its method
			return zone.intersection(this);
		} else if (zone instanceof ZoneList) {
			// If it is a ZoneList, call its method
			return zone.intersection(this);
		} else {
			System.out.println("Zone Lookup intersection method : unimplemented type");
			return new ZoneNull();
		}

	}

	@Override
	public boolean equal(AZone zone) {
		boolean result = false;
		if (zone instanceof ZoneLookup) {
			result = (this.coordsA.equals(((ZoneLookup) zone).getCoordsA()))
					&& (this.coordsB.equals(((ZoneLookup) zone).getCoordsB()));
		}
		return result;
	}

	@Override
	public boolean belongs(Coords coords) {
		boolean result = false;
		double Clat = coords.getLat();
		double Clong = coords.getLon();
		double Alat = this.getCoordsA().getLat();
		double Blat = this.getCoordsB().getLat();
		double Along = this.getCoordsA().getLon();
		double Blong = this.getCoordsB().getLon();
		if (Along > Blong) {
			result = (Clat >= Alat)
					&& (Clat <= Blat)
					&& ((Clong >= Along && Clong <= 180) || (Clong <= Blong && Clong > -180));
		} else {
			result = (Clat >= Alat) && (Clat <= Blat) && (Clong >= Along)
					&& (Clong <= Blong);
		}
		return result;
	}
	
	public double distance(AZone zone) {
		if (zone instanceof ZoneNull) {
			return zone.distance(this);
		}
		else if (zone instanceof ZonePoint) {
			if (this.belongs(zone.getCenter())) {
				return 0;
			}
			else {
				if ((zone.getCenter().getLon()>this.getCoordsA().getLon()) && 
						(zone.getCenter().getLon()<this.getCoordsB().getLon())) {
					return Math.min(Math.abs(zone.getCenter().getLat()-coordsA.getLat()),
							Math.abs(zone.getCenter().getLat()-coordsB.getLat()));
				}
				else if ((zone.getCenter().getLat()>this.getCoordsA().getLat()) && 
						(zone.getCenter().getLat()<this.getCoordsB().getLat())) {
					return Math.min(Math.abs(zone.getCenter().getLon()-coordsA.getLon()),
							Math.abs(zone.getCenter().getLon()-coordsB.getLon()));
				}
				else {
					Coords A = this.getCoordsA();
					Coords C = this.getCoordsB();
					Coords B = new Coords (A.getLat(),C.getLon());
					Coords D = new Coords (C.getLat(),A.getLon());
					Coords E = zone.getCenter();
					return Math.min(Math.min(A.distance(E), B.distance(E)), Math.min(C.distance(E), D.distance(E)));
				}
			}
		}
		else if (zone instanceof ZoneCircle) {
			return zone.distance(this);
		}
		else if (zone instanceof ZoneLookup) {
			if (this.intersection(zone) instanceof ZoneNull) {
				ZonePoint A = new ZonePoint(this.getCoordsA());
				ZonePoint C = new ZonePoint(this.getCoordsB());
				ZonePoint B = new ZonePoint(new Coords (A.getCenter().getLat(),C.getCenter().getLon()));
				ZonePoint D = new ZonePoint(new Coords (C.getCenter().getLat(),A.getCenter().getLon()));
				return Math.min(Math.min(zone.distance(A), zone.distance(B)), Math.min(zone.distance(C), zone.distance(D)));
			}
			else {
				return 0;
			}
		}
		else if (zone instanceof ZoneRoute) {
			return zone.distance(this);
		}
		else if (zone instanceof ZonePolygon) {
			return zone.distance(this);
		}
		else if (zone instanceof ZoneList) {
			return zone.distance(this);
		}
		return 0;
	}
	
	/**
	 * Calculates the point of intersection between a segment and the calling ZoneLookup
	 * (assuming the interection exists and is unique)
	 * @param A
	 * @param B
	 * @return
	 */
	public Coords segment_inter(Coords A, Coords B) {
		return this.transformToPolygon().segment_inter(A,B);
	}
	
	/**
	 * Transforms a ZoneLookup into a Polygon
	 * @return
	 */
	public ZonePolygon transformToPolygon() {
		Coords ZL_A = this.getCoordsA();
		Coords ZL_C = this.getCoordsB();
		Coords ZL_B = new Coords (ZL_A.getLat(),ZL_C.getLon());
		Coords ZL_D = new Coords (ZL_C.getLat(),ZL_A.getLon());
		ArrayList<Coords> list = new ArrayList<Coords>();
		list.add(ZL_A);
		list.add(ZL_B);
		list.add(ZL_C);
		list.add(ZL_D);
		ZonePolygon polygonLookup = new ZonePolygon(list);
		return polygonLookup;
	}
	
	public String toString () {
		String result = "ZoneLookup\n  ";
		result=result+" PointA : "+this.getCoordsA().toString()+"\n";
		result=result+" PointB : "+this.getCoordsB().toString();
		return result;
	}
	
}
