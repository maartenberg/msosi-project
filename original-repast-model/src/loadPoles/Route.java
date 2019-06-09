package loadPoles;

import loadPoles.GridObjects.ParkingSpace;
import loadPoles.GridObjects.TransitStop;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Route {
	Grid<Object> grid;
	private double walkingDistance, travelDistance, utility;
	private Vehicle vehicle;
	private GridPoint from, to;
	
	/*
	 * For car: represents the two closest parking spaces from beginning and end locations on route
	 */
	ParkingSpace firstSpace, secondSpace;
	
	/**
	 * For public transport: Represents the two closest transit stops from beginning and end locations on route
	 */
	TransitStop firstStop, secondStop;
	
	public Route(Grid<Object> grid, GridPoint from, GridPoint to, Vehicle vehicle) {
		this.grid = grid;
		this.from = from;
		this.to = to;
		this.vehicle = vehicle;	
		
		// Set initial distances
		this.walkingDistance = 0;				
		this.travelDistance = this.grid.getDistance(this.from, this.to);
	}	
	
	// Sets the two stops that a human might make on a route
	public void setTransitStops(TransitStop firstStop, TransitStop secondStop) {
		this.firstStop = firstStop;
		this.secondStop = secondStop;		
		calculateDistances();
	}
	
	public void setParkingSpaces(ParkingSpace firstSpace, ParkingSpace secondSpace) {
		this.firstSpace = firstSpace;
		this.secondSpace = secondSpace;
		calculateDistances();
	}

	// Calculates walking and travel distance if there are stops
	private void calculateDistances() {
		GridPoint firstLoc = null;
		GridPoint secondLoc = null;	
		if(vehicle.isCar()) {
			firstLoc =  firstSpace.getLocation();
			secondLoc = secondSpace.getLocation();			
		}
		else {
			firstLoc = grid.getLocation(firstStop);
			secondLoc = grid.getLocation(secondStop);
		}
		
		walkingDistance = grid.getDistance(from, firstLoc) + grid.getDistance(to, secondLoc);
		travelDistance = grid.getDistance(firstLoc, secondLoc);
	}	

	// Set found utility for this route
	public void setUtility(double utility) {
		this.utility = utility;
	}
	
	public double getUtility() {
		return utility;
	}
	
	public double getWalkingDistance() {
		return walkingDistance;		
	}
	
	public void setTravelDistance(double value) {
		travelDistance = value;
	}
	
	public double getTravelDistance() {
		return travelDistance;
	}
	
	public Vehicle getVehicle() {
		return vehicle;
	}
	
	public TransitStop getFirstStop() {
		return firstStop;
	}
	
	public TransitStop getSecondStop() {
		return secondStop;
	}
	
	public ParkingSpace getFirstSpace() {
		return firstSpace;
	}
	
	public ParkingSpace getSecondSpace() {
		return secondSpace;
	}

}
