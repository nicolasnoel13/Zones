package zones;

import utils.Coords;

import java.util.ArrayList;

/**
 * This zone is a circle on the surface of the Earth
 * A big one looks like a contact lens
 * The center is in the abstract class AZone
 * The radius is in meters
 * @author Nicolas Noel
 *
 */
public class ZoneCircle extends AZoneSimple {
	
	protected double radius; //in meters
	
	public ZoneCircle(double radius) {
		super();
		this.radius = radius; //in meters
		this.type = IConstantsZones.Zone_Type_Circle;
	}
	
	public ZoneCircle(double radius, Coords center) {
		this(radius);
		this.setCenter(center);
	}

	@Override
	public AZone intersection(AZone zone) {
		if (zone instanceof ZoneNull) {
			return zone;
		}
		else if (zone instanceof ZonePoint) {
			if (this.belongs(((ZonePoint)zone).getCenter())) {
				return zone;
			}
			return new ZoneNull();
		}
		else if (zone instanceof ZoneLookup) {
			Coords corner_1,corner_2,corner_3,corner_4,cardinal_1,cardinal_2,cardinal_3,cardinal_4;
			corner_1 = ((ZoneLookup) zone).getCoordsA();
			corner_3 = ((ZoneLookup) zone).getCoordsB();
			corner_2 = new Coords (corner_1.getLat(),corner_3.getLon());
			corner_4 = new Coords (corner_3.getLat(),corner_1.getLon());
			cardinal_1 = new Coords(this.getCenter().getLat()-this.getRadius(),this.getCenter().getLon()); //down point of circle
			cardinal_2 = new Coords(this.getCenter().getLat(),this.getCenter().getLon()+this.getRadius()); //right point of circle
			cardinal_3 = new Coords(this.getCenter().getLat()+this.getRadius(),this.getCenter().getLon()); //up point of circle
			cardinal_4 = new Coords(this.getCenter().getLat(),this.getCenter().getLon()-this.getRadius()); //left point of circle
			
			System.out.println("j'essaye de calculer l'inter de circle avec une lookup");
			
			//if circle inside lookup (it is equivalent to : 4 cardinal points of circle are inside of lookup)
			if (this.belongs(corner_1) && this.belongs(corner_2) && this.belongs(corner_3) && this.belongs(corner_4)){
				System.out.println("cas 1");
				return zone;
			}
			//if lookup inside circle (it is equivalent to : 4 corner points of lookup are inside of circle)
			else if (zone.belongs(cardinal_1) && zone.belongs(cardinal_2) && zone.belongs(cardinal_3) && zone.belongs(cardinal_4)) {
				System.out.println("cas 2");
				return this;
			}
			//other cases are impossible to handle
			else {
				System.out.println("ERROR : Cannot calculate intersection between circle and rectangle");
				return new ZoneNull();
			}
		}
		else if (zone instanceof ZoneRoute) {
			ZoneList list = new ZoneList();
			
			boolean inside_A=false;
			boolean inside_B=false;
			Coords pointA=null;
			Coords pointB=null;
			ArrayList<Coords> return_list = new ArrayList<Coords>();
			
			for (int i=0;i<((ZoneRoute) zone).getPointsList().size();i++) {
				if (i==0) {
					pointA=((ZoneRoute) zone).getPointsList().get(0);
					inside_A = this.belongs(pointA);
				}
				else if (i!=((ZoneRoute) zone).getPointsList().size()-1){
					pointA=pointB;
					inside_A=inside_B;
				}
				
				if (i==((ZoneRoute) zone).getPointsList().size()-1) {
					if (!inside_A) {
						if (inside_B) {
							return_list.add(pointB);
						}
					}	
					else {
						if (inside_B) {
							return_list.add(pointB);
						}
					}
					list.addZone(new ZoneRoute(return_list));
				}
				else {
					pointB=((ZoneRoute) zone).getPointsList().get(i+1);
					inside_B = this.belongs(pointB);
					
					if (!inside_A) {
						if (inside_B) {
							return_list.add(segment_inter(pointA,pointB));
						}
					}	
					else {
						if (inside_B) {
							return_list.add(pointA);
						}
						else {
							return_list.add(pointA);
							return_list.add(segment_inter(pointA,pointB));
							//We were inside the circle and just got out
							list.addZone(new ZoneRoute(return_list));
							return_list = new ArrayList<Coords>();
						}
					}
				}
			}
			
			if (list.size()==0) {
				//ZoneNull
				return new ZoneNull();
			}
			else if (list.size()==1) {
				ZoneRoute zoneRoute = (ZoneRoute) list.head();
				if (zoneRoute.getPointsList().size()==1) {
					//A ZonePoint, the only point was in the only ZoneRoute in the ZoneList
					return new ZonePoint(zoneRoute.getPointsList().get(0));
				}
				else {
					//A ZoneRoute
					return zoneRoute;
				}
			}
			else {
				//ZoneList of ZoneRoads
				return list;
			}
		}
		else if (zone instanceof ZonePolygon) {
			System.out.println("ERROR : Cannot calculate intersection between circle and polygon");
			return null;
		}
		else if (zone instanceof ZoneCircle) {
			System.out.println("ERROR : Cannot calculate intersection between two circles");
			return null;
		} else if (zone instanceof ZoneList) {
			// If it is a ZoneList, call its method
			return zone.intersection(this);
		} else {
			return null;
		}
	}

	@Override
	public boolean equal(AZone zone) {
		boolean result=false;
		if (zone instanceof ZoneCircle) {
			result=((this.getCenter().equals(((ZoneCircle)zone).getCenter()))
					&&(this.radius==((ZoneCircle)zone).getRadius()));
		}
		return result;
	}

	@Override
	public boolean belongs(Coords coords) {
		return (coords.distance(this.getCenter())<this.radius);
	}
	
	/**
	 * Calculates the point of intersection between a segment and a circle
	 * (assuming the interection exists and is unique)
	 * @param A
	 * @param B
	 * @return
	 */
	public Coords segment_inter(Coords A, Coords B) {
		Coords in;
		Coords out;
		Coords M = new Coords();
		Coords N = new Coords();
		
		if (this.belongs(A)) {
			in = new Coords (A);
			out = new Coords (B);
		}
		else {
			in = new Coords (B);
			out = new Coords (A);
		}
		
		M = AZone.middle(in,out);
		
		while (Math.abs(M.distance(this.getCenter())-this.radius)>AZone.epsilonCalc) { //Tant que M n'est pas assez pret du cercle
			N=new Coords(M);
			if (this.belongs(N)) { // le dernier point a tape a l'interieur du cerle , on repart vers l'exterieur
				M=AZone.middle(N,out);
				in = new Coords(N);
			}
			else { // le dernier point a tape a l'exterieur du cercle , on repart ves l'interieur
				M=AZone.middle(N,in);
				out = new Coords(N);
			}
		}
		return M;
	}
	
	public double distance(AZone zone) {
		if (zone instanceof ZoneNull) {
			return zone.distance(this);
		}
		else if (zone instanceof ZoneList) {
			return zone.distance(this);
		}
		else  {
			double temp= zone.distance(new ZonePoint(this.getCenter()))-this.getRadius();
			if (temp>0) {
				return temp;
			}
			else {
				return 0;
			}
		}
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}
	
	public String toString () {
		String result = "ZoneCircle\n  ";
		result=result+" radius : "+this.radius+" m\n";
		result=result+" center : "+this.getCenter().toString();
		return result;
	}

}
