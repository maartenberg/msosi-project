package loadPoles;

import repast.simphony.data2.AggregateDataSource;

public class HappinessAggregateDataSourceB implements AggregateDataSource {

	@Override
	public String getId() {
		
		return "Happiness per Type (B)";
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
		if(human.getType() == "b") {
			return human.getHappiness();
		}
		return null;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

}
