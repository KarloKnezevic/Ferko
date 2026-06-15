package hr.fer.zemris.ferko.application.usecase.academic;

import hr.fer.zemris.ferko.application.port.AppUserRepository;
import hr.fer.zemris.ferko.application.port.ClassScheduleRepository;
import hr.fer.zemris.ferko.application.port.CourseRepository;
import hr.fer.zemris.ferko.application.port.EnrollmentRepository;
import hr.fer.zemris.ferko.application.port.RoomRepository;
import hr.fer.zemris.ferko.application.port.SemesterRepository;
import hr.fer.zemris.ferko.application.port.StudentRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.ClassSchedule;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.CourseStaff;
import hr.fer.zemris.ferko.domain.model.Enrollment;
import hr.fer.zemris.ferko.domain.model.EnrollmentStatus;
import hr.fer.zemris.ferko.domain.model.GroupMembership;
import hr.fer.zemris.ferko.domain.model.GroupType;
import hr.fer.zemris.ferko.domain.model.Role;
import hr.fer.zemris.ferko.domain.model.Room;
import hr.fer.zemris.ferko.domain.model.Semester;
import hr.fer.zemris.ferko.domain.model.Student;
import hr.fer.zemris.ferko.domain.model.StudentGroup;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/** Write-side facade for creating academic data; all operations are idempotent. */
public class AcademicProvisioningService {

  private final SemesterRepository semesterRepository;
  private final CourseRepository courseRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final StudentRepository studentRepository;
  private final RoomRepository roomRepository;
  private final AppUserRepository userRepository;
  private final ClassScheduleRepository classScheduleRepository;

  public AcademicProvisioningService(
      SemesterRepository semesterRepository,
      CourseRepository courseRepository,
      EnrollmentRepository enrollmentRepository,
      StudentRepository studentRepository,
      RoomRepository roomRepository,
      AppUserRepository userRepository,
      ClassScheduleRepository classScheduleRepository) {
    this.semesterRepository = semesterRepository;
    this.courseRepository = courseRepository;
    this.enrollmentRepository = enrollmentRepository;
    this.studentRepository = studentRepository;
    this.roomRepository = roomRepository;
    this.userRepository = userRepository;
    this.classScheduleRepository = classScheduleRepository;
  }

  /**
   * Adds a recurring weekly timetable slot for a course. Callers pass primitives only (kept free of
   * domain types) — the {@link ClassSchedule} aggregate is assembled here.
   */
  public long provisionClassSchedule(
      long courseId,
      Long groupId,
      String type,
      Long roomId,
      String dayOfWeek,
      LocalTime startsAt,
      LocalTime endsAt,
      String instructor) {
    GroupType groupType = GroupType.valueOf(type);
    DayOfWeek day = DayOfWeek.valueOf(dayOfWeek);
    // Idempotent: a slot is identified by its course, group, type, weekday, time span and room, so
    // re-running the seeder on a persistent database does not duplicate the timetable. The group is
    // part of the identity so two groups alternating in the same room/time are kept as distinct
    // slots (otherwise the second group's session would be lost).
    return classScheduleRepository.findByCourse(courseId).stream()
        .filter(
            slot ->
                slot.type() == groupType
                    && slot.dayOfWeek() == day
                    && slot.startsAt().equals(startsAt)
                    && slot.endsAt().equals(endsAt)
                    && java.util.Objects.equals(slot.roomId(), roomId)
                    && java.util.Objects.equals(slot.groupId(), groupId))
        .map(ClassSchedule::id)
        .findFirst()
        .orElseGet(
            () ->
                classScheduleRepository
                    .save(
                        new ClassSchedule(
                            0L,
                            courseId,
                            groupId,
                            groupType,
                            roomId,
                            day,
                            startsAt,
                            endsAt,
                            instructor))
                    .id());
  }

  public void provisionSemester(
      String code,
      String academicYear,
      String term,
      LocalDate startsOn,
      LocalDate endsOn,
      boolean active) {
    semesterRepository.save(new Semester(code, academicYear, term, startsOn, endsOn, active));
  }

  public long provisionRoom(
      String code, String building, int capacity, int requiredAssistants, boolean hasComputers) {
    return roomRepository
        .findByCode(code)
        .map(Room::id)
        .orElseGet(
            () ->
                roomRepository
                    .save(new Room(0L, code, building, capacity, requiredAssistants, hasComputers))
                    .id());
  }

  public long provisionCourse(
      String code,
      String name,
      String semesterCode,
      int ects,
      String description,
      String literature) {
    return courseRepository
        .findByCodeAndSemester(code, semesterCode)
        .map(Course::id)
        .orElseGet(
            () ->
                courseRepository
                    .save(new Course(0L, code, name, semesterCode, ects, description, literature))
                    .id());
  }

  public void assignStaff(long courseId, long userId, String role) {
    boolean exists =
        courseRepository.findStaffByCourse(courseId).stream()
            .anyMatch(staff -> staff.userId() == userId && staff.role().name().equals(role));
    if (!exists) {
      courseRepository.addStaff(new CourseStaff(0L, courseId, userId, Role.valueOf(role)));
    }
  }

  /**
   * Assigns a teaching role on a course to the user with the given username. Returns {@code false}
   * if no such user exists.
   */
  public boolean assignStaffByUsername(long courseId, String username, String role) {
    return userRepository
        .findByUsername(username)
        .map(
            user -> {
              assignStaff(courseId, user.id(), role);
              return true;
            })
        .orElse(false);
  }

  public long provisionGroup(
      long courseId, String groupCode, String type, String category, int capacity) {
    GroupType groupType = GroupType.valueOf(type);
    return courseRepository.findGroupsByCourse(courseId).stream()
        .filter(group -> group.groupCode().equals(groupCode) && group.type() == groupType)
        .map(StudentGroup::id)
        .findFirst()
        .orElseGet(
            () ->
                courseRepository
                    .addGroup(
                        new StudentGroup(0L, courseId, groupCode, groupType, category, capacity))
                    .id());
  }

  public long provisionStudent(
      String username,
      String passwordHash,
      String fullName,
      String email,
      String jmbag,
      String studyProgram,
      int yearOfStudy,
      LocalDateTime createdAt) {
    long userId =
        userRepository
            .findByUsername(username)
            .map(AppUser::id)
            .orElseGet(
                () ->
                    userRepository
                        .save(
                            new AppUser(
                                0L,
                                username,
                                passwordHash,
                                fullName,
                                email,
                                true,
                                createdAt,
                                EnumSet.of(Role.STUDENT)))
                        .id());
    return studentRepository
        .findByJmbag(jmbag)
        .map(Student::id)
        .orElseGet(
            () ->
                studentRepository
                    .save(new Student(0L, userId, jmbag, studyProgram, yearOfStudy))
                    .id());
  }

  public long provisionStaffUser(
      String username,
      String passwordHash,
      String fullName,
      String email,
      Set<String> roleNames,
      LocalDateTime createdAt) {
    return userRepository
        .findByUsername(username)
        .map(AppUser::id)
        .orElseGet(
            () -> {
              Set<Role> roles =
                  roleNames.stream()
                      .map(Role::valueOf)
                      .collect(Collectors.toCollection(() -> EnumSet.noneOf(Role.class)));
              return userRepository
                  .save(
                      new AppUser(
                          0L, username, passwordHash, fullName, email, true, createdAt, roles))
                  .id();
            });
  }

  public long enroll(long studentId, long courseId, LocalDateTime enrolledAt) {
    return enrollmentRepository
        .findByStudentAndCourse(studentId, courseId)
        .map(Enrollment::id)
        .orElseGet(
            () ->
                enrollmentRepository
                    .save(
                        new Enrollment(
                            0L, studentId, courseId, enrolledAt, EnrollmentStatus.ACTIVE))
                    .id());
  }

  public void assignGroup(long enrollmentId, long groupId) {
    boolean exists =
        enrollmentRepository.findMembershipsByEnrollment(enrollmentId).stream()
            .anyMatch(membership -> membership.groupId() == groupId);
    if (!exists) {
      enrollmentRepository.assignGroup(new GroupMembership(0L, enrollmentId, groupId));
    }
  }

  /**
   * Assigns the student with {@code jmbag} (enrolled in {@code courseId}) to a group of that
   * course. Returns {@code false} when the student, their enrollment, or the group (within the
   * course) does not exist; idempotent otherwise.
   */
  public boolean assignStudentToGroup(long courseId, String jmbag, long groupId) {
    boolean groupBelongsToCourse =
        courseRepository.findGroupsByCourse(courseId).stream()
            .anyMatch(group -> group.id() == groupId);
    if (!groupBelongsToCourse) {
      return false;
    }
    return studentRepository
        .findByJmbag(jmbag)
        .flatMap(student -> enrollmentRepository.findByStudentAndCourse(student.id(), courseId))
        .map(
            enrollment -> {
              assignGroup(enrollment.id(), groupId);
              return true;
            })
        .orElse(false);
  }
}
