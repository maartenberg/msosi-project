package loadPoles;

import java.util.Iterator;
import java.util.stream.Stream;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import repast.simphony.context.Context;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.RandomGridAdder;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.StrictBorders;
import repast.simphony.util.collections.IndexedIterable;

public class Neighbourhood {

	Context<Object> context;
	Grid<Object> dwellingsGrid;
	Grid<Object> parkingspacesGrid;
	
	/**
	 * The width of this Neighborhood's Grid.
	 */
	int gridX = 10; 
	/**
	 * The height of this Neighborhood's Grid.
	 */
	int gridY = 10;
	
	//Some default values
	public Neighbourhood(Context<Object> context) {
		this(context, 10, 10);
	}
	
	/**
	 * Load a Neighborhood from a file.
	 * @param context
	 * @param fileName
	 */
	public Neighbourhood(Context<Object> context, String fileName) {
		Path p = FileSystems.getDefault().getPath("neighborhoods", fileName);
		System.out.println(p.toAbsolutePath());

		try {
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

			dwellingsGrid = factory.createGrid("dwellingsGrid", context, gridParameters);
			parkingspacesGrid = factory.createGrid("parkingspacesGrid", context, gridParameters);
			
			Object[] lineContent = Files.lines(p).toArray();
			int lineCount = lineContent.length - 1;

			for (int y = 0; y < gridY; y++) {
				String line = (String) lineContent[lineCount - y];
				for (int x = 0; x < Math.min(gridX, line.length()); x++) {
					char c = line.charAt(x);
					
					switch (c) {
					case '#':
						Dwelling d = new Dwelling(context, dwellingsGrid);
						context.add(d);
						dwellingsGrid.moveTo(d, x, y);
						break;
						
					case '.':
						Road r = new Road();
						context.add(r);
						dwellingsGrid.moveTo(r, x, y);
						break;
						
					case 'P':
						ParkingSpace ps = new ParkingSpace(context);
						context.add(ps);
						parkingspacesGrid.moveTo(ps, x, y);
						dwellingsGrid.moveTo(ps, x, y);
						break;
						
					case 'W':
						Workplace w = new Workplace();
						context.add(w);
						dwellingsGrid.moveTo(w, x, y);
						break;
						
					default:
						// We don't recognize this, too bad.
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

			dwellingsGrid = factory.createGrid("dwellingsGrid", context, gridParameters);
			parkingspacesGrid = factory.createGrid("parkingspacesGrid", context, gridParameters);

			return;
		}
	}
	
	public Neighbourhood(Context<Object> context, int humancount, int dwellingcount) 
	{
		
		this.context = context;
		//this.dwellingsGrid = dwellingsGrid;
		//this.parkingspacesGrid = parkingspacesGrid;
	
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
		//context.addProjection(dwellingsGrid);
		
		//Has parking spaces
		parkingspacesGrid = factory.createGrid(
				"parkingspacesGrid",
				context, 
				new GridBuilderParameters<Object>(
					new StrictBorders(),
					new SimpleGridAdder<Object>(),
					true,
					gridX,
					gridY
				));
		//context.addProjection(parkingspacesGrid);
		
		
		//Add dwellings
		for(int i = 0; i < dwellingcount; i++)
		{
			context.add(new Dwelling(context, dwellingsGrid));
		}
		
		//Add people
		for(int i = 0; i < humancount; i++)
		{
			context.add(new Human(context));
		}
		
		//make sure they have a home
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object> ("livingin", context, true);
		Network<Object> livingin = netBuilder.buildNetwork();
		
		IndexedIterable<Object> humans = context.getObjects(Human.class);
		Iterator<Object> humansIterator = humans.iterator();
		IndexedIterable<Object> dwellings = context.getObjects(Dwelling.class);
		
		//System.out.println(humans.size());
		
		//And make someone live somewhere by adding an edge in the network.
		while (humansIterator.hasNext())
		{
			livingin.addEdge(humansIterator.next(), dwellings.get(RandomHelper.nextIntFromTo(0, dwellings.size()-1)));
		}
		
		//debugtesting
		Iterator<Object> dwellingsIterator = dwellings.iterator();
		
		while(dwellingsIterator.hasNext()) {
			Dwelling d = (Dwelling) dwellingsIterator.next();
	
			System.out.println("Dwelling " + d.getName() + "'s b types: " + d.getAmountOfParkingType("b"));
			
		}
		
		
		//fill the parkingspaceGrid with parkingspaces by adding to context and moving them to a location
		int parkX = parkingspacesGrid.getDimensions().getWidth();
		int parkY = parkingspacesGrid.getDimensions().getHeight();
		for(int i_x = 0; i_x < parkX; i_x++) {
			for(int i_y = 0; i_y < parkY; i_y++) {
				ParkingSpace ps = new ParkingSpace(context);
				context.add(ps);
				parkingspacesGrid.moveTo(ps, i_x, i_y);
			}
		}
	
	
	}
	
	//Distribute B type parking spaces at random given a ratio of B to A.
	public void distributeParkingSpaceRandom(double ratio) {
		Iterable<Object> parkingSpacesGrid = this.parkingspacesGrid.getObjects();
		Iterator<Object> parkingSpacesGridIterator = parkingSpacesGrid.iterator();
		while(parkingSpacesGridIterator.hasNext()) {
			try{
				ParkingSpace ps = (ParkingSpace) parkingSpacesGridIterator.next();
				
				//Do it randomly
				//ratio% distribution of b compared to a
				if(RandomHelper.nextDoubleFromTo(0, 1) < ratio) {
					ps.setType("b");
				} else {
					ps.setType("a");
				}
				
			} catch (Exception e) {
				System.out.println("Exception: " + e);
			} finally {
				//do nothing
			}
		}
	}
	
	public void distributeParkingSpacesToDwelling() {
		Iterable<Object> parkingSpacesGrid = this.parkingspacesGrid.getObjects();
		Iterator<Object> parkingSpacesGridIterator = parkingSpacesGrid.iterator();
		while(parkingSpacesGridIterator.hasNext()) {
			try{
				//Get all dwellings at this location
				ParkingSpace ps = (ParkingSpace) parkingSpacesGridIterator.next();
				Iterable<Object> dpsdwellings = (Iterable<Object>) this.dwellingsGrid.getObjectsAt(
					this.parkingspacesGrid.getLocation(ps).getX(),
					this.parkingspacesGrid.getLocation(ps).getY()
					);
				
				//If there are dwellings check for one with a "b-type" human.
				checkDwellings:
				while(dpsdwellings.iterator().hasNext()) {
					Dwelling dpsd = (Dwelling) dpsdwellings.iterator().next();
					if(dpsd.getAmountOfParkingType("b") > 0) {
						//Found one, we need a b-type parkingspace here.
						ps.setType("b");
						break checkDwellings;
					}
				}
				
			} catch (Exception e) {
				System.out.println("Exception: " + e);
			} finally {
				//do nothing
			}
		}
	
	}
	
	
}
