package loadPoles;

import java.util.Iterator;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.SimUtilities;

public class Human {

	boolean carUser, hasParked;
	String carType,name;
	Context<Object> context;
	float happiness;
	
	
	public Human(Context<Object> context) {
		
		this.context = context;
		
		//Uses cars?
		carUser = true;
		hasParked = false;
		//10% is type b
		if(RandomHelper.nextDoubleFromTo(0, 1) > 0.5) {
			// "Normal" car
			carType = "a";
		} else {
			// Electric car user
			carType = "b";
		}
		name = String.valueOf(RandomHelper.nextDoubleFromTo(0, 2));
		name = String.valueOf(context.getObjects(Human.class).size());
		happiness = 1;
		
	}
	
	//Park your car
	@ScheduledMethod(start = 1, interval = 1, priority = 2, shuffle = true)
	public void parkCar() {
		//Do we even use a car?
		if(!carUser) {
			return;
		}
		
		System.out.println("Parkingname: " + this.name);
		
		//Find best empty spot
		//Get location of home
		Grid<Object> grid = (Grid<Object>) context.getProjection("dwellingsGrid");
		Network<Object> livingin = (Network<Object>) context.getProjection("livingin");
		
		//Should always be 1
		//System.out.println(livingin.getDegree(this));
		
		Iterable<Object> home = livingin.getAdjacent(this);
		GridPoint homeLocation = grid.getLocation(home.iterator().next());
		
		//Now look for parking spots
		Grid<Object> parkingSpots = (Grid<Object>) context.getProjection("parkingspacesGrid");
		
		//get all cells around the home location
		GridCellNgh<ParkingSpace> nghCreator = new GridCellNgh<ParkingSpace>(parkingSpots, homeLocation, ParkingSpace.class, 2, 2);
		List<GridCell<ParkingSpace>> gridCells = nghCreator.getNeighborhood(true);
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform());

		//Check the neighbourhood for the closest empty spot
		// The spot has either no agent in it or an agent that isn't parked
		double minDistance = Double.MAX_VALUE, parkDistance = 0;
		GridCell<ParkingSpace> closestCell = null;
		ParkingSpace closestSpot = null;
		for (GridCell<ParkingSpace> cell : gridCells) {
			
			//Iterable<Object> spotUsers = parkingSpots.getObjectsAt(cell.getPoint().getX(),cell.getPoint().getY());
			//System.out.println("spotUsers: " + spotUsers + " size: "+ spotUsers.spliterator().estimateSize() + " somone parked: " + isSomeoneParked(parkingSpots, cell));
			
			ParkingSpace spot = (ParkingSpace) parkingSpots.getObjectAt(cell.getPoint().getX(),cell.getPoint().getY());
			
			System.out.println("this carType: " + this.carType + " and spot type: " + spot.getType());
			
			//if( spotUser != null) System.out.println("Spot "+ cell.getPoint().getX() + "," + cell.getPoint().getY() + " is not null but first occupant is parked?: " + spotUser.getHasParked());
			//if( spotUser == null || isSomeoneParked(parkingSpots, cell) == false) {
			if (!spot.getOccupied() && this.carType == spot.getType()) {
				parkDistance = parkingSpots.getDistance(homeLocation, cell.getPoint());
				if (parkDistance < minDistance) {
					minDistance = parkDistance;
					if(parkDistance == 0.0) System.out.println("is someone parked: " + spot.getOccupied());
					System.out.println("parkDistance: " + parkDistance);
					closestCell = cell;
					closestSpot = spot;
				}
			}
		}
		
		//If we have found a parking spot: park!
		if(closestCell != null) {
			System.out.println("I am moving to: " + closestCell.getPoint().getX() + "," + closestCell.getPoint().getY());
			parkingSpots.moveTo(this, closestCell.getPoint().getX(),closestCell.getPoint().getY());
			closestSpot.setOccupied(true);
			this.hasParked = true;
			//TODO: Should we add a network to track who is parked where?
			
			
			//happiness from parking, but unhappiness from parking far away. 0.07 is an arbitrary value.
			//See if quadratic is reasonable...
			if(happiness < 1) happiness = (float) (happiness + 0.1 - (parkDistance * parkDistance * 0.01));
			
			
			//TODO:
			// * Depending on type we can have different calculations for happiness:
			//		Current on is for type A, but Type b is compounded: both parking and
			//		the actual NEED to charge the car for use. So a two step process for
			//		happiness.
			// * Measure happiness of type a and type b separately.
			// * Setting to make type b parkingspace exclusively for b or not.
		} else {
			System.out.println("wasn't able to find a spot!");
			if (happiness > 0) {
				happiness = (float) (happiness - 0.3);
			}
		}
	}
	
	//Obsolete?
	private boolean isSomeoneParked(Grid<Object> parkingSpots, GridCell<Human> cell) 
	{
		Iterable<Object> spotUsers = parkingSpots.getObjectsAt(cell.getPoint().getX(),cell.getPoint().getY());
		for(Object spotUserElem : spotUsers) {
			if(spotUserElem.getClass().equals(Human.class)) {
				Human spotUserHuman = (Human) spotUserElem;
				if(spotUserHuman.getHasParked()) {
					return true;
				}
			}
			
		}
		return false;
	}
	
	//Leave with your car
	@ScheduledMethod(start = 1, interval = 1, priority = 3)
	public void depart() {
		//Do we even use a car?
		if(!carUser || !this.hasParked) {
			return;
		}
		
		//Grid<Object> parkingSpots = (Grid<Object>) context.getProjection("parkingspacesGrid");
		//System.out.println("Is leaving: " + name);
		this.hasParked = false;
		
		//Get the ParkingSpace object/agent at this location and tell it we have moved away.
		Grid<Object> parkingSpots = (Grid<Object>) context.getProjection("parkingspacesGrid");
		GridPoint test = parkingSpots.getLocation(this);
		//ParkingSpace spot = (ParkingSpace) parkingSpots.getObjectAt(cell.getPoint().getX(),cell.getPoint().getY());
		try {
			for( Object o : parkingSpots.getObjectsAt(parkingSpots.getLocation(this).getX(),parkingSpots.getLocation(this).getY())) {
				if(o.getClass().equals(ParkingSpace.class)) {
					ParkingSpace ps = (ParkingSpace) o;
					ps.setOccupied(false);
				}
			}
		} catch (Exception e) {
			System.out.println("Human: " + parkingSpots.getLocation(this));
			System.out.println("No object at all at " + parkingSpots.getLocation(this).getX() + "," +parkingSpots.getLocation(this).getY());
		}
		
	}
	
	//Return the happiness
	public float getHappiness() {
		return this.happiness;
	}
	
	public Object getHappinessA() {
		if(this.getType() == "a") {
			return this.getHappiness();
		}
		return null;
	}
	
	//is parked?
	public boolean getHasParked() {
		return this.hasParked;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getType() {
		return this.carType;
	}
	
	//
}
