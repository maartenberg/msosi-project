package loadPoles;

import loadPoles.ScenarioTree.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.util.collections.IndexedIterable;

public class ConsumatModel {
	Context<Object> context;
	DataUpdater dataUpdater;

	/**
	 * Human "parent" of this class
	 */
	Human human;

	/*
	 * Represents the list of all vehicles that can be bought
	 */
	List<Vehicle> products;

	/*
	 * Represents the action taken by this human
	 */
	String consumatAction;

	/*
	 * Represents the current satisfaction and uncertainty levels for this agent
	 */
	float satisfaction;
	float uncertainty;
	double socialFactor;
	boolean socialFactorInitialised;

	/*
	 * Represents at what threshold the agent considers itself satisfied or uncertain
	 */
	float satisfactionThreshold;
	float uncertaintyThreshold;

	public ConsumatModel(Human human, Context<Object> context) {
		this.human = human;
		this.context = context;

		// Initialise data updater
		IndexedIterable<Object> dataUpdaters = context.getObjects(DataUpdater.class);
		Iterator<Object> dataUpdaterIterator = dataUpdaters.iterator();
		while (dataUpdaterIterator.hasNext()) {
			this.dataUpdater = (DataUpdater) dataUpdaterIterator.next();
		}

		// Initialise parameters
		Parameters params = RunEnvironment.getInstance().getParameters();
		uncertaintyThreshold = params.getFloat("consumatUncertaintyThreshold");
		satisfactionThreshold = params.getFloat("consumatSatisfactionThreshold");
		consumatAction = "deliberate";

		// Initialise list of products
		this.products = new ArrayList<Vehicle>();
		this.products.add(new Bicycle("electric"));

		if (human.traits.hasCarLicense) {
			this.products.add(new Motor());
			this.products.add(new Car("normal"));
			this.products.add(new Car("hybrid"));
			this.products.add(new Car("electric", 1));
			this.products.add(new Car("electric", 2));
			this.products.add(new Car("electric", 3));
		}
	}

	// Initialise social factor
	private void initSocialFactor() {
		// if value 1 is the most important, the social factor is highest
		if (human.agentType == 1) {
			socialFactor = RandomHelper.nextDoubleFromTo(0.7, 1);
			socialFactorInitialised = true;
		}
		// if value 2 is the most important, the social factor might also be high
		if (human.agentType == 2) {
			socialFactor = RandomHelper.nextDoubleFromTo(0.45, 0.95);
			socialFactorInitialised = true;
		}
		// if value 1 is the most important, the social factor is most likely low or average
		if (human.agentType == 3) {
			socialFactor = RandomHelper.nextDoubleFromTo(0.2, 0.6);
			socialFactorInitialised = true;
		}
		// if value 3 is the most important, the social factor will be low
		if (human.agentType == 4) {
			socialFactor = RandomHelper.nextDoubleFromTo(0, 0.35);
			socialFactorInitialised = true;
		} else {
			socialFactor = RandomHelper.nextDoubleFromTo(0, 1);
		}
	}

	// Decides which vehicle to buy, if any
	public void buy() {
		if (!socialFactorInitialised) {
			initSocialFactor();
		}
		// Calculate satisfaction and uncertainty
		satisfaction = 0;
		uncertainty = 0;
		for (Vehicle vehicle : human.vehicles) {
			satisfaction += calcVehicleSatisfaction(vehicle);

			// Number of agents in the network that have this vehicle
			double x = findVehicleUsage(vehicle);
			uncertainty += (1 - socialFactor) * (1 - x);
		}

		// Decide whether the agent is satisfied and/or uncertain or not
		boolean satisfied = (satisfaction >= satisfactionThreshold);
		boolean uncertain = (uncertainty >= uncertaintyThreshold);

		// If the agent is satisfied and uncertain, perform imitation
		if (satisfied && uncertain) {
			imitate();
		}
		// If the agent is satisfied and not uncertain, perform repetition
		else if (satisfied && !uncertain) {
			repeat();
		}
		// If the agent is not satisfied and uncertain, perform social comparison
		else if (!satisfied && uncertain) {
			compare();
		}
		// If the agent is not satisfied and not uncertain, perform deliberation
		else if (!satisfied && !uncertain) {
			deliberate();
		}
	}

	// Repeat the action from before
	private void repeat() {
		// Simply choose the previous action
		switch (consumatAction) {
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

	// Look at what others do, and imitate them. Copy most popular product
	private void imitate() {
		consumatAction = "imitate";
		Vehicle mostUsed = mostPopularProduct();

		// If we already have this product
		for (Vehicle vehicle : human.vehicles) {
			if (vehicle.equals(mostUsed)) {
				// No action needed.
				return;
			}
		}

		// If we can afford it, buy it
		if (mostUsed != null && canAfford(mostUsed)) {
			buyProduct(mostUsed, false);
		}
	}

	// Calculate expected utility of buying each product. Choose the one with the highest expected utility.
	private void deliberate() {
		consumatAction = "deliberate";

		// Find product with highest expected utility
		double bestUtility = 0;
		Vehicle bestProduct = null;
		for (Vehicle product : products) {
			double utility = calcVehicleSatisfaction(product);
			if (utility > bestUtility) {
				bestUtility = utility;
				bestProduct = product;
			}
		}

		// Buy the best product, if there is one, and if we can afford it
		if (bestProduct != null && canAfford(bestProduct)) {
			buyProduct(bestProduct, true);
		}
	}

	// Look at the most popular option in the neighbourhood, only choose it if it increases the expected utility,
	// compared with staying with the current choice
	private void compare() {
		consumatAction = "compare";

		// Get most popular product
		Vehicle mostUsed = mostPopularProduct();

		// If we already have this product
		for (Vehicle vehicle : human.vehicles) {
			if (vehicle.equals(mostUsed)) {
				// No action needed.
				return;
			}
		}

		// If we can afford it, buy it if it increases utility
		if (mostUsed != null && canAfford(mostUsed) && calcVehicleSatisfaction(mostUsed) > 0) {
			buyProduct(mostUsed, false);
		}
	}

	// Buy a product
	public void buyProduct(Vehicle vehicle, boolean initial) {
		// Human can only have one car
		Vehicle oldCar = null;
		if (vehicle.isCar()) {
			for (Vehicle v : human.vehicles) {
				if (v.isCar()) {
					oldCar = v;
					human.vehicles.removeIf(obj -> obj.equals(v));
					break;
				}
			}
		}

		// Remove product from list of products and add to available vehicles
		for (Vehicle product : products) {
			if (product.equals(vehicle)) {
				if (!initial) {
					human.totalEmissions += vehicle.getPurchaseEmission();
					human.funds -= vehicle.getPurchaseCost();
				}
				human.vehicles.add(product);
				products.removeIf(obj -> obj.equals(product));
				break;
			}
		}

		if (oldCar != null) {
			products.add(oldCar);
			dataUpdater.carRemoveUpdate(oldCar);
		}

		dataUpdater.purchaseUpdate(vehicle, initial);
	}

	// Returns true if the human can afford the vehicle
	private boolean canAfford(Vehicle product) {
		return product.getPurchaseCost() < human.funds;
	}

	// Find the most popular product in the social network
	private Vehicle mostPopularProduct() {
		double maxValue = 0;
		Vehicle mostUsed = null;

		// Find the most occurring vehicle in the humans social network
		for (Vehicle product : products) {
			double x = findVehicleUsage(product);
			if (x >= maxValue) {
				maxValue = x;
				mostUsed = product;
			}
		}

		return mostUsed;
	}

	// Find the satisfaction of the given vehicle
	private double calcVehicleSatisfaction(Vehicle vehicle) {
		// Number of agents in the network that have this vehicle
		double x = findVehicleUsage(vehicle);

		// Average utility of the used vehicles
		float avgUsageUtil = dataUpdater.hd.getAverageHappiness(vehicle);

		// Difference between vehicle characteristics and personal preferences
		double productSatisfaction = avgUsageUtil * human.agentPreference.getUtilityFactor(vehicle);

		// Calculate expected satisfaction for this vehicle
		double satisfaction = socialFactor * (1 - Math.abs(productSatisfaction)) + (1 - socialFactor) * x;
		return satisfaction;
	}

	// Find how often the given vehicle occurs in the social network
	private double findVehicleUsage(Vehicle vehicle) {
		// Find humans from social network
		Network<Object> socialNetwork = (Network<Object>) this.context.getProjection("socialnetwork");
		Iterable<RepastEdge<Object>> contacts = socialNetwork.getOutEdges(human);
		Iterator<RepastEdge<Object>> contactsIterator = contacts.iterator();

		// Count how many use the given vehicle
		int count = 0;
		int total = 0;
		while (contactsIterator.hasNext()) {
			Human contact = (Human) contactsIterator.next().getTarget();
			total += contact.vehicles.size();
			for (Vehicle v : contact.vehicles) {
				if (vehicle.equals(v)) {
					count++;
					break;
				}
			}
		}

		if (total != 0) {
			return (double) count / total;
		}
		return 0;
	}
}
