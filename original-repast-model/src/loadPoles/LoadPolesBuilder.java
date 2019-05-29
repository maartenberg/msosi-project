package loadPoles;

import repast.simphony.context.Context;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

public class LoadPolesBuilder implements ContextBuilder<Object> {

	@Override
	public Context build(Context<Object> context) {
		context.setId("loadPoles");
		
		int gridX = 10, gridY = 10;
		//Grid<Object> dwellingsGrid = null,parkingspacesGrid;
		/*
		GridFactory factory = GridFactoryFinder.createGridFactory(null);
		
		//Has dwellings
		dwellingsGrid = factory.createGrid(
				"dwellingsGrid",
				context,
				new GridBuilderParameters<Object>(
					new StrictBorders(),
					new RandomGridAdder<Object>(),
					true,
					gridX,
					gridY
				));	
		
		//Has parking spaces
		parkingspacesGrid = factory.createGrid("parkingspacesGrid", context, new GridBuilderParameters<Object>(
				new StrictBorders(),
				new SimpleGridAdder<Object>(),
				false,
				gridX,
				gridY
				));
		
		*/
		
		//Get initializing params
		Parameters params = RunEnvironment.getInstance().getParameters();
		int humancount = params.getInteger("humancount");
		int dwellingcount = params.getInteger("dwellingcount");
		
		//build neighbourhood
		Neighbourhood nbh = new Neighbourhood(context, "bunnik.wijk", humancount);
		//Neighbourhood nbh = new Neighbourhood(context, humancount, dwellingcount);
		
		//Assign b types to parkingspace. Check from the params how we want to do it.
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
		/*
		
		//Every human has his car parked somewhere
		IndexedIterable humans = context.getObjects(Human.class);
		Iterator humansIterator = humans.iterator();
		
		while(humansIterator.hasNext()) {
			Human h = (Human) humansIterator.next();
			//System.out.println(h.name);
			//humansIterator.remove();
			
			h.parkCar();
		}
		*/
		
		/*
		for(Object d : dwellings)
		{
			if(humans.iterator().hasNext())
			{
				livingin.addEdge(humans.iterator().next(), d);
			}
			
		}
		*/
		//Add parkingspace
		
		
		
		return context;
	}

}
