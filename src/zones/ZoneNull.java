package zones;

import utils.Coords;

/**
 * ZoneNull is a type of AzoneSimple that represents an empty zone.
 * @author Nicolas Noel
 *
 */
public class ZoneNull extends AZoneSimple {

	public ZoneNull () {
		this.type = IConstantsZones.Zone_Type_Null;
	}
	
	@Override
	public AZone intersection(AZone zone) {
		return this;
	}

	@Override
	public boolean equal(AZone zone) {
		if (zone instanceof ZoneNull) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean belongs(Coords coords) {
		return false;
	}
	
	public double distance(AZone zone) {
		return 0;
	}

}
