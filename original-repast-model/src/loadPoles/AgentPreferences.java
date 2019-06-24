package loadPoles;

import repast.simphony.random.RandomHelper;

public class AgentPreferences {
	protected float utilityFactor_electric_car;
	protected float utilityFactor_electric_bicycle;
	protected float utilityFactor_normal_car;
	protected float utilityFactor_hybrid_car;
	protected float utilityFactor_bicycle;
	protected float utilityFactor_public_transport;
	protected float utilityFactor_motor;
	protected int agentActionType;
	private Human hum;	

	float r, t, l;
	float dl = -0.15f;
	float fluidlevels[];
	float positiveFactor = 1.4f;//TODO Maarten: deze factoren kunnen we evt. actief aanpasbaar maken
	float negativeFactor = 0.6f;
	float dlUp;
	float dlDown;
	float rhighest;

	public AgentPreferences(float[] valueTemps, Human hum)
	{
		this.hum = hum;
		fluidlevels = new float[] { 100f, 100f, 100f, 100f};
		agentActionType =  RandomHelper.nextIntFromTo(0, 3);
	}

	public void Update(float[] valueTemps, Human hum, float[] prevFluids) {
			//set the comparison value to the max distance 
			rhighest = -1000;
			System.out.println("\nHuman: " + hum.getName() + "\nagentActionType:" + agentActionType);
			for (int n = 0; n <valueTemps.length ; n++) {
						
			float temps = valueTemps[n];	
			fluidlevels[n] = prevFluids[n] + updateFluids(n, temps);
			
			//determine the upper and lower levels of the fluid tanks
			if(fluidlevels[n]> 200)
			{
				fluidlevels[n] =200;
			}
			if(fluidlevels[n] < 0)
			{
				fluidlevels[n] = 0;
			}
			
			r = (-((fluidlevels[n] - valueTemps[n])) / valueTemps[n]) * 100;
			System.out.print("\nvalue " + n + "\nvalueTemp: " + valueTemps [n] + "\nfluidlevels" + fluidlevels[n] + "\nr " + r +"\nfluid levels update:" + dl +"\n");
			
			if (r > rhighest) {
				rhighest = r;
				t = n;
			}
		}


		// In case self-trancendence is the most needed value
		if (t == 0) {
			agentActionType = 0;
			utilityFactor_electric_car = 1f * this.positiveFactor;
			utilityFactor_electric_bicycle = 1f;
			utilityFactor_normal_car = 1f * this.negativeFactor;
			utilityFactor_hybrid_car = 1f;
			utilityFactor_bicycle = 1f;
			utilityFactor_public_transport = 1f * this.positiveFactor;
			utilityFactor_motor = 1f * this.negativeFactor;
		}
	
		// In case conservation is the most needed value
	if (t == 1) {
		agentActionType =1;
		utilityFactor_electric_car = 1f;
		utilityFactor_electric_bicycle = 1f;
		utilityFactor_normal_car = 1f * 2* this.positiveFactor; 
		utilityFactor_hybrid_car = 1f;
		utilityFactor_bicycle = 1f;
		utilityFactor_public_transport = 1f * this.negativeFactor;
		utilityFactor_motor = 1f * this.negativeFactor;
	}
	
	// In case self-enhancement is the most needed value
if (t == 2) {
	agentActionType = 2;
	utilityFactor_electric_car = 1f;
	utilityFactor_electric_bicycle = 1f * this.negativeFactor;
	utilityFactor_normal_car = 1.f * this.positiveFactor;
	utilityFactor_hybrid_car = 1f * this.positiveFactor;
	utilityFactor_bicycle = 1f;
	utilityFactor_public_transport = 1f * this.negativeFactor;
	utilityFactor_motor = 1f;

}

// In case openness to change is the most needed value
if (t == 3) {
agentActionType =3;
utilityFactor_electric_car = 1f;
utilityFactor_electric_bicycle = 1f;
utilityFactor_normal_car = 1f * this.negativeFactor;
utilityFactor_hybrid_car = 1f * this.positiveFactor;
utilityFactor_bicycle = 1f;
utilityFactor_public_transport = 1f * this.negativeFactor;
utilityFactor_motor = 1f * this.positiveFactor;

}

System.out.println("\new action profile: " + agentActionType);
	}

	private float updateFluids(int n, float temps) {
		// fluid levels go down by -0.15 by default
		// if a vehicle was the previous action, some values have been fulfilled

		dl = -10f;
		dlUp = (100f - temps) * 0.2f;
		dlDown = -1 * dlUp;

		if(hum.travel.pastVehicle == null) {
			dl = 0;
			System.out.println("no past vehicle is listed");
		}
		
		else if(hum.travel.pastVehicle.getName() == "electric_car") {
			// here follows a list of the values that are positively linked to this action
			if (n == 0) {
				// if the values have been fulfilled, the fluid level OF THIS VALUE rises with 0.3
				dl = dlUp;
			}
			// There are no punishments for electric cars
		}

		else if(hum.travel.pastVehicle.getName() == "normal_car") {
			// here follows a list of the values that have been fulfilled
			if (n == 1|| n == 2) {
				// if the values have been fulfilled, the fluid level rises with 0.3
				dl = dlUp;
				// some values have a double score for this vehicle
				if (n == 1) {
					dl *= 1.5;
				}				
			}
			// here follows a list of the values that have received extra punishment
			if (n == 0 || n == 3) {
				// if the values have been punished, the fluid level goes down with 0.3
				dl = dlDown;				
			}
		}

		else if(hum.travel.pastVehicle.getName() == "hybrid_car") {
			// here follows a list of the values that have been fulfilled
			if (n == 2 || n == 3 ) {
				// if the values have been fulfilled, the fluid level rises with 0.3
				dl = dlUp;				
			}
			// There are no punishments for this vehicle type
		}

		else if(hum.travel.pastVehicle.getName() == "bicycle") {
			//there is a neutral stance towards bikes
			dl = 0;
		}

		else if(hum.travel.pastVehicle.getName() == "electric_bicycle") {
			//System.out.println("e-bike updated");
			// here follows a list of the values that have received extra punishment
			if (n == 2) {				
				dl = dlDown;				
			}
		}

		else if(hum.travel.pastVehicle.getName() == "public_transport") {
			//System.out.println("OV updated");
			// here follows a list of the values that have been fulfilled
			if (n == 0) {
				// if the values have been fulfilled, the fluid level rises with 0.3
				dl =dlUp;
			}
			// here follows a list of the values that have received extra punishment
			if(n == 1 || n == 2 || n == 3) {
				// if the values have been punished, the fluid level goes down with 0.2
				dl = dlDown;
			}
		}

		else if(hum.travel.pastVehicle.getName() == "motor") {
			//System.out.println("motor updated");
			// here follows a list of the values that have been fulfilled
			if(n == 3 ) {
				// if the values have been fulfilled, the fluid level rises with 0.3
				dl = dlUp;
			}
			// here follows a list of the values that have received extra punishment
			if(n == 0 || n == 1) {
				// if the values have been punished, the fluid level goes down with 0.2
				dl = dlDown;
			}
		}
		
		//System.out.println("value "+  n + "is updated with " + dl);
		return dl;		
	}
	
	public float getUtilityFactor(Vehicle vehicle) {
		switch (vehicle.getName()) {
		case "bicycle":
			return utilityFactor_bicycle;
		case "electric_bycicle":
			return utilityFactor_electric_bicycle;
		case "normal_car":
			return utilityFactor_normal_car;
		case "hybrid_car":
			return utilityFactor_hybrid_car;
		case "electric_car":
			return utilityFactor_electric_car;
		case "public_transport":
			return utilityFactor_public_transport;
		case "motor":
			return utilityFactor_motor;
		}

		return 0;
	}
}
