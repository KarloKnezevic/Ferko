package hr.fer.zemris.jcms.statistics.assessments;

import java.io.Serializable;

public class StatisticsName implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * E:G:groupID<br/>
	 * Effective, global (score only), for group with given ID
	 * A:G:groupID<br/>
	 * Assessment, global (score only), for group with given ID
	 * R:G:groupID<br/>
	 * Raw, global (score only), for group with given ID
	 */
	private String kind;
	private Long lectureGroupID;
	private String lectureGroupName;
	private String title;
	private int id;
	
	public StatisticsName() {
	}

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public Long getLectureGroupID() {
		return lectureGroupID;
	}

	public void setLectureGroupID(Long lectureGroupID) {
		this.lectureGroupID = lectureGroupID;
	}

	public String getLectureGroupName() {
		return lectureGroupName;
	}

	public void setLectureGroupName(String lectureGroupName) {
		this.lectureGroupName = lectureGroupName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StatisticsName other = (StatisticsName) obj;
		if (kind == null) {
			if (other.kind != null)
				return false;
		} else if (!kind.equals(other.kind))
			return false;
		return true;
	}
}
