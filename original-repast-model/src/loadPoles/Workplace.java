package loadPoles;

import repast.simphony.context.Context;
import repast.simphony.space.grid.Grid;

public class Workplace {

	private Grid<Object> workplaceGrid;
	Context<Object> context;
	private String name;
	
	public Workplace(Context<Object> context, Grid<Object> workplaceGrid){
		this.context = context;
		this.workplaceGrid = workplaceGrid;
		//number them.
		this.name = String.valueOf(context.getObjects(Workplace.class).size());
	}
	
	//Returns the amount of humans that have a certain type of car to park that live in this dwelling
	
	public String getName() {
		return this.name;
	}
	
}
