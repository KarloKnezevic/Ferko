package hr.fer.zemris.jcms.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name="course_component_tasks")
@NamedQueries({
    @NamedQuery(name="CourseComponentTask.findByTitle",query="select cct from CourseComponentTask as cct where cct.title=:title"),
    @NamedQuery(name="CourseComponentTask.findByTitleOnItem",query="select cct from CourseComponentTask as cct where cct.title=:title and cct.courseComponentItem=:courseComponentItem"),
    @NamedQuery(name="CourseComponentTask.getTaskUsers",query="select ccta from CourseComponentTaskAssignment as ccta where ccta.courseComponentTask=:courseComponentTask"),
    @NamedQuery(name="CourseComponentTask.getAssignmentOnTask",query="select ccta from CourseComponentTaskAssignment as ccta where ccta.courseComponentTask=:courseComponentTask AND ccta.user=:user"),
    @NamedQuery(name="CourseComponentTask.listAssignments",query="select ccta from CourseComponentTaskAssignment as ccta where ccta.courseComponentTask=:courseComponentTask"),
    @NamedQuery(name="CourseComponentTask.listUserTasks",query="select ccta.courseComponentTask from CourseComponentTaskAssignment as ccta where ccta.courseComponentTask.courseComponentItem=:courseComponentItem AND ccta.user=:user"),
    @NamedQuery(name="CourseComponentTask.listReviewersTask",query="select cct from CourseComponentTask as cct where :user MEMBER OF cct.reviewers AND cct.courseComponentItem=:item"),
    @NamedQuery(name="CourseComponentTask.listTasksOnItem",query="select cct from CourseComponentTask as cct where cct.courseComponentItem.position=:position and cct.courseComponentItem.courseComponent.descriptor.shortName=:componentShortName and cct.courseComponentItem.courseComponent.courseInstance=:courseInstance")
})
public class CourseComponentTask implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private CourseComponentItem courseComponentItem;
	private int position;
	private String title;
	private String description;
	private UserSpecificEvent2 deadline;
	private int filesRequiredCount;
	private String fileTags;
	private long maxFileSize = 2*1024*1024;
	private int maxFilesCount = 10;
	private Set<User> reviewers;
	private boolean needsReviewers = true;
	private Set<TaskDescriptionFile> taskDescriptionFiles = new HashSet<TaskDescriptionFile>();
	
	public CourseComponentTask() {
	}

	@Id @GeneratedValue
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=false)
	public CourseComponentItem getCourseComponentItem() {
		return courseComponentItem;
	}

	public void setCourseComponentItem(CourseComponentItem courseComponentItem) {
		this.courseComponentItem = courseComponentItem;
	}

	@Column(nullable=false)
	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	@Column(length=250,nullable=false)
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(length=5000,nullable=false)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Datum do kada se na ovoj komponenti zadaci mogu riješavati (uploadati i zaključavati).
	 * Može biti null
	 * @return
	 */
	@OneToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=true)
	public UserSpecificEvent2 getDeadline() {
		return deadline;
	}
	public void setDeadline(UserSpecificEvent2 deadline) {
		this.deadline = deadline;
	}

	@Column(nullable=false)
	public int getFilesRequiredCount() {
		return filesRequiredCount;
	}

	public void setFilesRequiredCount(int filesRequiredCount) {
		this.filesRequiredCount = filesRequiredCount;
	}

	@Column(length=1000,nullable=true)
	public String getFileTags() {
		return fileTags;
	}

	public void setFileTags(String fileTags) {
		this.fileTags = fileTags;
	}

	@Column(nullable=false)
	public long getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	@Column(nullable=false)
	public int getMaxFilesCount() {
		return maxFilesCount;
	}

	public void setMaxFilesCount(int maxFilesCount) {
		this.maxFilesCount = maxFilesCount;
	}

	@ManyToMany(fetch=FetchType.LAZY)
	@JoinTable(
			name="componentTask_to_users",
			joinColumns=@JoinColumn(name="task_id",referencedColumnName="id"),
			inverseJoinColumns=@JoinColumn(name="user_id",referencedColumnName="id")
	)
	public Set<User> getReviewers() {
		return reviewers;
	}

	public void setReviewers(Set<User> reviewers) {
		this.reviewers = reviewers;
	}

	@Column(nullable=false)
	public boolean isNeedsReviewers() {
		return needsReviewers;
	}

	public void setNeedsReviewers(boolean needsReviewers) {
		this.needsReviewers = needsReviewers;
	}
	
	@OneToMany(mappedBy="courseComponentTask",fetch=FetchType.LAZY)
	public Set<TaskDescriptionFile> getTaskDescriptionFiles() {
		return taskDescriptionFiles;
	}

	public void setTaskDescriptionFiles(
			Set<TaskDescriptionFile> taskDescriptionFiles) {
		this.taskDescriptionFiles = taskDescriptionFiles;
	}	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((getCourseComponentItem() == null) ? 0 : getCourseComponentItem()
						.hashCode());
		result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CourseComponentTask))
			return false;
		final CourseComponentTask other = (CourseComponentTask) obj;
		if (getCourseComponentItem() == null) {
			if (other.getCourseComponentItem() != null)
				return false;
		} else if (!getCourseComponentItem().equals(other.getCourseComponentItem()))
			return false;
		if (getTitle() == null) {
			if (other.getTitle() != null)
				return false;
		} else if (!getTitle().equals(other.getTitle()))
			return false;
		return true;
	}
}