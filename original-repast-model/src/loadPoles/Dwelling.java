package loadPoles;

import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;

public class Dwelling {

	private List<Human> inhabitants;
	private Grid<Object> dwellingsGrid;
	Context<Object> context;
	private String name;
	
	public Dwelling(Context<Object> context, Grid<Object> dwellingsGrid){
		this.context = context;
		this.dwellingsGrid = dwellingsGrid;
		//number them.
		this.name = String.valueOf(context.getObjects(Dwelling.class).size());
	}
	
	//A new human starts living here
	public void moveIn(Human inhabitant) {
		inhabitants.add(inhabitant);
	}
	
	//Returns the amount of humans that have a certain type of car to park that live in this dwelling
	public int getAmountOfParkingType(String type) {
		
		int amountFound = 0;
		
		@SuppressWarnings("unchecked")
		Network<Object> livingin = (Network<Object>) context.getProjection("livingin");
		Iterable<Object> adjacents = livingin.getAdjacent(this);
		
		for(Object adjacent : adjacents) {
			if(adjacent.getClass().equals(Human.class)) {
				Human adjacentHuman = (Human) adjacent;
				if(adjacentHuman.getType() == type)
					amountFound++;
				}
			}
				
		return amountFound;
	}
	
	public String getName() {
		return this.name;
	}
	
}
