package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.Enrollment;
import hr.fer.zemris.ferko.domain.model.GroupMembership;
import java.util.List;
import java.util.Optional;

/** Persistence port for course enrollments and their group assignments. */
public interface EnrollmentRepository {

  Enrollment save(Enrollment enrollment);

  Optional<Enrollment> findById(long id);

  Optional<Enrollment> findByStudentAndCourse(long studentId, long courseId);

  List<Enrollment> findByCourse(long courseId);

  List<Enrollment> findByStudent(long studentId);

  GroupMembership assignGroup(GroupMembership membership);

  List<GroupMembership> findMembershipsByEnrollment(long enrollmentId);
}
