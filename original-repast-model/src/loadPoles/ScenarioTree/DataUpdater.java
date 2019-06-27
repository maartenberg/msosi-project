package loadPoles.ScenarioTree;

import java.util.Iterator;

import loadPoles.Route;
import loadPoles.Vehicle;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.util.collections.IndexedIterable;

public class DataUpdater {
	Context<Object> context;
	public EmissionData ed;
	public VehicleUsageData vud;
	public HappinessData hd;
	public VehiclePossessionData vpd;

	public DataUpdater(Context<Object> context) {
		this.context = context;

		ed = new EmissionData();
		this.context.add(ed);

		vud = new VehicleUsageData();
		this.context.add(vud);

		hd = new HappinessData();
		this.context.add(hd);

		vpd = new VehiclePossessionData();
		this.context.add(vpd);
	}

	// Update data values every tick
	@ScheduledMethod(start = 1, interval = 1, priority = 5)
	public void travelUpdate() {
		IndexedIterable<Object> allRoutes = context.getObjects(Route.class);
		Iterator<Object> allRoutesIterator = allRoutes.iterator();

		while (allRoutesIterator.hasNext()) {
			Route route = (Route) allRoutesIterator.next();
			Vehicle vehicle = route.getVehicle();
			double emission = vehicle.getTravelEmission() * route.getTravelDistance();
			double utility = route.getUtility();

			ed.totalEmission += emission;
			ed.totalDistance += route.getTravelDistance();
			hd.totalHappiness += utility;

			switch (vehicle.getName()) {
			case "bicycle":
				vud.bicycleUsage++;
				hd.bicycleHappiness += utility;
				hd.avgBicycleHappiness = hd.bicycleHappiness / vud.bicycleUsage;
				break;
			case "electric_bicycle":
				ed.electricBicycleEmission += emission;
				vud.electricBicycleUsage++;
				hd.electricBicycleHappiness += utility;
				ed.avgElectricBicycleEmission = ed.electricBicycleEmission / vud.electricBicycleUsage;
				hd.avgElectricBicycleHappiness = hd.electricBicycleHappiness / vud.electricBicycleUsage;
				break;
			case "motor":
				ed.motorEmission += emission;
				vud.motorUsage++;
				hd.motorHappiness += utility;
				ed.avgMotorEmission = ed.motorEmission / vud.motorUsage;
				hd.avgMotorHappiness = hd.motorHappiness / vud.motorUsage;
				break;
			case "public_transport":
				ed.publicTransportEmission += emission;
				vud.publicTransportUsage++;
				hd.publicTransportHappiness += utility;
				ed.avgPublicTransportEmission = ed.publicTransportEmission / vud.publicTransportUsage;
				hd.avgPublicTransportHappiness = hd.publicTransportHappiness / vud.publicTransportUsage;
				break;
			case "normal_car":
				ed.normalCarEmission += emission;
				vud.normalCarUsage++;
				hd.normalCarHappiness += utility;
				ed.avgNormalCarEmission = ed.normalCarEmission / vud.normalCarUsage;
				hd.avgNormalCarHappiness = hd.normalCarHappiness / vud.normalCarUsage;
				break;
			case "hybrid_car":
				ed.hybridCarEmission += emission;
				vud.hybridCarUsage++;
				hd.hybridCarHappiness += utility;
				ed.avgHybridCarEmission = ed.hybridCarEmission / vud.hybridCarUsage;
				hd.avgHybridCarHappiness = hd.hybridCarHappiness / vud.hybridCarUsage;
				break;
			case "electric_car":
				ed.electricCarEmission += emission;
				vud.electricCarUsage++;
				hd.electricCarHappiness += utility;
				ed.avgElectricCarEmission = ed.electricCarEmission / vud.electricCarUsage;
				hd.avgElectricCarHappiness = hd.electricCarHappiness / vud.electricCarUsage;
				break;
			}
		}
	}

	// Update data values every time something is purchased
	public void purchaseUpdate(Vehicle vehicle, boolean initial) {
		if (!initial)
			ed.totalEmission += vehicle.getPurchaseEmission();

		switch (vehicle.getName()) {
		case "electric_bicycle":
			if (!initial) {
				ed.electricBicycleEmission += vehicle.getPurchaseEmission();
				ed.avgElectricBicycleEmission = ed.electricBicycleEmission / vud.electricBicycleUsage;
			}
			vpd.electricBicyclePossession++;
			break;

		case "motor":
			if (!initial) {
				ed.motorEmission += vehicle.getPurchaseEmission();
				ed.avgMotorEmission = ed.motorEmission / vud.motorUsage;
			}
			vpd.motorPossession++;
			break;
		case "normal_car":
			if (!initial) {
				ed.normalCarEmission += vehicle.getPurchaseEmission();
				ed.avgNormalCarEmission = ed.normalCarEmission / vud.normalCarUsage;
			}
			vpd.normalCarPossession++;
			break;
		case "hybrid_car":
			if (!initial) {
				ed.hybridCarEmission += vehicle.getPurchaseEmission();
				ed.avgHybridCarEmission = ed.hybridCarEmission / vud.hybridCarUsage;
			}
			vpd.hybridCarPossession++;
			break;
		case "electric_car":
			if (!initial) {
				ed.electricCarEmission += vehicle.getPurchaseEmission();
				ed.avgElectricCarEmission = ed.electricCarEmission / vud.electricCarUsage;
			}
			vpd.electricCarPossession++;
			break;
		}
	}
}
