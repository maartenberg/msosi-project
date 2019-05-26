package loadPoles;

public abstract class Vehicle {
	/*
	 * A human-readable name for this vehicle.	 
	 */
	protected String name;	

	/*
	 * Represents the cost to purchase this vehicle.	 
	 */
	protected float purchaseCost;

	/*
	 * Represents the costs for owning this vehicle, per kilometer.
	 */
	protected float upkeepCost;
	
	/*
	 * Represents the cost of using this vehicle, per kilometer.
	 */
	protected float kilometerCost;

	/*
	 * Represents how comfortable this vehicle is to use, per kilometer.
	 * Should be greater than or equal to zero, where 1 represents using a normal car.	 
	 */
	protected float comfortPerKilometer;

	/*
	 * Represents the maximum range in kilometers that can be traveled with this vehicle.
	 */
	protected float actionRadius;	
	
	/*
	 * Represents the maximum speed of this vehicle, where 1 is the fastest, 0 is the slowest.
	 * TODO: not implemented yet, since I don't know if this is necessary
	 */
	protected float speed;

	/*Represents the emission of CO2 in grams per kilometer (for one person)	
	 *Divide by number of people (since more people take a bus, a bus is better for the environment on average)
	 *TODO: Split into travelEmission and productionEmission ?)
	*/
	protected float emission;
	
    /*
     * TODO: Model impact on environment further by splitting emission, ability to transport cargo, others?
     */
	
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
		return comfortPerKilometer;
	}
	
	public float getActionRadius() {
		return actionRadius;
	}
	
	public float getMaxSpeed() {
		return speed;
	}
	
	public float getEmission() {
		return emission;
	}
	
}

class Motor extends Vehicle{
	public Motor() {
		name = "motor";
		purchaseCost = 7500;
		upkeepCost = 0.05f;
		kilometerCost = 0.08f;
		comfortPerKilometer = 0.8f;
		actionRadius = 250;		
		speed = 1;
		emission = 137;
	}
}

class Bicycle extends Vehicle {
	public Bicycle(String type) {
		switch(type) {
		case "normal":
			name = "bicycle";		
			purchaseCost = 300;
			upkeepCost = 0.001f;
			kilometerCost = 0;
			comfortPerKilometer = 0.3f;
			actionRadius = 15;		
			//speed = 4.17f;
			speed = 0.2f;
			emission = 0;			
			break;
			
		case "electric":
			name = "electric_bicycle";		
			purchaseCost = 1000;
			upkeepCost = 0.05f;
			kilometerCost = 0.01f;
			comfortPerKilometer = 0.5f;
			actionRadius = 60;	
			//speed = 6.94f;
			speed = 0.5f;
			emission = 5; //Emission from the energy needed to charge
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
		comfortPerKilometer = 0.7f;
		actionRadius = 100;	
		speed = 0.7f;
		emission = 20;  //Emission from the energy needed to charge
	}
}

class Car extends Vehicle {
	/*
	 * Represents the type of the car (i.e. normal, hybrid, electric)
	 */
	private String type;
	
	/*
	 * Represents the class of the vehicle. Ranges from 1 to 3
	 * The higher, the better (and more expensive) the car is
	 * In the case of electric cars, this means more range
	 */
	private int vehicleClass;
	
	public Car(String type, int vehicleClass) {
		this.type = type;
		this.vehicleClass = vehicleClass;
		initialise();		
	}
	
	//Alternative constructor so we don't have to specify vehicleClass for those that don't use it
	public Car(String type) {
		this.type = type;
		this.vehicleClass = 1;
		initialise();
	}
	
	/*
	 * Initialisers
	 */
	private void initialise() {
		comfortPerKilometer = 1;
		speed = 1;
		
		//Choose what initialise method to use, based on the type
		switch(type) {
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
		/*
		 * TODO Optionally: 
		 *   -Add different vehicleClass implementations for a normal car
		 */
		name="normal_car";
		purchaseCost = 20000;
		upkeepCost = 0.03f;
		kilometerCost = 0.15f;
		actionRadius = 1200;				
		emission = 130;
	}
	
	private void initHybrid() {
		/*
		 * TODO: Optionally:
		 *   -There are two hybrid types: Plug-in Hybrid and normal Hybrid. Implement this?		
		 *   -Add different vehicleClass implementations for a hybrid car
		*/
		name="hybrid_car";	
		purchaseCost = 20000;
		upkeepCost = 0.03f;
		kilometerCost = 0.10f;
		actionRadius = 1200;
		emission = 100;
	}
	
	private void initElectric() {
		name="electric_car";
		emission = 23;  //Emission from the energy used to charge
		upkeepCost = 0.01f;
		kilometerCost = 0.04f;
		
		switch(vehicleClass) {		
		case 1:
			//Based on Nissan LEAF (2019)
			purchaseCost = 38940;	
			actionRadius = 270;		
			break;
		case 2:	
			//Based on Tesla Model 3 (Base model)
			purchaseCost = 43500;	
			actionRadius = 335;		
			break;
		case 3:
			//Based on Tesla Model S (Base model)
			purchaseCost = 93020;	
			actionRadius = 525;		
			break;
		}		
	}

	/*
	 * Getters
	 */
	public String getType() {
		return type;
	}
	
	public int getVehicleClass() {
		return vehicleClass;
	}
}



