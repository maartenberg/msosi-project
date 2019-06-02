package loadPoles;

import repast.simphony.data2.AggregateDataSource;

public class HappinessAggregateDataSourceA implements AggregateDataSource {

	@Override
	public String getId() {
		
		return "Happiness per Type (A)";
	}

	@Override
	public Class<?> getDataType() {
		
		return float.class;
	}

	@Override
	public Class<?> getSourceType() {
		// TODO Auto-generated method stub
		return Human.class;
	}

	@Override
	public Object get(Iterable<?> objs, int size) {
		Human human = (Human) objs.iterator().next();
		if(!human.hasChargeableCar()) {
			return human.getHappiness();
		}
		//Crashes if it returns null. Return 0 for now
		//return null
		return 0;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

}
