package loadPoles;

import java.util.Iterator;
import java.util.List;

import loadPoles.GridObjects.Dwelling;
import loadPoles.GridObjects.ParkingLot;
import loadPoles.GridObjects.PublicBuilding;
import loadPoles.GridObjects.Road;
import loadPoles.GridObjects.TransitStop;
import loadPoles.GridObjects.Workplace;
import loadPoles.graph.*;

import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import repast.simphony.context.Context;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.space.grid.RandomGridAdder;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.StrictBorders;
import repast.simphony.util.SimUtilities;
import repast.simphony.util.collections.IndexedIterable;

public class Neighbourhood {

	Context<Object> context;
	Grid<Object> grid;
	Graph g;
	
	/**
	 * The width of this Neighborhood's Grid.
	 */
	int gridX = 10; 
	/**
	 * The height of this Neighborhood's Grid.
	 */
	int gridY = 10;
		
	/**
	 * Load a Neighborhood from a file.
	 * @param context
	 * @param fileName
	 */
	public Neighbourhood(Context<Object> context, String fileName, int humanCount) {
		Reader reader = new Reader(null);
		this.g = reader.getGraph();
		Path p = FileSystems.getDefault().getPath("neighborhoods", fileName);
		System.out.println(p.toAbsolutePath());

		// Read neighbourhood from file
		try {
			// Get the X and Y dimensions from the file
			gridY = (int) Files.lines(p).count();
			gridX = Files.lines(p)
					  .map((String x) -> x.length())
					  .max((i, j) -> i.compareTo(j))
					  .orElse(10);
			
			GridFactory factory = GridFactoryFinder.createGridFactory(null);
			
			GridBuilderParameters<Object> gridParameters = new GridBuilderParameters<Object>(
					new StrictBorders(),
					new SimpleGridAdder<Object>(),
					true,
					gridX,
					gridY
			);

			grid = factory.createGrid("grid", context, gridParameters);
			
			// Read each line and add the correponsing objects
			Object[] lineContent = Files.lines(p).toArray();
			int lineCount = lineContent.length - 1;

			for (int y = 0; y < gridY; y++) {
				String line = (String) lineContent[lineCount - y];
				for (int x = 0; x < Math.min(gridX, line.length()); x++) {
					char c = line.charAt(x);
					
					switch (c) {
					case '#':
						Dwelling d = new Dwelling(context);
						d.setClosestVertex(g.findNearestVertex(x, y));
						context.add(d);
						grid.moveTo(d, x, y);
						break;
						
					case '.':
						Road r = new Road();
						context.add(r);
						grid.moveTo(r, x, y);
						break;
						
					case ',':
						Road rd = new Road();
						TransitStop ts = new TransitStop();
						context.add(rd);
						context.add(ts);
						grid.moveTo(rd, x,y);
						grid.moveTo(ts, x,y);
						break;
						
					case 'P':
						ParkingLot pl = new ParkingLot(grid);
						context.add(pl);
						grid.moveTo(pl, x, y);
						break;
						
					case 'W':
						Workplace w = new Workplace();
						context.add(w);
						grid.moveTo(w, x, y);
						break;
						
					case 'O':
						PublicBuilding pb = new PublicBuilding();
						context.add(pb);
						grid.moveTo(pb, x, y);
						break;
						
					default:
						// We don't recognize this, empty cell
						break;
					}
				}
			} 

		} catch (IOException e) {
			// Build an empty neighbourhood as a fallback.
			gridX = 10;
			gridY = 10;

			GridFactory factory = GridFactoryFinder.createGridFactory(null);
			
			GridBuilderParameters<Object> gridParameters = new GridBuilderParameters<Object>(
					new StrictBorders(),
					new SimpleGridAdder<Object>(),
					true,
					gridX,
					gridY
			);		
		
			grid = factory.createGrid("grid", context, gridParameters);
			
			//TODO: add randomly scattered dwellings, workplaces, roads and public buildings
			// in case we DO need something to fall back on
		}
			
		//Add people
		for(int i = 0; i < humanCount; i++) {
			context.add(new Human(context, grid));
		}
		
		// Initialise networks
		NetworkBuilder<Object> dwellingNetBuilder = new NetworkBuilder<Object>("livingin", context, true);
		Network<Object> livingin = dwellingNetBuilder.buildNetwork();
		
		NetworkBuilder<Object> workNetBuilder = new NetworkBuilder<Object>("workingin", context, true);
		Network<Object> workingin = workNetBuilder.buildNetwork();
		
		NetworkBuilder<Object> journeysBuilder = new NetworkBuilder<Object>("journeys", context, true);
		Network<Object> journeys = journeysBuilder.buildNetwork();
		
		NetworkBuilder<Object> socialNetworkBuilder = new NetworkBuilder<Object>("socialnetwork", context, true);
		Network<Object> socialnetwork = socialNetworkBuilder.buildNetwork();
		
		NetworkBuilder<Object> consumerMarketBuilder = new NetworkBuilder<Object>("consumermarket", context, true);
		Network<Object> consumerMarket = consumerMarketBuilder.buildNetwork();

		IndexedIterable humans = context.getObjects(Human.class);
		Iterator humansIterator = humans.iterator();
		IndexedIterable dwellings = context.getObjects(Dwelling.class);
		IndexedIterable workplaces = context.getObjects(Workplace.class);
		
		//And make someone live somewhere by adding an edge in the network.
		//Also make someone work somewhere
		while (humansIterator.hasNext())
		{		
			Human h = (Human)humansIterator.next();
			
			// Assign human to a random dwelling
			Dwelling d = (Dwelling) dwellings.get(RandomHelper.nextIntFromTo(0, dwellings.size()-1));
			livingin.addEdge(h, d);
			h.setDwelling(d);		
			
			// Get location of the dwelling, and move the human to there		
			GridPoint dwellingLocation = grid.getLocation(d);		
			grid.moveTo(h, dwellingLocation.getX(), dwellingLocation.getY());
			
			// If the human is employed, add a random workplace
			if(h.isEmployed()) {
				Workplace w = (Workplace) workplaces.get(RandomHelper.nextIntFromTo(0, workplaces.size() - 1));
				workingin.addEdge(h, w);				
				h.setWorkplace(w);
			}
		}
				
		this.context = context;		
	}

	//Distribute B type parking spaces at random given a ratio of B to A.
	public void distributeParkingSpaceRandom(double ratio) {
		IndexedIterable parkingLots = this.context.getObjects(ParkingLot.class);
		Iterator parkingLotsIterator = parkingLots.iterator();
		while(parkingLotsIterator.hasNext()) {
			ParkingLot pl = (ParkingLot) parkingLotsIterator.next();
			
			//Get a random number for the amount of parking spaces this parking lot has
			int nrOfSpaces = RandomHelper.nextIntFromTo(20, 50);
			
			//Add parking spaces to the parking lots with a given ratio
			for(int i = 0; i < nrOfSpaces; i++) {				
				//Do it randomly
				//ratio% distribution of electric (loading poles) compared to normal parking spaces
				if(RandomHelper.nextDoubleFromTo(0, 1) < ratio) {
					pl.addSpace("electric", null);
				} else {
					pl.addSpace("normal", null);
				}
			}
		} 		
	}
	
	public void distributeParkingSpacesToDwelling(double normalSpotChance, double loadingPoleChance){
		IndexedIterable dwellings = this.context.getObjects(Dwelling.class);
		Iterator dwellingsIterator = dwellings.iterator();
		while(dwellingsIterator.hasNext()) {
			Dwelling d = (Dwelling) dwellingsIterator.next();			
			
			// Find the number of electric cars in this dwelling
			int nrLoadingPoleNeeded = d.getAmountOfParkingType(true);
			
			// If it is bigger than 0, then add a loading pole with a given chance
			if(nrLoadingPoleNeeded > 0) {
				if(RandomHelper.nextDoubleFromTo(0, 1) < loadingPoleChance) {
					d.addParkingSpace("electric", grid);
				}
			}
			
			//If the dwelling already has a parking space, lower the chance of a normal parking space
			if(d.hasParkingSpace()) {
				normalSpotChance *= 0.5;
			}
			
			// Add a normal parking spot to this dwelling with a given chance
			if(RandomHelper.nextDoubleFromTo(0, 1) < normalSpotChance) {
				d.addParkingSpace("normal", grid);
			}
		}		
	}
	
	public void initSocialNetwork() {
		// Get all humans, and networks that we need
		IndexedIterable<Object> humans = this.context.getObjects(Human.class);
		Iterator<Object> humansIterator = humans.iterator();
		
		Network<Object> livingin =  (Network<Object>) this.context.getProjection("livingin");
		Network<Object> workingin = (Network<Object>) this.context.getProjection("workingin");
		Network<Object> socialnetwork = (Network<Object>) this.context.getProjection("socialnetwork");
		
		// Establish social network for each human
		while(humansIterator.hasNext()) {
			Human human = (Human) humansIterator.next();
			
			// Add random colleagues to their social network
			if(human.isEmployed()) {
				Workplace workplace = human.workplace;			
				Iterable<RepastEdge<Object>> colleagues = workingin.getInEdges(workplace);
				Iterator<RepastEdge<Object>> colleguesIterator = colleagues.iterator();
				
				// Human and collegue are added to social network with 20% chance
				while(colleguesIterator.hasNext()) {
					Human colleague = (Human) colleguesIterator.next().getSource();				
					if(RandomHelper.nextDoubleFromTo(0, 1) < 0.20) {
						RepastEdge<Object> edge = new RepastEdge(human, colleague, false);
						if(socialnetwork.containsEdge(edge)) {
							socialnetwork.addEdge(edge);
						}
					}
				}
			}
			
			// Add people living in dwelling to social network ("family")
			Dwelling dwelling = human.dwelling;
			Iterable<RepastEdge<Object>> family = livingin.getInEdges(dwelling);
			Iterator<RepastEdge<Object>> familyIterator = family.iterator();			
			while(familyIterator.hasNext()) {
				Human familyMember = (Human) familyIterator.next().getSource();
				if(human != familyMember) {
					RepastEdge<Object> edge = new RepastEdge(human, familyMember, false);
					if(!socialnetwork.containsEdge(edge)) {
						socialnetwork.addEdge(edge);
					}
				}
			}
			
			
			// Add random people from their neighbourhood to their social network
			GridPoint pt = grid.getLocation(dwelling);
			
			// Use the GridCellNgh class to create GridCells for the surrounding neighbourhood, in a 10 cell radius
			GridCellNgh<Dwelling> nghCreator = new GridCellNgh<Dwelling>(this.grid, pt, Dwelling.class, 10, 10);
			List<GridCell<Dwelling>> gridCells = nghCreator.getNeighborhood(false);
			
			// For each cell, find the corresponding dwelling, and humans living in it
			for(GridCell<Dwelling> cell : gridCells) {
				Iterator neighbourHousesIterator = cell.items().iterator();
				if(neighbourHousesIterator.hasNext()) {
					Dwelling neighbourHouse = (Dwelling) neighbourHousesIterator.next();
					Iterable<RepastEdge<Object>> neighbours = livingin.getInEdges(neighbourHouse);
					Iterator<RepastEdge<Object>> neighboursIterator = neighbours.iterator();
					
					// Human and neighbour are added to social network with 30% chance
					while(neighboursIterator.hasNext()) {
						Human neighbour = (Human) neighboursIterator.next().getSource();
						if(RandomHelper.nextDoubleFromTo(0, 1) < 0.3) {
							RepastEdge<Object> edge = new RepastEdge(human, neighbour, false);
							if(!socialnetwork.containsEdge(edge)) {
								socialnetwork.addEdge(edge);
							}
						}
					}
				}
			}			
		}
	}
	
	public void parkAllCars() {
		//Every human has his car parked somewhere
		IndexedIterable humans = this.context.getObjects(Human.class);
		Iterator humansIterator = humans.iterator();
		
		while(humansIterator.hasNext()) {
			Human h = (Human) humansIterator.next();
			h.parkAllCars();
		}
	}
	
}
