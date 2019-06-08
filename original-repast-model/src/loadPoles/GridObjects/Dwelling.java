package loadPoles.GridObjects;

import java.util.List;

import loadPoles.Human;
import loadPoles.graph.Vertex;
import repast.simphony.context.Context;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Dwelling {

	private List<Human> inhabitants;
	Context<Object> context;
	private String name;
	private ParkingLot parkingSpaces;
	private Vertex closestVertex;
	
	public Dwelling(Context<Object> context){
		this.context = context;
		//number them.
		this.name = String.valueOf(context.getObjects(Dwelling.class).size());
	}
	
	//A new human starts living here
	public void moveIn(Human inhabitant) {
		inhabitants.add(inhabitant);
	}
	
	//Returns the amount of humans that have a certain type of car (chargeable or not) to park that live in this dwelling
	public int getAmountOfParkingType(boolean chargable) {		
		int amountFound = 0;
		
		@SuppressWarnings("unchecked")
		Network<Object> livingin = (Network<Object>) context.getProjection("livingin");
		Iterable<Object> adjacents = livingin.getAdjacent(this);
		
		for(Object adjacent : adjacents) {
			if(adjacent.getClass().equals(Human.class)) {
				Human adjacentHuman = (Human) adjacent;
				if(adjacentHuman.hasChargeableCar() == chargable)
					amountFound++;
				}
			}
				
		return amountFound;
	}
		
	// Add a parking space to this dwelling of a given type
	public void addParkingSpace(String type, Grid<Object> grid) {
		if(this.parkingSpaces ==  null) {
			this.parkingSpaces = new ParkingLot(grid);
		}
		parkingSpaces.addSpace(type, this);
	}
	
	// Returns true if this dwelling has a parking space
	public boolean hasParkingSpace() {
		if(this.parkingSpaces == null) {
			return false;
		}
		return true;
	}
	
	// Returns true if this dwelling has a spot for charging an electric car
	public boolean hasChargingSpot() {
		return parkingSpaces.hasChargingSpot();
	}
	
	// Returns true if all spots of a given type are occupied
	public boolean getOccupied(String type) {
		return parkingSpaces.getOccupied(type);	
	}
	
	// Sets a spot in the parking lot for a given type to the given boolean occupied
	public ParkingSpace setOccupied(String type, boolean occupied) {
		return parkingSpaces.setOccupied(type, occupied);
	}
	
	// Get an available parking spot for a given type
	public ParkingSpace getAvailable(String type) {		
		return parkingSpaces.getAvailable(type);
	}
	
	public String getName() {
		return this.name;
	}

	public void setClosestVertex(Vertex nearest) {
		closestVertex = nearest;
	}
	
}
