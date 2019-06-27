package loadPoles.ScenarioTree;

import loadPoles.Vehicle;

public class HappinessData {
	protected float totalHappiness;
	protected float bicycleHappiness;
	protected float electricBicycleHappiness;
	protected float motorHappiness;
	protected float publicTransportHappiness;
	protected float normalCarHappiness;
	protected float hybridCarHappiness;
	protected float electricCarHappiness;
	
	protected float avgBicycleHappiness;
	protected float avgElectricBicycleHappiness;
	protected float avgMotorHappiness;
	protected float avgPublicTransportHappiness;
	protected float avgNormalCarHappiness;
	protected float avgHybridCarHappiness;
	protected float avgElectricCarHappiness;
	
	public float getTotalHappiness() {
		return totalHappiness;
	}
	
	public float getBicycleHappiness() {
		return bicycleHappiness;
	}
	
	public float getElectricBicycleHappiness() {
		return electricBicycleHappiness;
	}
	
	public float getMotorHappiness() {
		return motorHappiness;
	}
	
	public float getPublicTransportHappiness() {
		return publicTransportHappiness;
	}
	
	public float getNormalCarHappiness() {
		return normalCarHappiness;
	}
	
	public float getHybridCarHappiness() {
		return hybridCarHappiness;
	}
	
	public float getElectricCarHappiness() {
		return electricCarHappiness;
	}
	
	// Get average happiness for a vehicle based on total happiness
	public float getAverageHappiness(Vehicle vehicle) {
		switch(vehicle.getName()) {
		case "bicycle":
			return bicycleHappiness/totalHappiness;
		case "electric_bicycle":
			return electricBicycleHappiness/totalHappiness;
		case "motor":
			return motorHappiness/totalHappiness;
		case "public_transport":
			return publicTransportHappiness/totalHappiness;
		case "normal_car":
			return normalCarHappiness/totalHappiness;
		case "hybrid_car":
			return hybridCarHappiness/totalHappiness;
		case "electric_car":
			return electricCarHappiness/totalHappiness;
		}		
		return 0;
	}
	
	public float getAvgBicycleHappiness() {
		return avgBicycleHappiness;
	}
	
	public float getAvgElectricBicycleHappiness() {
		return avgElectricBicycleHappiness;
	}
	
	public float getAvgMotorHappiness() {
		return avgMotorHappiness;
	}
	
	public float getAvgPublicTransportHappiness() {
		return avgPublicTransportHappiness;
	}
	
	public float getAvgNormalCarHappiness() {
		return avgNormalCarHappiness;
	}
	
	public float getAvgHybridCarHappiness() {
		return avgHybridCarHappiness;
	}
	
	public float getAvgElectricCarHappiness() {
		return avgElectricCarHappiness;
	}
}
