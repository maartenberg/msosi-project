package loadPoles;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repast.simphony.context.Context;
import repast.simphony.space.grid.Grid;

//import java.util.List; 
public class Route {

	List <double[]> allArrays;
	HashMap <String, double[]> legenda;

	
	double[] bikeArray = {0.1, 2.0};
	double[] ebikeArray = {6.0, 4.3}; 
	double[] carArray = {6.0, 4.3}; 
	double[] evArray = {6.0, 4.3}; 
	double[] motorArray = {6.0, 4.3}; 
	double[] ovArray = {6.0, 4.3};
	double[] walkArray = {6.0, 4.3};
	

	// 0.2* ;
	// type: work/hustle/trip/visit
	public Route(int startnode, int endnode){
		allArrays = new ArrayList<double[]>();
		legenda = new HashMap<String, double[]>();
		//
		legenda.put("bicycle", bikeArray);
		legenda.put("e-bike", ebikeArray);
		legenda.put("car", carArray);
		legenda.put("EV", evArray);
		legenda.put("walk", walkArray);
		legenda.put("motor", motorArray);
		legenda.put("ov", ovArray);
	}

	
public void getValues ()
{
	//determine the comfort of all vehicles
	//evArray[1] = ;



	// afstand
	// new york distance
	// time
	// zones

	// cost
	// comfort
	// energy
	// CO2

	// saves route list
	// route list = double array
}



}
