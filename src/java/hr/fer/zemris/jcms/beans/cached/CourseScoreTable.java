package hr.fer.zemris.jcms.beans.cached;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Ukupni prikaz bodovnog stanja za sve studente na kolegiju. Tablica započinje prvim stupcem koji čuva objekte tipa:
 * {@link STEStudent}, gdje se nalaze informacije o studentu (ime, prezime i jmbag). Zaglavlje je u tom slučaju objekt
 * {@link STHEStudent}. Svi sljedeći stupci su tipa {@link ScoreTableEntry}, tj. jednog od podrazreda tog tipa, a zaglavlja
 * tih stupaca su tipa {@link ScoreTableHeaderEntry} odnosno nekog od podrazreda tog tipa.
 * 
 * @author marcupic
 */
public class CourseScoreTable implements Serializable {

	private static final long serialVersionUID = 1L;

	private String courseInstanceID;
	private List<ScoreTableEntry[]> tableItems;
	private List<ScoreTableHeaderEntry> tableHeader;
	private Map<Long, Integer> mapUserToRow;
	private List<int[]> indexes;
	
	public CourseScoreTable() {
	}
	
	public String getCourseInstanceID() {
		return courseInstanceID;
	}
	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
	public List<ScoreTableEntry[]> getTableItems() {
		return tableItems;
	}
	public void setTableItems(List<ScoreTableEntry[]> tableItems) {
		this.tableItems = tableItems;
	}
	public List<ScoreTableHeaderEntry> getTableHeader() {
		return tableHeader;
	}
	public void setTableHeader(List<ScoreTableHeaderEntry> tableHeader) {
		this.tableHeader = tableHeader;
	}
	public List<int[]> getIndexes() {
		return indexes;
	}
	public void setIndexes(List<int[]> indexes) {
		this.indexes = indexes;
	}
	public Map<Long, Integer> getMapUserToRow() {
		return mapUserToRow;
	}
	public void setMapUserToRow(Map<Long, Integer> mapUserToRow) {
		this.mapUserToRow = mapUserToRow;
	}
}
