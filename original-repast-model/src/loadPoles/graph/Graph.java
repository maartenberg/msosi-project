package loadPoles.graph;

import java.util.HashMap;
import java.util.Map;

public class Graph {
	private Map<Integer, Vertex> nodes;
	
	public Graph() {
		nodes = new HashMap<Integer, Vertex>();
	}
	
	public void addVertex(int id, Vertex v) {
		nodes.put(id, v);
	}
	
	public Vertex getVertex(Integer n) {
		return nodes.get(n);
	}
}
