package loadPoles;

import java.io.IOException;
import java.util.ArrayList;
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
	ParkingSpace currentParkingSpot;
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
		float[] valueInit = new float[9];
		for (int i = 0; i < valueInit.length/2; i ++)
		{
			float valueWeight = RandomHelper.getSeed();
			float contrastWeight = RandomHelper.getSeed();
			float c = 100f; 
			
			//if (100-(c/2))
			
			int j = i+5;
			valueInit[i] = valueWeight;
			valueInit[j] = contrastWeight;
					
		}
		
		this.preference = new Preferences (valueInit, this);
		//this.preference = new Preferences(0.103f, 0.023f, 0.14f, 0.132f, 0.133f, 0.136f, 0.092f, 0.112f, 0.053f, 0.088f, this);
		// TODO make values add up to 1
		// TODO incorporate cultural dimension Hofstede
		// TODO make the 5 latter values dependent on the 5 first values
		// TODO make these values different with a normal distribution
	}
	
	//TODO new method for this (without scheduler, just call at depart in the TODO when human is moving)
	//Given a destination, make method to park car at closest parking lot, or at own dwelling if available
	/*
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
	} */

	public void parkCar(GridPoint location) {
		//TODO: see above
		if(!carUser) {
			return;
		}
		
		//Else, find closest parking spot (either in a parking lot, or at its own dwelling
		ParkingSpace bestSpot = null;
		
		//update currentParkingSpot
		currentParkingSpot = bestSpot;
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 3)
	public void depart() {
		GridPoint destination = getDestination();		
		GridPoint homeLocation = grid.getLocation(dwelling);
		GridPoint currentLocation = grid.getLocation(this);
				
		Vehicle vehicle = null;
		//If destination is home location, choose same vehicle as when traveling from home to destination
		if(destination.getX() == homeLocation.getX() && destination.getY() == homeLocation.getY()) {
			vehicle = pastVehicle;
		}
		else {
			//Find distance and choose appropriate vehicle
			double distance = grid.getDistance(currentLocation, destination);
			vehicle = chooseVehicle(distance);
		}
		
		if(vehicle.getName() == "normal_car" || vehicle.getName() == "normal_car" || vehicle.getName() == "electric_car") {
			//TODO:
			//Unpark current vehicle
			//currentParkingSpot.setOccupied(false);
			//Find new spot to park near location			
			parkCar(destination);
		}		
		
		pastVehicle = vehicle;
		System.out.println("Travelling to: (" + destination.getX() + ", " + destination.getY() 
							+ ")\n from: (" + currentLocation.getX() + ", " + currentLocation.getY() + ")"
							+ "\n with distance: " + grid.getDistance(currentLocation, destination) 
							+ "\n and vehicle: " + vehicle.getName() +"\n");
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
	public Vehicle chooseVehicle(double distance) {		
		//Keep track of the best vehicle
		double bestUtility = 0;
		Vehicle bestVehicle = null;
		
		//Calculate utility for each vehicle available to this human
		for(Vehicle vehicle : vehicles) {
			//TODO: find a good starting value
			//Start with a utility of 1000
			double utility = 5000;
			
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
			
			//The more the cost impacts the income, the worse the utility
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
		Vehicle bestVehicle = chooseVehicle(rndDistance);
		System.out.println("If I were to travel " + rndDistance + " kilometers I would choose the " + bestVehicle.getName());
	}

	//
}
