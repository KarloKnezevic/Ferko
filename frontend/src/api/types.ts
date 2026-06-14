export interface CurrentUser {
  id: number;
  username: string;
  fullName: string;
  roles: string[];
}

export interface Semester {
  code: string;
  academicYear: string;
  term: string;
  startsOn: string;
  endsOn: string;
  active: boolean;
}

export interface CourseSummary {
  id: number;
  code: string;
  name: string;
  semesterCode: string;
  ects: number;
  enrolledStudents: number;
}

export interface CourseStaff {
  userId: number;
  fullName: string;
  role: string;
}

export interface StudentGroup {
  id: number;
  groupCode: string;
  type: string;
  category: string | null;
  capacity: number;
}

export interface CourseDetail {
  id: number;
  code: string;
  name: string;
  semesterCode: string;
  ects: number;
  description: string | null;
  literature: string | null;
  enrolledStudents: number;
  staff: CourseStaff[];
  groups: StudentGroup[];
}

export interface Student {
  id: number;
  jmbag: string;
  fullName: string;
  studyProgram: string | null;
  yearOfStudy: number;
}

export interface Room {
  id: number;
  code: string;
  building: string | null;
  capacity: number;
  requiredAssistants: number;
  hasComputers: boolean;
}

export interface Exam {
  id: number;
  courseId: number;
  title: string;
  shortName: string;
  kind: string;
  startsAt: string | null;
  durationMinutes: number;
  maxPoints: number;
  published: boolean;
  registeredStudents: number;
  reservedRooms: number;
  totalRoomCapacity: number;
  seatedStudents: number;
}

export interface ExamSeat {
  studentId: number;
  studentJmbag: string;
  studentFullName: string;
  roomId: number;
  roomCode: string;
  seatNo: number | null;
  testGroup: string | null;
}

export interface RoomSeating {
  roomId: number;
  roomCode: string;
  capacity: number;
  assignedStudents: number;
  seats: ExamSeat[];
}

export interface MyExam {
  examId: number;
  courseId: number;
  courseCode: string;
  courseName: string;
  title: string;
  shortName: string;
  kind: string;
  startsAt: string | null;
  durationMinutes: number;
  maxPoints: number;
  registered: boolean;
  published: boolean;
  roomCode: string | null;
  seatNo: number | null;
  testGroup: string | null;
}

export interface MyCourseGrade {
  courseId: number;
  courseCode: string;
  courseName: string;
  components: {
    shortName: string;
    name: string;
    points: number;
    maxPoints: number;
  }[];
  totalPoints: number;
  maxPoints: number;
  finalGrade: number;
}

export interface ExamAssistant {
  id: number;
  examId: number;
  roomId: number;
  roomCode: string;
  userId: number;
  username: string;
  fullName: string;
}

export interface GradeComponentView {
  id: number;
  name: string;
  shortName: string;
  maxPoints: number;
  ordinal: number;
}

export interface PointsOverviewRow {
  studentId: number;
  jmbag: string;
  fullName: string;
  pointsByComponent: Record<string, number>;
  total: number;
  finalGrade: number;
}

export interface GradeView {
  studentId: number;
  jmbag: string;
  fullName: string;
  finalGrade: number;
  pointsTotal: number;
}

export interface AutoGradeResult {
  jmbag: string;
  total: number;
  correct: number;
  questions: number;
}

export interface GroupExchange {
  id: number;
  courseId: number;
  studentJmbag: string;
  studentName: string;
  fromGroup: string;
  toGroup: string;
  status: string;
  reason: string | null;
  decidedBy: string | null;
  createdAt: string;
}

export interface RepoFile {
  id: number;
  courseId: number;
  filename: string;
  contentType: string | null;
  sizeBytes: number;
  uploadedBy: string | null;
  uploadedAt: string;
}

export interface CourseComponent {
  id: number;
  courseId: number;
  title: string;
  content: string;
  ordinal: number;
  visible: boolean;
}

export interface CourseEnrollment {
  id: number;
  studentId: number;
  studentJmbag: string;
  studentFullName: string;
  courseId: number;
  status: string;
  groupCodes: string[];
}

export interface MyDuty {
  examId: number;
  examTitle: string;
  examShortName: string;
  courseId: number;
  courseCode: string;
  courseName: string;
  startsAt: string | null;
  roomCode: string;
  published: boolean;
}

export interface MyProfile {
  username: string;
  fullName: string;
  email: string;
  roles: string[];
  jmbag: string | null;
  studyProgram: string | null;
  yearOfStudy: number;
}

export interface ScheduleSlot {
  id: number;
  dayOfWeek: string;
  startsAt: string;
  endsAt: string;
  type: string;
  groupCode: string;
  roomCode: string;
  instructor: string;
}

export interface Consultation {
  id: number;
  courseId: number;
  staffName: string;
  dayOfWeek: string;
  startsAt: string;
  endsAt: string;
  location: string;
}

export interface CourseLiterature {
  id: number;
  courseId: number;
  title: string;
  author: string;
  mandatory: boolean;
  ordinal: number;
}

export interface ForumPost {
  id: number;
  courseId: number;
  parentId: number | null;
  authorName: string | null;
  body: string;
  createdAt: string;
}

export interface SurveyQuestionView {
  id: number;
  text: string;
  ordinal: number;
}

export interface SurveyView {
  id: number;
  courseId: number;
  title: string;
  active: boolean;
  questions: SurveyQuestionView[];
}

export interface SurveyResult {
  questionId: number;
  text: string;
  responses: number;
  average: number;
}

export interface AdminUser {
  id: number;
  username: string;
  fullName: string;
  email: string | null;
  active: boolean;
  roles: string[];
}

export interface SyncStatus {
  semesters: number;
  courses: number;
  students: number;
  rooms: number;
}

export interface WeeklySlot {
  dayOfWeek: string;
  startsAt: string;
  endsAt: string;
  type: string;
  courseCode: string;
  courseName: string;
  room: string | null;
  instructor: string | null;
}

export interface UpcomingExam {
  startsAt: string;
  title: string;
  shortName: string;
  courseCode: string;
  courseName: string;
  durationMinutes: number;
}

export interface CalendarView {
  weekly: WeeklySlot[];
  exams: UpcomingExam[];
}

export interface Notice {
  id: number;
  courseId: number | null;
  title: string;
  body: string;
  authorName: string | null;
  createdAt: string;
  pinned: boolean;
}

export interface AlgorithmRun {
  algorithm: string;
  penalty: number;
  iterations: number;
  feasible: boolean;
  durationMillis: number;
  penaltyHistory: number[];
}

export interface SeatingResult {
  strategy: string;
  seatedStudents: number;
  overCapacityPenalty: number;
  feasible: boolean;
  penaltyHistory: number[];
  rooms: RoomSeating[];
}
