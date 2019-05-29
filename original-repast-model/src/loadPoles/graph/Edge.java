package loadPoles.graph;

public class Edge {
	private Vertex vertexA;
	private Vertex vertexB;
	private double distance;
	private double time;
	private int type;
	
	public Edge(Vertex a, Vertex b, double distance, double time, int type) {
		vertexA = a; vertexB = b;
		this.distance = distance;
		this.time = time;
		this.type = type;
	}
	
	public double getDistance() {
		return distance;
	}
	
	public double getTime() {
		return time;
	}
	
	public int getType() {
		return type;
	}
	
}
