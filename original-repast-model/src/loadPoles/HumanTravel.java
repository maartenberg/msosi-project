package loadPoles;

import java.util.Iterator;

import loadPoles.GridObjects.ParkingLot;
import loadPoles.GridObjects.ParkingSpace;
import loadPoles.GridObjects.PublicBuilding;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.collections.IndexedIterable;

public class HumanTravel {
	Human human;
	Context<Object> context;
	Grid<Object> grid;
	Vehicle pastVehicle;
	
	public HumanTravel(Human human, Context<Object> context, Grid<Object> grid) {
		this.human = human;
		this.context = context;
		this.grid = grid;
	}
	
	// Find a parking spot for your car and park there
	public ParkingSpace parkCar(GridPoint location, Vehicle vehicle) {
		//If we are no car user, no need to park, so just return
		if(!human.carUser) {
			return null;
		}
		
		ParkingSpace bestSpot = null;
		
		String type = "";
		if(vehicle.getName() == "electric_car") {
			type = "electric";
		} else {
			type = "normal";
		}
		
		// Check if the electric car has enough range
		if(type == "electric") {
			// Find closest charging space, normal space locations
			ParkingSpace closestChargingSpace = findClosestParkingSpace(location, type);
			ParkingSpace closestNormalSpace = findClosestParkingSpace(location, "normal");
			GridPoint closestChargingLoc = closestChargingSpace.getLocation();
			GridPoint closestNormalLoc = closestNormalSpace.getLocation();
			
			// Get remaining range of electric car
			double remainingRange = vehicle.getRemainingRange();
			
			// If we are planning a trip away from home, check if we need to charge in between
			GridPoint homeLocation = grid.getLocation(human.dwelling);
			if(location.getX() != homeLocation.getX() && location.getY() != homeLocation.getY()) {							
				// If electric car can come home when parking at a normal spot, then no need to charge	
				ParkingSpace currentParkingSpace = vehicle.getParkingSpace();
				GridPoint currentParkingLoc = currentParkingSpace.getLocation();						
				
				double normalDistance = grid.getDistance(currentParkingLoc, closestNormalLoc);
				double chargeDistance = grid.getDistance(currentParkingLoc, closestChargingLoc);		
				
				//System.out.println("\nRemaining range of car: " + remainingRange);
				//System.out.println("Distance to normal spot: " + normalDistance);
				//System.out.println("Distance to charging spot: " + chargeDistance);
				
				if(remainingRange - (2*normalDistance) > 0) {
					bestSpot = closestNormalSpace;
					vehicle.setRemainingRange(remainingRange - (2*normalDistance));					
					//System.out.println("Can make double trip easily!");
				}
				// Else, check if we can make single trip to closest charging space
				else if(remainingRange - chargeDistance > 0) {
					bestSpot = closestChargingSpace;
					// Since vehicle is charged here, range is reset
					vehicle.setRemainingRange(vehicle.getActionRadius());					
					//System.out.println("Need to charge at destination!");
				}
				// Else, we cannot even make a single trip
				else {
					//System.out.println("Need to choose new vehicle!");
					return null;
				}
			}
			// Else, if we are going home, then always park at a charging spot
			else {
				bestSpot = closestChargingSpace;
				vehicle.setRemainingRange(vehicle.getActionRadius());
			}
		}
		// Else, just find closest normal parking space
		else {	
			bestSpot = findClosestParkingSpace(location, type);		
		}
		
		return bestSpot;
	}

	// Find closest parking space with available parking spaces of a given type
	private ParkingSpace findClosestParkingSpace(GridPoint location, String type) {
		IndexedIterable parkingLots = this.context.getObjects(ParkingLot.class);
		Iterator parkingLotsIterator = parkingLots.iterator();
		
		ParkingSpace closest = null;
		double minDistance = Integer.MAX_VALUE;			

		// For printing to console: (can be removed)
		GridPoint bestLocation = null;
		String locType = "";
		
		//Find closest parking lot with an available parking space
		while(parkingLotsIterator.hasNext()) {
			ParkingLot pl = (ParkingLot) parkingLotsIterator.next();
			GridPoint plLocation = grid.getLocation(pl);						
			double distance = grid.getDistance(location, plLocation);
			ParkingSpace available = pl.getAvailable(type);
			
			if(available != null && distance < minDistance) {
				minDistance = distance;
				closest = available;
				bestLocation = plLocation;
				locType = "a parking lot";
			}	
		}
		
		//If home has an available parking space for this car, keep it as an option
		if(human.dwelling.hasParkingSpace()) {
			ParkingSpace homeParking = human.dwelling.getAvailable(type);
			if(homeParking != null) {
				GridPoint homeLocation = grid.getLocation(human.dwelling);
				double distance = grid.getDistance(location, homeLocation);
				if(distance < minDistance) {
					minDistance = distance;
					closest = homeParking;
					bestLocation = homeLocation;
					locType = "home";
				}			
			}
		}
		
		
		//TODO: do something with minDistance affecting the human's happiness
		return closest;
	}
	
	// Find a destination and vehicle and travel there 
	//@ScheduledMethod(start = 1, interval = 1, priority = 3)
	public void depart() {
		GridPoint destination = getDestination();		
		GridPoint homeLocation = grid.getLocation(human.dwelling);
		GridPoint currentLocation = grid.getLocation(human);
				
		Vehicle vehicle = null;
		// If destination is home location, choose same vehicle as when traveling from home to destination
		if(destination.getX() == homeLocation.getX() && destination.getY() == homeLocation.getY()) {
			vehicle = pastVehicle;
		}
		else {
			// Find distance and choose appropriate vehicle
			double distance = grid.getDistance(currentLocation, destination);
			vehicle = chooseVehicle(distance, null);
		}
		
		ParkingSpace closestSpace = null;
		boolean parked = false;
		while(vehicle.isCar() && !parked) {			
			// Find new spot to park near location			
			closestSpace = parkCar(destination, vehicle);
			
			// Park our car at new spot if we can
			if(closestSpace != null) {			
				vehicle.getParkingSpace().setOccupied(false);		
				closestSpace.setOccupied(true);
				vehicle.setParkingSpace(closestSpace);
				parked = true;
			}
			else {
				// Find second best vehicle by excluding current vehicle
				double distance = grid.getDistance(currentLocation, destination);
				String oldVehicle = vehicle.getName();
				vehicle = chooseVehicle(distance, oldVehicle);
				System.out.println("Changed vehicle from: " + oldVehicle + " to "  + vehicle.getName());
				
				// If there is no better vehicle, don't travel and subtract a lot from happiness
				if(vehicle.getName() == oldVehicle) {
					//TODO: subtract a lot from happiness
					System.out.println("Can't find a suitable vehicle to travel with.\n");
					return;
				}					
			}
		}		
		
		pastVehicle = vehicle;
		System.out.println("\nTravelling from: (" + currentLocation.getX() + ", " + currentLocation.getY() + ")"
							+ " to: (" + destination.getX() + ", " + destination.getY() + ")"
							+ "\n with distance: " + grid.getDistance(currentLocation, destination) 
							+ "\n and vehicle: " + vehicle.getName());
		
		if(closestSpace != null) {
			GridPoint parkLocation = closestSpace.getLocation();
			double walkingDistance = grid.getDistance(parkLocation, destination);
			//TODO: subtract from happiness based on this walking distance
			System.out.println(" Parked at: (" + parkLocation.getX() + ", " + parkLocation.getY() + ") "
							 + "\n  with walking distance from destination: " + walkingDistance);
			
		}
		grid.moveTo(human, destination.getX(), destination.getY());
	}
		
	//Decides which vehicle to use
	public Vehicle chooseVehicle(double distance, String exclude) {		
		//Keep track of the best vehicle
		double bestUtility = 0;
		Vehicle bestVehicle = null;
		
		//Calculate utility for each vehicle available to this human
		for(Vehicle vehicle : human.vehicles) {
			//TODO: find a good starting value
			//Start with a utility of 1000
			double utility = 5000;
			
			//If vehicle is one that we want to exclude, set utility to 1 and continue
			if(vehicle.getName() == exclude) {
				utility = 1;
				continue;
			}
			
			//The less comfortable, the less utility
			utility *= vehicle.getComfort();
			
			//The slower it is, the less utility
			utility *= vehicle.getSpeed();	
			
			//If the vehicle cannot travel this distance comfortably, subtract a lot from utility			
			if(vehicle.getActionRadius() < distance) {
				//Get the difference between the action radius and distance to travel, and use this as punishment
				double punishment = distance - vehicle.getActionRadius();
				utility -= punishment * 10;
			}
			
			//The more emission of CO2, the worse the utility
			//TODO: make this "punishment" of utility also depend on personal values of the environment
			utility -= human.traits.environmentFactor * (distance * vehicle.getTravelEmission());
			
			//The more the cost of travelling impacts the income, the worse the utility
			utility *= (1 - (distance * vehicle.getKilometerCost())/human.traits.income);
			
			//Make sure utility is not lower than 1, so at least one vehicle is always chosen
			utility = Math.max(1, utility);		
			
			//Update best values
			if(utility > bestUtility) {
				bestUtility = utility;
				bestVehicle = vehicle;
			}			
		}
		
		//TODO: also do something with happiness based on how high (or low) this utility is?
		return bestVehicle;		
	}	
	
	//Finds a random destination for the Human to go to
	private GridPoint getDestination() {
		GridPoint currentLocation = grid.getLocation(human);
		
		//If not home, go home
		GridPoint homeLocation = grid.getLocation(human.dwelling);
		if(currentLocation.getX() != homeLocation.getX() && currentLocation.getY() != homeLocation.getY()) {
			return homeLocation;
		}
		
		//Go to work if employed (40% chance)
		if(human.isEmployed()) {
			double goingToWork = 0.4;		
			if(RandomHelper.nextDoubleFromTo(0,1) < goingToWork) {
				// Return location of work place
				return grid.getLocation(human.workplace);
			}
		}
		
		//Find all public building that one can go to, and pick a random one
		IndexedIterable publicBuildings = context.getObjects(PublicBuilding.class);
		if(publicBuildings.size() > 0) {
			PublicBuilding pb = (PublicBuilding) publicBuildings.get(RandomHelper.nextIntFromTo(0, publicBuildings.size() - 1));
			return grid.getLocation(pb);
		}
		
		return homeLocation;
	}
}