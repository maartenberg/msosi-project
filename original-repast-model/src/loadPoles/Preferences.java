package loadPoles;
import java.util.List;

public class Preferences {
	protected float utilityFactor_electric_car;
	protected float utilityFactor_electric_bicycle;
	protected float utilityFactor_normal_car;
	protected float utilityFactor_hybrid_car;
	protected float utilityFactor_bicycle;
	protected float utilityFactor_public_transport;
	protected float utilityFactor_motor;
	
	/*
	 * Getters
	 */	
	public float getUtilityFactor_electric_car() {
		return utilityFactor_electric_car;
	}
	
	public float getUtilityFactor_electric_bicycle () {
		return utilityFactor_electric_bicycle;
	}
	
	public float getUtilityFactor_normal_car () {
		return utilityFactor_normal_car;
	}
	
	public float getUtilityFactor_hybrid_car () {
		return utilityFactor_hybrid_car;
	}
	
	public float getUtilityFactor_bicycle () {
		return utilityFactor_bicycle;
	}
	
	public float getUtilityFactor_public_transport () {
		return utilityFactor_public_transport;
	}
	
	public float getUtilityFactor_motor () {
		return utilityFactor_motor;
	}
	
	float r, t, l;
	float dl = -0.15f;
	float valuetemps[], fluidlevels[];

	public Preferences(float va, float vb,float vc, float vd, float ve, float vf,float vg, float vh, float vi, float vj)
	//float b,float c, float d, float e, float f,float g, float h, float i, float j
	//let op bij aanmaken: initieer er 5 random, laat de andere 5 daarvan afhangen (het zijn schalen)
	
	{
		

	valuetemps = new float[] {va, vb, vc, vd, ve, vf, vg, vh, vi, vj};	
	fluidlevels  = new float[] {0.4f,0.4f,0.4f,0.4f,0.4f,0.4f,0.4f,0.4f, 0.4f, 0.4f};

		}
	
	public void Update() {
		for( int n = 0; n< valuetemps.length; n++) {
			
			updateFluids(n);
			
			
			
			
			
			fluidlevels[n] = fluidlevels[n] + dl;
			
			
			
			float rlowest = 100;
			r = ((fluidlevels[n]-valuetemps[n])/valuetemps[n])*100;
			

			if (r< rlowest)
				{
				rlowest = r;
				t = n;
				}
			}
			
			//In case power is the most needed
			if (t==1) {
				utilityFactor_electric_car = 1.4f;
				utilityFactor_electric_bicycle = 1f;
				utilityFactor_normal_car = 1.4f;
				utilityFactor_hybrid_car = 1f;
				utilityFactor_bicycle = 1f;
				utilityFactor_public_transport = 0.6f;
				utilityFactor_motor = 1f;
			
			}
			else {
				System.out.println("power is not the most needed");
			}
	}
	private float updateFluids(int n) {
		//TODO update fluid levels based on previous actions
		//fluid levels go down by -0.15 by default
		//if a vehicle was the previous action, some values have been fulfilled
		
		if (Human.pastVehicle == "electric_car")
		{
			//here follows a list of the values that have been fulfilled
			if (n == 6 || n == 7 || n == 8 || n == 0 || n == 1)
			{
					//if the values have been fulfilled, the fluid level rises with 0.3 
					dl = 0.3f;
					//some values have a double score for this vehicle
					if(n == 3)
					{
						dl *=1.5;
					}
			}
			//here follows a list of the values that have received extra punishment
			if (n == 9 || n == 2 || n == 4)
			{
				{
					//if the values have been punished, the fluid level goes down with 0.2
					dl = -0.2f;
				}
			}
		}
		
		if (Human.pastVehicle == "normal_car")
		{
			//here follows a list of the values that have been fulfilled
			if (n == 8 || n == 9 || n == 0 || n == 0 || n == 2 || n == 4 || n == 6 || n == 7 || n == 8)
			{
				{
					//if the values have been fulfilled, the fluid level rises with 0.3 
					dl = 0.3f;
					//some values have a double score for this vehicle
					if(n == 8 || n == 9 || n == 0 || n == 0 || n == 2 || n == 4)
					{
						dl *=1.5;
					}
					
				}
			}
			//here follows a list of the values that have received extra punishment
			if (n == 5 || n == 3 )
			{
				{
					//if the values have been punished, the fluid level goes down with 0.2
					//all values have a double score for this vehicle
					dl = -0.3f;
				}
			}
		}
		
		if (Human.pastVehicle == "hybrid_car")
		{
			//here follows a list of the values that have been fulfilled
			if (n == 2 || n == 0 || n == 1 )
			{
				{
					//if the values have been fulfilled, the fluid level rises with 0.3 
					dl = 0.3f;
					
				}
			}
			//here follows a list of the values that have received extra punishment
			if (n == 7 )
			{
				{
					//if the values have been punished, the fluid level goes down with 0.2
					dl = -0.2f;
				}
			}
		}
		
		if (Human.pastVehicle == "bicycle")
		{
			//here follows a list of the values that have been fulfilled
			if (n == 7 || n == 9 || n == 5 || n == 6 || n == 8)
			{
				{
					//if the values have been fulfilled, the fluid level rises with 0.3 
					dl = 0.3f;
					//some values have a double score for this vehicle
					if(n == 7 || n == 9)
					{
						dl *=1.5;
					}
					
				}
			}
			//here follows a list of the values that have received extra punishment
			if (n == 1)
			{
				{
					//if the values have been punished, the fluid level goes down with 0.2
					dl = -0.2f;
				}
			}
		}
		
		if (Human.pastVehicle == "electric_bicycle")
		{
			//here follows a list of the values that have been fulfilled
			if (n == 1 || n == 2|| n == 3 )
			{
				{
					//if the values have been fulfilled, the fluid level rises with 0.3 
					dl = 0.3f;					
				}
			}
		}
		
		if (Human.pastVehicle == "public_transport")
		{
			//here follows a list of the values that have been fulfilled
			if (n == 5)
			{
					//if the values have been fulfilled, the fluid level rises with 0.3 
					dl = 0.3f;
			}
			//here follows a list of the values that have received extra punishment
			if (n == 9 || n == 9 || n == 1 || n == 2 || n== 4 || n== 0)
			{
					//if the values have been punished, the fluid level goes down with 0.2
					dl = -0.2f;
					//some values have a double -score for this vehicle
					if (n== 0)
					{
						dl *= 1.5;
					}
			}
		}
		
		if (Human.pastVehicle == "motor")
		{
			//here follows a list of the values that have been fulfilled
			if (n == 3 || n== 4)
			{
					//if the values have been fulfilled, the fluid level rises with 0.3 
					dl = 0.3f;
					//some values have a double score for this vehicle
					if(n == 4)
					{
						dl *=1.5;
					}

			}
			//here follows a list of the values that have received extra punishment
			if (n == 6 || n == 7 || n == 8)
			{
					//if the values have been punished, the fluid level goes down with 0.2
					dl = -0.2f;
			}
		}
		
	//	TODO add walk
		
		return dl;
	}
	
	
	
}