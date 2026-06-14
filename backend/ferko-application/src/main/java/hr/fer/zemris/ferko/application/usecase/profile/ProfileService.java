package hr.fer.zemris.ferko.application.usecase.profile;

import hr.fer.zemris.ferko.application.port.AppUserRepository;
import hr.fer.zemris.ferko.application.port.StudentRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.Role;
import hr.fer.zemris.ferko.domain.model.Student;
import java.util.List;
import java.util.Optional;

/** Assembles the signed-in user's personal data, enriched with student details when applicable. */
public class ProfileService {

  private final AppUserRepository userRepository;
  private final StudentRepository studentRepository;

  public ProfileService(AppUserRepository userRepository, StudentRepository studentRepository) {
    this.userRepository = userRepository;
    this.studentRepository = studentRepository;
  }

  /** Returns the profile, or empty when the username is unknown. */
  public Optional<MyProfileView> forUser(String username) {
    return userRepository.findByUsername(username).map(this::toView);
  }

  private MyProfileView toView(AppUser user) {
    List<String> roles = user.roles().stream().map(Role::name).sorted().toList();
    Optional<Student> student = studentRepository.findByUserId(user.id());
    return new MyProfileView(
        user.username(),
        user.fullName(),
        user.email(),
        roles,
        student.map(Student::jmbag).orElse(null),
        student.map(Student::studyProgram).orElse(null),
        student.map(Student::yearOfStudy).orElse(0));
  }
}
