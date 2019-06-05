package loadPoles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.SimUtilities;
import repast.simphony.util.collections.IndexedIterable;

public class Human {

	/**
	 * Represents how much influence the neighbors have on this Human's decision to buy an EV.
	 * Ranges from 0 to 1, inclusive.
	 */
	float socialFactor;

	/**
	 * Represents how much influence the environmental effects have on this Human's decision to buy an EV.
	 * Ranges from 0 to 1, inclusive.
	 */
	float environmentFactor;

	/**
	 * Represents whether or not this Human has a license to drive a car.
	 */
	boolean hasCarLicense;	

	/**
	 * Represents the different vehicles available to this Human.
	 */
	List<Vehicle> vehicles;
	
	/*
	 * Represents the list of all vehicles that can be bought
	 */
	List<Vehicle> products;
	
	/*
	 * Represents the gender of this person
	 */
	String gender;
	
	/*
	 * Represents the age of this person
	 */
	int age;
	
	/*
	 * Represents the current income of this person per year
	 */
	int income;	

	/*
	 * Represents whether or not this human is employed
	 */
	boolean isemployed;
	
	/*
	 * Represents the action taken by this human
	 */
	String consumatAction;
	
	/*
	 * Represents the current satisfaction and uncertainty levels for this agent
	 */
	float satisfaction;
	float uncertainty;
	
	/*
	 * Represents at what threshold the agent considers itself satisfied or uncertain
	 */
	float satisfactionThreshold;
	float uncertaintyThreshold;

	boolean carUser, hasParked;
	String name;
	Context<Object> context;
	float happiness;
	Preferences preference;
	Vehicle pastVehicle;
	Dwelling dwelling;
	Workplace workplace;	
	Grid<Object> grid;

	public Human(Context<Object> context, Grid<Object> grid) {

		this.context = context;
		this.grid = grid;
		
		//Initialise list of products
		products = new ArrayList<Vehicle>();
		products.add(new Bicycle("electric"));
		products.add(new Motor());
		products.add(new Car("normal"));
		products.add(new Car("hybrid"));
		products.add(new Car("electric", 1));
		products.add(new Car("electric", 2));
		products.add(new Car("electric", 3));
		
		
		//Initialise variables, vehicles, and preferences belonging to this human		
		initFeatures();
		initVehicles();
		initPreferences();
	
		//TODO: for now leaving this unchanged, but it needs to be changed eventually
		//Uses cars?
		carUser = false;
		for(Vehicle v : vehicles) {
			if(v.getName() == "normal_car" || v.getName() == "hybrid_car" || v.getName() == "electric_car") {
				carUser = true;
			}
		}
		
		
		
		name = String.valueOf(RandomHelper.nextDoubleFromTo(0, 2));
		name = String.valueOf(context.getObjects(Human.class).size());
		happiness = 1;
	}
	
	private void initFeatures() {
		//Choose a random gender with 50% chance
		if(RandomHelper.nextDoubleFromTo(0, 1) > 0.5) {
			gender = "female";
		}
		else {
			gender = "male";
		}
		
		//Choose a random age between 15 and 75
		age = RandomHelper.nextIntFromTo(15, 75);
		
		//Different incomes for different age groups, based on data from CBS
		if(15 <= age && age < 25) {
			//Between the ages of 15 to 25, the employment rate is 68.9%, the income is between 600 and 1200
			setIncome(0.689, 600, 1200);
		}
		else if(25 <= age && age < 55) {
			//Between the ages of 15 to 25, the employment rate is 86.0%, the income is between 1600 and 3500
			setIncome(0.860, 1600, 4500);
		}
		else if(55 <= age && age < 65) {
			//Between the ages of 15 to 25, the employment rate is 70.9%, the income is between 1600 and 3500
			setIncome(0.709, 1600, 4500);
		}	
		else {
			//Between the ages 65 and up, the employment rate is 12.9%, the income is between 1600 and 300
			setIncome(0.129, 1600, 3500);
		}
		
		//Determine if person has car license or not, based on data from CBS
		hasCarLicense = false;
		if(18 <= age && age < 20) {
			//Between the ages of 18 to 20, 40% chance of owning a car license
			if(RandomHelper.nextDoubleFromTo(0, 1) > 0.6) {
				hasCarLicense = true;
			}
		}
		else if(20 <= age && age < 30) {
			//Between the ages of 20 and 30, 75% chance of owning a car license
			if(RandomHelper.nextDoubleFromTo(0, 1) > 0.25) {
				hasCarLicense = true;
			}
		}
		else if(30 <= age && age < 70) {
			//Between the ages of 20 and 30, 86% chance of owning a car license
			if(RandomHelper.nextDoubleFromTo(0, 1) > 0.14) {
				hasCarLicense = true;
			}
		}
		else {
			//From ages 70 and up, 61% chance of owning a car license
			if(RandomHelper.nextDoubleFromTo(0, 1) > 0.39) {
				hasCarLicense = true;
			}
		}		
	}
	
	private void setIncome(double employmentRate, int minIncome, int maxIncome) {
		//If the person is employed, set income randomly between minimum and maximum income
		if(RandomHelper.nextDoubleFromTo(0,1) > (1-employmentRate)) {
			income = RandomHelper.nextIntFromTo(minIncome, maxIncome);
			isemployed = true;
		}
		//Else, the person is unemployed, and thus give a low income (so they can atleast travel a bit)
		else {
			income = 500;
		}
	}
	
	private void initVehicles() {
		//Add vehicles that this human already owns before the simulation
		//For now, does not take into account social values etc. TODO ?
		vehicles = new ArrayList<Vehicle>();
		
		// BICYCLE AND PUBLIC TRANSPORT
		//Always have a normal bicycle and "public transport" available
		vehicles.add(new Bicycle("normal"));				
		vehicles.add(new PublicTransport());
		
		// MOTORBIKE:
		//If age is over 26, 20% chance of owning a motor
		if(age >= 26 && age <= 26) {
			if(RandomHelper.nextDoubleFromTo(0, 1) > 0.8) {
				buyProduct(new Motor(), true);
			}
		}	
		
		// ELECTRIC BICYCLE
		//If age is under 55, 10% chance of owning an electric bicycle
		if(age < 55) {
			if(RandomHelper.nextDoubleFromTo(0, 1) > 0.9) {
				buyProduct(new Bicycle("electric"), true);
			}
		}
		//If age is above 55, 20% chance of owning an electric bicycle
		else {
			if(RandomHelper.nextDoubleFromTo(0, 1) > 0.8) {
				buyProduct(new Bicycle("electric"), true);
			}
		}
		
		// CARS
		//Person is age 18 or over
		if(age >= 18) {			
			//If the person has a car license, and a decent income
			if(hasCarLicense && income > 1500) {		
				//70% chance of owning a normal car, or own a normal car when income is on the lower side
				if(RandomHelper.nextDoubleFromTo(0, 1) > 0.30 || income < 2000) {
					buyProduct(new Car("normal"), true);
					
				}
				//If income is high enough, and does not own a normal car, 70% chance of owning a hybrid car
				else if(RandomHelper.nextDoubleFromTo(0, 1) > 0.30) {
					buyProduct(new Car("hybrid"), true);
				}
				//Else, own an electric car, with a random vehicle class
				else {
					int rndVehicleClass = RandomHelper.nextIntFromTo(1, 3);
					buyProduct(new Car("electric", rndVehicleClass), true);
				}
			}
		}
	}	
	
	private void initPreferences() {
		this.preference = new Preferences(0.103f, 0.023f, 0.14f, 0.132f, 0.133f, 0.136f, 0.092f, 0.112f, 0.053f, 0.088f, this);
		// TODO make values add up to 1
		// TODO incorporate cultural dimension Hofstede
		// TODO make the 5 latter values dependent on the 5 first values
		// TODO make these values different with a normal distribution
	}
	
	// Function to park all cars of this human on initialisation
	public void parkAllCars() {
		if(!carUser) {
			return;
		}
		
		//Park all cars
		GridPoint currentLocation = grid.getLocation(this);
		for(Vehicle v : vehicles) {
			if(v.isCar()) {
				ParkingSpace closest = parkCar(currentLocation, v);
				closest.setOccupied(true);
				v.setParkingSpace(closest);
			}			
		}
	}
	
	// Find a parking spot for your car and park there
	public ParkingSpace parkCar(GridPoint location, Vehicle vehicle) {
		//If we are no car user, no need to park, so just return
		if(!carUser) {
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
			GridPoint homeLocation = grid.getLocation(dwelling);
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
		if(dwelling.hasParkingSpace()) {
			ParkingSpace homeParking = dwelling.getAvailable(type);
			if(homeParking != null) {
				GridPoint homeLocation = grid.getLocation(dwelling);
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
	@ScheduledMethod(start = 1, interval = 1, priority = 3)
	public void depart() {
		GridPoint destination = getDestination();		
		GridPoint homeLocation = grid.getLocation(dwelling);
		GridPoint currentLocation = grid.getLocation(this);
				
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
		grid.moveTo(this, destination.getX(), destination.getY());
	}
		
	//Buy a product
	private void buyProduct(Vehicle vehicle, boolean initial) {		
		//Remove product from list of products and add to available vehicles
		for(Vehicle product : products) {
			if(product.equals(vehicle)) {				
				vehicles.add(vehicle);
				products.removeIf(obj -> obj.equals(product));
				break;
			}
		}
		
		//If it is not the "initial" adding of the product
		if(!initial) {
			float cost = vehicle.getPurchaseCost();
			//TODO: infect income based on this cost. 
			//Perhaps divide cost by 12 and subtract from income for some limited amount of time
			satisfaction += calcVehicleSatisfaction(vehicle);
		}		
	}
	
	//Find the most popular product in the social network
	private Vehicle mostPopularProduct() {
		int maxValue = 0;
		Vehicle mostUsed = null;

		//Find the most occurring vehicle in the neighbourhood
		for(Vehicle product : products) 
		{
			int x = findVehicleUsage(product);			
			if(x > maxValue) {
				maxValue = x;
				mostUsed = product;
			}
		}
		
		return mostUsed;
	}
	
	//Decides which vehicle to use
	public Vehicle chooseVehicle(double distance, String exclude) {		
		//Keep track of the best vehicle
		double bestUtility = 0;
		Vehicle bestVehicle = null;
		
		//Calculate utility for each vehicle available to this human
		for(Vehicle vehicle : vehicles) {
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
			utility -= environmentFactor * (distance * vehicle.getTravelEmission());
			
			//The more the cost of travelling impacts the income, the worse the utility
			utility *= (1 - (distance * vehicle.getKilometerCost())/income);
			
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
	
	//Decides which vehicle to buy, if any
	public void buyVehicle() {				
		//For now, this is put here:
		//TODO: Only calculate initial satisfaction and uncertainty after creation of all agents and networks
		//      And let this carry over every tick, so we do not have to recalculate it every time
		for(Vehicle vehicle : vehicles) {			
			satisfaction += calcVehicleSatisfaction(vehicle);
			
			//Number of agents in the network that have this vehicle
			int x = findVehicleUsage(vehicle);
			uncertainty += (1 - socialFactor)*(1 - x);			
		}
		
		//Decide whether the agent is satisfied and/or uncertain or not
		boolean satisfied = (satisfaction >= satisfactionThreshold);
		boolean uncertain = (uncertainty >= uncertaintyThreshold);
		
		//If the agent is satisfied and uncertain, perform imitation
		if(satisfied && uncertain) {
			imitate();
		}
		//If the agent is satisfied and not uncertain, perform repetition
		else if(satisfied && !uncertain) {
			repeat();			
		}
		//If the agent is not satisfied and uncertain, perform social comparison
		else if(!satisfied && uncertain) {
			compare();			
		}
		//If the agent is not satisfied and not uncertain, perform deliberation
		else if(!satisfied && !uncertain) {
			deliberate();
		}
	}
	
	//Repeat the action from before
	public void repeat() {
		//Simply choose the previous action
		switch(consumatAction) {
		case "imitate":
			imitate();
			break;
		case "deliberate":
			deliberate();
			break;
		case "compare":
			compare();
			break;
		}
	}

	//Look at what others do, and imitate them. Copy most popular product
	public void imitate() {
		consumatAction = "imitate";				
		Vehicle mostUsed = mostPopularProduct();
		
		//If we already have this product
		for(Vehicle vehicle : vehicles) {
			if(vehicle == mostUsed) {
				//TODO: Do something?
			}
		}
		
		//TODO: Check if we can afford this product
		boolean canAfford = false;
		
		//If we can afford it, buy it
		if(canAfford) {			
			buyProduct(mostUsed, false);
		}
	}

	//Calculate expected utility of buying each product. Choose the one with the highest expected utility.
	public void deliberate() {
		consumatAction = "deliberate";
		
		//Find product with highest expected utility
		float bestUtility = 0;
		Vehicle bestProduct = null;
		for(Vehicle product : products) {
			float utility = calcVehicleSatisfaction(product);
			if(utility > bestUtility) {
				bestUtility = utility;
				bestProduct = product;
			}
		}
		
		//Buy the best product
		buyProduct(bestProduct, true);
	}
	
	//Look at the most popular option in the neighbourhood, choose it if it increases the expected utility, compared with staying with the current choice
	public void compare() {		
		consumatAction = "compare";
		
		//Get most popular product
		Vehicle mostUsed = mostPopularProduct();
		
		//If we already have this product
		for(Vehicle vehicle : vehicles) {
			if(vehicle == mostUsed) {
				//TODO: Do something?
			}
		}
		
		//TODO: Check if we can afford this product
		boolean canAfford = false;
		
		//If we can afford it, buy it if it increases utility
		if(canAfford) {			
			if(calcVehicleSatisfaction(mostUsed) > 0) {
				buyProduct(mostUsed, false);
			}
		}
	}
	
	//Find the satisfaction of the given vehicle
	public float calcVehicleSatisfaction(Vehicle vehicle) {
		//Number of agents in the network that have this vehicle
		int x = findVehicleUsage(vehicle); 
		
		//Difference between vehicle characteristics and personal preferences
		//TODO: placeholder value for now, convert to real value
		float productSatisfaction = 10;
		
		//TODO: make this possibly also depend on other factors
		
		//Calculate expected satisfaction for this vehicle
		float satisfaction = socialFactor * (1 - Math.abs(productSatisfaction)) + (1 - socialFactor) * x;
		return satisfaction;
	}
	
	//Find how often the given vehicle occurs in the social network
	public int findVehicleUsage(Vehicle vehicle) {
		//TODO: replace with actual implementation
		return 10;
	}
	
	//Finds a random destination for the Human to go to
	private GridPoint getDestination() {
		GridPoint currentLocation = grid.getLocation(this);
		
		//If not home, go home
		GridPoint homeLocation = grid.getLocation(dwelling);
		if(currentLocation.getX() != homeLocation.getX() && currentLocation.getY() != homeLocation.getY()) {
			return homeLocation;
		}
		
		//Go to work if employed (40% chance)
		if(isEmployed()) {
			double goingToWork = 0.4;		
			if(RandomHelper.nextDoubleFromTo(0,1) < goingToWork) {
				// Return location of work place
				return grid.getLocation(workplace);
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

	//Return the happiness
	public float getHappiness() {
		return this.happiness;
	}

	public Object getHappinessA() {
		if(!hasChargeableCar()) {
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

	public boolean hasChargeableCar() {
		for(Vehicle v : vehicles) {
			if(v.getName() == "hybrid_car" || v.getName() == "electric_car") {
				return true;
			}
		}
		return false;		
	}
	
	public boolean isEmployed() {
		return isemployed;
	}
	
	public void setDwelling(Dwelling dwelling) {
		this.dwelling = dwelling;
	}
	
	public void setWorkplace(Workplace workplace) {
		this.workplace = workplace;
	}
	
	public void Print() {
		//Print human characteristics and vehicles
		System.out.println("\nHuman with: ");
		System.out.println("  -age = " + this.age);
		System.out.println("  -gender = " + this.gender);
		System.out.println("  -income = " + this.income);
		System.out.println("  -vehicles = ");
		for(Vehicle v : vehicles) {
			System.out.println("       -" + v.getName());
		}
		
		//Print what vehicle this human would choose for a random route
		float rndDistance = RandomHelper.nextIntFromTo(1, 100);
		Vehicle bestVehicle = chooseVehicle(rndDistance, null);
		System.out.println("If I were to travel " + rndDistance + " kilometers I would choose the " + bestVehicle.getName());
	}

	//
}
