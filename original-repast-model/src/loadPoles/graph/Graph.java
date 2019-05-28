package loadPoles.graph;

import java.util.HashMap;
import java.util.Map;

public class Graph {
	private Map<Integer, Vertex> nodes;
	
	public Graph() {
		nodes = new HashMap<Integer, Vertex>();
	}
	
	public void updateVertex(int id, Vertex v) {
		nodes.put(id, v);
	}
	
	public Vertex getVertex(Integer n) {
		return nodes.get(n);
	}
	
	public Vertex findNearestVertex(int x, int y) {
		Vertex best = null;
		int xB = 1000000; int yB = 1000000;
		for(Map.Entry<Integer, Vertex> node : nodes.entrySet()) {
			Vertex v = node.getValue();
			int vX = v.getX(); int vY = v.getY();
			if(Math.abs(x - vX) + Math.abs(y - vY) > Math.abs(x - xB) + Math.abs(y - yB)) {
				best = v;
				xB = vX; yB = vY;
			}
		}
		return best;
	}
}
