package loadPoles;

import java.util.*;

import repast.simphony.context.Context;

public class ParkingLot {
	Context<Object> context;
	List<ParkingSpace> parkingSpaces;
	
	public ParkingLot(Context<Object> context){		
		this.context = context;
		this.parkingSpaces = new ArrayList<ParkingSpace>();
	}	
	
	// Adds a new parking space to this parking lot, of a given type
	public void addSpace(String type) {
		ParkingSpace ps = new ParkingSpace(context);
		ps.setType(type);
		parkingSpaces.add(ps);
	}
	
	// Returns true if this parkinglot has a spot for charging an electric car
	public boolean hasChargingSpot() {
		return numberOfSpots("electric") > 0;
	}
	
	// Returns true if this parkinglot has a spot for a normal car
	public boolean hasNormalSpot() {
		return numberOfSpots("normal") > 0;
	}	

	// Returns the number of spots of a given type on this parking lot
	public int numberOfSpots(String type) {
		int count = 0;
		for(ParkingSpace ps : parkingSpaces) {
			if(ps.getType() == type) {
				count++;
			}
		}
		return count;
	}
	
	// Returns true if all spots of a given type are occupied
	public boolean getOccupied(String type) {
		for(ParkingSpace ps : parkingSpaces) {
			//If the type is the same and it is not occupied, return false
			if(ps.getType() == type && !ps.getOccupied()) {
				return false;
			}
		}		
		return true;		
	}
	
	// Sets a spot in the parking lot for a given type to the given boolean occupied
	public void setOccupied(String type, boolean occupied) {
		for(ParkingSpace ps : parkingSpaces) {
			// If this spot is of the same type, and has the opposite occupied value of what we want to set it, 
			// set this one to the new value of occupied
			if(ps.getType() == type && ps.getOccupied() != occupied) {
				ps.setOccupied(occupied);
				return;
			}
		}
	}
	
}
