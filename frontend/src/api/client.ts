import type {
  AdminUser,
  AlgorithmRun,
  AutoGradeResult,
  CalendarView,
  CourseComponent,
  CourseDetail,
  CourseSummary,
  CurrentUser,
  Exam,
  ForumPost,
  GradeComponentView,
  Notice,
  GradeView,
  PointsOverviewRow,
  Room,
  RepoFile,
  RoomSeating,
  SeatingResult,
  Semester,
  Student,
  SurveyResult,
  SurveyView,
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
  courses: () => request<CourseSummary[]>('/api/v1/academic/courses'),
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

  // Forum (Pitanja i problemi)
  courseForum: (courseId: number) =>
    request<ForumPost[]>(`/api/v1/academic/courses/${courseId}/forum`),
  postForum: (courseId: number, body: { parentId: number | null; body: string }) =>
    request<{ id: number }>(`/api/v1/academic/courses/${courseId}/forum`, {
      method: 'POST',
      body: JSON.stringify(body),
    }),

  // Calendar (kalendar)
  calendar: () => request<CalendarView>('/api/v1/academic/calendar'),

  // Admin
  semesters: () => request<Semester[]>('/api/v1/academic/semesters'),
  adminUsers: () => request<AdminUser[]>('/api/v1/academic/users'),
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
