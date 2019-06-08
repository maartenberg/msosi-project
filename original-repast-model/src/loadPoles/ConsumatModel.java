package loadPoles;

import java.util.ArrayList;
import java.util.List;

public class ConsumatModel {
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
	
	public ConsumatModel(Human human) {
		this.human = human;		

		//Initialise list of products
		this.products = new ArrayList<Vehicle>();
		this.products.add(new Bicycle("electric"));
		this.products.add(new Motor());
		this.products.add(new Car("normal"));
		this.products.add(new Car("hybrid"));
		this.products.add(new Car("electric", 1));
		this.products.add(new Car("electric", 2));
		this.products.add(new Car("electric", 3));	
	}
			
	//Decides which vehicle to buy, if any
	public void buy() {				
		//For now, this is put here:
		//TODO: Only calculate initial satisfaction and uncertainty after creation of all agents and networks
		//      And let this carry over every tick, so we do not have to recalculate it every time
		for(Vehicle vehicle : human.vehicles) {			
			satisfaction += calcVehicleSatisfaction(vehicle);
			
			//Number of agents in the network that have this vehicle
			int x = findVehicleUsage(vehicle);
			uncertainty += (1 - human.traits.socialFactor)*(1 - x);			
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
	private void repeat() {
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
	private void imitate() {
		consumatAction = "imitate";				
		Vehicle mostUsed = mostPopularProduct();
		
		//If we already have this product
		for(Vehicle vehicle : human.vehicles) {
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
	private void deliberate() {
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
	private void compare() {		
		consumatAction = "compare";
		
		//Get most popular product
		Vehicle mostUsed = mostPopularProduct();
		
		//If we already have this product
		for(Vehicle vehicle : human.vehicles) {
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
	
	//Buy a product
	public void buyProduct(Vehicle vehicle, boolean initial) {		
		//Remove product from list of products and add to available vehicles
		for(Vehicle product : products) {
			if(product.equals(vehicle)) {				
				human.vehicles.add(vehicle);
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
	
	//Find the satisfaction of the given vehicle
	private float calcVehicleSatisfaction(Vehicle vehicle) {
		//Number of agents in the network that have this vehicle
		int x = findVehicleUsage(vehicle); 
		
		//Difference between vehicle characteristics and personal preferences
		//TODO: placeholder value for now, convert to real value
		float productSatisfaction = 10;
		
		//TODO: make this possibly also depend on other factors
		
		//Calculate expected satisfaction for this vehicle
		float satisfaction = human.traits.socialFactor * (1 - Math.abs(productSatisfaction)) + (1 - human.traits.socialFactor) * x;
		return satisfaction;
	}
	

	//Find how often the given vehicle occurs in the social network
	private int findVehicleUsage(Vehicle vehicle) {
		//TODO: replace with actual implementation
		return 10;
	}
		
	
}
