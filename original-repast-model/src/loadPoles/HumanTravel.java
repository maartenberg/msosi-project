package loadPoles;

import java.util.Iterator;

import loadPoles.GridObjects.ParkingLot;
import loadPoles.GridObjects.ParkingSpace;
import loadPoles.GridObjects.PublicBuilding;
import loadPoles.GridObjects.TransitStop;
import loadPoles.ScenarioTree.EmissionData;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.collections.IndexedIterable;

public class HumanTravel {
	Human human;
	Context<Object> context;
	Grid<Object> grid;
	Vehicle pastVehicle;
	Route previousRoute;
	
	public HumanTravel(Human human, Context<Object> context, Grid<Object> grid) {
		this.human = human;
		this.context = context;
		this.grid = grid;
	}

	// Find closest parking space with available parking spaces of a given type
	public ParkingSpace findClosestParkingSpace(GridPoint location, String type) {
		IndexedIterable<Object> parkingLots = this.context.getObjects(ParkingLot.class);
		Iterator<Object> parkingLotsIterator = parkingLots.iterator();
		
		ParkingSpace closest = null;
		double minDistance = Integer.MAX_VALUE;		
		
		//Find closest parking lot with an available parking space
		while(parkingLotsIterator.hasNext()) {
			ParkingLot pl = (ParkingLot) parkingLotsIterator.next();
			GridPoint plLocation = grid.getLocation(pl);						
			double distance = grid.getDistance(location, plLocation);
			ParkingSpace available = pl.getAvailable(type);
			
			if(available != null && distance < minDistance) {
				minDistance = distance;
				closest = available;
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
				}			
			}
		}
		return closest;
	}
	
	// Find closest transit stop to a certain location on the grid
	private TransitStop findClosestTransitStop(GridPoint location) {
		IndexedIterable<Object> transitStops = this.context.getObjects(TransitStop.class);
		Iterator<Object> transitStopsIterator = transitStops.iterator();
		
		TransitStop closest = null;
		double minDistance = Integer.MAX_VALUE;			
		
		//Find closest parking lot with an available parking space
		while(transitStopsIterator.hasNext()) {
			TransitStop ts = (TransitStop) transitStopsIterator.next();
			GridPoint tsLocation = grid.getLocation(ts);						
			double distance = grid.getDistance(location, tsLocation);
			
			if(distance < minDistance) {
				minDistance = distance;
				closest = ts;
			}	
		}	
		
		return closest;
	}
	
	// Find a route given a destination and a vehicle
	private Route findRoute(GridPoint destination, Vehicle vehicle) {
		Route route = null;
		
		if(vehicle.isCar()) {
			route = findCarRoute(destination, vehicle);
		}
		else if(vehicle.getName() == "public_transport") {
			route = findTransitRoute(destination, vehicle);			
		}
		else {		
			GridPoint currentLocation = grid.getLocation(human);
			route = new Route(grid, currentLocation, destination, vehicle);		
		}

		if(route != null) {
			// If vehicle is bicycle or public transport, then distance is 0.8 times shorter with a 40% chance
			// (Because of possible bicycle paths and transit only roads)
			double distance = route.getTravelDistance();
			if(vehicle.getName() == "bicycle" || vehicle.getName() == "electric_bicycle" || vehicle.getName() == "public_transport") {
				if(RandomHelper.nextDoubleFromTo(0, 1) < 0.4) {
					distance *= 0.8;
					route.setTravelDistance(distance);
				}
			}		
		}
		
		return route;
	}
	
	// Finds a corresponding route when the vehicle is public transport that includes transit stops
	private Route findTransitRoute(GridPoint destination, Vehicle vehicle) {
		GridPoint currentLocation = grid.getLocation(human);
		
		// Find closest transits stops at beginning and end of route
		TransitStop from = findClosestTransitStop(currentLocation);
		TransitStop to = findClosestTransitStop(destination);
		
		// If the two transit stops are at the same location, we basically do not travel with public transport
		if(grid.getLocation(to).getX() == grid.getLocation(from).getX() && grid.getLocation(to).getY() == grid.getLocation(from).getY()) {
			return null;
		}
		
		Route route = new Route(grid, currentLocation, destination, vehicle);
		route.setTransitStops(from, to);
		return route;
	}	
	
	// Finds a corresponding route when the vehicle is a car that includes finding parking spaces
	public Route findCarRoute(GridPoint destination, Vehicle vehicle) {		
		GridPoint currentLocation = grid.getLocation(human);
		GridPoint homeLocation = grid.getLocation(human.dwelling);
		
		ParkingSpace from = vehicle.getParkingSpace();
		if(from == null) {
			if(vehicle.getName() == "electric_car") {
				from = findClosestParkingSpace(currentLocation, "electric");
			} else {
				from = findClosestParkingSpace(currentLocation, "normal");
			}
		}
		ParkingSpace to = null;
		// Check if the electric car has enough range
		if(vehicle.getName() == "electric_car") {						
			// Find closest charging space, normal space locations
			ParkingSpace closestChargingSpace = findClosestParkingSpace(destination, "electric");
			ParkingSpace closestNormalSpace = findClosestParkingSpace(destination, "normal");
			
			// Always charge if going home
			if(destination.getX() == homeLocation.getX() && destination.getY() == homeLocation.getY()) {
				to = closestChargingSpace;
			}
			else {
				// Get remaining range of electric car, and distance between each type of parking lot
				double remainingRange = vehicle.getRemainingRange();				
				double normalDistance = grid.getDistance(from.getLocation(), closestNormalSpace.getLocation());
				double chargeDistance = grid.getDistance(from.getLocation(), closestChargingSpace.getLocation());		
				
				// If we can make the double trip easily, just park at normal parking space
				if(remainingRange - (2*normalDistance) > 0) {
					to = closestNormalSpace;		
				}
				// Else, check if we can make single trip to closest charging space
				else if(remainingRange - chargeDistance > 0) {
					to = closestChargingSpace;		
				}
				// Else, we cannot even make a single trip
				else {
					return null;
				}
			}
		}
		// Else, just find closest normal parking space
		else {	
			to = findClosestParkingSpace(destination, "normal");		
		}
		
		// If we cannot find a parking space (all are occupied)
		if(to == null) {
			return null;
		}		
		
		// If park at the same parking spaces, we are not driving at all
		if(to.getLocation().getX() == from.getLocation().getX() && to.getLocation().getY() == from.getLocation().getY()) {
			return null;
		}
		
		Route route = new Route(grid, currentLocation, destination, vehicle);
		route.setParkingSpaces(from, to);
		return route;		
	}
	
	// Calculate the utility for each vehicle and corresponding route, and return the best one 
	private Route findBestRoute(GridPoint destination) {		
		//Keep track of the best route (which includes the best vehicle)
		double bestUtility = 0;
		Route bestRoute = null;
		
		//Calculate utility for each vehicle available to this human
		for(Vehicle vehicle : human.vehicles) {
			// TODO all: find a good starting value for utility
			double utility = 5000;
			
			// Find the corresponding route for this vehicle
			Route route = findRoute(destination, vehicle);
			
			// If route is null, it is an invalid route, ignore it
			if(route == null) {
				continue;			
			}
			else {							
				// The less comfortable, the less utility
				utility *= vehicle.getComfort();
				
				// The slower it is, the less utility
				utility *= vehicle.getSpeed();	
				
				//If the vehicle cannot travel this distance comfortably, subtract a lot from utility			
				if(vehicle.getActionRadius() < route.getTravelDistance()) {
					//Get the difference between the action radius and distance to travel, and use this as punishment
					double punishment = route.getTravelDistance() - vehicle.getActionRadius();
					utility -= punishment * 10;
				}
				
				//The more emission of CO2, the worse the utility
				utility -= human.traits.environmentFactor * (route.getTravelDistance() * vehicle.getTravelEmission());
				
				//The more the cost of travelling impacts the income, the worse the utility
				utility *= (1 - (route.getTravelDistance() * vehicle.getKilometerCost())/human.traits.income);
				
				//The more we have to walk between stops during the route, the worse the utility
				utility -= route.getWalkingDistance() * 5;
				
				//Make sure utility is not lower than 1, so at least one vehicle is always chosen
				utility = Math.max(1, utility);					
			}
			
			route.setUtility(utility);
			
			if(utility > bestUtility) {
				bestUtility = utility;
				bestRoute = route;
			}			
		}		
		return bestRoute;		
	}	
	
	// Finds a random destination for the Human to go to
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
		IndexedIterable<Object> publicBuildings = context.getObjects(PublicBuilding.class);
		if(publicBuildings.size() > 0) {
			PublicBuilding pb = (PublicBuilding) publicBuildings.get(RandomHelper.nextIntFromTo(0, publicBuildings.size() - 1));
			return grid.getLocation(pb);
		}
		
		return homeLocation;
	}
	
	// Find a destination and vehicle and travel there 
	public void depart() {
		GridPoint destination = getDestination();		
		GridPoint homeLocation = grid.getLocation(human.dwelling);
		GridPoint currentLocation = grid.getLocation(human);
				
		Route route = null;
		boolean goingHome = false;
		// If destination is home location, choose same route as when traveling from home to destination
		// BUT always find a new route for cars, because of parking space occupancy, we cannot use old parking spaces on the previous route
		if(previousRoute != null && !previousRoute.getVehicle().isCar() && destination.getX() == homeLocation.getX() && destination.getY() == homeLocation.getY()) {
			route = previousRoute;
			goingHome = true;			
		} else {
			route = findBestRoute(destination);
		}		
		
		Vehicle vehicle = route.getVehicle();
		GridPoint currentFrom = null; //Only for console printing
		GridPoint currentTo = null; //Only for console printing
		
		// Handle parking and un-parking cars
		if(vehicle.isCar()) {		
			ParkingSpace from = null;
			ParkingSpace to = null;
			if(goingHome) {
				from = (ParkingSpace) route.getSecondSpace();
				to = (ParkingSpace) route.getFirstSpace();
			} else {
				from = (ParkingSpace) route.getFirstSpace();
				to = (ParkingSpace) route.getSecondSpace();
			}
			
			from.setOccupied(false);
			to.setOccupied(true);
			vehicle.setParkingSpace(to);
			
			//Only for console printing:
			currentFrom = from.getLocation();
			currentTo = to.getLocation();
			
			// Update remaining range if we are using an electric car
			if(vehicle.getName() == "electric_car") {
				double remainingRange = vehicle.getRemainingRange();
			
				// If we are charging, reset range
				if(to.getType() == "electric") {
					vehicle.setRemainingRange(vehicle.getActionRadius());
				}
				else {
					vehicle.setRemainingRange(remainingRange - route.getTravelDistance());
				}
			}			
		}		
		
		// The following is just printing information to console. Can be deleted if obsolete
		System.out.println( "\nHUMAN " + human.getName() + ":" +
							"\n travelling from: (" + currentLocation.getX() + ", " + currentLocation.getY() + ")"
							+ " to: (" + destination.getX() + ", " + destination.getY() + ")"
							+ "\n with distance: " + grid.getDistance(currentLocation, destination) 
							+ "\n and vehicle: " + vehicle.getName());
		
		if(vehicle.isCar()) {
			System.out.println(" went from parking space at: (" + currentFrom.getX() + ", " + currentFrom.getY() + ")"
							 + "\n  to parking space at: (" + currentTo.getX() + ", " + currentTo.getY() + ")"
							 + "\n  with total walking distance: " + route.getWalkingDistance());			
		}
		if(vehicle.getName() == "public_transport") {
			if(goingHome) {
				currentFrom = grid.getLocation(route.getSecondStop());
				currentTo = grid.getLocation(route.getFirstStop());				
			} else {
				currentFrom = grid.getLocation(route.getFirstStop());
				currentTo = grid.getLocation(route.getSecondStop());
			}
			System.out.println(" went from transit stop at: (" + currentFrom.getX() + ", " + currentFrom.getY() + ")"
							 + "\n  to transit stop at: (" + currentTo.getX() + ", " + currentTo.getY() + ")"
							 + "\n  with total walking distance: " + route.getWalkingDistance());		
		}
		
		// Remove the previousroute and add new route to the context
		if(previousRoute != null) {
			this.context.remove(previousRoute);
		}
		this.context.add(route);
		
		// Put Route on Human's current location, so arrows are directed in the correct way
		this.grid.moveTo(route, currentLocation.getX(), currentLocation.getY());
		Network<Object> journeysNetwork = (Network<Object>) this.context.getProjection("journeys");
		journeysNetwork.addEdge(route, human);				

		// Move human and update variables
		grid.moveTo(human, destination.getX(), destination.getY());
		human.totalEmissions += vehicle.getTravelEmission() * route.getTravelDistance();
		human.happiness += route.getUtility();		
		previousRoute = route;
	}
}