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
