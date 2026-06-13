import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';

export type Lang = 'hr' | 'en';

type Dict = Record<string, string>;

const HR: Dict = {
  'nav.home': 'Početna',
  'nav.courses': 'Kolegiji',
  'nav.calendar': 'Kalendar',
  'nav.notices': 'Obavijesti',
  'nav.rooms': 'Prostorije',
  'nav.students': 'Studenti',
  'nav.logout': 'Odjava',
  'footer.tagline': 'FERKO — sustav za organizaciju nastave',
  'footer.faculty': 'Fakultet elektrotehnike i računarstva, Sveučilište u Zagrebu',
  'footer.language': 'Jezik',
  'common.loading': 'Učitavanje…',
  'common.total': 'Ukupno',
  'common.grade': 'Ocjena',
  'common.student': 'Student',
  'common.add': 'Dodaj',
  'common.save': 'Spremi',
  'grading.title': 'Preglednik bodova',
  'grading.subtitle': 'Bodovne komponente, unos bodova, ocjene i auto-ocjenjivanje',
  'grading.components': 'Bodovne komponente',
  'grading.addComponent': 'Dodaj komponentu',
  'grading.name': 'Naziv',
  'grading.short': 'Kratica',
  'grading.maxPoints': 'Maks. bodova',
  'grading.overview': 'Preglednik bodova',
  'grading.enterPoints': 'Unos bodova',
  'grading.points': 'Bodovi',
  'grading.assignGrade': 'Dodijeli ocjenu',
  'grading.noComponents': 'Još nema definiranih bodovnih komponenti.',
  'grading.autograde': 'Auto-ocjenjivanje obrasca',
  'grading.correctKey': 'Točni odgovori (npr. A, B, A,C, A+B — odvojeni s ;)',
  'grading.submissions': 'Odgovori studenata (jedan red: JMBAG = A;B;C)',
  'grading.run': 'Ocijeni',
  'grading.result': 'Rezultat',
  'grading.correct': 'Točno',
  'login.subtitle': 'Sustav za organizaciju nastave',
  'notices.new': 'Nova obavijest',
  'notices.title': 'Naslov',
  'notices.body': 'Sadržaj',
  'notices.pin': 'Prikvači na vrh',
  'notices.publish': 'Objavi',
  'notices.empty': 'Trenutno nema obavijesti.',
  'calendar.subtitle': 'Tjedni raspored nastave i nadolazeće provjere',
  'calendar.weekly': 'Tjedni raspored nastave',
  'calendar.exams': 'Nadolazeće provjere',
  'calendar.emptyWeekly': 'Nema unesenog rasporeda nastave.',
  'calendar.emptyExams': 'Nema datiranih provjera.',
  'calendar.when': 'Termin',
  'calendar.exam': 'Provjera',
  'nav.admin': 'Administracija',
  'admin.title': 'Administracija sustava',
  'admin.subtitle': 'Semestri i korisnici',
  'admin.newSemester': 'Novi semestar',
  'admin.code': 'Šifra',
  'admin.year': 'Akademska godina',
  'admin.term': 'Razdoblje',
  'admin.from': 'Početak',
  'admin.to': 'Kraj',
  'admin.activeSemester': 'Aktivni semestar',
  'admin.semesters': 'Semestar',
  'admin.status': 'Status',
  'admin.active': 'Aktivan',
  'admin.inactive': 'Neaktivan',
  'admin.user': 'Korisnik',
  'admin.username': 'Korisničko ime',
  'admin.roles': 'Uloge',
  'admin.sync': 'Sinkronizacija podataka',
  'admin.syncNote':
    'Trenutno učitani podaci. Uvoz iz ISVU-a izvodi se idempotentno pri pokretanju (seeder); brojači prikazuju zatečeno stanje.',
  'admin.semestersCount': 'Semestri',
  'surveys.title': 'Ankete',
  'surveys.new': 'Nova anketa',
  'surveys.surveyTitle': 'Naziv ankete',
  'surveys.questions': 'Pitanja (jedno po retku)',
  'surveys.submit': 'Pošalji ocjene',
  'surveys.thanks': 'Hvala na ispunjenoj anketi!',
  'surveys.results': 'Rezultati',
  'surveys.question': 'Pitanje',
  'surveys.average': 'Prosjek',
  'surveys.responses': 'Odgovora',
  'surveys.empty': 'Trenutno nema anketa.',
  'forum.title': 'Pitanja i problemi',
  'forum.ask': 'Postavi pitanje',
  'forum.reply': 'Odgovori',
  'forum.send': 'Pošalji',
  'forum.placeholder': 'Opišite pitanje ili problem…',
  'forum.empty': 'Još nema poruka. Budite prvi!',
  'repo.title': 'Repozitorij',
  'repo.upload': 'Učitaj datoteku',
  'repo.uploadBtn': 'Učitaj',
  'repo.uploading': 'Učitavanje…',
  'repo.file': 'Datoteka',
  'repo.size': 'Veličina',
  'repo.uploadedBy': 'Učitao',
  'repo.date': 'Datum',
  'repo.download': 'Preuzmi',
  'repo.empty': 'Nema datoteka.',
};

const EN: Dict = {
  'nav.home': 'Home',
  'nav.courses': 'Courses',
  'nav.calendar': 'Calendar',
  'nav.notices': 'Notices',
  'nav.rooms': 'Rooms',
  'nav.students': 'Students',
  'nav.logout': 'Sign out',
  'footer.tagline': 'FERKO — teaching organisation system',
  'footer.faculty': 'Faculty of Electrical Engineering and Computing, University of Zagreb',
  'footer.language': 'Language',
  'common.loading': 'Loading…',
  'common.total': 'Total',
  'common.grade': 'Grade',
  'common.student': 'Student',
  'common.add': 'Add',
  'common.save': 'Save',
  'grading.title': 'Points overview',
  'grading.subtitle': 'Grade components, points entry, grades and auto-grading',
  'grading.components': 'Grade components',
  'grading.addComponent': 'Add component',
  'grading.name': 'Name',
  'grading.short': 'Short name',
  'grading.maxPoints': 'Max points',
  'grading.overview': 'Points overview',
  'grading.enterPoints': 'Enter points',
  'grading.points': 'Points',
  'grading.assignGrade': 'Assign grade',
  'grading.noComponents': 'No grade components defined yet.',
  'grading.autograde': 'Answer-sheet auto-grading',
  'grading.correctKey': 'Correct answers (e.g. A, B, A,C, A+B — separated by ;)',
  'grading.submissions': 'Student answers (one row: JMBAG = A;B;C)',
  'grading.run': 'Grade',
  'grading.result': 'Result',
  'grading.correct': 'Correct',
  'login.subtitle': 'Teaching organisation system',
  'notices.new': 'New notice',
  'notices.title': 'Title',
  'notices.body': 'Content',
  'notices.pin': 'Pin to top',
  'notices.publish': 'Publish',
  'notices.empty': 'No notices yet.',
  'calendar.subtitle': 'Weekly teaching timetable and upcoming assessments',
  'calendar.weekly': 'Weekly timetable',
  'calendar.exams': 'Upcoming assessments',
  'calendar.emptyWeekly': 'No timetable entries.',
  'calendar.emptyExams': 'No dated assessments.',
  'calendar.when': 'When',
  'calendar.exam': 'Assessment',
  'nav.admin': 'Administration',
  'admin.title': 'System administration',
  'admin.subtitle': 'Semesters and users',
  'admin.newSemester': 'New semester',
  'admin.code': 'Code',
  'admin.year': 'Academic year',
  'admin.term': 'Term',
  'admin.from': 'Start',
  'admin.to': 'End',
  'admin.activeSemester': 'Active semester',
  'admin.semesters': 'Semester',
  'admin.status': 'Status',
  'admin.active': 'Active',
  'admin.inactive': 'Inactive',
  'admin.user': 'User',
  'admin.username': 'Username',
  'admin.roles': 'Roles',
  'admin.sync': 'Data synchronisation',
  'admin.syncNote':
    'Currently loaded data. The ISVU import runs idempotently on startup (seeder); the counts show the current state.',
  'admin.semestersCount': 'Semesters',
  'surveys.title': 'Surveys',
  'surveys.new': 'New survey',
  'surveys.surveyTitle': 'Survey title',
  'surveys.questions': 'Questions (one per line)',
  'surveys.submit': 'Submit ratings',
  'surveys.thanks': 'Thank you for completing the survey!',
  'surveys.results': 'Results',
  'surveys.question': 'Question',
  'surveys.average': 'Average',
  'surveys.responses': 'Responses',
  'surveys.empty': 'No surveys yet.',
  'forum.title': 'Questions & problems',
  'forum.ask': 'Ask a question',
  'forum.reply': 'Reply',
  'forum.send': 'Send',
  'forum.placeholder': 'Describe your question or problem…',
  'forum.empty': 'No messages yet. Be the first!',
  'repo.title': 'Repository',
  'repo.upload': 'Upload a file',
  'repo.uploadBtn': 'Upload',
  'repo.uploading': 'Uploading…',
  'repo.file': 'File',
  'repo.size': 'Size',
  'repo.uploadedBy': 'Uploaded by',
  'repo.date': 'Date',
  'repo.download': 'Download',
  'repo.empty': 'No files.',
};

const DICTS: Record<Lang, Dict> = { hr: HR, en: EN };

interface I18nValue {
  lang: Lang;
  setLang: (lang: Lang) => void;
  t: (key: string) => string;
}

const I18nContext = createContext<I18nValue | null>(null);

const STORAGE_KEY = 'ferko.lang';

export function I18nProvider({ children }: { children: ReactNode }) {
  const [lang, setLangState] = useState<Lang>(() => {
    const stored = typeof localStorage !== 'undefined' ? localStorage.getItem(STORAGE_KEY) : null;
    return stored === 'en' ? 'en' : 'hr';
  });

  const setLang = useCallback((next: Lang) => {
    setLangState(next);
    try {
      localStorage.setItem(STORAGE_KEY, next);
    } catch {
      /* storage unavailable */
    }
  }, []);

  const t = useCallback((key: string) => DICTS[lang][key] ?? DICTS.hr[key] ?? key, [lang]);

  const value = useMemo(() => ({ lang, setLang, t }), [lang, setLang, t]);
  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>;
}

export function useI18n(): I18nValue {
  const ctx = useContext(I18nContext);
  if (!ctx) throw new Error('useI18n must be used within I18nProvider');
  return ctx;
}
