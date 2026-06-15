import type {
  AdminUser,
  AdminStudentProfile,
  PasswordResetResult,
  AppSettings,
  AuditEvent,
  AlgorithmRun,
  AutoGradeResult,
  AppliedTimetable,
  CalendarView,
  CollisionReport,
  Consultation,
  GeneratedExamTimetable,
  GeneratedTimetable,
  TimetableComparison,
  CourseComponent,
  CourseDetail,
  CourseEnrollment,
  CourseLiterature,
  ScheduleSlot,
  CourseSummary,
  CurrentUser,
  Demonstrator,
  MyDemonstratorDuty,
  Exam,
  ExamAssistant,
  ForumPost,
  MyCourseGrade,
  MyDuty,
  MyExam,
  MyProfile,
  PortfolioEntry,
  GroupExchange,
  GradeComponentView,
  GradeThresholds,
  Notice,
  GradeView,
  PointsOverviewRow,
  Room,
  RepoFile,
  ResolutionReport,
  ResolutionCandidate,
  CourseConflictMatrix,
  StudentStudySummary,
  TeachingLoad,
  RoomSeating,
  SeatingResult,
  Semester,
  Student,
  TimetableSlot,
  SurveyResult,
  SurveyView,
  SyncStatus,
} from './types';

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
  ) {
    super(message);
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...(init?.headers ?? {}) },
    ...init,
  });
  if (!response.ok) {
    let message = `Pogreška (${response.status})`;
    try {
      const body = await response.json();
      if (body?.message) message = body.message;
    } catch {
      /* ignore non-JSON error bodies */
    }
    throw new ApiError(response.status, message);
  }
  if (response.status === 204) return undefined as T;
  return (await response.json()) as T;
}

export const api = {
  // Build/version info (public Actuator endpoint)
  appInfo: () => request<{ build?: { version?: string } }>(`/actuator/info`),
  // Auth
  login: (username: string, password: string) =>
    request<CurrentUser>('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    }),
  me: () => request<CurrentUser>('/api/v1/auth/me'),
  logout: () => request<void>('/api/v1/auth/logout', { method: 'POST' }),

  // Academic
  activeSemester: () => request<Semester>('/api/v1/academic/semesters/active'),
  courses: (semester?: string) =>
    request<CourseSummary[]>(
      semester
        ? `/api/v1/academic/courses?semester=${encodeURIComponent(semester)}`
        : '/api/v1/academic/courses',
    ),
  course: (id: number) => request<CourseDetail>(`/api/v1/academic/courses/${id}`),
  students: () => request<Student[]>('/api/v1/academic/students'),
  rooms: () => request<Room[]>('/api/v1/academic/rooms'),

  // Exams
  exams: (courseId: number) =>
    request<Exam[]>(`/api/v1/academic/courses/${courseId}/exams`),
  createExam: (courseId: number, body: unknown) =>
    request<{ id: number }>(`/api/v1/academic/courses/${courseId}/exams`, {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  reserveRoom: (examId: number, body: unknown) =>
    request<void>(`/api/v1/academic/exams/${examId}/rooms`, {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  registerFromCourse: (examId: number, courseId: number) =>
    request<{ registered: number }>(
      `/api/v1/academic/exams/${examId}/registrations/from-course/${courseId}`,
      { method: 'POST' },
    ),
  generateSeating: (examId: number, strategy: string) =>
    request<SeatingResult>(
      `/api/v1/academic/exams/${examId}/seating?strategy=${strategy}`,
      { method: 'POST' },
    ),
  seatingWithAlgorithm: (examId: number, algorithm: string) =>
    request<SeatingResult>(
      `/api/v1/academic/exams/${examId}/seating/algorithm?algorithm=${algorithm}`,
      { method: 'POST' },
    ),
  compareAlgorithms: (examId: number) =>
    request<AlgorithmRun[]>(`/api/v1/academic/exams/${examId}/seating/compare`, {
      method: 'POST',
    }),
  seating: (examId: number) =>
    request<RoomSeating[]>(`/api/v1/academic/exams/${examId}/seating`),
  publishExam: (examId: number) =>
    request<void>(`/api/v1/academic/exams/${examId}/publish`, { method: 'POST' }),
  myProfile: () => request<MyProfile>(`/api/v1/academic/my/profile`),
  myStudySummary: () =>
    request<StudentStudySummary>(`/api/v1/academic/my/study-summary`),
  myTeachingLoad: () => request<TeachingLoad>(`/api/v1/academic/my/teaching-load`),
  myPortfolio: () => request<PortfolioEntry[]>(`/api/v1/academic/my/portfolio`),
  addPortfolioEntry: (body: {
    title: string;
    description: string;
    category: string;
    link: string;
  }) =>
    request<{ id: number }>(`/api/v1/academic/my/portfolio`, {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  removePortfolioEntry: (entryId: number) =>
    request<void>(`/api/v1/academic/my/portfolio/${entryId}`, { method: 'DELETE' }),
  myDuties: () => request<MyDuty[]>(`/api/v1/academic/my/duties`),
  myExams: () => request<MyExam[]>(`/api/v1/academic/my/exams`),
  registerForExam: (examId: number) =>
    request<void>(`/api/v1/academic/my/exams/${examId}/registration`, { method: 'POST' }),
  unregisterFromExam: (examId: number) =>
    request<void>(`/api/v1/academic/my/exams/${examId}/registration`, { method: 'DELETE' }),
  myGrades: () => request<MyCourseGrade[]>(`/api/v1/academic/my/grades`),
  examAssistants: (examId: number) =>
    request<ExamAssistant[]>(`/api/v1/academic/exams/${examId}/assistants`),
  assignAssistant: (examId: number, roomId: number, username: string) =>
    request<void>(`/api/v1/academic/exams/${examId}/rooms/${roomId}/assistants`, {
      method: 'POST',
      body: JSON.stringify({ username }),
    }),
  removeAssistant: (examId: number, assignmentId: number) =>
    request<void>(`/api/v1/academic/exams/${examId}/assistants/${assignmentId}`, {
      method: 'DELETE',
    }),

  // Surveys (ankete)
  courseSurveys: (courseId: number) =>
    request<SurveyView[]>(`/api/v1/academic/courses/${courseId}/surveys`),
  createSurvey: (courseId: number, body: { title: string; questions: string[] }) =>
    request<{ id: number }>(`/api/v1/academic/courses/${courseId}/surveys`, {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  submitSurvey: (surveyId: number, answers: { questionId: number; rating: number }[]) =>
    request<void>(`/api/v1/academic/surveys/${surveyId}/responses`, {
      method: 'POST',
      body: JSON.stringify({ answers }),
    }),
  surveyResults: (surveyId: number) =>
    request<SurveyResult[]>(`/api/v1/academic/surveys/${surveyId}/results`),

  // Course components (KOMPONENTE)
  courseComponents: (courseId: number) =>
    request<CourseComponent[]>(`/api/v1/academic/courses/${courseId}/components`),
  addCourseComponent: (
    courseId: number,
    body: { title: string; content: string; ordinal: number; visible: boolean },
  ) =>
    request<{ id: number }>(`/api/v1/academic/courses/${courseId}/components`, {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  courseEnrollments: (courseId: number) =>
    request<CourseEnrollment[]>(`/api/v1/academic/courses/${courseId}/enrollments`),
  assignGroup: (courseId: number, body: { jmbag: string; groupId: number }) =>
    request<void>(`/api/v1/academic/courses/${courseId}/group-assignments`, {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  courseSchedule: (courseId: number) =>
    request<ScheduleSlot[]>(`/api/v1/academic/courses/${courseId}/schedule`),
  courseConsultations: (courseId: number) =>
    request<Consultation[]>(`/api/v1/academic/courses/${courseId}/consultations`),
  addConsultation: (
    courseId: number,
    body: { dayOfWeek: string; startsAt: string; endsAt: string; location: string },
  ) =>
    request<{ id: number }>(`/api/v1/academic/courses/${courseId}/consultations`, {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  removeConsultation: (courseId: number, consultationId: number) =>
    request<void>(`/api/v1/academic/courses/${courseId}/consultations/${consultationId}`, {
      method: 'DELETE',
    }),
  courseLiterature: (courseId: number) =>
    request<CourseLiterature[]>(`/api/v1/academic/courses/${courseId}/literature`),
  addCourseLiterature: (
    courseId: number,
    body: { title: string; author: string; mandatory: boolean; ordinal: number },
  ) =>
    request<{ id: number }>(`/api/v1/academic/courses/${courseId}/literature`, {
      method: 'POST',
      body: JSON.stringify(body),
    }),

  // Repository (datoteke)
  courseFiles: (courseId: number) =>
    request<RepoFile[]>(`/api/v1/academic/courses/${courseId}/files`),
  uploadCourseFile: async (courseId: number, file: File) => {
    const form = new FormData();
    form.append('file', file);
    const response = await fetch(`/api/v1/academic/courses/${courseId}/files`, {
      method: 'POST',
      credentials: 'include',
      body: form,
    });
    if (!response.ok) {
      throw new ApiError(response.status, `Prijenos nije uspio (${response.status})`);
    }
    return (await response.json()) as { id: number };
  },
  fileDownloadUrl: (fileId: number) => `/api/v1/academic/files/${fileId}/download`,

  // Group exchange (burza grupa)
  courseGroupExchange: (courseId: number) =>
    request<GroupExchange[]>(`/api/v1/academic/courses/${courseId}/group-exchange`),
  requestGroupExchange: (
    courseId: number,
    body: { fromGroupId: number | null; toGroupId: number | null; reason: string },
  ) =>
    request<{ id: number }>(`/api/v1/academic/courses/${courseId}/group-exchange`, {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  decideGroupExchange: (requestId: number, approve: boolean) =>
    request<void>(`/api/v1/academic/group-exchange/${requestId}/decision`, {
      method: 'POST',
      body: JSON.stringify({ approve }),
    }),

  // Forum (Pitanja i problemi)
  courseForum: (courseId: number) =>
    request<ForumPost[]>(`/api/v1/academic/courses/${courseId}/forum`),
  postForum: (courseId: number, body: { parentId: number | null; body: string }) =>
    request<{ id: number }>(`/api/v1/academic/courses/${courseId}/forum`, {
      method: 'POST',
      body: JSON.stringify(body),
    }),

  // Demonstrators (demonstrature)
  courseDemonstrators: (courseId: number) =>
    request<Demonstrator[]>(`/api/v1/academic/courses/${courseId}/demonstrators`),
  assignDemonstrator: (courseId: number, jmbag: string) =>
    request<void>(`/api/v1/academic/courses/${courseId}/demonstrators`, {
      method: 'POST',
      body: JSON.stringify({ jmbag }),
    }),
  removeDemonstrator: (courseId: number, studentId: number) =>
    request<void>(`/api/v1/academic/courses/${courseId}/demonstrators/${studentId}`, {
      method: 'DELETE',
    }),
  myDemonstratures: () =>
    request<MyDemonstratorDuty[]>('/api/v1/academic/my/demonstratures'),

  generateExamTimetable: (body: {
    studyYear?: number;
    courseIds?: number[];
    slots: number;
    algorithm: string;
    referenceTerm?: string;
  }) =>
    request<GeneratedExamTimetable>('/api/v1/academic/exam-timetable/generate', {
      method: 'POST',
      body: JSON.stringify(body),
    }),

  // Calendar (kalendar)
  calendar: () => request<CalendarView>('/api/v1/academic/calendar'),

  // Timetable (raspored nastave)
  timetable: () => request<TimetableSlot[]>('/api/v1/academic/timetable'),
  timetableCollisions: () => request<CollisionReport>('/api/v1/academic/timetable/collisions'),
  timetableResolution: () => request<ResolutionReport>('/api/v1/academic/timetable/resolution'),
  courseConflictMatrix: (semester?: string) =>
    request<CourseConflictMatrix>(
      semester
        ? `/api/v1/academic/timetable/conflict-matrix?semester=${encodeURIComponent(semester)}`
        : '/api/v1/academic/timetable/conflict-matrix',
    ),
  resolveMove: (body: { slotId: number; dayOfWeek: string; startsAt: string; roomId: number | null }) =>
    request<ResolutionReport>('/api/v1/academic/timetable/resolution/move', {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  resolutionCandidates: (slotId: number, limit = 8) =>
    request<ResolutionCandidate[]>(
      `/api/v1/academic/timetable/resolution/candidates?slotId=${slotId}&limit=${limit}`,
    ),
  resolveAuto: () =>
    request<ResolutionReport>('/api/v1/academic/timetable/resolution/auto', { method: 'POST' }),
  generateFacultyTimetable: () =>
    request<ResolutionReport>('/api/v1/academic/timetable/resolution/generate', { method: 'POST' }),
  compareTimetable: (body: { studyYear?: number; courseIds?: number[]; periods: number }) =>
    request<TimetableComparison>('/api/v1/academic/timetable/compare', {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  applyTimetable: (body: {
    studyYear?: number;
    courseIds?: number[];
    periods: number;
    algorithm: string;
  }) =>
    request<AppliedTimetable>('/api/v1/academic/timetable/apply', {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  generateTimetable: (body: {
    studyYear?: number;
    courseIds?: number[];
    periods: number;
    algorithm: string;
  }) =>
    request<GeneratedTimetable>('/api/v1/academic/timetable/generate', {
      method: 'POST',
      body: JSON.stringify(body),
    }),

  // Admin
  semesters: () => request<Semester[]>('/api/v1/academic/semesters'),
  adminUsers: () => request<AdminUser[]>('/api/v1/academic/users'),
  adminUserProfile: (userId: number) =>
    request<AdminStudentProfile>(`/api/v1/academic/users/${userId}/profile`),
  adminResetPassword: (userId: number) =>
    request<PasswordResetResult>(`/api/v1/academic/users/${userId}/reset-password`, {
      method: 'POST',
    }),
  syncStatus: () => request<SyncStatus>('/api/v1/academic/sync/status'),
  settings: () => request<AppSettings>('/api/v1/academic/settings'),
  auditEvents: (limit = 100) => request<AuditEvent[]>(`/api/v1/academic/audit?limit=${limit}`),
  assignCourseStaff: (courseId: number, body: { username: string; role: string }) =>
    request<void>(`/api/v1/academic/courses/${courseId}/staff`, {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  createSemester: (body: {
    code: string;
    academicYear: string;
    term: string;
    startsOn: string;
    endsOn: string;
    active: boolean;
  }) =>
    request<void>('/api/v1/academic/semesters', {
      method: 'POST',
      body: JSON.stringify(body),
    }),

  // Notices (obavijesti)
  notices: (limit = 20) =>
    request<Notice[]>(`/api/v1/academic/notices?limit=${limit}`),
  courseNotices: (courseId: number) =>
    request<Notice[]>(`/api/v1/academic/courses/${courseId}/notices`),
  publishNotice: (body: { courseId: number | null; title: string; body: string; pinned: boolean }) =>
    request<{ id: number }>(`/api/v1/academic/notices`, {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  deleteNotice: (id: number) =>
    request<void>(`/api/v1/academic/notices/${id}`, { method: 'DELETE' }),

  // Grading (points overview)
  gradeComponents: (courseId: number) =>
    request<GradeComponentView[]>(`/api/v1/academic/courses/${courseId}/grade-components`),
  addGradeComponent: (courseId: number, body: { name: string; shortName: string; maxPoints: number }) =>
    request<GradeComponentView>(`/api/v1/academic/courses/${courseId}/grade-components`, {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  pointsOverview: (courseId: number) =>
    request<PointsOverviewRow[]>(`/api/v1/academic/courses/${courseId}/points-overview`),
  enterPoints: (courseId: number, body: { studentId: number; componentId: number; points: number }) =>
    request<void>(`/api/v1/academic/courses/${courseId}/points`, {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  grades: (courseId: number) =>
    request<GradeView[]>(`/api/v1/academic/courses/${courseId}/grades`),
  assignGrade: (courseId: number, body: { studentId: number; finalGrade: number }) =>
    request<void>(`/api/v1/academic/courses/${courseId}/grades`, {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  gradeThresholds: (courseId: number) =>
    request<GradeThresholds>(`/api/v1/academic/courses/${courseId}/grade-thresholds`),
  setGradeThresholds: (
    courseId: number,
    body: { excellent: number; veryGood: number; good: number; sufficient: number },
  ) =>
    request<void>(`/api/v1/academic/courses/${courseId}/grade-thresholds`, {
      method: 'PUT',
      body: JSON.stringify(body),
    }),
  computeFinalGrades: (courseId: number) =>
    request<{ graded: number }>(`/api/v1/academic/courses/${courseId}/grades/compute`, {
      method: 'POST',
    }),

  // Auto-grading answer sheets
  autoGrade: (
    examId: number,
    body: { correctAnswers: string[]; submissions: { jmbag: string; answers: string[] }[] },
  ) =>
    request<AutoGradeResult[]>(`/api/v1/academic/exams/${examId}/auto-grade`, {
      method: 'POST',
      body: JSON.stringify(body),
    }),
};
