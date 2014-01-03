package hr.fer.zemris.jcms.activities.types;

public enum ActivityEventKind {
	CREATED,
	MODIFIED,
	DELETED,
	SILENT, // Ako ne treba emitirati event jer se ništa nije promijenilo
	PUBLISH // Ako nastavnik naknadno stisne gumb objavi, kada se vise nista ne mijenja
}
