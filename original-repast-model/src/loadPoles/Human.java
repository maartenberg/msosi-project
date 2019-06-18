package loadPoles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import loadPoles.GridObjects.Dwelling;
import loadPoles.GridObjects.ParkingLot;
import loadPoles.GridObjects.ParkingSpace;
import loadPoles.GridObjects.PublicBuilding;
import loadPoles.GridObjects.Workplace;
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
	 * A class that holds the traits for this human, such as income, gender, age, personal values, etc.
	 */
	HumanTraits traits;
	
	/**
	 * A class that holds the functionalities for travelling
	 */
	HumanTravel travel;
	
	/**
	 * Consumat model helper class for this human
	 */
	ConsumatModel consumat;
	
	/**
	 * Represents the different vehicles available to this Human.
	 */
	List<Vehicle> vehicles;
	
	/*
	 * Represents an array in which this humans values can be stored 
	 */
	float[] valueInit = new float[9];
	
	/* Represents this human's CO2 footprint. */
	public float totalEmissions = 0;
	
	boolean carUser;
	String name;
	Context<Object> context;
	float happiness, funds;
	AgentPreferences agentPreference;
	Dwelling dwelling;
	Workplace workplace;	
	Grid<Object> grid;

	public Human(Context<Object> context, Grid<Object> grid) {
		this.context = context;
		this.grid = grid;			
		
		traits = new HumanTraits();			
		consumat = new ConsumatModel(this, context);
		travel = new HumanTravel(this, context, grid);
		initVehicles();
		initAgentPreferences();
	
		carUser = false;
		for(Vehicle v : vehicles) {
			if(v.getName() == "normal_car" || v.getName() == "hybrid_car" || v.getName() == "electric_car") {
				carUser = true;
			}
		}		
		
		name = String.valueOf(RandomHelper.nextDoubleFromTo(0, 2));
		name = String.valueOf(context.getObjects(Human.class).size());
		happiness = 0.5f;
	}
	
	// Initialise what vehicles this human has based on their traits
	private void initVehicles() {
		//Add vehicles that this human already owns before the simulation
		//For now, does not take into account social values etc.
		vehicles = new ArrayList<Vehicle>();
		
		// BICYCLE AND PUBLIC TRANSPORT
		//Always have a normal bicycle and "public transport" available
		vehicles.add(new Bicycle("normal"));				
		vehicles.add(new PublicTransport());
		
		// MOTORBIKE:
		//If age is over 26, 20% chance of owning a motor
		if(traits.age >= 26) {
			if(RandomHelper.nextDoubleFromTo(0, 1) > 0.8) {
				consumat.buyProduct(new Motor(), true);
			}
		}	
		
		// ELECTRIC BICYCLE
		//If age is under 55, 10% chance of owning an electric bicycle
		if(traits.age < 55) {
			if(RandomHelper.nextDoubleFromTo(0, 1) > 0.9) {
				consumat.buyProduct(new Bicycle("electric"), true);
			}
		}
		//If age is 55 and over, 20% chance of owning an electric bicycle
		else {
			if(RandomHelper.nextDoubleFromTo(0, 1) > 0.8) {
				consumat.buyProduct(new Bicycle("electric"), true);
			}
		}
		
		// CARS
		//Person is age 18 or over
		if(traits.age >= 18) {			
			//If the person has a car license, and a decent income
			if(traits.hasCarLicense && traits.income >= 1500) {		
				//70% chance of owning a normal car, or own a normal car when income is on the lower side
				if(RandomHelper.nextDoubleFromTo(0, 1) > 0.30 || traits.income < 2000) {
					consumat.buyProduct(new Car("normal"), true);
					
				}
				//If income is high enough, and does not own a normal car, 70% chance of owning a hybrid car
				else if(RandomHelper.nextDoubleFromTo(0, 1) > 0.30) {
					consumat.buyProduct(new Car("hybrid"), true);
				}
				//Else, own an electric car, with a random vehicle class
				else {
					int rndVehicleClass = RandomHelper.nextIntFromTo(1, 3);
					consumat.buyProduct(new Car("electric", rndVehicleClass), true);
				}
			}
		}
	}	
	
	// Initialise the preferences that this human has
	private void initAgentPreferences() {

		for (int i = 0; i < valueInit.length/2; i ++)
		{
			float valueWeight = RandomHelper.nextIntFromTo(0, 100);
			//float valueWeight = 0.4f;
			float contrastWeight = 100 - valueWeight;
			float c = 100f; 
			
			//if (100-(c/2))
			
			int j = i+5;
			valueInit[i] = valueWeight;
			valueInit[j] = contrastWeight;	
		}
		//System.out.println("This hums values are" + valueInit);
		
		this.agentPreference = new AgentPreferences(valueInit, this);
		//this.preference = new Preferences(0.103f, 0.023f, 0.14f, 0.132f, 0.133f, 0.136f, 0.092f, 0.112f, 0.053f, 0.088f, this);
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
				ParkingSpace closest = null;
				if(v.getName() == "electric_car") {
					closest = travel.findClosestParkingSpace(currentLocation, "electric");
				}
				else {
					closest = travel.findClosestParkingSpace(currentLocation, "normal");
				}
				closest.setOccupied(true);
				v.setParkingSpace(closest);
			}			
		}
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = 1)
	public void depart() {
		travel.depart();
	}
	
	@ScheduledMethod(start = 2, interval = 2, priority = 2)
	public void buy() {
		// Update this human's preferences
		agentPreference.Update(valueInit, this, agentPreference.fluidlevels);
		
		// See if we want to buy a product
		consumat.buy();
	}
	
	@ScheduledMethod(start = 1, interval = 60, priority = 1)
	public void updateFunds() {
		funds += traits.income*0.15;
		
		System.out.println("Human " + getName() + " has funds: " + funds);
	}

	public float getHappiness() {
		return this.happiness;
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
		return traits.isemployed;
	}
	
	public void setDwelling(Dwelling dwelling) {
		this.dwelling = dwelling;
	}
	
	public void setWorkplace(Workplace workplace) {
		this.workplace = workplace;
	}
}
