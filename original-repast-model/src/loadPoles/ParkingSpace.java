package loadPoles;

import repast.simphony.context.Context;

public class ParkingSpace {
	
	Context<Object> context;
	String type;
	boolean occupied;
	
	public ParkingSpace(Context<Object> context){
		this.context = context;
		this.type = "a";
		this.occupied = false;
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
}
