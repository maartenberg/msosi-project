package loadPoles;

import loadPoles.ScenarioTree.*;
import repast.simphony.context.Context;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.grid.Grid;

public class LoadPolesBuilder implements ContextBuilder<Object> {

	@Override
	public Context build(Context<Object> context) {
		context.setId("loadPoles");
		
		//Get initializing params
		Parameters params = RunEnvironment.getInstance().getParameters();
		int humancount = params.getInteger("humancount");
		int dwellingcount = params.getInteger("dwellingcount");

		DataUpdater dataUpdater = new DataUpdater(context);
		context.add(dataUpdater);
		
		//build neighbourhood
		Neighbourhood nbh = new Neighbourhood(context, "metropolis.wijk", humancount);
		
		// Randomly assign parking spaces to dwellings
		nbh.distributeParkingSpacesToDwelling(0.6, 0.2);		
		
		//Assign b (electric) types to parking spaces in parking lots. Check from the params how we want to do it.
		String assignTypeB = params.getString("assignTypeB");
		switch(assignTypeB) {
			case "ratio" : 
				double bratio = params.getDouble("ratio_b");
				nbh.distributeParkingSpaceRandom(bratio);
				break;
			case "dwelling":
				break;
			default: 
				System.out.println("Unkown assignment type for B: " + assignTypeB);
				break;
		} 
		
		//Every human has their car parked somewhere
		nbh.parkAllCars();
		
		//Initialise social network
		nbh.initSocialNetwork();

		// If running in batch, we end at 20000 ticks.
		if (RunEnvironment.getInstance().isBatch()) {
			RunEnvironment.getInstance().endAt(20_000);
		}

		return context;
	}

}
