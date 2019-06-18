package loadPoles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;

public class ConsumatModel {
	Context<Object> context;
	Network<Object> consumerMarket;
	
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
	
	/*
	 * Represents at what threshold the agent considers itself satisfied or uncertain
	 */
	float satisfactionThreshold;
	float uncertaintyThreshold;
	
	public ConsumatModel(Human human, Context<Object> context) {
		this.human = human;		
		this.context = context;
		//this.consumerMarket = (Network<Object>) this.context.getProjection("consumermarket");		
		
		consumatAction = "deliberate";
		
		//Initialise list of products
		this.products = new ArrayList<Vehicle>();
		this.products.add(new Bicycle("electric"));
		
		if(human.traits.hasCarLicense) {
			this.products.add(new Motor());
			this.products.add(new Car("normal"));
			this.products.add(new Car("hybrid"));
			this.products.add(new Car("electric", 1));
			this.products.add(new Car("electric", 2));
			this.products.add(new Car("electric", 3));
		}
	}
			
	// Decides which vehicle to buy, if any
	public void buy() {	
		// Calculate satisfaction and uncertainty
		satisfaction = 0;
		uncertainty = 0;
		for(Vehicle vehicle : human.vehicles) {			
			satisfaction += calcVehicleSatisfaction(vehicle);
			
			//Number of agents in the network that have this vehicle
			int x = findVehicleUsage(vehicle);
			uncertainty += (1 - human.traits.socialFactor)*(1 - x);			
		}
		
		// Decide whether the agent is satisfied and/or uncertain or not
		boolean satisfied = (satisfaction >= satisfactionThreshold);
		boolean uncertain = (uncertainty >= uncertaintyThreshold);
		
		// If the agent is satisfied and uncertain, perform imitation
		if(satisfied && uncertain) {
			imitate();
		}
		// If the agent is satisfied and not uncertain, perform repetition
		else if(satisfied && !uncertain) {
			repeat();			
		}
		// If the agent is not satisfied and uncertain, perform social comparison
		else if(!satisfied && uncertain) {
			compare();			
		}
		// If the agent is not satisfied and not uncertain, perform deliberation
		else if(!satisfied && !uncertain) {
			deliberate();
		}
	}
	
	// Repeat the action from before
	private void repeat() {
		// Simply choose the previous action
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

	// Look at what others do, and imitate them. Copy most popular product
	private void imitate() {
		consumatAction = "imitate";				
		Vehicle mostUsed = mostPopularProduct();
		
		//If we already have this product
		for(Vehicle vehicle : human.vehicles) {
			if(vehicle == mostUsed) {
				// No action needed.
				return;
			}
		}
		
		//If we can afford it, buy it
		if(canAfford(mostUsed)) {			
			buyProduct(mostUsed, false);
		}
	}

	// Calculate expected utility of buying each product. Choose the one with the highest expected utility.
	private void deliberate() {
		consumatAction = "deliberate";
		
		// Find product with highest expected utility
		double bestUtility = 0;
		Vehicle bestProduct = null;
		for(Vehicle product : products) {
			double utility = calcVehicleSatisfaction(product);
			System.out.println(utility);
			if(utility > bestUtility) {
				bestUtility = utility;
				bestProduct = product;
			}
		}

		// Buy the best product, if there is one, and if we can afford it
		if(bestProduct != null && canAfford(bestProduct)) {
			buyProduct(bestProduct, true);
		}		
	}
	
	// Look at the most popular option in the neighbourhood, only choose it if it increases the expected utility, compared with staying with the current choice
	private void compare() {		
		consumatAction = "compare";
		
		// Get most popular product
		Vehicle mostUsed = mostPopularProduct();
		
		// If we already have this product
		for(Vehicle vehicle : human.vehicles) {
			if(vehicle == mostUsed) {
				// No action needed.
				return;
			}
		}
		
		//If we can afford it, buy it if it increases utility
		if(canAfford(mostUsed) || calcVehicleSatisfaction(mostUsed) > 0) {
			buyProduct(mostUsed, false);
		}		
	}
	
	// Buy a product
	public void buyProduct(Vehicle vehicle, boolean initial) {			
		//Network<Object> consumerMarket = (Network<Object>) this.context.getProjection("consumermarket");	
		//Remove product from list of products and add to available vehicles
		for(Vehicle product : products) {
			if(product.equals(vehicle)) {				
				human.totalEmissions += vehicle.getPurchaseEmission();
				human.vehicles.add(product);
				products.removeIf(obj -> obj.equals(product));
				//consumerMarket.addEdge(human, product);
				break;
			}
		}
		
		//If it is not the "initial" adding of the product, affect human funds
		if(!initial) {
			human.funds -= vehicle.getPurchaseCost();	
		}		
	}
	
	// Returns true if the human can afford the vehicle
	private boolean canAfford(Vehicle product) {
		return product.getPurchaseCost() < human.funds;
	}
	
	// Find the most popular product in the social network
	private Vehicle mostPopularProduct() {
		int maxValue = 0;
		Vehicle mostUsed = null;

		//Find the most occurring vehicle in the humans social network
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
	
	// Find the satisfaction of the given vehicle
	private double calcVehicleSatisfaction(Vehicle vehicle) {
		// Number of agents in the network that have this vehicle
		int x = findVehicleUsage(vehicle); 
		
		// Difference between vehicle characteristics and personal preferences
		double productSatisfaction = human.agentPreference.getUtilityFactor(vehicle);
		
		// TODO Anouk: make this possibly also depend on other factors
		// i.e. Happiness of humans when traveling
		
		// Calculate expected satisfaction for this vehicle
		double satisfaction = human.traits.socialFactor * (1 - Math.abs(productSatisfaction)) + (1 - human.traits.socialFactor) * x;
		return satisfaction;
	}
	
	// Find how often the given vehicle occurs in the social network
	private int findVehicleUsage(Vehicle vehicle) {
		// Find humans from social network
		Network<Object> socialNetwork = (Network<Object>) this.context.getProjection("socialnetwork");
		Iterable<RepastEdge<Object>> contacts = socialNetwork.getOutEdges(human);
		Iterator<RepastEdge<Object>> contactsIterator = contacts.iterator();
		
		// Count how many use the given vehicle
		int count = 0;
		while(contactsIterator.hasNext()) {
			Human contact = (Human) contactsIterator.next().getTarget();
			for(Vehicle v : contact.vehicles) {
				if(vehicle.equals(v)) {
					count++;
					break;
				}
			}
		}		
		return count;
	}	
}
