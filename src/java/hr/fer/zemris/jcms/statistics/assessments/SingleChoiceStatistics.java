package hr.fer.zemris.jcms.statistics.assessments;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SingleChoiceStatistics extends StatisticsBase implements Serializable {

	private static final long serialVersionUID = 1L;

	List<SingleChoiceStatisticsRow> rows; 

	public SingleChoiceStatistics(List<SingleChoiceStatisticsRow> list) {
		rows = new ArrayList<SingleChoiceStatisticsRow>(list);
	}
	
	public List<SingleChoiceStatisticsRow> getRows() {
		return rows;
	}

	@Override
	public int getStatisticsBaseType() {
		return 2;
	}

}
