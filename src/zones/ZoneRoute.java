package zones;

import utils.Coords;
import java.util.ArrayList;

/**
 * ZoneRoute is a type of AzoneSimple.
 * Its ArrayList of Coords represents a broken line (representing a road).
 * @author Nicolas Noel
 *
 */
public class ZoneRoute extends AZoneSimple {
	protected ArrayList<Coords> pointsList;
	
	public ZoneRoute() {
		super();
		pointsList = new ArrayList<Coords>();
		this.type = IConstantsZones.Zone_Type_Route;
	}
	
	public ZoneRoute(Coords center) {
		super(center);
		pointsList = new ArrayList<Coords>();
		this.type = IConstantsZones.Zone_Type_Route;
	}

	public ZoneRoute(ArrayList<Coords> pointsList) {
		super();
		this.pointsList = new ArrayList<Coords>();
		this.type = IConstantsZones.Zone_Type_Route;
		
		for (Coords coords : pointsList){
			this.pointsList.add(coords);
		}
		this.pointsList = pointsList;
	}
	
	public ZoneRoute(ArrayList<Coords> pointsList, Coords center) {
		this(pointsList);
		this.setCenter(center);
	}
	
	public ArrayList<Coords> getPointsList() {
		return this.pointsList;
	}

	public void setPointsList(ArrayList<Coords> pointsList) {
		this.pointsList = pointsList;
	}

	@Override
	
	public AZone intersection(AZone zone) {
		if (zone instanceof ZoneNull){
			return zone;
		}
		else if (zone instanceof ZonePoint){
			//If its a ZonePoint
			if (this.belongs(((ZonePoint) zone).getCenter())) {
				return zone;
			}
			else {
				return new ZoneNull();
			}
		}
		else if (zone instanceof ZoneLookup) {
			ZoneList list = new ZoneList();

			boolean inside_A=false;
			boolean inside_B=false;
			Coords pointA=null;
			Coords pointB=null;
			Coords tempToAdd=null;
			ArrayList<Coords> return_list = new ArrayList<Coords>();

			for (int i=0;i<this.getPointsList().size()-1;i++) {
				if (i==0) {
					pointA=this.getPointsList().get(0);
					inside_A = zone.belongs(pointA);
				}
				else {
					pointA=new Coords (pointB);
					inside_A=inside_B;
				}
				
				pointB=this.getPointsList().get(i+1);
				inside_B = zone.belongs(pointB);
				
				System.out.println("i = "+i);
				System.out.println("inside_A : "+inside_A);
				System.out.println("inside_B : "+inside_B);
				System.out.println("pointA : "+pointA.toString());
				System.out.println("pointB : "+pointB.toString());
				
				if (!inside_A) {
					if (inside_B) {
						tempToAdd=((ZoneLookup)zone).segment_inter(pointA,pointB);
						if (tempToAdd!=null) {
							return_list.add(tempToAdd);
							System.out.println("just added : "+tempToAdd.toString());
						}
					}
				}	
				else {
					if (inside_B) {
						return_list.add(pointA);
						System.out.println("just added : "+pointA.toString());
					}
					else {
						if (!pointA.equals(((ZoneLookup)zone).segment_inter(pointA,pointB))) {
							//si je ne viens pas juste d'ajouter ce point l'ajouter
							return_list.add(pointA);
							System.out.println("just added : "+pointA.toString());
						}
						
						
						if (tempToAdd!=null) {
							if (!tempToAdd.equals(((ZoneLookup)zone).segment_inter(pointA,pointB))) {
								//si je ne viens pas juste d'ajouter ce point l'ajouter
								tempToAdd=((ZoneLookup)zone).segment_inter(pointA,pointB);
								return_list.add(tempToAdd);
								System.out.println("just added : "+tempToAdd.toString());
							}
						}
						//We were inside the ZoneLookup and just got out
						list.addZone(new ZoneRoute(return_list));
						return_list = new ArrayList<Coords>();
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
		else if (zone instanceof ZoneRoute) {
			//If its a ZoneRoute, calculate intersection
			
			//liste des points intersection
			ArrayList<Coords> list =new ArrayList<Coords>();
			//liste des segments intersection
			ArrayList<ZoneRoute> list_roads =new ArrayList<ZoneRoute>();
			ArrayList<Coords> listA = this.pointsList;
			ArrayList<Coords> listB = ((ZoneRoute)zone).getPointsList();
			Coords A,B,C,D,inter;
			
			for (int i=0;i<listA.size()-1;i++) {
				A=listA.get(i);
				B=listA.get(i+1);
				for (int j=0;j<listB.size()-1;j++) {
					C=listB.get(j);
					D=listB.get(j+1);
					
					//if intersection of the 2 segments is a segment
					if (point_in_line(C, A, B) && point_in_line(D, A, B)) {
						ArrayList<Coords> list_temp =new ArrayList<Coords> ();
						if ((point_in_segment(C,A,B))&& !(point_in_segment(D,A,B))) {
							list_temp.add(C);
							list_temp.add(B);
						}
						else if ((point_in_segment(D,A,B))&& !(point_in_segment(C,A,B))) {
							list_temp.add(A);
							list_temp.add(D);
						}
						else if ((point_in_segment(C,A,B))&& (point_in_segment(D,A,B))) { //[cd] in [ab]
							list_temp.add(C);
							list_temp.add(D);
						}
						else { //[ab] in [cd]
							list_temp.add(A);
							list_temp.add(B);
						}
						list_roads.add(new ZoneRoute(list_temp));
					}
					//else : it is either a point or null
					else {
						//Calculate intersection point
						inter = segmentsIntersection (A,B,C,D);
						if (inter!=null) {
							//If point inter in segments [AB] and [CD]
							if (point_in_segment(inter,A,B)&&point_in_segment(inter,C,D)) {
								list.add(inter); //Add inter point to list
							}
						}
					}
				}
			}
			if (list_roads.size()==0) {
			//0 segments intersection
				if (list.size()==0) {
					return new ZoneNull();
				}
				else if (list.size()==1) {
					return new ZonePoint(list.get(0));
				}
				else {
					ZoneList zonelist = new ZoneList();
					for (Coords coords : list) {
						zonelist.addZone(new ZonePoint(coords));
					}
					return zonelist;
				}
			}
			else {
				//Cleanup : virer les points qui appartiennent aux segments
				ArrayList<Coords> listCopy =new ArrayList<Coords>(list);
				for (Coords toClean : listCopy) {
					for (ZoneRoute road : list_roads) {
						if (road.belongs(toClean)) {
							list.remove(toClean);
						}
					}
				}
				
				
				ZoneList zoneListReturn = new ZoneList();
				for (Coords coords : list) {
					zoneListReturn.addZone(new ZonePoint(coords));
				}
				for (ZoneRoute zoneRoute : list_roads) {
					zoneListReturn.addZone(zoneRoute);
				}
				if (list_roads.size()==1) {
					//return the ZoneRoute
					return zoneListReturn.getZonesList().get(0);
				}
				else {
					return zoneListReturn;
				}
			}
		}
		else if (zone instanceof ZonePolygon) {
			//If it is a ZonePolygon, call its method
			return zone.intersection(this);
		}
		else if (zone instanceof ZoneCircle) {
			//If it is a ZoneCircle, call its method
			return zone.intersection(this);
		} else if (zone instanceof ZoneList) {
			// If it is a ZoneList, call its method
			return zone.intersection(this);
		} else {
			return new ZoneNull();
		}
	}

	@Override
	public boolean equal(AZone zone) {
		boolean result=false;
		if (zone instanceof ZoneRoute) {
			if (this.pointsList.size()==((ZoneRoute)zone).pointsList.size()) {
				for (int i=0;i<this.pointsList.size();i++) {
					result=true;
					result=result&&(this.pointsList.get(i).equals(((ZoneRoute)zone).pointsList.get(i)));
				}
			}
		}
		return result;
	}

	@Override
	public boolean belongs(Coords coords) {
		boolean result=false;
		
		for (int i=0;i<this.pointsList.size();i++) {
			int j=i+1;
			if (i==this.pointsList.size()-1) {j=0;}
			Coords A=this.pointsList.get(i);
			Coords B=this.pointsList.get(j);
			//Test if point belongs to segment
			if (point_in_line(coords,A,B) && point_in_segment(coords,A,B)) {
				result=true;
			}
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
				Coords A,B;
				double dist_mini = 0;
				double dist_temp;
				for (int i=0;i<this.getPointsList().size()-1;i++) {
					A= new Coords (this.getPointsList().get(i));
					B= new Coords (this.getPointsList().get(i+1));
					dist_temp = Math.min(A.distance(zone.getCenter()),B.distance(zone.getCenter()));
					if (i==0) {
						dist_mini=dist_temp;
					}
					if (isBetweenNormals(zone.getCenter(),A,B)) {
						dist_temp = distProjOrth(zone.getCenter(),A,B);
					}
					if (dist_temp <dist_mini) {
						dist_mini=dist_temp;
					}
				}
				return dist_mini;
			}
			
		}
		else if (zone instanceof ZoneCircle) {
			return zone.distance(this);
		}
		else if (zone instanceof ZoneLookup) {
			return ((ZoneLookup) zone).transformToPolygon().distance(this);
		}
		else if (zone instanceof ZoneRoute) {
			if (this.intersection(zone) instanceof ZoneNull) {
				//c'est le minimum des distances des points de route A a la route B
				//et des distances des points de route B a route A
				double dist_mini=zone.distance(new ZonePoint(this.getPointsList().get(0)));
				double temp_dist;
				for (int i=1;i<this.getPointsList().size();i++) {
					temp_dist =zone.distance(new ZonePoint(this.getPointsList().get(i)));
					if (temp_dist<dist_mini ) {
						dist_mini=temp_dist;
					}
				}
				
				for (int i=0;i<((ZoneRoute)zone).getPointsList().size();i++) {
					temp_dist =this.distance(new ZonePoint(((ZoneRoute)zone).getPointsList().get(i)));
					if (temp_dist<dist_mini ) {
						dist_mini=temp_dist;
					}
				}
				return dist_mini;
			}
			else {
				return 0;
			}
		}
		else if (zone instanceof ZonePolygon) {
			return zone.distance(this);
		}
		else if (zone instanceof ZoneList) {
			return zone.distance(this);
		}
		return 0;
	}
	
	public String toString () {
		String result = "ZoneRoute\n  ";
		result=result+this.getPointsList().size()+" points\n  ";
		result=result+this.getPointsList().toString();
		return result;
	}
}
