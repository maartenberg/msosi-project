package loadPoles;

import loadPoles.GridObjects.ParkingSpace;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

public abstract class Vehicle {
	protected Parameters params = RunEnvironment.getInstance().getParameters();
	/*
	 * A human-readable name for this vehicle.
	 */
	protected String name;

	/*
	 * Represents the cost to purchase this vehicle.
	 */
	protected float purchaseCost;

	/*
	 * Represents the costs for owning this vehicle, per tick.
	 */
	protected float upkeepCost;
	/*
	 * Represents an additional cost to own this vehicle, per tick.
	 */
	protected float roadTaxCost;

	/*
	 * Represents the cost of using this vehicle, per kilometer.
	 */
	protected float kilometerCost;

	/*
	 * Represents how comfortable this vehicle is to use. Should be greater than or equal to zero, where 1 represents
	 * using a normal car.
	 */
	protected float comfort;

	/*
	 * Represents the maximum range in kilometers that can be traveled with this vehicle.
	 */
	protected float actionRadius;

	/*
	 * Represents the maximum speed of this vehicle, where 1 is the fastest, 0 is the slowest.
	 */
	protected float speed;

	/*
	 * Represents the emission of CO2 in grams per kilometer (for one person) Possible expansion / TODO: Divide by
	 * number of people (since more people take a bus, a bus is better for the environment on average)
	 */
	protected float travelEmission;

	/*
	 * Represents the emission of CO2 when purchasing this vehicle TODO: Use this
	 */
	protected float purchaseEmission;

	/*
	 * Represents the class of the vehicle, ranging from 1-3. The higher, the better the vehicle. For now, only used by
	 * electric cars.
	 */
	protected int vehicleClass;

	/*
	 * Represents the range that is left in this electric car
	 */
	protected double remainingRange;

	/*
	 * Represents the current parking space that this car is parked at
	 */
	protected ParkingSpace parkingSpace;

	/*
	 * Override equals
	 */
	public boolean equals(Vehicle obj) {
		if (obj == null) {
			return false;
		}

		if (this.getName() == obj.getName() && this.getVehicleClass() == obj.getVehicleClass()) {
			return true;
		}
		return false;
	}

	/*
	 * Getters
	 */
	public String getName() {
		return name;
	}

	public float getPurchaseCost() {
		return purchaseCost;
	}

	public float getUpkeepCosts() {
		return upkeepCost;
	}

	public float getKilometerCost() {
		return kilometerCost;
	}

	public float getComfort() {
		return comfort;
	}

	public float getActionRadius() {
		return actionRadius;
	}

	public float getSpeed() {
		return speed;
	}

	public float getTravelEmission() {
		return travelEmission;
	}

	public float getPurchaseEmission() {
		return purchaseEmission;
	}

	public boolean isCar() {
		if (getName() == "normal_car" || getName() == "hybrid_car" || getName() == "electric_car") {
			return true;
		}

		return false;
	}

	// Only relevant for electric cars for now
	public int getVehicleClass() {
		return vehicleClass;
	}

	// Only relevant for electric cars for now
	public double getRemainingRange() {
		return remainingRange;
	}

	public void setRemainingRange(double value) {
		remainingRange = value;
	}

	public void setParkingSpace(ParkingSpace ps) {
		parkingSpace = ps;
	}

	public ParkingSpace getParkingSpace() {
		return parkingSpace;
	}
}

class Motor extends Vehicle {
	public Motor() {
		name = "motor";
		purchaseCost = 7500;
		upkeepCost = 0.05f;
		kilometerCost = 0.08f;
		// comfort = 0.8f;
		actionRadius = 250;
		speed = 1;
		travelEmission = 137;
		purchaseEmission = 1_581_750;
		roadTaxCost = 0.3f * params.getFloat("roadTax");
	}
}

class Bicycle extends Vehicle {
	public Bicycle(String type) {
		switch (type) {
		case "normal":
			name = "bicycle";
			purchaseCost = 300;
			upkeepCost = 0.001f;
			kilometerCost = 0;
			// comfort = 0.3f;
			actionRadius = 15;
			speed = 0.2f;
			travelEmission = 0;
			purchaseEmission = 0;
			break;

		case "electric":
			name = "electric_bicycle";
			purchaseCost = 1000;
			upkeepCost = 0.05f;
			kilometerCost = 0.01f;
			// comfort = 0.5f;
			actionRadius = 25;
			speed = 0.5f;
			travelEmission = 5 * params.getFloat("electricityEmissionsFactor"); // Emission from the energy needed to
																				// charge
			purchaseEmission = 180_000;
			break;
		}
	}
}

class PublicTransport extends Vehicle {
	public PublicTransport() {
		name = "public_transport";
		purchaseCost = 0;
		upkeepCost = 0;
		kilometerCost = 0.17f;
		// comfort = 0.7f;
		actionRadius = 100;
		speed = 0.7f;
		travelEmission = 20;
	}
}

class Car extends Vehicle {
	/*
	 * Represents the type of the car (i.e. normal, hybrid, electric)
	 */
	private String type;

	public Car(String type, int vehicleClass) {
		this.type = type;
		this.vehicleClass = vehicleClass;
		initialise();
	}

	// Alternative constructor so we don't have to specify vehicleClass for those that don't use it
	public Car(String type) {
		this.type = type;
		this.vehicleClass = 1;
		initialise();
	}

	/*
	 * Initialisers
	 */
	private void initialise() {
		comfort = 1;
		speed = 1;

		// Choose what initialise method to use, based on the type
		switch (type) {
		case "normal":
			initNormal();
			break;
		case "hybrid":
			initHybrid();
			break;
		case "electric":
			initElectric();
		}
	}

	private void initNormal() {
		name = "normal_car"; // DieselSlurper 3000™
		purchaseCost = 20000;
		upkeepCost = 0.03f;
		kilometerCost = 0.15f + params.getFloat("fuelTax");
		actionRadius = 1200;
		travelEmission = 218; // Average of diesel and gasoline.
		purchaseEmission = 6_450_000;
		roadTaxCost = params.getFloat("roadTax");
	}

	private void initHybrid() {
		name = "hybrid_car";
		purchaseCost = 33000;
		upkeepCost = 0.03f;
		kilometerCost = 0.10f + 0.5f * params.getFloat("fuelTax");
		actionRadius = 1200;
		travelEmission = 127;
		purchaseEmission = 19_220_960;
		roadTaxCost = 0.5f * params.getFloat("roadTax");
	}

	private void initElectric() {
		name = "electric_car";
		upkeepCost = 0.01f;
		kilometerCost = 0.04f;
		purchaseEmission = 13_650_000;
		roadTaxCost = 0; // https://www.belastingdienst.nl/wps/wcm/connect/nl/auto-en-vervoer/content/hulpmiddel-motorrijtuigenbelasting-berekenen

		switch (vehicleClass) {
		case 1:
			// Based on Nissan LEAF (2019)
			purchaseCost = 38940 - params.getInteger("evSubsidy");
			actionRadius = 270;
			travelEmission = 16.5f * params.getFloat("electricityEmissionsFactor");
			break;
		case 2:
			// Based on Tesla Model 3 (Base model)
			purchaseCost = 43500 - params.getInteger("evSubsidy");
			actionRadius = 335;
			travelEmission = 14.9f * params.getFloat("electricityEmissionsFactor");
			break;
		case 3:
			// Based on Tesla Model S (Base model)
			purchaseCost = 93020 - params.getInteger("evSubsidy");
			actionRadius = 525;
			travelEmission = 17.5f * params.getFloat("electricityEmissionsFactor");
			break;
		}

		remainingRange = actionRadius;
	}
}
