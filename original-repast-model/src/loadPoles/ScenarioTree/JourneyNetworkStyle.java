package loadPoles.ScenarioTree;

import java.awt.Color;

import loadPoles.Route;
import loadPoles.Vehicle;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.visualizationOGL2D.DefaultEdgeStyleOGL2D;

public class JourneyNetworkStyle extends DefaultEdgeStyleOGL2D {
	@Override
	public Color getColor(RepastEdge<?> edge) {
		Route route = (Route) edge.getSource();

		Vehicle vehicle = route.getVehicle();
		if (vehicle.getName() == "electric_car") {
			return Color.GREEN;
		} else if (vehicle.getName() == "normal_car") {
			return Color.BLUE;
		} else if (vehicle.getName() == "hybrid_car") {
			return Color.CYAN;
		} else if (vehicle.getName() == "public_transport") {
			return Color.ORANGE;
		} else if (vehicle.getName() == "bicycle") {
			return Color.YELLOW;
		} else if (vehicle.getName() == "electric_bicycle") {
			return Color.MAGENTA;
		} else if (vehicle.getName() == "motor") {
			return Color.RED;
		}

		return Color.BLACK;
	}
}
