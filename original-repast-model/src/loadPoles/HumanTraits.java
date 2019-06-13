package loadPoles;

import java.util.ArrayList;
import java.util.List;

import loadPoles.GridObjects.Dwelling;
import loadPoles.GridObjects.Workplace;
import repast.simphony.context.Context;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;

public class HumanTraits {
	/**
	 * Represents how much influence the neighbors have on this Human's decision to buy an EV.
	 * Ranges from 0 to 1, inclusive.
	 */
	double socialFactor;

	/**
	 * Represents how much influence the environmental effects have on this Human's decision to buy an EV.
	 * Ranges from 0 to 1, inclusive.
	 */
	double environmentFactor;

	/**
	 * Represents whether or not this Human has a license to drive a car.
	 */
	boolean hasCarLicense;	

	/**
	 * Represents the different vehicles available to this Human.
	 */
	List<Vehicle> vehicles;
	
	/*
	 * Represents the list of all vehicles that can be bought
	 */
	List<Vehicle> products;
	
	/*
	 * Represents the gender of this person
	 */
	String gender;
	
	/*
	 * Represents the age of this person
	 */
	int age;
	
	/*
	 * Represents the current income of this person per year
	 */
	int income;	

	/*
	 * Represents whether or not this human is employed
	 */
	boolean isemployed;

	public HumanTraits() {		
		//Initialise variables, vehicles, and preferences belonging to this human		
		socialFactor = RandomHelper.nextDoubleFromTo(0, 1);
		environmentFactor = RandomHelper.nextDoubleFromTo(0, 1);
		
		initFeatures();		
	}
	
	private void initFeatures() {
		//Choose a random gender with 50% chance
		if(RandomHelper.nextDoubleFromTo(0, 1) > 0.5) {
			gender = "female";
		}
		else {
			gender = "male";
		}
		
		//Choose a random age between 15 and 75
		age = RandomHelper.nextIntFromTo(15, 75);
		
		//Different incomes for different age groups, based on data from CBS
		if(15 <= age && age < 25) {
			//Between the ages of 15 to 25, the employment rate is 68.9%, the income is between 600 and 1200
			setIncome(0.689, 600, 1200);
		}
		else if(25 <= age && age < 55) {
			//Between the ages of 15 to 25, the employment rate is 86.0%, the income is between 1600 and 3500
			setIncome(0.860, 1600, 4500);
		}
		else if(55 <= age && age < 65) {
			//Between the ages of 15 to 25, the employment rate is 70.9%, the income is between 1600 and 3500
			setIncome(0.709, 1600, 4500);
		}	
		else {
			//Between the ages 65 and up, the employment rate is 12.9%, the income is between 1600 and 300
			setIncome(0.129, 1600, 3500);
		}
		
		//Determine if person has car license or not, based on data from CBS
		hasCarLicense = false;
		if(18 <= age && age < 20) {
			//Between the ages of 18 to 20, 40% chance of owning a car license
			if(RandomHelper.nextDoubleFromTo(0, 1) > 0.6) {
				hasCarLicense = true;
			}
		}
		else if(20 <= age && age < 30) {
			//Between the ages of 20 and 30, 75% chance of owning a car license
			if(RandomHelper.nextDoubleFromTo(0, 1) > 0.25) {
				hasCarLicense = true;
			}
		}
		else if(30 <= age && age < 70) {
			//Between the ages of 20 and 30, 86% chance of owning a car license
			if(RandomHelper.nextDoubleFromTo(0, 1) > 0.14) {
				hasCarLicense = true;
			}
		}
		else {
			//From ages 70 and up, 61% chance of owning a car license
			if(RandomHelper.nextDoubleFromTo(0, 1) > 0.39) {
				hasCarLicense = true;
			}
		}		
	}
	
	private void setIncome(double employmentRate, int minIncome, int maxIncome) {
		//If the person is employed, set income randomly between minimum and maximum income
		if(RandomHelper.nextDoubleFromTo(0,1) > (1-employmentRate)) {
			income = RandomHelper.nextIntFromTo(minIncome, maxIncome);
			isemployed = true;
		}
		//Else, the person is unemployed, and thus give a low income (so they can atleast travel a bit)
		else {
			income = 500;
		}
	}	
}
