package loadPoles;

import java.util.ArrayList;
import java.util.List;

import loadPoles.GridObjects.Dwelling;
import loadPoles.GridObjects.ParkingSpace;
import loadPoles.GridObjects.Workplace;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Human {
	// A class that holds the traits for this human, such as income, gender, age, personal values, etc.
	HumanTraits traits;

	// A class that holds the functionalities for traveling
	HumanTravel travel;

	// CONSUMAT model helper class for this human
	ConsumatModel consumat;

	// A class that keeps track of this human's preferences
	AgentPreferences agentPreference;

	// Represents the different vehicles available to this Human
	List<Vehicle> vehicles;

	// Represents this human's main value section
	protected int agentType;

	// Context, grid, human's dwelling and workplace on grid
	Context<Object> context;
	Grid<Object> grid;
	Dwelling dwelling;
	Workplace workplace;
	
	boolean carUser;
	String name;
	float happiness, funds, totalEmissions;

	public Human(Context<Object> context, Grid<Object> grid) {
		this.context = context;
		this.grid = grid;

		traits = new HumanTraits();
		consumat = new ConsumatModel(this, context);
		travel = new HumanTravel(this, context, grid);
		agentPreference = new AgentPreferences(this);
		initVehicles();

		// Set car user to true if human has a car
		carUser = false;
		for (Vehicle v : vehicles) {
			if (v.isCar()) {
				carUser = true;
				break;
			}
		}

		// Get unique name for this human
		name = String.valueOf(context.getObjects(Human.class).size());
		happiness = 0;
	}

	// Initialise what vehicles this human has based on their traits
	private void initVehicles() {
		// Add vehicles that this human already owns before the simulation
		vehicles = new ArrayList<Vehicle>();

		// BICYCLE AND PUBLIC TRANSPORT
		// Always have a normal bicycle and "public transport" available
		vehicles.add(new Bicycle("normal"));
		vehicles.add(new PublicTransport());

		// MOTORBIKE:
		// If age is over 26, 20% chance of owning a motor
		if (traits.age >= 26) {
			if (RandomHelper.nextDoubleFromTo(0, 1) < 0.2) {
				consumat.buyProduct(new Motor(), true);
			}
		}

		// ELECTRIC BICYCLE
		// If age is under 55, 10% chance of owning an electric bicycle
		if (traits.age < 55) {
			if (RandomHelper.nextDoubleFromTo(0, 1) < 0.1) {
				consumat.buyProduct(new Bicycle("electric"), true);
			}
		}
		// If age is 55 and over, 20% chance of owning an electric bicycle
		else {
			if (RandomHelper.nextDoubleFromTo(0, 1) < 0.2) {
				consumat.buyProduct(new Bicycle("electric"), true);
			}
		}

		// CARS
		// Person is age 18 or over
		if (traits.age >= 18) {
			// If the person has a car license, and a decent income
			if (traits.hasCarLicense && traits.income >= 1500) {
				// 70% chance of owning a normal car, or own a normal car when income is on the lower side
				if (RandomHelper.nextDoubleFromTo(0, 1) < 0.70 || traits.income < 2000) {
					consumat.buyProduct(new Car("normal"), true);

				}
				// If income is high enough, and does not own a normal car, 70% chance of owning a hybrid car
				else if (RandomHelper.nextDoubleFromTo(0, 1) < 0.70) {
					consumat.buyProduct(new Car("hybrid"), true);
				}
				// Else, own an electric car, with a random vehicle class
				else {
					int rndVehicleClass = RandomHelper.nextIntFromTo(1, 3);
					consumat.buyProduct(new Car("electric", rndVehicleClass), true);
				}
			}
		}
	}

	// Function to park all cars of this human on initialisation
	public void parkAllCars() {
		if (!carUser) {
			return;
		}

		// Park all cars
		GridPoint currentLocation = grid.getLocation(this);
		for (Vehicle v : vehicles) {
			if (v.isCar()) {
				// Find closest parking space. Loading pole if car is electric, else normal space
				ParkingSpace closest = null;
				if (v.getName() == "electric_car") {
					closest = travel.findClosestParkingSpace(currentLocation, "electric");
				} else {
					closest = travel.findClosestParkingSpace(currentLocation, "normal");
				}
				closest.setOccupied(true);
				v.setParkingSpace(closest);
			}
		}
	}

	// Update this human's funds every month (60 ticks)
	@ScheduledMethod(start = 1, interval = 60, priority = 1)
	public void updateFunds() {
		// Add small amount of monthly income to funds
		funds += traits.income * 0.20;

		// Pay road tax
		for (Vehicle v : vehicles) {
			funds -= v.roadTaxCost;
		}
	}

	// Make this human travel somewhere (every tick)
	@ScheduledMethod(start = 1, interval = 1, priority = 2)
	public void depart() {
		travel.depart();
	}

	// Make this human buy something (or not), every 2 years (1440 ticks)
	@ScheduledMethod(start = 1440, interval = 1440, priority = 3)
	public void buy() {
		consumat.buy();
	}

	// Update this human's preferences (every tick)
	@ScheduledMethod(start = 1, interval = 1, priority = 4)
	public void updatePreferences() {
		agentPreference.update();
	}

	// Get the name of this human
	public String getName() {
		return this.name;
	}

	// True if human has chargable car
	public boolean hasChargeableCar() {
		for (Vehicle v : vehicles) {
			if (v.getName() == "hybrid_car" || v.getName() == "electric_car") {
				return true;
			}
		}
		return false;
	}

	// True if human is employed
	public boolean isEmployed() {
		return traits.isemployed;
	}

	// Set human's dwelling
	public void setDwelling(Dwelling dwelling) {
		this.dwelling = dwelling;
	}

	// Set human's workplace
	public void setWorkplace(Workplace workplace) {
		this.workplace = workplace;
	}
}
