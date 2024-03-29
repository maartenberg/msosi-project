package loadPoles;

import repast.simphony.random.RandomHelper;

public class HumanTraits {
	// Represents whether or not this Human has a license to drive a car.
	boolean hasCarLicense;

	// Represents the gender of this person
	String gender;

	// Represents the age of this person
	int age;

	// Represents the current income of this person per year
	int income;

	// Represents whether or not this human is employed
	boolean isemployed;

	public HumanTraits() {
		initFeatures();
	}

	// Initialise this human's features based on data from CBS
	private void initFeatures() {
		// Choose a random gender with 50% chance
		if (RandomHelper.nextDoubleFromTo(0, 1) > 0.5) {
			gender = "female";
		} else {
			gender = "male";
		}

		// Choose a random age between 15 and 75
		age = RandomHelper.nextIntFromTo(15, 75);

		// Different incomes for different age groups, based on data from CBS
		if (15 <= age && age < 25) {
			// Between the ages of 15 to 25, the employment rate is 68.9%, the income is between 600 and 1200
			setIncome(0.689, 600, 1200);
		} else if (25 <= age && age < 55) {
			// Between the ages of 25 to 55, the employment rate is 86.0%, the income is between 1600 and 4500
			setIncome(0.860, 1600, 4500);
		} else if (55 <= age && age < 65) {
			// Between the ages of 55 to 65, the employment rate is 70.9%, the income is between 1600 and 4500
			setIncome(0.709, 1600, 4500);
		} else {
			// Between the ages 65 and up, the employment rate is 12.9%, the income is between 1600 and 3500
			setIncome(0.129, 1600, 3500);
		}

		// Determine if person has car license or not, based on data from CBS
		hasCarLicense = false;
		if (18 <= age && age < 20) {
			// Between the ages of 18 to 20, 40% chance of owning a car license
			if (RandomHelper.nextDoubleFromTo(0, 1) < 0.4) {
				hasCarLicense = true;
			}
		} else if (20 <= age && age < 30) {
			// Between the ages of 20 and 30, 75% chance of owning a car license
			if (RandomHelper.nextDoubleFromTo(0, 1) < 0.75) {
				hasCarLicense = true;
			}
		} else if (30 <= age && age < 70) {
			// Between the ages of 20 and 30, 86% chance of owning a car license
			if (RandomHelper.nextDoubleFromTo(0, 1) < 0.86) {
				hasCarLicense = true;
			}
		} else {
			// From ages 70 and up, 61% chance of owning a car license
			if (RandomHelper.nextDoubleFromTo(0, 1) < 0.61) {
				hasCarLicense = true;
			}
		}
	}

	// Sets the income for this human based on an employment rate, minimum income and maximum income
	private void setIncome(double employmentRate, int minIncome, int maxIncome) {
		// If the person is employed, set income randomly between minimum and maximum income
		if (RandomHelper.nextDoubleFromTo(0, 1) < employmentRate) {
			income = RandomHelper.nextIntFromTo(minIncome, maxIncome);
			isemployed = true;
		}
		// Else, the person is unemployed, and thus give a low income (so they can at least travel a bit)
		else {
			income = 500;
		}
	}
}
