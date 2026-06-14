package hr.fer.zemris.ferko.webapi.config;

import hr.fer.zemris.ferko.application.port.AppUserRepository;
import hr.fer.zemris.ferko.application.port.AuditEventRepository;
import hr.fer.zemris.ferko.application.port.ClassScheduleRepository;
import hr.fer.zemris.ferko.application.port.ConsultationRepository;
import hr.fer.zemris.ferko.application.port.CourseComponentRepository;
import hr.fer.zemris.ferko.application.port.CourseLiteratureRepository;
import hr.fer.zemris.ferko.application.port.CourseRepository;
import hr.fer.zemris.ferko.application.port.EnrollmentRepository;
import hr.fer.zemris.ferko.application.port.ExamAssistantRepository;
import hr.fer.zemris.ferko.application.port.ExamRepository;
import hr.fer.zemris.ferko.application.port.FileStorage;
import hr.fer.zemris.ferko.application.port.ForumRepository;
import hr.fer.zemris.ferko.application.port.GradingRepository;
import hr.fer.zemris.ferko.application.port.GroupExchangeRepository;
import hr.fer.zemris.ferko.application.port.MailSender;
import hr.fer.zemris.ferko.application.port.NoticeRepository;
import hr.fer.zemris.ferko.application.port.PortfolioRepository;
import hr.fer.zemris.ferko.application.port.RepositoryFileRepository;
import hr.fer.zemris.ferko.application.port.RoomRepository;
import hr.fer.zemris.ferko.application.port.SemesterRepository;
import hr.fer.zemris.ferko.application.port.StudentRepository;
import hr.fer.zemris.ferko.application.port.SurveyRepository;
import hr.fer.zemris.ferko.application.port.ToDoAuditLogPort;
import hr.fer.zemris.ferko.application.port.ToDoTaskRepository;
import hr.fer.zemris.ferko.application.usecase.PingUseCase;
import hr.fer.zemris.ferko.application.usecase.academic.AcademicProvisioningService;
import hr.fer.zemris.ferko.application.usecase.academic.AcademicQueryService;
import hr.fer.zemris.ferko.application.usecase.audit.AuditService;
import hr.fer.zemris.ferko.application.usecase.auth.LoadAuthUserUseCase;
import hr.fer.zemris.ferko.application.usecase.auth.ProvisionUserUseCase;
import hr.fer.zemris.ferko.application.usecase.calendar.CalendarService;
import hr.fer.zemris.ferko.application.usecase.component.CourseComponentService;
import hr.fer.zemris.ferko.application.usecase.consultation.ConsultationService;
import hr.fer.zemris.ferko.application.usecase.exam.ExamAssistantService;
import hr.fer.zemris.ferko.application.usecase.exam.ExamSchedulingService;
import hr.fer.zemris.ferko.application.usecase.exam.InvigilationService;
import hr.fer.zemris.ferko.application.usecase.exchange.GroupExchangeService;
import hr.fer.zemris.ferko.application.usecase.forum.ForumService;
import hr.fer.zemris.ferko.application.usecase.grading.GradingService;
import hr.fer.zemris.ferko.application.usecase.literature.CourseLiteratureService;
import hr.fer.zemris.ferko.application.usecase.notice.NoticeService;
import hr.fer.zemris.ferko.application.usecase.portfolio.PortfolioService;
import hr.fer.zemris.ferko.application.usecase.profile.ProfileService;
import hr.fer.zemris.ferko.application.usecase.repository.RepositoryService;
import hr.fer.zemris.ferko.application.usecase.schedule.CourseScheduleService;
import hr.fer.zemris.ferko.application.usecase.student.StudentExamService;
import hr.fer.zemris.ferko.application.usecase.student.StudentGradesService;
import hr.fer.zemris.ferko.application.usecase.survey.SurveyService;
import hr.fer.zemris.ferko.application.usecase.todo.CloseToDoTaskUseCase;
import hr.fer.zemris.ferko.application.usecase.todo.CreateToDoTaskUseCase;
import hr.fer.zemris.ferko.application.usecase.todo.ListAssignedOpenToDoTasksUseCase;
import hr.fer.zemris.ferko.application.usecase.todo.ListMyOpenToDoTasksUseCase;
import hr.fer.zemris.ferko.infrastructure.adapter.InMemoryAuditAdapter;
import hr.fer.zemris.ferko.infrastructure.adapter.InMemoryToDoTaskRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcAppUserRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcAuditEventRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcClassScheduleRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcConsultationRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcCourseComponentRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcCourseLiteratureRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcCourseRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcEnrollmentRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcExamAssistantRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcExamRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcForumRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcGradingRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcGroupExchangeRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcNoticeRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcPortfolioRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcRepositoryFileRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcRoomRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcSemesterRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcStudentRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcSurveyRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcToDoAuditLogRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcToDoTaskRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.LocalFileStorage;
import hr.fer.zemris.ferko.infrastructure.adapter.LoggingMailSender;
import hr.fer.zemris.ferko.infrastructure.adapter.SmtpMailSender;
import org.springframework.beans.factory.annotation.Value;
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
  @ConditionalOnProperty(name = "ferko.mail.enabled", havingValue = "false", matchIfMissing = true)
  public MailSender loggingMailSender() {
    return new LoggingMailSender();
  }

  @Bean
  @ConditionalOnProperty(name = "ferko.mail.enabled", havingValue = "true")
  public MailSender smtpMailSender(
      org.springframework.mail.MailSender springMailSender,
      @Value("${ferko.mail.from:ferko@fer.hr}") String from) {
    return new SmtpMailSender(springMailSender, from);
  }

  @Bean
  public AuditEventRepository auditEventRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcAuditEventRepository(jdbcTemplate);
  }

  @Bean
  public AuditService auditService(AuditEventRepository auditEventRepository) {
    return new AuditService(auditEventRepository);
  }

  @Bean
  public ExamSchedulingService examSchedulingService(
      ExamRepository examRepository,
      RoomRepository roomRepository,
      StudentRepository studentRepository,
      EnrollmentRepository enrollmentRepository,
      AppUserRepository appUserRepository,
      MailSender mailSender) {
    return new ExamSchedulingService(
        examRepository,
        roomRepository,
        studentRepository,
        enrollmentRepository,
        appUserRepository,
        mailSender);
  }

  @Bean
  public ExamAssistantRepository examAssistantRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcExamAssistantRepository(jdbcTemplate);
  }

  @Bean
  public ExamAssistantService examAssistantService(
      ExamAssistantRepository examAssistantRepository,
      ExamRepository examRepository,
      RoomRepository roomRepository,
      AppUserRepository appUserRepository) {
    return new ExamAssistantService(
        examAssistantRepository, examRepository, roomRepository, appUserRepository);
  }

  @Bean
  public InvigilationService invigilationService(
      ExamAssistantRepository examAssistantRepository,
      ExamRepository examRepository,
      CourseRepository courseRepository,
      RoomRepository roomRepository,
      AppUserRepository appUserRepository) {
    return new InvigilationService(
        examAssistantRepository,
        examRepository,
        courseRepository,
        roomRepository,
        appUserRepository);
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
  public ForumRepository forumRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcForumRepository(jdbcTemplate);
  }

  @Bean
  public ForumService forumService(ForumRepository forumRepository) {
    return new ForumService(forumRepository);
  }

  @Bean
  public CourseComponentRepository courseComponentRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcCourseComponentRepository(jdbcTemplate);
  }

  @Bean
  public CourseComponentService courseComponentService(
      CourseComponentRepository courseComponentRepository) {
    return new CourseComponentService(courseComponentRepository);
  }

  @Bean
  public CourseLiteratureRepository courseLiteratureRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcCourseLiteratureRepository(jdbcTemplate);
  }

  @Bean
  public CourseLiteratureService courseLiteratureService(
      CourseLiteratureRepository courseLiteratureRepository) {
    return new CourseLiteratureService(courseLiteratureRepository);
  }

  @Bean
  public ConsultationRepository consultationRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcConsultationRepository(jdbcTemplate);
  }

  @Bean
  public ConsultationService consultationService(ConsultationRepository consultationRepository) {
    return new ConsultationService(consultationRepository);
  }

  @Bean
  public RepositoryFileRepository repositoryFileRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcRepositoryFileRepository(jdbcTemplate);
  }

  @Bean
  public FileStorage fileStorage(
      @Value("${ferko.storage.dir:${java.io.tmpdir}/ferko-files}") String storageDir) {
    return new LocalFileStorage(storageDir);
  }

  @Bean
  public RepositoryService repositoryService(
      RepositoryFileRepository repositoryFileRepository, FileStorage fileStorage) {
    return new RepositoryService(repositoryFileRepository, fileStorage);
  }

  @Bean
  public GroupExchangeRepository groupExchangeRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcGroupExchangeRepository(jdbcTemplate);
  }

  @Bean
  public GroupExchangeService groupExchangeService(
      GroupExchangeRepository groupExchangeRepository,
      AppUserRepository appUserRepository,
      StudentRepository studentRepository,
      CourseRepository courseRepository) {
    return new GroupExchangeService(
        groupExchangeRepository, appUserRepository, studentRepository, courseRepository);
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

  @Bean
  public ProfileService profileService(
      AppUserRepository appUserRepository, StudentRepository studentRepository) {
    return new ProfileService(appUserRepository, studentRepository);
  }

  @Bean
  public PortfolioRepository portfolioRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcPortfolioRepository(jdbcTemplate);
  }

  @Bean
  public PortfolioService portfolioService(
      PortfolioRepository portfolioRepository, AppUserRepository appUserRepository) {
    return new PortfolioService(portfolioRepository, appUserRepository);
  }

  @Bean
  public CourseScheduleService courseScheduleService(
      ClassScheduleRepository classScheduleRepository,
      RoomRepository roomRepository,
      CourseRepository courseRepository) {
    return new CourseScheduleService(classScheduleRepository, roomRepository, courseRepository);
  }

  @Bean
  public StudentExamService studentExamService(
      AppUserRepository appUserRepository,
      StudentRepository studentRepository,
      EnrollmentRepository enrollmentRepository,
      CourseRepository courseRepository,
      ExamRepository examRepository,
      RoomRepository roomRepository) {
    return new StudentExamService(
        appUserRepository,
        studentRepository,
        enrollmentRepository,
        courseRepository,
        examRepository,
        roomRepository);
  }

  @Bean
  public StudentGradesService studentGradesService(
      AppUserRepository appUserRepository,
      StudentRepository studentRepository,
      EnrollmentRepository enrollmentRepository,
      CourseRepository courseRepository,
      GradingRepository gradingRepository) {
    return new StudentGradesService(
        appUserRepository,
        studentRepository,
        enrollmentRepository,
        courseRepository,
        gradingRepository);
  }
}
