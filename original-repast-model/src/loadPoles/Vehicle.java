package loadPoles;

public class Vehicle {
	/**
	 * A human-readable name for this vehicle.
	 */
	public String name;

	/**
	 * Represents the cost to purchase this vehicle.
	 */
	public float purchaseCost;

	/**
	 * Represents the costs for owning this vehicle, per tick.
	 */
	public float upkeepCosts = 0;

	/**
	 * Represents the cost per traveled kilometer for using this vehicle.
	 */
	public float kilometerCost = 0;

	/**
	 * Represents how comfortable this vehicle is to use, per kilometer.
	 * Should be greater than or equal to zero, where 1 represents using a normal car.
	 */
	public float comfortPerKilometer = 1;

	/**
	 * Represents the maximum range in kilometers that can be traveled with this vehicle.
	 */
	public float actionRadius = 100;

    // TODO: Model impact on environment, ability to transport cargo, others?

    /**
     * Return a new Vehicle representing a bike.
     */
    static Vehicle Bicycle() {
        return Vehicle(
                "Bicycle",
                purchaseCost=30,
                upkeepCosts=0,
                kilometerCost=0,
                comfortPerKilometer=0.3,
                actionRadius=30
        );
    }

    /**
     * Return a new Vehicle representing a car.
     * Note: No distinction yet for electric/non-electric vehicles.
     */
    static Vehicle Car() {
        return Vehicle(
                "Car",
                purchaseCost=20000,
                upkeepCosts=10,
                kilometerCost=1,
                comfortPerKilometer=5.0,
                actionRadius=1200
        );
    }
}
