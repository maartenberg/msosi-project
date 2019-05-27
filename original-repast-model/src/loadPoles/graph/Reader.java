package loadPoles.graph;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Reader {
	
	private String url;
	private Graph g;
	
	public Reader(String url) {
		if(url == null || url == "") {
			this.url = "src/loadPoles/graph/basicGraphTestModel.txt";
		}
		try {
			g = readGraph();
		}
		catch(FileNotFoundException e) {
			System.out.println("This path was not found! Therefore, we couldn't load the graph.");
		}
		catch(Exception e) {
			System.out.println("Something else went wrong! This was the error message:");
			System.out.println(e);
		}
	}
	
	private Graph readGraph() throws IOException {
		Graph g = new Graph();
		String line;
		BufferedReader reader = new BufferedReader(new FileReader(url));
		line = reader.readLine();
		int totalVertex = Integer.parseInt(line);
		for(int i = 0; i< totalVertex; i++) {
			line = reader.readLine();
			String[] vertex = line.split(" ");
			int vertX = Integer.parseInt(vertex[0]);
			int vertY = Integer.parseInt(vertex[1]);
			Vertex v = new Vertex(vertX, vertY, i);
			g.updateVertex(i, v);
		}
		while((line = reader.readLine()) != null) {
			String[] edge = line.split(" ");
			Vertex first = g.getVertex(Integer.parseInt(edge[0]) - 1);
			Vertex second = g.getVertex(Integer.parseInt(edge[1]) - 1);
			int distance = Integer.parseInt(edge[2]);
			int time = Integer.parseInt(edge[3]);
			int type = Integer.parseInt(edge[4]);
			Edge e = new Edge(first, second, distance, time, type);
			first.addEdge(e);
			second.addEdge(e);
			g.updateVertex(first.returnId(), first);
			g.updateVertex(second.returnId(), second);
		}
		reader.close();
		return g;
	}
	
	public Graph getGraph() {
		return g;
	}
}
