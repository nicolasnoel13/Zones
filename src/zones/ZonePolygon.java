package zones;

import utils.Coords;
import java.util.ArrayList;
import java.awt.geom.Path2D;

/**
 * ZonePolygon will represent a polygon by extending AZone
 * It is a convex polygon, its vertices are quoted in the mathematical order (counterclockwise)
 * starting from the lowest (and if needed the most to the left) vertex
 * NB : a concave polygon will be described as a ZoneList of convex polygons
 * 
 * CAUTION : ALWAYS CALL TRANSFORMPOLYGON while creating or modifying a polygon
 * 
 * @author Nicolas Noel
 */
public class ZonePolygon extends AZoneSimple {
	protected ArrayList<Coords> pointsList;

	
	public ZonePolygon() {
		super();
		pointsList = new ArrayList<Coords>();
		this.type = IConstantsZones.Zone_Type_Polygon;
	}
	
	public ZonePolygon(Coords center) {
		super(center);
		pointsList = new ArrayList<Coords>();
		this.type = IConstantsZones.Zone_Type_Polygon;
	}

	public ZonePolygon(ArrayList<Coords> pointsList) {
		super();
		if (pointsList.size() > 2) {
			this.pointsList = pointsList;
		} else {
			System.out
					.println("ERROR in ZonePolygon Constructor : Cannot create polygon with less than 3 points");
		}
		this.type = IConstantsZones.Zone_Type_Polygon;
	}
	
	public ZonePolygon(ArrayList<Coords> pointsList, Coords center) {
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
		if (zone instanceof ZoneNull) {
			return zone;
		} else if (zone instanceof ZonePoint) {
			if (this.belongs(((ZonePoint) zone).getCenter())) {
				return zone;
			}
			return new ZoneNull();
		} else if (zone instanceof ZoneLookup) {
			Coords A = ((ZoneLookup) zone).getCoordsA();
			Coords C = ((ZoneLookup) zone).getCoordsB();
			Coords B = new Coords(A.getLat(), C.getLon());
			Coords D = new Coords(C.getLat(), A.getLon());
			if (this.belongs(A) && this.belongs(B) && this.belongs(C)
					&& this.belongs(D)) {
				// if 4 points of ZoneLookup inside the polygon then return the
				// ZoneLookup
				return zone;
			} else {
				// else create a polygon to represent the ZoneLookup and call
				// intersectino with polygon method
				return this.intersection(((ZoneLookup) zone).transformToPolygon());
			}
		} else if (zone instanceof ZoneRoute) {
			ZoneList list = new ZoneList();
			boolean inside_A = false;
			boolean inside_B = false;
			Coords pointA = null;
			Coords pointB = null;
			ArrayList<Coords> return_list = new ArrayList<Coords>();

			for (int i = 0; i < ((ZoneRoute) zone).getPointsList().size(); i++) {
				if (i == 0) {
					pointA = ((ZoneRoute) zone).getPointsList().get(0);
					inside_A = this.belongs(pointA);
				} else {
					pointA = pointB;
					inside_A = inside_B;
				}
				if (i == ((ZoneRoute) zone).getPointsList().size() - 1) {

				} else {
					pointB = ((ZoneRoute) zone).getPointsList().get(i + 1);
					inside_B = this.belongs(pointB);

					if (!inside_A) {
						if (inside_B) {
							return_list.add(segment_inter(pointA, pointB));
						}
					} else {
						if (inside_B) {
							return_list.add(pointA);
						} else {
							return_list.add(pointA);
							return_list.add(segment_inter(pointA, pointB));
							// We were inside the circle and just got out
							list.addZone(new ZoneRoute(return_list));
							return_list = new ArrayList<Coords>();
						}
					}
				}
			}

			if (list.size() == 0) {
				// ZoneNull
				return new ZoneNull();
			} else if (list.size() == 1) {
				ZoneRoute zoneRoute = (ZoneRoute) list.head();
				if (zoneRoute.getPointsList().size() == 1) {
					// A ZonePoint, the only point was in the only ZoneRoute in
					// the ZoneList
					return new ZonePoint(zoneRoute.getPointsList().get(0));
				} else {
					// A ZoneRoute
					return zoneRoute;
				}
			} else {
				// ZoneList of ZoneRoads
				return list;
			}
		} else if (zone instanceof ZonePolygon) {
			System.out.println("intersection called between 2 polygons : caller"+this.toString()+"\nargument : "+zone.toString());
			
			if (this.containsPolygon((ZonePolygon)zone)) {
				System.out.println("ZonePoly.intersection : a polygon was contained by the other");
				return zone;
			}
			if (((ZonePolygon)zone).containsPolygon(this)) {
				System.out.println("ZonePoly.intersection : a polygon was contained by the other");
				return this;
			}
			
			ArrayList<Coords> listA = new ArrayList<Coords>();
			for (Coords merciAntho : this.getPointsList()){
				listA.add(merciAntho);
			}
			ZonePolygon newThis = new ZonePolygon (listA);
					
			ArrayList<Coords> listB = new ArrayList<Coords>();
			for (Coords mercibcpAntho : ((ZonePolygon) zone).getPointsList()){
				listB.add(mercibcpAntho);
			}
			
			ZonePolygon newZone = new ZonePolygon (listB);
			
			ArrayList<Coords> listInter = new ArrayList<Coords>();

			// Enrichir les listA et B des points d'inter
			Coords pointA, pointB, pointC, pointD, inter;
			int iPlusOne, jPlusOne = 0;
			boolean one_insertion = false;

			for (int i = 0; i < listA.size(); i++) {
				if (i == listA.size() - 1) {
					iPlusOne = 0;
				} else {
					iPlusOne = i + 1;
				}
				for (int j = 0; j < listB.size(); j++) {
					if (j == listB.size() - 1) {
						jPlusOne = 0;
					} else {
						jPlusOne = j + 1;
					}
					pointA = listA.get(i);
					pointB = listA.get(iPlusOne);
					pointC = listB.get(j);
					pointD = listB.get(jPlusOne);

					inter = segmentsIntersection(pointA, pointB, pointC,
							pointD);

					if (inter != null
							&& point_in_segment(inter, pointA, pointB)
							&& point_in_segment(inter, pointC, pointD)) {
						boolean contained = false;
						for (int k = 0; k < listInter.size(); k++) {
							if (listInter.get(k).equals(inter)) {
								contained = true;
								break;
							}
						}
						if (!contained) {
							listA.add(i + 1, inter);
							listB.add(j + 1, inter);
							listInter.add(inter);
							one_insertion=true;
						}
					}
				}
			}

			// algo qui vire les points externes des deux listes
			ArrayList<Coords> listACopy = new ArrayList<Coords>();
			for (Coords lil : listA) {
				listACopy.add(lil);
			}
			ArrayList<Coords> listBCopy = new ArrayList<Coords>();
			for (Coords lul : listB) {
				listBCopy.add(lul);
			}
			
			for (Coords coords : listA) {
				if ((!newZone.belongs(coords))&&(!listInter.contains(coords))) {
					listACopy.remove(coords);
				}
			}

			for (Coords coords : listB) {
				if ((!newThis.belongs(coords))&&(!listInter.contains(coords))) {
					listBCopy.remove(coords);
				}
			}

			// algo qui fusionne les 2 listes

			if (!one_insertion) {
				System.out.println("ZonePolygon.intersection with polygon, no intersection decteded");
				return new ZoneNull();
			}
			
			int indexA = 0;
			int indexB = 0;
			Coords start = listACopy.get(0);
			Coords currentA, currentB;

			ArrayList<Coords> listResult = new ArrayList<Coords>(listInter);

			while (!listInter.contains(start)) {
				indexA++;
				start = listACopy.get(indexA);
			}

			indexB = listBCopy.indexOf(start);
			int start_indexB = indexB;
			int start_indexA = indexA;

			// On est sur un point d'intersection, le meme pour les 2 listes
			// Ce point d'intersection est le suivant
			currentA = listACopy.get(indexA);
			// on place donc aussi l'index au bon point dans la liste de
			// resultats
			int indexResult = listResult.indexOf(currentA);

			// Avant de commencer, on se deplace dans les deux listes en avant
			// de ce point d'intersection
			indexA = (indexA + 1) % listACopy.size();
			indexB = (indexB + 1) % listBCopy.size();
			indexResult = (indexResult + 1) % listResult.size();

			do {
				currentA = listACopy.get(indexA);
				currentB = listBCopy.get(indexB);

				if (listInter.contains(currentA)) {
					if (listInter.contains(currentB)) {
						indexA = (indexA + 1) % listACopy.size();
						indexB = (indexB + 1) % listBCopy.size();
						indexResult = (indexResult + 1) % listResult.size();
					} else {
						listResult.add(indexResult, currentB);
						indexB = (indexB + 1) % listBCopy.size();
						indexResult = (indexResult + 1) % listResult.size();
					}
				} else {
					if (listInter.contains(currentB)) {
						listResult.add(indexResult, currentA);
						indexA = (indexA + 1) % listACopy.size();
						indexResult = (indexResult + 1) % listResult.size();
					}
				}

			} while ((indexA != start_indexA) || (indexB != start_indexB));

			// retour du polygone forme par la liste fusion
			if (listResult.size() > 2) {
				System.out.println("ZonePoly.inter : listResult = "+listResult.toString());
				ZonePolygon polygonReturn = new ZonePolygon(listResult);
				polygonReturn.makeMeConvex();
				polygonReturn.reOrder();
				System.out.println("ZonePoly.inter : result = "+polygonReturn.toString());
				return polygonReturn;
			} else {
				System.out.println("ERROR in ZonePolygon.intersection : listResult contains less thant 3 points");
				return new ZoneNull();
			}

		} else if (zone instanceof ZoneCircle) {
			// If it is a ZoneCircle, call its method
			return zone.intersection(this);
		} else if (zone instanceof ZoneList) {
			// If it is a ZoneList, call its method
			return zone.intersection(this);
		} else {
			return null;
		}
	}

	@Override
	public boolean equal(AZone zone) {
		boolean result = false;
		if (zone instanceof ZonePolygon) {
			if (this.pointsList.size() == ((ZonePolygon) zone).pointsList
					.size()) {
				result = true;
				for (int i = 0; i < this.pointsList.size(); i++) {
					result = result
							&& (this.pointsList.get(i)
									.equals(((ZonePolygon) zone).pointsList
											.get(i)));
				}
			}
		}
		return result;
	}

	@Override
	public boolean belongs(Coords coords) {
		boolean result = true;
		int j;
		for (int i = 0; i < this.pointsList.size(); i++) {
			j = i + 1;
			if (i == this.pointsList.size() - 1) {
				j = 0;
			}
			result = result
					&& isLeftOf(coords, this.pointsList.get(i),
							this.pointsList.get(j));
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
				int j=0;
				for (int i=0;i<this.getPointsList().size();i++) {
					if (i==this.getPointsList().size()-1) {
						j=0;
					}
					else {
						j=i+1;
					}
					A= new Coords (this.getPointsList().get(i));
					B= new Coords (this.getPointsList().get(j));
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
				//c'est le minimum des distances des points de route aux segments de polygone
				//et des distances des segments de route a points de polygone
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
			if (this.intersection(zone) instanceof ZoneNull) {
				//c'est le minimum des distances des points de pA aux segments de pB
				//et des distances des segments de pA aux points de polygoneB
				double dist_mini=zone.distance(new ZonePoint(this.getPointsList().get(0)));
				double temp_dist;
				for (int i=1;i<this.getPointsList().size();i++) {
					temp_dist =zone.distance(new ZonePoint(this.getPointsList().get(i)));
					if (temp_dist<dist_mini ) {
						dist_mini=temp_dist;
					}
				}
				
				for (int i=0;i<((ZonePolygon)zone).getPointsList().size();i++) {
					temp_dist =this.distance(new ZonePoint(((ZonePolygon)zone).getPointsList().get(i)));
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
		else if (zone instanceof ZoneList) {
			return zone.distance(this);
		}
		return 0;
	}
	
	/**
	 * Calculates the point of intersection between a segment and the calling
	 * polygon (assuming the intesection exists)
	 * 
	 * @param A
	 * @param B
	 * @return
	 */
	public Coords segment_inter(Coords A, Coords B) {
		Coords inter, C, D, result = null;
		int j = 1;
		for (int i = 0; (i < this.getPointsList().size()) && (result==null); i++) {
			if (i == this.getPointsList().size() - 1) {
				j = 0;
			} else {
				j = i + 1;
			}
			C = this.getPointsList().get(i);
			D = this.getPointsList().get(j);
			
			try {
				// Calculate the intersection point between lines
				inter = AZone.segmentsIntersection(A, B, C, D);
				// if the point belongs to the segment of the polygon then it is the one
				if ((inter !=null) && (AZone.point_in_segment(inter, C, D)) && (AZone.point_in_segment(inter, A, B))) {
					result = new Coords(inter);
				}
			}
			catch (Exception e) {
				
			}
		}
		return result;
	}

	/**
	 * This method will extract the convex hull from a polygon
	 * only considering it as a cloud of points
	 */
	public void makeMeConvex() {

		// Extract the convex hull ( points listed counterclockwise)
		Coords pt_down = new Coords(this.getPointsList().get(0));
		Coords pt_up = new Coords(this.getPointsList().get(0));
		Coords pt_right = new Coords(this.getPointsList().get(0));
		Coords pt_left = new Coords(this.getPointsList().get(0));
		Coords temp;
		ArrayList<Coords> listConvex = new ArrayList<Coords>();

		for (int i = 1; i < this.getPointsList().size(); i++) {
			temp = new Coords(this.getPointsList().get(i));
			if (temp.getLat() < pt_down.getLat()) {
				pt_down = new Coords(temp);
			}
			if (temp.getLat() > pt_up.getLat()) {
				pt_up = new Coords(temp);
			}
			if (temp.getLon() > pt_right.getLon()) {
				pt_right = new Coords(temp);
			}
			if (temp.getLon() < pt_left.getLon()) {
				pt_left = new Coords(temp);
			}
		}

		listConvex.add(pt_up);

		if (!pt_up.equals(pt_left)) {
			listConvex.add(pt_left);
		}
		if (!pt_left.equals(pt_down)) {
			listConvex.add(pt_down);
		}
		if (!pt_down.equals(pt_right) && !pt_right.equals(pt_up)) {
			listConvex.add(pt_right);
		}

		Coords A;
		Coords B;
		Coords C;
		double temp_distance = 0;
		Coords temp_point =null;
		double distance;
		boolean insertion; // Dans la boucle for sur j, sur une iteration de j
							// (c'est � dire pour un segment s�lectionn�)
							// on a trouv� un point � ins�rer donc il faudra
							// l'ins�rer avant la fin de la boucle for sur j
		boolean one_insertion = true; // Il y a eu une insertion dans cette
										// iteration de la boucle while donc on
										// recommence la boucle while

		int jPlusOne;

		while (one_insertion) // Tant qu'une insertion a �t� faite � l'it�ration
								// pr�c�dente on recommence
		{
			one_insertion = false;
			for (int j = 0; j < listConvex.size(); j++) {
				if (j == listConvex.size() - 1) {
					jPlusOne = 0;
				} else {
					jPlusOne = j + 1;
				}
				A = new Coords(listConvex.get(j));
				B = new Coords(listConvex.get(jPlusOne));

				temp_distance = 0;
				insertion = false;
				for (int i = 0; i < this.getPointsList().size(); i++) {
					C = new Coords(this.getPointsList().get(i));

					if (!AZone.isLeftOf(C, A, B)){ //If C is right of AB
						distance=distProjOrthSquared(C,A,B);
						if (distance > temp_distance) {
							insertion = true;
							temp_distance = distance;
							temp_point = new Coords(this.getPointsList().get(i));
						}
					}
				}

				if (insertion) {
					listConvex.add(jPlusOne, temp_point); // Rajoute le point
														// associ� a temp_point
														// ds l'env_convexe au
														// bon endroit
					one_insertion = true;
				}
			}

		} // fin du while
		
		
		this.setPointsList(listConvex);
	}
	
	/**
	 * If crossed polygon returns the convex hull of the cloud of points
	 * If convex just rearrange the points in the good order
	 * if concave, recursively transform it in a list of convex polygons listed in the right order
	 */
	public AZone transformPolygon () {
		System.out.println("calling transformPolygon");
		if (this.isCrossedPolygon()) {
			System.out.println("polygon is crossed");
			this.makeMeConvex(); //le met dans le sens trigo
			this.reOrder();
			return this;
		}
		else {
			System.out.println("polygon is not crossed");
			//changer le sens de description du polygone si necessaire (counterclockwise)
			this.rotate();
			if (this.isConvex()) {
				this.reOrder();
				return this;
			}
			else {
				ZoneList listReturn = new ZoneList();
				this.listConvexPolygons(listReturn);
				System.out.println("CAUTION ! The polygon was concave and has been transformed into a ZoneList");
				listReturn.setCenter(this.getCenter());
				for (AZoneSimple zone :listReturn.getZonesList() ) {
					zone.setCenter(this.getCenter());
					((ZonePolygon)zone).reOrder();
				}
				return listReturn;
			}
		}
	}
	
	/**
	 * Returns true if the polygon is crossed
	 * @return
	 */
	public boolean isCrossedPolygon() {
		
		Coords pointA, pointB, pointC, pointD, inter;
		int iPlusOne, jPlusOne = 0;
		int size=this.getPointsList().size();
		
		for (int i=0;i<size;i++) {
			if (i == size - 1) {
				iPlusOne = 0;
			} else {
				iPlusOne = i + 1;
			}
			for (int j=0;j<size;j++) {
				if (j == size - 1) {
					jPlusOne = 0;
				} else {
					jPlusOne = j + 1;
				}
				if (i!=j && jPlusOne!=i && iPlusOne!=j) {
					pointA = this.getPointsList().get(i);
					pointB = this.getPointsList().get(iPlusOne);
					pointC = this.getPointsList().get(j);
					pointD = this.getPointsList().get(jPlusOne);
					
					inter = segmentsIntersection(pointA, pointB, pointC,
							pointD);
					if (inter != null
							&& point_in_segment(inter, pointA, pointB)
							&& point_in_segment(inter, pointC, pointD)) {
						return true;
					}
				}
				
			}
		}
		
		return false;
	}

	/**
	 * Assuming the polygon is not crossed
	 * This method returns true if the polygon is convex
	 * @return
	 */
	public boolean isConvex () {
		Coords A, B,C;
		int size=this.getPointsList().size();
		
		A=this.getPointsList().get(0);
		B=this.getPointsList().get((1)%size);
		C=this.getPointsList().get((2)%size);
		
		boolean sense = isLeftOf(C, A, B);
		
		for (int i=0;i<size;i++) {
			A=this.getPointsList().get(i);
			B=this.getPointsList().get((i+1)%size);
			C=this.getPointsList().get((i+2)%size);
			if (sense!=isLeftOf(C, A, B)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Assuming the polygon is convex
	 * This method will reorder the points to start with the lowest/most to the left
	 * @return
	 */
	public void reOrder () {
		// translate the entire list of point to make sure we start with the
		// lowest/most to the left point
		Coords lowest = this.getPointsList().get(0);
		int j = 0;

		for (int i = 1; i < this.getPointsList().size(); i++) {
			if (this.getPointsList().get(i).getLat() < lowest.getLat()) {
				lowest = this.getPointsList().get(i);
				j = i;
			} else if (Math.abs(this.getPointsList().get(i).getLat()
					- lowest.getLat()) < epsilonTest) {
				if (this.getPointsList().get(i).getLon() < lowest.getLon()) {
					lowest = this.getPointsList().get(i);
					j = i;
				}
			}
		}

		Coords temp2;

		for (int k = 0; k < j; k++) {
			temp2 = new Coords(this.getPointsList().get(0));
			this.getPointsList().add(temp2);
			this.getPointsList().remove(0);
		}
	}
	
	/**
	 * Assuming the polygon is not crossed but is concave
	 * This method will recursively transform it into a list of concave polygons
	 * @return
	 */
	public void listConvexPolygons (ZoneList list) {
		if (this.isConvex()) {
			list.addZone(this);
		}
		else {
			ZonePolygon returnPolygonA =new ZonePolygon();
			ZonePolygon returnPolygonB =new ZonePolygon();
			this.divideConcavePolygon(returnPolygonA,returnPolygonB);
			returnPolygonA.reOrder();
			returnPolygonB.reOrder();
			returnPolygonA.listConvexPolygons(list);
			returnPolygonB.listConvexPolygons(list);
		}
	}
	
	public void divideConcavePolygon (ZonePolygon returnPolygonA,ZonePolygon returnPolygonB) {
		Coords A, B,C,D;
		int iPlusOne, jPlusOne,kPlusOne= 0;
		int size=this.getPointsList().size();
		int i_best =-1;
		int j_best =-1;
		int to_minimize=-1;
		
		for (int i=0;i<size;i++) {
			if (i == size - 1) {
				iPlusOne = 0;
			} else {
				iPlusOne = i + 1;
			}
			for (int j=0;j<size;j++) {
				if (j == size - 1) {
					jPlusOne = 0;
				} else {
					jPlusOne = j + 1;
				}
				if (i!=j && jPlusOne!=i && iPlusOne!=j) {
					A = this.getPointsList().get(i);
					B = this.getPointsList().get(j);
					//Bon voila AB est une diagonale candidate
					//Reste a voir is elle reste a l'intereieur du polygone ..... intersecte le polygone ou pas ... ne suffit pas
					boolean intersected =false;
					for (int k=0;k<size;k++) {
						if (k == size - 1) {
							kPlusOne = 0;
						} else {
							kPlusOne = k + 1;
						}
						if (k!=i && k!=j && kPlusOne!=i && kPlusOne!=j) {
							C = this.getPointsList().get(k);
							D = this.getPointsList().get(kPlusOne);
							Coords inter = segmentsIntersection(A, B, C, D);
							if ((inter!=null)&&(point_in_segment(inter,C,D))&&(point_in_segment(inter,A,B))) {
								intersected =true;
							}
						}
					}
					if (!intersected) {
						//recherche si exterieur ou interieur
						if(this.containsPointAwt(middle(A,B))) {
							int to_minimize_current = Math.abs (size-2*Math.abs(i-j));
							if ((to_minimize_current<to_minimize) || (to_minimize==-1)) {
								i_best = i;
								j_best = j;
								to_minimize=to_minimize_current;
							}
						}
					}
				}
			}
		}
		if (i_best!=-1 && j_best!=-1) {
			int min = Math.min (i_best,j_best);
			int max = Math.max (i_best,j_best);
			
			ArrayList<Coords> pointsListA = new ArrayList<Coords>();
			ArrayList<Coords> pointsListB = new ArrayList<Coords>();
			for (int it=min;it<=max;it++) {
				pointsListA.add(this.getPointsList().get(it));
			}
			for (int it=max;it<=min+size;it++) {
				pointsListB.add(this.getPointsList().get(it%size));
			}
			returnPolygonA.setPointsList(pointsListA);
			returnPolygonB.setPointsList(pointsListB);
			
		}
		else {
			System.out.println("ERROR in divideConcavePolygon : couldnt find a diagonal");
		}
	}
	
	public void rotate () {
		Coords A, B;
		int iPlusOne=0;
		int size=this.getPointsList().size();
		double sum=0;
		for (int i=0;i<size;i++) {
			if (i == size - 1) {
				iPlusOne = 0;
			} else {
				iPlusOne = i + 1;
			}
			A= this.getPointsList().get(i);
			B= this.getPointsList().get(iPlusOne);
			sum=sum+(B.getLon()-A.getLon())*(B.getLat()+A.getLat());
		}
		if (sum>0) {
			ArrayList<Coords> tempList = new ArrayList<Coords> (this.getPointsList());
			for(int i=0;i<size;i++) {
				this.getPointsList().set(i, tempList.get(size-i-1));
			}
		}
	}
	
	public boolean containsPointAwt (Coords point) {
		double x=point.getLon();
		double y=point.getLat();
		int size = this.getPointsList().size();
		double[] xpoints = new double[size];
		double[] ypoints = new double[size];
		
		for (int i=0;i<size;i++) {
			xpoints[i]=this.getPointsList().get(i).getLon();
			ypoints[i]=this.getPointsList().get(i).getLat();
		}

		Path2D path = new Path2D.Double();

		path.moveTo(xpoints[0], ypoints[0]);
		for(int i = 1; i < xpoints.length; ++i) {
		   path.lineTo(xpoints[i], ypoints[i]);
		}
		path.closePath();
		
		return path.contains(x,y);
	}
	
	/**
	 * This method returns true if the polygon in argument is contained in the calling polygon
	 * It is used in intersection
	 * @param poly
	 * @return
	 */
	public boolean containsPolygon (ZonePolygon poly) {
		boolean result = true;
		for (Coords point : poly.getPointsList()) {
			result = result && (this.containsPointAwt(point));
		}
		return result;
	}
	
	public String toString () {
		String result = "ZonePolygon\n  ";
		result=result+this.getPointsList().size()+" points\n  ";
		result=result+this.getPointsList().toString();
		return result;
	}
}
