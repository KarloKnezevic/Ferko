package hr.fer.zemris.ferko.webapi.config;

import hr.fer.zemris.ferko.application.port.AppUserRepository;
import hr.fer.zemris.ferko.application.port.ClassScheduleRepository;
import hr.fer.zemris.ferko.application.port.CourseRepository;
import hr.fer.zemris.ferko.application.port.EnrollmentRepository;
import hr.fer.zemris.ferko.application.port.ExamRepository;
import hr.fer.zemris.ferko.application.port.GradingRepository;
import hr.fer.zemris.ferko.application.port.NoticeRepository;
import hr.fer.zemris.ferko.application.port.RoomRepository;
import hr.fer.zemris.ferko.application.port.SemesterRepository;
import hr.fer.zemris.ferko.application.port.StudentRepository;
import hr.fer.zemris.ferko.application.port.SurveyRepository;
import hr.fer.zemris.ferko.application.port.ToDoAuditLogPort;
import hr.fer.zemris.ferko.application.port.ToDoTaskRepository;
import hr.fer.zemris.ferko.application.usecase.PingUseCase;
import hr.fer.zemris.ferko.application.usecase.academic.AcademicProvisioningService;
import hr.fer.zemris.ferko.application.usecase.academic.AcademicQueryService;
import hr.fer.zemris.ferko.application.usecase.auth.LoadAuthUserUseCase;
import hr.fer.zemris.ferko.application.usecase.auth.ProvisionUserUseCase;
import hr.fer.zemris.ferko.application.usecase.calendar.CalendarService;
import hr.fer.zemris.ferko.application.usecase.exam.ExamSchedulingService;
import hr.fer.zemris.ferko.application.usecase.grading.GradingService;
import hr.fer.zemris.ferko.application.usecase.notice.NoticeService;
import hr.fer.zemris.ferko.application.usecase.survey.SurveyService;
import hr.fer.zemris.ferko.application.usecase.todo.CloseToDoTaskUseCase;
import hr.fer.zemris.ferko.application.usecase.todo.CreateToDoTaskUseCase;
import hr.fer.zemris.ferko.application.usecase.todo.ListAssignedOpenToDoTasksUseCase;
import hr.fer.zemris.ferko.application.usecase.todo.ListMyOpenToDoTasksUseCase;
import hr.fer.zemris.ferko.infrastructure.adapter.InMemoryAuditAdapter;
import hr.fer.zemris.ferko.infrastructure.adapter.InMemoryToDoTaskRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcAppUserRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcClassScheduleRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcCourseRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcEnrollmentRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcExamRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcGradingRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcNoticeRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcRoomRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcSemesterRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcStudentRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcSurveyRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcToDoAuditLogRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcToDoTaskRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ApplicationBeans {

  @Bean
  public PingUseCase pingUseCase() {
    return new PingUseCase();
  }

  @Bean
  @ConditionalOnProperty(name = "ferko.todo.repository", havingValue = "in-memory")
  public ToDoTaskRepository inMemoryToDoTaskRepository() {
    return new InMemoryToDoTaskRepository();
  }

  @Bean
  @ConditionalOnProperty(
      name = "ferko.todo.repository",
      havingValue = "jdbc",
      matchIfMissing = true)
  public ToDoTaskRepository jdbcToDoTaskRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcToDoTaskRepository(jdbcTemplate);
  }

  @Bean
  @ConditionalOnProperty(name = "ferko.audit.repository", havingValue = "in-memory")
  public ToDoAuditLogPort inMemoryToDoAuditLogPort() {
    return new InMemoryAuditAdapter();
  }

  @Bean
  @ConditionalOnProperty(
      name = "ferko.audit.repository",
      havingValue = "jdbc",
      matchIfMissing = true)
  public ToDoAuditLogPort jdbcToDoAuditLogPort(JdbcTemplate jdbcTemplate) {
    return new JdbcToDoAuditLogRepository(jdbcTemplate);
  }

  @Bean
  public CreateToDoTaskUseCase createToDoTaskUseCase(
      ToDoTaskRepository repository, ToDoAuditLogPort auditLogPort) {
    return new CreateToDoTaskUseCase(repository, auditLogPort);
  }

  @Bean
  public ListMyOpenToDoTasksUseCase listMyOpenToDoTasksUseCase(ToDoTaskRepository repository) {
    return new ListMyOpenToDoTasksUseCase(repository);
  }

  @Bean
  public ListAssignedOpenToDoTasksUseCase listAssignedOpenToDoTasksUseCase(
      ToDoTaskRepository repository) {
    return new ListAssignedOpenToDoTasksUseCase(repository);
  }

  @Bean
  public CloseToDoTaskUseCase closeToDoTaskUseCase(
      ToDoTaskRepository repository, ToDoAuditLogPort auditLogPort) {
    return new CloseToDoTaskUseCase(repository, auditLogPort);
  }

  @Bean
  public AppUserRepository appUserRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcAppUserRepository(jdbcTemplate);
  }

  @Bean
  public LoadAuthUserUseCase loadAuthUserUseCase(AppUserRepository appUserRepository) {
    return new LoadAuthUserUseCase(appUserRepository);
  }

  @Bean
  public ProvisionUserUseCase provisionUserUseCase(AppUserRepository appUserRepository) {
    return new ProvisionUserUseCase(appUserRepository);
  }

  @Bean
  public SemesterRepository semesterRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcSemesterRepository(jdbcTemplate);
  }

  @Bean
  public CourseRepository courseRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcCourseRepository(jdbcTemplate);
  }

  @Bean
  public EnrollmentRepository enrollmentRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcEnrollmentRepository(jdbcTemplate);
  }

  @Bean
  public StudentRepository studentRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcStudentRepository(jdbcTemplate);
  }

  @Bean
  public RoomRepository roomRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcRoomRepository(jdbcTemplate);
  }

  @Bean
  public AcademicQueryService academicQueryService(
      SemesterRepository semesterRepository,
      CourseRepository courseRepository,
      EnrollmentRepository enrollmentRepository,
      StudentRepository studentRepository,
      RoomRepository roomRepository,
      AppUserRepository appUserRepository) {
    return new AcademicQueryService(
        semesterRepository,
        courseRepository,
        enrollmentRepository,
        studentRepository,
        roomRepository,
        appUserRepository);
  }

  @Bean
  public AcademicProvisioningService academicProvisioningService(
      SemesterRepository semesterRepository,
      CourseRepository courseRepository,
      EnrollmentRepository enrollmentRepository,
      StudentRepository studentRepository,
      RoomRepository roomRepository,
      AppUserRepository appUserRepository,
      ClassScheduleRepository classScheduleRepository) {
    return new AcademicProvisioningService(
        semesterRepository,
        courseRepository,
        enrollmentRepository,
        studentRepository,
        roomRepository,
        appUserRepository,
        classScheduleRepository);
  }

  @Bean
  public ExamRepository examRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcExamRepository(jdbcTemplate);
  }

  @Bean
  public ExamSchedulingService examSchedulingService(
      ExamRepository examRepository,
      RoomRepository roomRepository,
      StudentRepository studentRepository,
      EnrollmentRepository enrollmentRepository,
      AppUserRepository appUserRepository) {
    return new ExamSchedulingService(
        examRepository, roomRepository, studentRepository, enrollmentRepository, appUserRepository);
  }

  @Bean
  public GradingRepository gradingRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcGradingRepository(jdbcTemplate);
  }

  @Bean
  public GradingService gradingService(
      GradingRepository gradingRepository,
      EnrollmentRepository enrollmentRepository,
      StudentRepository studentRepository,
      AppUserRepository appUserRepository) {
    return new GradingService(
        gradingRepository, enrollmentRepository, studentRepository, appUserRepository);
  }

  @Bean
  public NoticeRepository noticeRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcNoticeRepository(jdbcTemplate);
  }

  @Bean
  public NoticeService noticeService(NoticeRepository noticeRepository) {
    return new NoticeService(noticeRepository);
  }

  @Bean
  public ClassScheduleRepository classScheduleRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcClassScheduleRepository(jdbcTemplate);
  }

  @Bean
  public SurveyRepository surveyRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcSurveyRepository(jdbcTemplate);
  }

  @Bean
  public SurveyService surveyService(SurveyRepository surveyRepository) {
    return new SurveyService(surveyRepository);
  }

  @Bean
  public CalendarService calendarService(
      AppUserRepository appUserRepository,
      StudentRepository studentRepository,
      EnrollmentRepository enrollmentRepository,
      CourseRepository courseRepository,
      ExamRepository examRepository,
      ClassScheduleRepository classScheduleRepository,
      RoomRepository roomRepository) {
    return new CalendarService(
        appUserRepository,
        studentRepository,
        enrollmentRepository,
        courseRepository,
        examRepository,
        classScheduleRepository,
        roomRepository);
  }
}
