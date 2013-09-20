package zones;

import utils.Coords;

/**
 * An AZoneSimple can be : ZoneCircle, Lookup, Null, Point, Polygon, Route
 * @see ZoneCircle
 * @see ZoneLookup
 * @see ZoneNull
 * @see ZonePoint
 * @see ZonePolygon
 * @see ZoneRoute
 * @author Nicolas Noel
 *
 */
public abstract class AZoneSimple extends AZone {

	public AZoneSimple() {
		super();
	}
	
	public AZoneSimple(Coords center) {
		super(center);
	}
}
