package hr.fer.zemris.ferko.application.usecase.exchange;

import hr.fer.zemris.ferko.application.port.AppUserRepository;
import hr.fer.zemris.ferko.application.port.CourseRepository;
import hr.fer.zemris.ferko.application.port.GroupExchangeRepository;
import hr.fer.zemris.ferko.application.port.StudentRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.ExchangeStatus;
import hr.fer.zemris.ferko.domain.model.GroupExchangeRequest;
import hr.fer.zemris.ferko.domain.model.Student;
import hr.fer.zemris.ferko.domain.model.StudentGroup;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Group-exchange marketplace ("burza grupa"): students request switches, staff decide. */
public class GroupExchangeService {

  private final GroupExchangeRepository exchangeRepository;
  private final AppUserRepository userRepository;
  private final StudentRepository studentRepository;
  private final CourseRepository courseRepository;

  public GroupExchangeService(
      GroupExchangeRepository exchangeRepository,
      AppUserRepository userRepository,
      StudentRepository studentRepository,
      CourseRepository courseRepository) {
    this.exchangeRepository = exchangeRepository;
    this.userRepository = userRepository;
    this.studentRepository = studentRepository;
    this.courseRepository = courseRepository;
  }

  public long request(
      long courseId, String username, Long fromGroupId, Long toGroupId, String reason) {
    Student student =
        userRepository
            .findByUsername(username)
            .flatMap(user -> studentRepository.findByUserId(user.id()))
            .orElseThrow(() -> new IllegalArgumentException("Samo studenti mogu tražiti zamjenu."));
    GroupExchangeRequest saved =
        exchangeRepository.save(
            new GroupExchangeRequest(
                0L,
                courseId,
                student.id(),
                fromGroupId,
                toGroupId,
                ExchangeStatus.PENDING,
                reason,
                null,
                LocalDateTime.now(),
                null));
    return saved.id();
  }

  public List<GroupExchangeView> listForCourse(long courseId) {
    Map<Long, String> groupCodes =
        courseRepository.findGroupsByCourse(courseId).stream()
            .collect(Collectors.toMap(StudentGroup::id, StudentGroup::groupCode));
    Map<Long, String> userNames =
        userRepository.findAll().stream()
            .collect(Collectors.toMap(AppUser::id, AppUser::fullName, (a, b) -> a));
    return exchangeRepository.findByCourse(courseId).stream()
        .map(request -> toView(request, groupCodes, userNames))
        .toList();
  }

  public void decide(long requestId, boolean approve, String decidedBy) {
    exchangeRepository
        .findById(requestId)
        .orElseThrow(() -> new IllegalArgumentException("Zahtjev ne postoji."));
    exchangeRepository.updateDecision(
        requestId,
        approve ? ExchangeStatus.APPROVED : ExchangeStatus.REJECTED,
        decidedBy,
        LocalDateTime.now());
  }

  private GroupExchangeView toView(
      GroupExchangeRequest request, Map<Long, String> groupCodes, Map<Long, String> userNames) {
    Student student = studentRepository.findById(request.studentId()).orElse(null);
    String jmbag = student == null ? "" : student.jmbag();
    String name = student == null ? "" : userNames.getOrDefault(student.userId(), "");
    return new GroupExchangeView(
        request.id(),
        request.courseId(),
        jmbag,
        name,
        request.fromGroupId() == null ? "—" : groupCodes.getOrDefault(request.fromGroupId(), "?"),
        request.toGroupId() == null ? "—" : groupCodes.getOrDefault(request.toGroupId(), "?"),
        request.status().name(),
        request.reason(),
        request.decidedBy(),
        request.createdAt());
  }
}
