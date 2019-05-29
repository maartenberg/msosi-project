package loadPoles.graph;

import java.util.ArrayList;
import java.util.List;

public class Vertex {
	private int id;
	private int x,y;
	private List<Edge> edges;
	
	public Vertex(int x, int y, int id) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.edges = new ArrayList<Edge>();
	}
	
	public void addEdge(Edge e) {
		edges.add(e);
	}
	
	public List<Edge> getEdges(){
		return edges;
	}
	
	public int returnId() {
		return id;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
}
