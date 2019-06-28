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
	private Human human;

	float r, t, l;
	float dl = -0.15f;
	float[] valueTemps;
	float[] fluidlevels;
	float positiveFactor = 1.4f;
	float negativeFactor = 0.6f;
	float dlUp;
	float dlDown;
	float rhighest;

	public AgentPreferences(Human human) {
		this.human = human;
		fluidlevels = new float[] { 100f, 100f, 100f, 100f };
		agentActionType = RandomHelper.nextIntFromTo(0, 3);
		initAgentPreferences();
	}

	// Initialise the preferences that this human has
	private void initAgentPreferences() {
		valueTemps = new float[4];

		int maxv = 0;
		for (int i = 0; i < valueTemps.length / 2; i++) {
			// assign a value to the valueWeight (i) and the contrastWeight (j) so that:
			// i is between 0 and 150
			// if i == 0, j >= 50 and the other way around
			// i + j together are between 50 and 150
			// j cannot be smaller than 0
			int valueWeight = RandomHelper.nextIntFromTo(0, 150);
			int contrastWeight = RandomHelper.nextIntFromTo(Math.max(0, 50 - valueWeight), 150 - valueWeight);

			int j = i + valueTemps.length / 2;
			valueTemps[i] = valueWeight;
			valueTemps[j] = contrastWeight;

			// determine the type of agent by logging what value has the highest temperature
			if (valueWeight > maxv) {
				maxv = valueWeight;
				human.agentType = i;
			}

			if (contrastWeight > maxv) {
				maxv = contrastWeight;
				human.agentType = j;
			}
		}
	}

	// Update the fluidlevels
	public void update() {
		// set the comparison value to the max distance
		rhighest = -1000;
		for (int n = 0; n < valueTemps.length; n++) {
			float temps = valueTemps[n];
			fluidlevels[n] += updateFluids(n, temps);

			// determine the upper and lower levels of the fluid tanks
			if (fluidlevels[n] > 200) {
				fluidlevels[n] = 200;
			}
			if (fluidlevels[n] < 0) {
				fluidlevels[n] = 0;
			}

			r = (-((fluidlevels[n] - valueTemps[n])) / valueTemps[n]) * 100;

			if (r > rhighest) {
				rhighest = r;
				t = n;
			}

			// In case self-transcendence is the most needed value
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
				agentActionType = 1;
				utilityFactor_electric_car = 1f;
				utilityFactor_electric_bicycle = 1f;
				utilityFactor_normal_car = 1f * 2 * this.positiveFactor;
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
				agentActionType = 3;
				utilityFactor_electric_car = 1f;
				utilityFactor_electric_bicycle = 1f;
				utilityFactor_normal_car = 1f * this.negativeFactor;
				utilityFactor_hybrid_car = 1f * this.positiveFactor;
				utilityFactor_bicycle = 1f;
				utilityFactor_public_transport = 1f * this.negativeFactor;
				utilityFactor_motor = 1f * this.positiveFactor;
			}
		}
	}

	private float updateFluids(int n, float temps) {
		// Fluid levels go down by -0.15 by default
		// if a vehicle was the previous action, some values have been fulfilled
		dl = -10f;
		dlUp = (100f - temps) * 0.2f;
		dlDown = -1 * dlUp;

		if (human.travel.pastVehicle == null) {
			dl = 0;
		}

		else if (human.travel.pastVehicle.getName() == "electric_car") {
			// Here follows a list of the values that are positively linked to this action
			if (n == 0) {
				// If the values have been fulfilled, the fluid level OF THIS VALUE rises with 0.3
				dl = dlUp;
			}
			// There are no punishments for electric cars
		}

		else if (human.travel.pastVehicle.getName() == "normal_car") {
			// Here follows a list of the values that have been fulfilled
			if (n == 1 || n == 2) {
				// If the values have been fulfilled, the fluid level rises with 0.3
				dl = dlUp;
				// Some values have a double score for this vehicle
				if (n == 1) {
					dl *= 1.5;
				}
			}
			// Here follows a list of the values that have received extra punishment
			if (n == 0 || n == 3) {
				// If the values have been punished, the fluid level goes down with 0.3
				dl = dlDown;
			}
		}

		else if (human.travel.pastVehicle.getName() == "hybrid_car") {
			// Here follows a list of the values that have been fulfilled
			if (n == 2 || n == 3) {
				// if the values have been fulfilled, the fluid level rises with 0.3
				dl = dlUp;
			}
			// There are no punishments for this vehicle type
		}

		else if (human.travel.pastVehicle.getName() == "bicycle") {
			// There is a neutral stance towards bikes
			dl = 0;
		}

		else if (human.travel.pastVehicle.getName() == "electric_bicycle") {
			// Here follows a list of the values that have received extra punishment
			if (n == 2) {
				dl = dlDown;
			}
		}

		else if (human.travel.pastVehicle.getName() == "public_transport") {
			// Here follows a list of the values that have been fulfilled
			if (n == 0) {
				// If the values have been fulfilled, the fluid level rises with 0.3
				dl = dlUp;
			}
			// Here follows a list of the values that have received extra punishment
			if (n == 1 || n == 2 || n == 3) {
				// If the values have been punished, the fluid level goes down with 0.2
				dl = dlDown;
			}
		}

		else if (human.travel.pastVehicle.getName() == "motor") {
			// Here follows a list of the values that have been fulfilled
			if (n == 3) {
				// If the values have been fulfilled, the fluid level rises with 0.3
				dl = dlUp;
			}
			// Here follows a list of the values that have received extra punishment
			if (n == 0 || n == 1) {
				// If the values have been punished, the fluid level goes down with 0.2
				dl = dlDown;
			}
		}

		return dl;
	}

	// Find the utility factor for a given vehicle
	public float getUtilityFactor(Vehicle vehicle) {
		switch (vehicle.getName()) {
		case "bicycle":
			return utilityFactor_bicycle;
		case "electric_bicycle":
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
