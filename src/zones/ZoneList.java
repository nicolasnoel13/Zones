package zones;

import utils.Coords;

import java.util.ArrayList;

/**
 * ZoneList is a type of AZone, it is an ArrayList of AzoneSimple
 * It should not contain any ZoneNull or possess less than two zones
 * 
 * @author Nicolas Noel
 * 
 */
public class ZoneList extends AZone {

	protected ArrayList<AZoneSimple> zonesList;

	public ZoneList() {
		this.zonesList = new ArrayList<AZoneSimple>();
		this.type = "ZoneList";
	}

	public ZoneList(ArrayList<AZoneSimple> list) {
		this.zonesList = new ArrayList<AZoneSimple>();

		for (AZoneSimple azone : list) {
			this.zonesList.add(azone);
		}
		this.type = "ZoneList";
	}

	/**
	 * Adding a new zone to the list avoiding doubles
	 * @param zone
	 */
	public void addZone(AZoneSimple zone) {
		boolean alreadyExists = false;
		for (AZoneSimple zoneSimple : this.getZonesList()) {
			if (zoneSimple.equal(zone)) {
				alreadyExists=true;
			}
		}
		if (!alreadyExists) {
			this.zonesList.add(zone);
		}
		else {
			System.out.println("WARNING in ZoneList.addZone : the zone already existed !");
		}
	}
	
	/**
	 * add the zones from argument in the calling zonelist avoiding doubles
	 * @param zone
	 */
	public void mergeZoneList(ZoneList zone) {
		for (AZoneSimple zoneSimple : zone.getZonesList()) {
			this.addZone(zoneSimple);
		}
	}

	public AZoneSimple getZone(int index) {
		return this.zonesList.get(index);
	}

	public int size() {
		return this.zonesList.size();
	}

	public ArrayList<AZoneSimple> getZonesList() {
		return this.zonesList;
	}

	public void setZonesList(ArrayList<AZoneSimple> zonesList) {
		this.zonesList = zonesList;
	}

	public boolean contains(AZoneSimple zone) {
		// returns true if zone is in the Zone_list, false if it is not in the
		// list
		boolean result = false;
		for (AZoneSimple zone_a : this.zonesList) {
			if (zone_a.equal(zone)) {
				result = true;
			}
		}
		return result;
	}

	public int rank_contains(AZoneSimple zone) {
		// returns the rank int the list if zone is in the Zone_list, -1 if it
		// is not in the list
		int result = -1;
		int i = 0;
		for (AZoneSimple zone_a : this.zonesList) {
			if (zone_a.equal(zone)) {
				result = i;
			}
			i = i + 1;
		}
		return result;
	}

	// returns the first element of the list
	public AZoneSimple head() {
		return this.zonesList.get(0);
	}

	// returns the other elements of the list
	public AZone tail() {
		ZoneList result = new ZoneList(this.zonesList);
		result.zonesList.remove(0);
		if (result.zonesList.size() == 1) {
			return result.zonesList.get(0);
		} else {
			return result;
		}
	}

	@Override
	public AZone intersection(AZone zone) {
		System.out.println("intersection called by a zonelist :"+this.toString());
		ArrayList<AZoneSimple> list_return = new ArrayList<AZoneSimple>();
		AZone inter;
		AZone result;

		if (!(zone instanceof ZoneList)) { // if argument is not a list (so it
											// is an elementary zone)
			System.out.println("the argument is not zonelist : "+zone.toString());
			for (AZoneSimple zone_a : this.zonesList) {
				System.out.println("for on zonelist : element : "+zone_a.toString());
				inter = zone_a.intersection(zone); // intersectionof 2
													// elementary zones
				System.out.println("Zone simple ? :"+(inter instanceof AZoneSimple));
				System.out.println("Zone nulle ? :"+(inter.getClass().equals(ZoneNull.class)));
				if ((inter instanceof AZoneSimple)&&(!inter.getClass().equals(ZoneNull.class))) {
					System.out.println("\nj'ajoute lA ZONE \n");
					list_return.add((AZoneSimple)inter);
				}
			}
		} else { // if argument is a list of zones
			for (AZoneSimple zone_a : this.zonesList) {
				for (AZoneSimple zone_b : ((ZoneList) zone).getZonesList()) {
					inter = zone_a.intersection(zone_b);
					System.out.println("Zone simple ? :"+(inter instanceof AZoneSimple));
					System.out.println("Zone nulle ? :"+(inter.getClass().equals(ZoneNull.class)));
					if ((inter instanceof AZoneSimple)&&(!inter.getClass().equals(ZoneNull.class))) {
						System.out.println("\nj'ajoute lA ZONE \n");
						list_return.add((AZoneSimple)inter);
					}
					else if (inter instanceof ZoneList) {
						for (AZoneSimple subInter : ((ZoneList) inter).getZonesList()) {
							if ((subInter instanceof AZoneSimple)&&(!subInter.getClass().equals(ZoneNull.class))) {
								list_return.add((AZoneSimple)subInter);
							}
						}
					}
				}
			}
		}
		
		//Cleaning up
		if (list_return.size() == 0) {
			System.out.println("Zonelist.intersection list_return.size=0");
			result = new ZoneNull();
		} else if (list_return.size() == 1) {
			System.out.println("Zonelist.intersection list_return.size=1");
			result = list_return.get(0);
		} else {
			System.out.println("Zonelist.intersection list_return.size>1");
			result = new ZoneList(list_return);
		}
		return result;
	}

	@Override
	public boolean equal(AZone zone) {
		if (!(zone instanceof ZoneList)) {
			return false;
		} else {
			//ce sont deux zones listes, ont elles la meme taille ?
			if (this.size()!=((ZoneList) zone).size()) {
				return false;
			}
			boolean resultat= true;
			for (int i=0;i<this.size();i++) {
				AZoneSimple zone_a = this.getZonesList().get(i);
				AZoneSimple zone_b = ((ZoneList) zone).getZonesList().get(i);
				resultat =resultat && (zone_a.equal(zone_b));
			}
			return resultat;
		}
	}

	@Override
	public boolean belongs(Coords coords) {
		boolean result = false;
		for (AZoneSimple zone_of_list : this.zonesList) {
			result = result || (zone_of_list.belongs(coords));
		}
		return result;
	}
	
	public double distance(AZone zone) {
		double dist_mini=zone.distance(this.getZonesList().get(0));
		double dist;
		for (int i=1;(i<this.getZonesList().size() && (dist_mini >0));i++) {
			dist = zone.distance(this.getZonesList().get(i));
			if (dist<dist_mini) {
				dist_mini=dist;
			}
		}
		return dist_mini;
	}

}