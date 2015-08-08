package inc.morsecode.pagerduty;

import java.util.ArrayList;

public class ListResult<T> extends ArrayList<T> {

	private int count;
	
	public ListResult(int count) {
		super();
		this.count= count;
	}

	public int getCount() {
		return count;
	}
}
