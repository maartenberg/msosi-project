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

	// Given a route, overwrite its information to this route
	public void overwriteFrom(Route other) {
		this.from = other.getFrom();
		this.to = other.getTo();
		this.vehicle = other.getVehicle();
		this.walkingDistance = other.getWalkingDistance();
		this.travelDistance = other.getTravelDistance();
		this.firstStop = other.getFirstStop();
		this.secondStop = other.getSecondStop();
		this.firstSpace = other.getFirstSpace();
		this.secondSpace = other.getSecondSpace();
		this.utility = other.getUtility();
	}

	// Sets the two stops that a human might make on a route
	public void setTransitStops(TransitStop firstStop, TransitStop secondStop) {
		this.firstStop = firstStop;
		this.secondStop = secondStop;
		calculateDistances();
	}

	// Sets the two parking spaces that a human might use on a route
	public void setParkingSpaces(ParkingSpace firstSpace, ParkingSpace secondSpace) {
		this.firstSpace = firstSpace;
		this.secondSpace = secondSpace;
		calculateDistances();
	}

	// Calculates walking and travel distance if there are stops
	private void calculateDistances() {
		GridPoint firstLoc = null;
		GridPoint secondLoc = null;
		if (vehicle.isCar()) {
			firstLoc = firstSpace.getLocation();
			secondLoc = secondSpace.getLocation();
		} else {
			firstLoc = grid.getLocation(firstStop);
			secondLoc = grid.getLocation(secondStop);
		}

		walkingDistance = grid.getDistance(from, firstLoc) + grid.getDistance(to, secondLoc);
		travelDistance = grid.getDistance(firstLoc, secondLoc);
	}

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

	public GridPoint getFrom() {
		return from;
	}

	public GridPoint getTo() {
		return to;
	}
}
