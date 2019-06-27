package loadPoles.GridObjects;

import repast.simphony.space.grid.GridPoint;

public class ParkingSpace {
	
	String type;
	boolean occupied;
	GridPoint location;
	
	public ParkingSpace(GridPoint location){
		this.type = "normal";
		this.occupied = false;
		this.location = location;
	}

	public String getType() {
		return this.type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public boolean getOccupied() {
		return this.occupied;
	}
	
	public void setOccupied(boolean occupied) {
		this.occupied = occupied;
	}
	
	public GridPoint getLocation() {
		return this.location;
	}
}
