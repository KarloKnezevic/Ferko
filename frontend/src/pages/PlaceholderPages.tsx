import { useI18n } from '../i18n';

function Placeholder({ titleKey, note }: { titleKey: string; note: string }) {
  const { t } = useI18n();
  return (
    <div>
      <h1>{t(titleKey)}</h1>
      <div className="card">
        <p className="muted">{note}</p>
      </div>
    </div>
  );
}

export function CalendarPage() {
  return (
    <Placeholder
      titleKey="nav.calendar"
      note="Osobni kalendar nastavnih aktivnosti (predavanja, vježbe, provjere) — agregacija po korisniku i ulozi."
    />
  );
}

export function NoticesPage() {
  return (
    <Placeholder
      titleKey="nav.notices"
      note="Obavijesti s kolegija i fakulteta — pregled po kolegiju, čitanje i objava."
    />
  );
}
