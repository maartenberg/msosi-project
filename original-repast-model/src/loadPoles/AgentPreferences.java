package loadPoles;

public class AgentPreferences {
	protected float utilityFactor_electric_car;
	protected float utilityFactor_electric_bicycle;
	protected float utilityFactor_normal_car;
	protected float utilityFactor_hybrid_car;
	protected float utilityFactor_bicycle;
	protected float utilityFactor_public_transport;
	protected float utilityFactor_motor;
	private Human hum;	

	float r, t, l;
	float dl = -0.15f;
	float fluidlevels[];

	public AgentPreferences(float[] valueTemps, Human hum)
	{
		this.hum = hum;
		fluidlevels = new float[] { 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f };
	}

	public void Update(float[] valueTemps, Human hum, float[] prevFluids) {
		System.out.println("Human = " + hum);
		
		for (int n = 0; n <valueTemps.length ; n++) {
			
			//System.out.println("Current value = " +n);
			updateFluids(n);

			fluidlevels[n] = prevFluids[n] + dl;
			System.out.println(n+ "'s fluid level = " + fluidlevels[n]);
			
			float rlowest = 100;
			r = ((fluidlevels[n] - valueTemps[n]) / valueTemps[n]) * 100;

			if (r < rlowest) {
				rlowest = r;
				t = n;
			}
		}
		System.out.println(t + " is the value that needs most attention");

		// In case power is the most needed
		if (t == 1) {
			utilityFactor_electric_car = 1.4f;
			utilityFactor_electric_bicycle = 1f;
			utilityFactor_normal_car = 1.4f;
			utilityFactor_hybrid_car = 1f;
			utilityFactor_bicycle = 1f;
			utilityFactor_public_transport = 0.6f;
			utilityFactor_motor = 1f;
			System.out.println("updated power levels");

		} else {
			System.out.println("power is not the most needed");
		}
	}

	private float updateFluids(int n) {
		// fluid levels go down by -0.15 by default
		// if a vehicle was the previous action, some values have been fulfilled

		dl = -0.15f;

		if(hum.travel.pastVehicle == null) {
			dl = 0;
		}
		
		else if(hum.travel.pastVehicle.getName() == "electric_car") {
			//System.out.println("EC updatedd");
			// here follows a list of the values that are linked to this action
			if (n == 6 || n == 7 || n == 8 || n == 0 || n == 1) {
				// if the values have been fulfilled, the fluid level OF THIS VALUE rises with 0.3
				dl = 0.3f;
				// some values have a double score for this vehicle
				if (n == 3) {
					dl *= 1.5;
				}
			}
			// here follows a list of the values that have received extra punishment
			if (n == 9 || n == 2 || n == 4) {
				// if the values have been punished, the fluid level goes down with 0.2
				dl = -0.2f;				
			}
		}

		else if(hum.travel.pastVehicle.getName() == "normal_car") {
			//.out.println("ICEV updatedd");
			// here follows a list of the values that have been fulfilled
			if (n == 8 || n == 9 || n == 0 || n == 0 || n == 2 || n == 4 || n == 6 || n == 7 || n == 8) {
				// if the values have been fulfilled, the fluid level rises with 0.3
				dl = 0.3f;
				// some values have a double score for this vehicle
				if (n == 8 || n == 9 || n == 0 || n == 0 || n == 2 || n == 4) {
					dl *= 1.5;
				}				
			}
			// here follows a list of the values that have received extra punishment
			if (n == 5 || n == 3) {
				// if the values have been punished, the fluid level goes down with 0.2
				// all values have a double score for this vehicle
				dl = -0.3f;				
			}
		}

		else if(hum.travel.pastVehicle.getName() == "hybrid_car") {
			//System.out.println("hybrid updatedd");
			// here follows a list of the values that have been fulfilled
			if (n == 2 || n == 0 || n == 1) {
				// if the values have been fulfilled, the fluid level rises with 0.3
				dl = 0.3f;				
			}
			// here follows a list of the values that have received extra punishment
			if (n == 7) {
				// if the values have been punished, the fluid level goes down with 0.2
				dl = -0.2f;				
			}
		}

		else if(hum.travel.pastVehicle.getName() == "bicycle") {
			//System.out.println("bicycle updated");
			// here follows a list of the values that have been fulfilled
			if (n == 7 || n == 9 || n == 5 || n == 6 || n == 8) {				
				// if the values have been fulfilled, the fluid level rises with 0.3
				dl = 0.3f;
				// some values have a double score for this vehicle
				if (n == 7 || n == 9) {
					dl *= 1.5;
				}				
			}
			// here follows a list of the values that have received extra punishment
			if (n == 1) {
				// if the values have been punished, the fluid level goes down with 0.2
				dl = -0.2f;				
			}
		}

		else if(hum.travel.pastVehicle.getName() == "electric_bicycle") {
			//System.out.println("e-bike updated");
			// here follows a list of the values that have been fulfilled
			if (n == 1 || n == 2 || n == 3) {				
				// if the values have been fulfilled, the fluid level rises with 0.3
				dl = 0.3f;				
			}
		}

		else if(hum.travel.pastVehicle.getName() == "public_transport") {
			//System.out.println("OV updatedd");
			// here follows a list of the values that have been fulfilled
			if (n == 5) {
				// if the values have been fulfilled, the fluid level rises with 0.3
				dl = 0.3f;
			}
			// here follows a list of the values that have received extra punishment
			if(n == 9 || n == 9 || n == 1 || n == 2 || n == 4 || n == 0) {
				// if the values have been punished, the fluid level goes down with 0.2
				dl = -0.2f;
				// some values have a double -score for this vehicle
				if(n == 0) {
					dl *= 1.5;
				}
			}
		}

		else if(hum.travel.pastVehicle.getName() == "motor") {
			//System.out.println("motor updatedd");
			// here follows a list of the values that have been fulfilled
			if(n == 3 || n == 4) {
				// if the values have been fulfilled, the fluid level rises with 0.3
				dl = 0.3f;
				// some values have a double score for this vehicle
				if(n == 4) {
					dl *= 1.5;
				}
			}
			// here follows a list of the values that have received extra punishment
			if(n == 6 || n == 7 || n == 8) {
				// if the values have been punished, the fluid level goes down with 0.2
				dl = -0.2f;
			}
		}

		// TODO add walk?
		if(dl == 0)	{
			System.out.println("Be careful: preferences are not updated");
		}
		
		System.out.println("value "+  n + "is updated with " + dl);
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
