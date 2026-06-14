package hr.fer.zemris.ferko.application.usecase.academic;

import hr.fer.zemris.ferko.application.port.AppUserRepository;
import hr.fer.zemris.ferko.application.port.CourseRepository;
import hr.fer.zemris.ferko.application.port.EnrollmentRepository;
import hr.fer.zemris.ferko.application.port.RoomRepository;
import hr.fer.zemris.ferko.application.port.SemesterRepository;
import hr.fer.zemris.ferko.application.port.StudentRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.Enrollment;
import hr.fer.zemris.ferko.domain.model.Role;
import hr.fer.zemris.ferko.domain.model.Room;
import hr.fer.zemris.ferko.domain.model.Semester;
import hr.fer.zemris.ferko.domain.model.Student;
import hr.fer.zemris.ferko.domain.model.StudentGroup;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/** Read-side facade exposing academic data as interface-friendly views. */
public class AcademicQueryService {

  private final SemesterRepository semesterRepository;
  private final CourseRepository courseRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final StudentRepository studentRepository;
  private final RoomRepository roomRepository;
  private final AppUserRepository userRepository;

  public AcademicQueryService(
      SemesterRepository semesterRepository,
      CourseRepository courseRepository,
      EnrollmentRepository enrollmentRepository,
      StudentRepository studentRepository,
      RoomRepository roomRepository,
      AppUserRepository userRepository) {
    this.semesterRepository = semesterRepository;
    this.courseRepository = courseRepository;
    this.enrollmentRepository = enrollmentRepository;
    this.studentRepository = studentRepository;
    this.roomRepository = roomRepository;
    this.userRepository = userRepository;
  }

  public List<SemesterView> listSemesters() {
    return semesterRepository.findAll().stream().map(AcademicQueryService::toView).toList();
  }

  public Optional<SemesterView> activeSemester() {
    return semesterRepository.findActive().map(AcademicQueryService::toView);
  }

  public List<CourseSummaryView> listCourses(String semesterCode) {
    List<Course> courses =
        semesterCode == null || semesterCode.isBlank()
            ? courseRepository.findAll()
            : courseRepository.findBySemester(semesterCode);
    return courses.stream()
        .map(
            course ->
                new CourseSummaryView(
                    course.id(),
                    course.code(),
                    course.name(),
                    course.semesterCode(),
                    course.ects(),
                    enrollmentRepository.findByCourse(course.id()).size()))
        .toList();
  }

  public Optional<CourseDetailView> courseDetail(long courseId) {
    return courseRepository
        .findById(courseId)
        .map(
            course -> {
              Map<Long, String> names = userNames();
              List<CourseStaffView> staff =
                  courseRepository.findStaffByCourse(courseId).stream()
                      .map(
                          member ->
                              new CourseStaffView(
                                  member.userId(),
                                  names.getOrDefault(member.userId(), ""),
                                  member.role().name()))
                      .toList();
              List<StudentGroupView> groups =
                  courseRepository.findGroupsByCourse(courseId).stream()
                      .map(
                          group ->
                              new StudentGroupView(
                                  group.id(),
                                  group.groupCode(),
                                  group.type().name(),
                                  group.category(),
                                  group.capacity()))
                      .toList();
              return new CourseDetailView(
                  course.id(),
                  course.code(),
                  course.name(),
                  course.semesterCode(),
                  course.ects(),
                  course.description(),
                  course.literature(),
                  enrollmentRepository.findByCourse(courseId).size(),
                  staff,
                  groups);
            });
  }

  public List<StudentView> listStudents() {
    Map<Long, String> names = userNames();
    return studentRepository.findAll().stream()
        .map(student -> toStudentView(student, names))
        .toList();
  }

  public Optional<StudentView> getStudentByJmbag(String jmbag) {
    Map<Long, String> names = userNames();
    return studentRepository.findByJmbag(jmbag).map(student -> toStudentView(student, names));
  }

  public List<RoomView> listRooms() {
    return roomRepository.findAll().stream().map(AcademicQueryService::toView).toList();
  }

  public SyncStatusView syncStatus() {
    return new SyncStatusView(
        semesterRepository.findAll().size(),
        courseRepository.findAll().size(),
        studentRepository.findAll().size(),
        roomRepository.findAll().size());
  }

  public List<AppUserView> listUsers() {
    return userRepository.findAll().stream()
        .map(
            user ->
                new AppUserView(
                    user.id(),
                    user.username(),
                    user.fullName(),
                    user.email(),
                    user.active(),
                    user.roles().stream().map(Role::name).sorted().toList()))
        .toList();
  }

  public List<EnrollmentView> listEnrollments(long courseId) {
    Map<Long, String> names = userNames();
    Map<Long, String> groupCodeById =
        courseRepository.findGroupsByCourse(courseId).stream()
            .collect(Collectors.toMap(StudentGroup::id, StudentGroup::groupCode, (a, b) -> a));
    return enrollmentRepository.findByCourse(courseId).stream()
        .map(enrollment -> toEnrollmentView(enrollment, names, groupCodeById))
        .toList();
  }

  private Map<Long, String> userNames() {
    return userRepository.findAll().stream()
        .collect(Collectors.toMap(AppUser::id, AppUser::fullName, (a, b) -> a));
  }

  private EnrollmentView toEnrollmentView(
      Enrollment enrollment, Map<Long, String> names, Map<Long, String> groupCodeById) {
    String jmbag = "";
    String fullName = "";
    Optional<Student> student = studentRepository.findById(enrollment.studentId());
    if (student.isPresent()) {
      jmbag = student.get().jmbag();
      fullName = names.getOrDefault(student.get().userId(), "");
    }
    List<String> groupCodes =
        enrollmentRepository.findMembershipsByEnrollment(enrollment.id()).stream()
            .map(membership -> groupCodeById.get(membership.groupId()))
            .filter(Objects::nonNull)
            .toList();
    return new EnrollmentView(
        enrollment.id(),
        enrollment.studentId(),
        jmbag,
        fullName,
        enrollment.courseId(),
        enrollment.status().name(),
        groupCodes);
  }

  private static StudentView toStudentView(Student student, Map<Long, String> names) {
    return new StudentView(
        student.id(),
        student.jmbag(),
        names.getOrDefault(student.userId(), ""),
        student.studyProgram(),
        student.yearOfStudy());
  }

  private static SemesterView toView(Semester semester) {
    return new SemesterView(
        semester.code(),
        semester.academicYear(),
        semester.term(),
        semester.startsOn(),
        semester.endsOn(),
        semester.active());
  }

  private static RoomView toView(Room room) {
    return new RoomView(
        room.id(),
        room.code(),
        room.building(),
        room.capacity(),
        room.requiredAssistants(),
        room.hasComputers());
  }
}
