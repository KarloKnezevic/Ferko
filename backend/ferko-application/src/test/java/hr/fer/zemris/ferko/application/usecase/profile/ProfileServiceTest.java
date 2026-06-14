package hr.fer.zemris.ferko.application.usecase.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.Role;
import hr.fer.zemris.ferko.domain.model.Student;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProfileServiceTest {

  private InMemoryAcademicRepositories.Users users;
  private InMemoryAcademicRepositories.Students students;
  private ProfileService service;

  @BeforeEach
  void setUp() {
    users = new InMemoryAcademicRepositories.Users();
    students = new InMemoryAcademicRepositories.Students();
    service = new ProfileService(users, students);
  }

  @Test
  void enrichesStudentProfileWithAcademicDetails() {
    AppUser user =
        users.save(
            new AppUser(
                0L,
                "student.ana",
                "x",
                "Ana Anić",
                "ana@fer.hr",
                true,
                LocalDateTime.now(),
                Set.of(Role.STUDENT)));
    students.save(new Student(0L, user.id(), "0036000001", "Računarstvo", 2));

    MyProfileView view = service.forUser("student.ana").orElseThrow();
    assertEquals("Ana Anić", view.fullName());
    assertEquals("ana@fer.hr", view.email());
    assertEquals("0036000001", view.jmbag());
    assertEquals("Računarstvo", view.studyProgram());
    assertEquals(2, view.yearOfStudy());
    assertTrue(view.roles().contains("STUDENT"));
  }

  @Test
  void nonStudentHasNoAcademicDetails() {
    users.save(
        new AppUser(
            0L,
            "admin.x",
            "x",
            "Admin",
            "a@fer.hr",
            true,
            LocalDateTime.now(),
            Set.of(Role.ADMIN)));
    MyProfileView view = service.forUser("admin.x").orElseThrow();
    assertNull(view.jmbag());
    assertNull(view.studyProgram());
    assertEquals(0, view.yearOfStudy());
    assertTrue(view.roles().contains("ADMIN"));
  }

  @Test
  void emptyForUnknownUser() {
    assertTrue(service.forUser("ne.postoji").isEmpty());
  }
}
