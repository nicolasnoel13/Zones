package zones;

import utils.Coords;

/**
 * ZonePoint is a type of AZoneSimple, it is only a point
 * @author Nicolas Noel
 *
 */
public class ZonePoint extends AZoneSimple {
	
	//Empty Constructor
	public ZonePoint() {
		super();
	}
	
	public ZonePoint(Coords coords) {
		super(coords);
		this.type = IConstantsZones.Zone_Type_Point;
	}

	@Override
	//The intersection of a point with a zone is the point if it belongs to the zone
	public AZone intersection(AZone zone) {
		if (zone.belongs(this.getCenter())) {
			return this;
		}
		else {
			return new ZoneNull();
		}
	}

	@Override
	public boolean equal(AZone zone) {
		boolean result=false;
		if (zone instanceof ZonePoint) {
			result=this.getCenter().equals(((ZonePoint) zone).getCenter());
		}
		return result;
	}

	@Override
	//For a point, belongs(coords) is equivalent to equal.
	public boolean belongs(Coords coords) {
		return this.getCenter().equals(coords);
	}
	
	public double distance(AZone zone) {
		if (zone.belongs(this.getCenter())) {
			return 0;
		}
		else {
			if (zone instanceof ZonePoint) {
				return zone.getCenter().distance(this.getCenter());}
			else {
				return zone.distance(this);
			}
		}
	}
	
	public String toString () {
		String result = "ZonePoint\n  ";
		result=result+" Point : "+this.getCenter().toString()+"\n";
		return result;
	}

}
