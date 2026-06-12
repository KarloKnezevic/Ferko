package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.CourseStaff;
import hr.fer.zemris.ferko.domain.model.StudentGroup;
import java.util.List;
import java.util.Optional;

/** Persistence port for courses together with their staff and groups. */
public interface CourseRepository {

  Course save(Course course);

  Optional<Course> findById(long id);

  Optional<Course> findByCodeAndSemester(String code, String semesterCode);

  List<Course> findBySemester(String semesterCode);

  List<Course> findAll();

  CourseStaff addStaff(CourseStaff staff);

  List<CourseStaff> findStaffByCourse(long courseId);

  StudentGroup addGroup(StudentGroup group);

  List<StudentGroup> findGroupsByCourse(long courseId);
}
