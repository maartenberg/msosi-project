package loadPoles;

import java.util.ArrayList;
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

	boolean carUser, hasParked;
	String carType,name;
	Context<Object> context;
	float happiness;

	public Human(Context<Object> context) {

		this.context = context;
		
		//Initialise variables and vehicles belonging to this human
		
		initFeatures();
		initVehicles();

		//TODO: for now leaving this unchanged, but it needs to be changed eventually
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
		}
		//Else, the person is unemployed, and thus the income is 0
		else {
			income = 0;
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
				vehicles.add(new Motor());
			}
		}	
		
		// ELECTRIC BICYCLE
		//If age is under 55, 10% chance of owning an electric bicycle
		if(age < 55) {
			if(RandomHelper.nextDoubleFromTo(0, 1) > 0.9) {
				vehicles.add(new Bicycle("electric"));
			}
		}
		//If age is above 55, 20% chance of owning an electric bicycle
		else {
			if(RandomHelper.nextDoubleFromTo(0, 1) > 0.8) {
				vehicles.add(new Bicycle("electric"));
			}
		}
		
		// CARS
		//Person is age 18 or over
		if(age >= 18) {			
			//If the person has a car license, and a decent income
			if(hasCarLicense && income > 1500) {		
				//70% chance of owning a normal car, or own a normal car when income is on the lower side
				if(RandomHelper.nextDoubleFromTo(0, 1) > 0.30 || income < 2000) {
					vehicles.add(new Car("normal"));
				}
				//If income is high enough, and does not own a normal car, 70% chance of owning a hybrid car
				else if(RandomHelper.nextDoubleFromTo(0, 1) > 0.30) {
					vehicles.add(new Car("hybrid"));
				}
				//Else, own an electric car, with a random vehicle class
				else {
					int rndVehicleClass = RandomHelper.nextIntFromTo(1, 3);
					vehicles.add(new Car("electric", rndVehicleClass));
				}
			}
		}
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
	
	public void buyVehicle() {
		// TODO: implement a method for deciding to buy a new vehicle based on multiple factors
	}
	
	public void chooseVehicle() {
		// TODO: implement a method of choosing a vehicle for a given route
		
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
