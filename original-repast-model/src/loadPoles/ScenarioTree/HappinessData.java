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
	
	public float getAverageHappiness(Vehicle vehicle) {
		switch(vehicle.getName()) {
		case "bicycle":
			return avgBicycleHappiness;
		case "electric_bycicle":
			return avgElectricBicycleHappiness;
		case "motor":
			return avgMotorHappiness;
		case "public_transport":
			return avgPublicTransportHappiness;
		case "normal_car":
			return avgNormalCarHappiness;
		case "hybrid_car":
			return avgHybridCarHappiness;
		case "electric_car":
			return avgElectricCarHappiness;
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
