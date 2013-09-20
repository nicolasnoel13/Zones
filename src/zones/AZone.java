package zones;

import utils.Coords;

import java.util.ArrayList;

/**
 * This abstract class represents all possible zones
 * It can either be a ZoneList which is an arraylist of AzoneSimple or an elementary AZoneSimple :
 * @see ZoneList
 * @see ZonePoint
 * @see ZoneNull
 * @see ZonePolygon
 * @see ZoneLookup
 * @see ZoneCircle
 * @author Nicolas Noel
 */

public abstract class AZone {
	
	protected Coords center;
	protected String type;
	protected long id;

	public static final double epsilonTest = 0.0005; //tolerance for tests (for Ifs)
	public static final double epsilonCalc = 0.000001; // tolerance for calculations (for Whiles)
	
	public AZone() {
	}
	
	public AZone(Coords center) {
		this.center = center;
	}
	
	public Coords getCenter () {
		return this.center;
	}

	public void setCenter(Coords center) {
		this.center = center;
		}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * This abstract method returns the distance between the AZone zone and the calling instance.
	 * Returns 0 if the zones intersect
	 * @param zone
	 * @return
	 */
	public abstract double distance(AZone zone);

	/**
	 * This abstract method returns the intersection zone between the AZone zone and the calling instance.
	 * Returns an instance of Zone_null if there is no intersection
	 * @param zone
	 * @return
	 */
	public abstract AZone intersection(AZone zone);
	
	/**
	 * This method returns a boolean : true if the two zones are equal
	 * @param zone
	 * @return
	 */

	public abstract boolean equal(AZone zone);
	
	/**
	 * This method returns a boolean : true if the coords belong to the calling zone
	 * @param coords
	 * @return
	 */
	public abstract boolean belongs(Coords coords);
	
	
	/**This method returns true if the point I belongs to the line (AB) (3D exact spherical !)
	 * 
	 */
	public static boolean point_in_line(Coords I,Coords A,Coords B) {
		double LonA=A.getLon()*Math.PI/180;
		double LatA=A.getLat()*Math.PI/180;
		double LonB=B.getLon()*Math.PI/180;
		double LatB=B.getLat()*Math.PI/180;
		double LonI=I.getLon()*Math.PI/180;
		double LatI=I.getLat()*Math.PI/180;
		
		//Test I==A ou I==B
		if (I.equals(A) || I.equals(B)) {
			//System.out.println("AZone.pointInLine returned true because I=A or I=B");
			return true;
		}
		
		//if (AB) is vertical
		if (LonA==LonB) {
			return (LonA==LonI);
		}
		//else
		else {
			double lambda = (Math.sin(LonB)*Math.cos(LatB)*Math.sin(LatI)-Math.sin(LatB)*Math.sin(LonI)*Math.cos(LatI))/(Math.sin(LonB)*Math.cos(LatB)*Math.sin(LatA)-Math.sin(LatB)*Math.sin(LonA)*Math.cos(LatA));
			//System.out.println("lambda = "+lambda);
			
			boolean testA = Math.abs((Math.sin(LatB)*Math.cos(LonI)*Math.cos(LatI)-Math.sin(LatI)*Math.cos(LonB)*Math.cos(LatB))/(Math.sin(LatB)*Math.cos(LonA)*Math.cos(LatA)-Math.sin(LatA)*Math.cos(LonB)*Math.cos(LatB)) - lambda)<epsilonTest;
			//System.out.println(Math.abs((Math.sin(LatB)*Math.cos(LonI)*Math.cos(LatI)-Math.sin(LatI)*Math.cos(LonB)*Math.cos(LatB))/(Math.sin(LatB)*Math.cos(LonA)*Math.cos(LatA)-Math.sin(LatA)*Math.cos(LonB)*Math.cos(LatB)) - lambda));
			
			boolean testB = Math.abs((Math.cos(LonB)*Math.cos(LatB)*Math.sin(LonI)*Math.cos(LatI)-Math.sin(LonB)*Math.cos(LatB)*Math.cos(LonI)*Math.cos(LatI))/(Math.cos(LonB)*Math.cos(LatB)*Math.sin(LonA)*Math.cos(LatA)-Math.sin(LonB)*Math.cos(LatB)*Math.cos(LonA)*Math.cos(LatA)) - lambda)<epsilonTest;
			//System.out.println("testA : "+testA);
			//System.out.println("testB : "+testB);
			return testA&&testB;
		}
	}
	
	
	
	
	/**
	 * This method returns true if the point I belongs to the segment [AB] (assuming that I belongs to (AB))
	 * @param inter
	 * @param A
	 * @param B
	 * @return
	 */
	public static boolean point_in_segment (Coords inter,Coords A,Coords B) {
		return (Math.abs(inter.distance(A)+inter.distance(B)-A.distance(B))<epsilonTest);
	}
	
	/**
	 * This method returns the point of intersection of segments [AB] and [CD] assuming it exists
	 * @param A
	 * @param B
	 * @param C
	 * @param D
	 * @return
	 */
	//TESTED OK
	public static Coords segmentsIntersection (Coords A,Coords B,Coords C,Coords D) {
		if (A.equals(C) || A.equals(D)) {
			return A;
		}
		else if (B.equals(C) || B.equals(D)) {
			return B;
		}
		else {
			Coords M1,M2,M3,N1,N2,N3,Mleft,Mright,Nleft,Nright;
			M1=AZone.middle(A,B);
			Mleft = new Coords(A);
			Mright = new Coords(B);
			N1=AZone.middle(C,D);
			Nleft = new Coords(C);
			Nright = new Coords(D);
			int count=0;
			while ((M1.distance(N1)>epsilonCalc)&&count<1500) {
				count++;
				M2 = AZone.middle(M1, Mleft);
				M3 = AZone.middle(M1, Mright);
				N2 = AZone.middle(N1, Nleft);
				N3 = AZone.middle(N1, Nright);
				
				if (isLeftOf(N1, A, B)!=isLeftOf(Nleft, A, B)) { //N1Nleft intersects AB so N2 is the next choice
					Nright=new Coords(N1);  N1= new Coords(N2);
				}
				else {//N1Nright intersects AB so N3 is the next choice
					Nleft=new Coords(N1); N1= new Coords(N3);
				}
				
				if (isLeftOf(M1, C, D)!=isLeftOf(Mleft, C, D)) { //M1Mleft intersects CD so M2 is the next choice
					Mright=new Coords(M1); M1= new Coords(M2);
				}
				else {//M1Mright intersects CD so M3 is the next choice
					Mleft=new Coords(M1); M1= new Coords(M3);
				}
				
			}
			if (count==1500) {
				//System.out.println("segmentsIntersection failed");
				return null;
			}
			else {
				//System.out.println("segmentsIntersection completed succesfully in "+count+" iterations");
				return M1;
			}
		}
		
	}
	
	/**
	 * Returns true if point M is left of the line (AB) or if it belongs to the line (3d spherical exact)
	 * TESTED OK
	 * @author Nicolas Noel
	 */
	public static boolean isLeftOf (Coords M,Coords A,Coords B) {
		double LonA=A.getLon()*Math.PI/180;
		double LatA=A.getLat()*Math.PI/180;
		double LonB=B.getLon()*Math.PI/180;
		double LatB=B.getLat()*Math.PI/180;
		double LonM=M.getLon()*Math.PI/180;
		double LatM=M.getLat()*Math.PI/180;
		
		//Test M==A ou M==B
		if (M.equals(A) ||M.equals(B)) {
			//System.out.println("AZone.isLeftOf returned true because M=A or M=B");
			return true;
		}
		else {
			double xM = Math.cos(LonM)*Math.cos(LatM);
			double yM = Math.sin(LonM)*Math.cos(LatM);
			double zM = Math.sin(LatM);
			
			//NV is the normal vector to OAB
			double xNV= Math.sin(LonA)*Math.cos(LatA)*Math.sin(LatB)-Math.sin(LonB)*Math.cos(LatB)*Math.sin(LatA);
			double yNV= Math.cos(LonB)*Math.cos(LatB)*Math.sin(LatA)-Math.cos(LonA)*Math.cos(LatA)*Math.sin(LatB);
			double zNV= Math.cos(LonA)*Math.cos(LatA)*Math.sin(LonB)*Math.cos(LatB)-Math.sin(LonA)*Math.cos(LatA)*Math.cos(LonB)*Math.cos(LatB);
			
			return ((xM*xNV+yM*yNV+zM*zNV)>=0);
		}
	}

	/**
	 * Returns true if C is between the normals to (AB) passing thtough A and B
	 * @param C
	 * @param A
	 * @param B
	 * @return
	 */
	public static boolean isBetweenNormals (Coords C, Coords A, Coords B) {
		
		CartesianCoords OA = new CartesianCoords(A);
		CartesianCoords OB = new CartesianCoords(B);
		CartesianCoords OC = new CartesianCoords(C);
		
		CartesianCoords NA = OB.vectorialProduct(OA);
		
		NA=NA.vectorialProduct(OA);
		
		CartesianCoords NB = OA.vectorialProduct(OB);
		NB=NB.vectorialProduct(OB);
		
		double OCNA = OC.scalarProduct(NA);
		double OCNB = OC.scalarProduct(NB);
		
		return ((OCNA<=0)&&(OCNB<=0));
	}
	
	/**
	 * Calculate the orthognal distance from C to segment [AB] squared
	 * @param C
	 * @param A
	 * @param B
	 * @return
	 */
	public static double distProjOrthSquared (Coords C, Coords A, Coords B) {
		return distProjOrth(C, A, B)*distProjOrth(C, A, B);
	}
	
	public static double distProjOrth(Coords C, Coords A, Coords B) {
		if (A.equals(C) || B.equals(C)) {
			return 0;
		}
		
		if (!isBetweenNormals(C, A, B)) {
			System.out.println("ERROR in AZone.distProjOrth : C is not between A and B normals");
			return Double.MAX_VALUE;
		}
		
		else {
			Coords M1,M2,M3,Mleft,Mright;
			M1=AZone.middle(A,B);
			Mleft = new Coords(A);
			Mright = new Coords(B);
		
			int count=0;
			while ((Mleft.distance(Mright)>epsilonCalc)&&count<1500) {
				count++;
				M2 = AZone.middle(M1, Mleft);
				M3 = AZone.middle(M1, Mright);
				
				if (isBetweenNormals(C,Mleft,M1)) {
					Mright=new Coords(M1); M1= new Coords(M2);
				}
				else {
					Mleft=new Coords(M1); M1= new Coords(M3);
				}
				
			}
			if (count==1500) {
				return Double.MAX_VALUE;
			}
			else {
				return C.distance(Mleft);
			}
		}
		
	}
	
	/**
	 * This method returns the middle point between A and B considering orthodromic distance (3D spherical exact)
	 * TESTED : works, the point belongs to the line and to the segment
	 * @param A
	 * @param B
	 * @return
	 */
	public static Coords middle (Coords A, Coords B) {
		double LonA=A.getLon()*Math.PI/180;
		double LatA=A.getLat()*Math.PI/180;
		double LonB=B.getLon()*Math.PI/180;
		double LatB=B.getLat()*Math.PI/180;
		double xA = Math.cos(LonA)*Math.cos(LatA);
		double yA = Math.sin(LonA)*Math.cos(LatA);
		double zA = Math.sin(LatA);
		double xB = Math.cos(LonB)*Math.cos(LatB);
		double yB = Math.sin(LonB)*Math.cos(LatB);
		double zB = Math.sin(LatB);
		double xH = xA+xB;
		double yH = yA+yB;
		double zH = zA+zB;
		double NormOA = Math.sqrt(xA*xA+yA*yA+zA*zA);
		double NormOAPlusOB = Math.sqrt(xH*xH+yH*yH+zH*zH);
		//double xM = NormOA*xH/NormOAPlusOB;
		double yM = NormOA*yH/NormOAPlusOB;
		double zM = NormOA*zH/NormOAPlusOB;
		double LatM = Math.asin(zM);
		double LonM = Math.asin(yM/(Math.cos(LatM)));
		Coords result = new Coords (LatM*180/Math.PI,LonM*180/Math.PI);
		//TEST
		/*
		System.out.println("Lat M : "+LatM*180/Math.PI);
		System.out.println("Lon M : "+LonM*180/Math.PI);
		System.out.println("difference between distances from M to A and M to B : "+(result.distance(A)-result.distance(B)));
		System.out.println("difference between distances from M to A + M to B and A to B: "+(result.distance(A)+result.distance(B)-A.distance(B)));
		*/
		return result;
	}
	
	/**
	 * This recursive method returns 'count' points on the segment to be able to draw them
	 * Before returning the list it is sorted in distance from A
	 * @param A
	 * @param B
	 * @param list
	 * @param count
	 */
	public static void drawSegment(Coords A, Coords B,ArrayList<Coords> list,int count) {
		if (count > 0) {
			Coords mid = middle(A,B);
			list.add(mid);
			count = count-1;
			drawSegment(A,mid,list,count/2);
			drawSegment(mid,B,list,count-(count/2));
		}
	}
	
	public static void sortList (Coords A,ArrayList<Coords> list) {
		//sort the list
		ArrayList<Coords> listTemp = new ArrayList<Coords>();
		int size = list.size();
		for (int i=0;i<size;i++) {
			double distMin=-1;
			int k=0;
			for (int j=0;j<list.size();j++) {
				if ((A.distance(list.get(j))<distMin)||(distMin==-1)) {
					k=j;
					distMin=A.distance(list.get(j));
				}
			}
			listTemp.add(new Coords (list.get(k)));
			list.remove(k);
		}		
		for (int i=0;i<listTemp.size();i++){
			list.add(listTemp.get(i));
		}
	}
	
	/**
	 * produit scalaire de BA et BC "en metres"
	 * produit scalaire 3d base sur la loxodromie (exact)
	 * @param A
	 * @param B
	 * @param C
	 * @return
	 */
	public static double pdtScalaire (Coords A, Coords B, Coords C) {
		double LonA=A.getLon()*Math.PI/180;
		double LatA=A.getLat()*Math.PI/180;
		double LonB=B.getLon()*Math.PI/180;
		double LatB=B.getLat()*Math.PI/180;
		double LonC=C.getLon()*Math.PI/180;
		double LatC=C.getLat()*Math.PI/180;
		double XBA =(Math.cos(LonA)*Math.cos(LatA)-Math.cos(LonB)*Math.cos(LatB))*1852;
		double XBC =(Math.cos(LonC)*Math.cos(LatC)-Math.cos(LonB)*Math.cos(LatB))*1852;
		double YBA =(Math.sin(LonA)*Math.cos(LatA)-Math.sin(LonB)*Math.cos(LatB))*1852;
		double YBC =(Math.sin(LonC)*Math.cos(LatC)-Math.sin(LonB)*Math.cos(LatB))*1852;
		double ZBA =(Math.sin(LatA)-Math.sin(LatB))*1852;
		double ZBC =(Math.sin(LatC)-Math.sin(LatB))*1852;
		return ((XBA*XBC)+(YBA*YBC)+(ZBA*ZBC));
	}
	
	
	/**
	 * angle en radians ABC compris entre 0 et +pi (entre vecteurs BA et BC) en valeur absolue
	 * @param A
	 * @param B
	 * @param C
	 * @return
	 */
	public static double angle (Coords A, Coords B, Coords C) {
		double LonA=A.getLon()*Math.PI/180;
		double LatA=A.getLat()*Math.PI/180;
		double LonB=B.getLon()*Math.PI/180;
		double LatB=B.getLat()*Math.PI/180;
		double LonC=C.getLon()*Math.PI/180;
		double LatC=C.getLat()*Math.PI/180;
		double XBA =(Math.cos(LonA)*Math.cos(LatA)-Math.cos(LonB)*Math.cos(LatB))*1852;
		double XBC =(Math.cos(LonC)*Math.cos(LatC)-Math.cos(LonB)*Math.cos(LatB))*1852;
		double YBA =(Math.sin(LonA)*Math.cos(LatA)-Math.sin(LonB)*Math.cos(LatB))*1852;
		double YBC =(Math.sin(LonC)*Math.cos(LatC)-Math.sin(LonB)*Math.cos(LatB))*1852;
		double ZBA =(Math.sin(LatA)-Math.sin(LatB))*1852;
		double ZBC =(Math.sin(LatC)-Math.sin(LatB))*1852;
		double pdtscal = ((XBA*XBC)+(YBA*YBC)+(ZBA*ZBC));
		double BA = Math.sqrt((XBA*XBA)+(YBA*YBA)+(ZBA*ZBA));
		double BC = Math.sqrt((XBC*XBC)+(YBC*YBC)+(ZBC*ZBC));
		
		return Math.acos(pdtscal/(BA*BC));
	}
	
	
	//FALSE 2D but ok because not critical (only used in ReroutingReloaded)
	public static Coords rotationVector (Coords A, double angle) {
		double x=A.getLon();
		double y=A.getLat();
		double c = Math.cos(angle);
		double s = Math.sin(angle);
		double xr = (x*c) - (y*s);
		double yr = (x*s) + (y*c);
		return new Coords(yr,xr);
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

}
