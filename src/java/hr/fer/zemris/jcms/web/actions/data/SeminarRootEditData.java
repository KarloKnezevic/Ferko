package hr.fer.zemris.jcms.web.actions.data;

import java.util.List;

import hr.fer.zemris.jcms.model.SeminarRoot;
import hr.fer.zemris.jcms.web.actions.SeminarRootEdit;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link SeminarRootEdit}.
 *  
 * @author marcupic
 *
 */
public class SeminarRootEditData extends AbstractActionData {

	private Long id;
	private String yearSemester;
	private boolean active;
	private String source;
	private String groupName;
	private List<SeminarRoot> allSeminarRoots;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public SeminarRootEditData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getYearSemester() {
		return yearSemester;
	}

	public void setYearSemester(String yearSemester) {
		this.yearSemester = yearSemester;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	public List<SeminarRoot> getAllSeminarRoots() {
		return allSeminarRoots;
	}
	public void setAllSeminarRoots(List<SeminarRoot> allSeminarRoots) {
		this.allSeminarRoots = allSeminarRoots;
	}
}
