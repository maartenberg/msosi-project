package loadPoles.graph;

import java.util.List;

public class Vertex {
	private int id;
	private int x,y;
	private List<Edge> edges;
	
	public Vertex(int x, int y, int id) {
		this.id = id;
		this.x = x;
		this.y = y;
	}
	
	public void addEdge(Edge e) {
		edges.add(e);
	}
	
	public List<Edge> getEdges(){
		return edges;
	}
}
