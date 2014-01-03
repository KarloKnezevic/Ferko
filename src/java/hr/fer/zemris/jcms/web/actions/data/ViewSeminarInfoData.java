package hr.fer.zemris.jcms.web.actions.data;

import java.util.List;

import hr.fer.zemris.jcms.model.SeminarInfo;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.jcms.web.actions2.course.ViewSeminarInfo;

/**
 * Podatkovna struktura za akciju {@link ViewSeminarInfo}.
 *  
 * @author marcupic
 *
 */
public class ViewSeminarInfoData extends AbstractActionData {

	private Long id;
	private SeminarInfo selectedSeminar;
	private List<SeminarInfo> allSeminars;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public ViewSeminarInfoData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public SeminarInfo getSelectedSeminar() {
		return selectedSeminar;
	}
	public void setSelectedSeminar(SeminarInfo selectedSeminar) {
		this.selectedSeminar = selectedSeminar;
	}
	public List<SeminarInfo> getAllSeminars() {
		return allSeminars;
	}
	public void setAllSeminars(List<SeminarInfo> allSeminars) {
		this.allSeminars = allSeminars;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
}
