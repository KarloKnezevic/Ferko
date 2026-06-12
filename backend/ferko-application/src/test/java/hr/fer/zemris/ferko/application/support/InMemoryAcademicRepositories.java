package hr.fer.zemris.ferko.application.support;

import hr.fer.zemris.ferko.application.port.AppUserRepository;
import hr.fer.zemris.ferko.application.port.CourseRepository;
import hr.fer.zemris.ferko.application.port.EnrollmentRepository;
import hr.fer.zemris.ferko.application.port.RoomRepository;
import hr.fer.zemris.ferko.application.port.SemesterRepository;
import hr.fer.zemris.ferko.application.port.StudentRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.CourseStaff;
import hr.fer.zemris.ferko.domain.model.Enrollment;
import hr.fer.zemris.ferko.domain.model.GroupMembership;
import hr.fer.zemris.ferko.domain.model.Room;
import hr.fer.zemris.ferko.domain.model.Semester;
import hr.fer.zemris.ferko.domain.model.Student;
import hr.fer.zemris.ferko.domain.model.StudentGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** Lightweight in-memory fakes of the academic repositories for use-case tests. */
public final class InMemoryAcademicRepositories {

  private InMemoryAcademicRepositories() {}

  /** In-memory {@link AppUserRepository}. */
  public static final class Users implements AppUserRepository {
    private final List<AppUser> data = new ArrayList<>();
    private final AtomicLong seq = new AtomicLong(1000);

    @Override
    public AppUser save(AppUser user) {
      AppUser stored =
          user.id() <= 0
              ? new AppUser(
                  seq.incrementAndGet(),
                  user.username(),
                  user.passwordHash(),
                  user.fullName(),
                  user.email(),
                  user.active(),
                  user.createdAt(),
                  user.roles())
              : user;
      data.removeIf(existing -> existing.id() == stored.id());
      data.add(stored);
      return stored;
    }

    @Override
    public Optional<AppUser> findById(long id) {
      return data.stream().filter(user -> user.id() == id).findFirst();
    }

    @Override
    public Optional<AppUser> findByUsername(String username) {
      return data.stream().filter(user -> user.username().equals(username)).findFirst();
    }

    @Override
    public List<AppUser> findAll() {
      return List.copyOf(data);
    }
  }

  /** In-memory {@link SemesterRepository}. */
  public static final class Semesters implements SemesterRepository {
    private final List<Semester> data = new ArrayList<>();

    @Override
    public Semester save(Semester semester) {
      data.removeIf(existing -> existing.code().equals(semester.code()));
      data.add(semester);
      return semester;
    }

    @Override
    public Optional<Semester> findByCode(String code) {
      return data.stream().filter(s -> s.code().equals(code)).findFirst();
    }

    @Override
    public Optional<Semester> findActive() {
      return data.stream().filter(Semester::active).findFirst();
    }

    @Override
    public List<Semester> findAll() {
      return List.copyOf(data);
    }
  }

  /** In-memory {@link CourseRepository}. */
  public static final class Courses implements CourseRepository {
    private final List<Course> courses = new ArrayList<>();
    private final List<CourseStaff> staff = new ArrayList<>();
    private final List<StudentGroup> groups = new ArrayList<>();
    private final AtomicLong seq = new AtomicLong(0);

    @Override
    public Course save(Course course) {
      long id = course.id() <= 0 ? seq.incrementAndGet() : course.id();
      Course stored =
          new Course(
              id,
              course.code(),
              course.name(),
              course.semesterCode(),
              course.ects(),
              course.description(),
              course.literature());
      courses.removeIf(existing -> existing.id() == id);
      courses.add(stored);
      return stored;
    }

    @Override
    public Optional<Course> findById(long id) {
      return courses.stream().filter(c -> c.id() == id).findFirst();
    }

    @Override
    public Optional<Course> findByCodeAndSemester(String code, String semesterCode) {
      return courses.stream()
          .filter(c -> c.code().equals(code) && c.semesterCode().equals(semesterCode))
          .findFirst();
    }

    @Override
    public List<Course> findBySemester(String semesterCode) {
      return courses.stream().filter(c -> c.semesterCode().equals(semesterCode)).toList();
    }

    @Override
    public List<Course> findAll() {
      return List.copyOf(courses);
    }

    @Override
    public CourseStaff addStaff(CourseStaff member) {
      CourseStaff stored =
          new CourseStaff(seq.incrementAndGet(), member.courseId(), member.userId(), member.role());
      staff.add(stored);
      return stored;
    }

    @Override
    public List<CourseStaff> findStaffByCourse(long courseId) {
      return staff.stream().filter(s -> s.courseId() == courseId).toList();
    }

    @Override
    public StudentGroup addGroup(StudentGroup group) {
      StudentGroup stored =
          new StudentGroup(
              seq.incrementAndGet(),
              group.courseId(),
              group.groupCode(),
              group.type(),
              group.category(),
              group.capacity());
      groups.add(stored);
      return stored;
    }

    @Override
    public List<StudentGroup> findGroupsByCourse(long courseId) {
      return groups.stream().filter(g -> g.courseId() == courseId).toList();
    }
  }

  /** In-memory {@link StudentRepository}. */
  public static final class Students implements StudentRepository {
    private final List<Student> data = new ArrayList<>();
    private final AtomicLong seq = new AtomicLong(0);

    @Override
    public Student save(Student student) {
      long id = student.id() <= 0 ? seq.incrementAndGet() : student.id();
      Student stored =
          new Student(
              id, student.userId(), student.jmbag(), student.studyProgram(), student.yearOfStudy());
      data.removeIf(existing -> existing.id() == id);
      data.add(stored);
      return stored;
    }

    @Override
    public Optional<Student> findById(long id) {
      return data.stream().filter(s -> s.id() == id).findFirst();
    }

    @Override
    public Optional<Student> findByJmbag(String jmbag) {
      return data.stream().filter(s -> s.jmbag().equals(jmbag)).findFirst();
    }

    @Override
    public Optional<Student> findByUserId(long userId) {
      return data.stream().filter(s -> s.userId() == userId).findFirst();
    }

    @Override
    public List<Student> findAll() {
      return List.copyOf(data);
    }
  }

  /** In-memory {@link EnrollmentRepository}. */
  public static final class Enrollments implements EnrollmentRepository {
    private final List<Enrollment> data = new ArrayList<>();
    private final List<GroupMembership> memberships = new ArrayList<>();
    private final AtomicLong seq = new AtomicLong(0);

    @Override
    public Enrollment save(Enrollment enrollment) {
      long id = enrollment.id() <= 0 ? seq.incrementAndGet() : enrollment.id();
      Enrollment stored =
          new Enrollment(
              id,
              enrollment.studentId(),
              enrollment.courseId(),
              enrollment.enrolledAt(),
              enrollment.status());
      data.removeIf(existing -> existing.id() == id);
      data.add(stored);
      return stored;
    }

    @Override
    public Optional<Enrollment> findById(long id) {
      return data.stream().filter(e -> e.id() == id).findFirst();
    }

    @Override
    public Optional<Enrollment> findByStudentAndCourse(long studentId, long courseId) {
      return data.stream()
          .filter(e -> e.studentId() == studentId && e.courseId() == courseId)
          .findFirst();
    }

    @Override
    public List<Enrollment> findByCourse(long courseId) {
      return data.stream().filter(e -> e.courseId() == courseId).toList();
    }

    @Override
    public List<Enrollment> findByStudent(long studentId) {
      return data.stream().filter(e -> e.studentId() == studentId).toList();
    }

    @Override
    public GroupMembership assignGroup(GroupMembership membership) {
      GroupMembership stored =
          new GroupMembership(
              seq.incrementAndGet(), membership.enrollmentId(), membership.groupId());
      memberships.add(stored);
      return stored;
    }

    @Override
    public List<GroupMembership> findMembershipsByEnrollment(long enrollmentId) {
      return memberships.stream().filter(m -> m.enrollmentId() == enrollmentId).toList();
    }
  }

  /** In-memory {@link RoomRepository}. */
  public static final class Rooms implements RoomRepository {
    private final List<Room> data = new ArrayList<>();
    private final AtomicLong seq = new AtomicLong(0);

    @Override
    public Room save(Room room) {
      long id = room.id() <= 0 ? seq.incrementAndGet() : room.id();
      Room stored =
          new Room(
              id,
              room.code(),
              room.building(),
              room.capacity(),
              room.requiredAssistants(),
              room.hasComputers());
      data.removeIf(existing -> existing.id() == id);
      data.add(stored);
      return stored;
    }

    @Override
    public Optional<Room> findById(long id) {
      return data.stream().filter(r -> r.id() == id).findFirst();
    }

    @Override
    public Optional<Room> findByCode(String code) {
      return data.stream().filter(r -> r.code().equals(code)).findFirst();
    }

    @Override
    public List<Room> findAll() {
      return List.copyOf(data);
    }
  }
}
