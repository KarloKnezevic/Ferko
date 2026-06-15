import { useMemo, useState, type ReactNode } from 'react';

export interface Column<T> {
  /** Stable key, also used as React key and sort identifier. */
  key: string;
  header: string;
  /** Sortable/searchable scalar for the cell; also the default rendered content. */
  value: (row: T) => string | number | null | undefined;
  /** Optional custom cell renderer (falls back to `value`). */
  render?: (row: T) => ReactNode;
  /** Defaults to true. */
  sortable?: boolean;
  className?: string;
}

interface DataTableProps<T> {
  rows: T[];
  columns: Column<T>[];
  rowKey: (row: T) => string | number;
  /** Extra text per row included in the search match (besides column values). */
  searchText?: (row: T) => string;
  searchPlaceholder?: string;
  emptyText?: string;
  loading?: boolean;
}

type SortState = { key: string; dir: 'asc' | 'desc' } | null;

function compare(a: string | number | null | undefined, b: string | number | null | undefined): number {
  const av = a ?? '';
  const bv = b ?? '';
  if (typeof av === 'number' && typeof bv === 'number') return av - bv;
  return String(av).localeCompare(String(bv), 'hr', { numeric: true, sensitivity: 'base' });
}

/**
 * Generic list table with clickable, sortable column headers and a single search box that filters
 * rows by their column values (plus optional extra `searchText`). Keeps the existing table styling.
 */
export function DataTable<T>({
  rows,
  columns,
  rowKey,
  searchText,
  searchPlaceholder = 'Pretraži…',
  emptyText = 'Nema rezultata.',
  loading = false,
}: DataTableProps<T>) {
  const [query, setQuery] = useState('');
  const [sort, setSort] = useState<SortState>(null);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return rows;
    return rows.filter((row) => {
      const haystack =
        columns.map((c) => c.value(row) ?? '').join(' ') + ' ' + (searchText?.(row) ?? '');
      return haystack.toLowerCase().includes(q);
    });
  }, [rows, columns, query, searchText]);

  const sorted = useMemo(() => {
    if (!sort) return filtered;
    const col = columns.find((c) => c.key === sort.key);
    if (!col) return filtered;
    const factor = sort.dir === 'asc' ? 1 : -1;
    return filtered.slice().sort((a, b) => factor * compare(col.value(a), col.value(b)));
  }, [filtered, sort, columns]);

  const toggleSort = (key: string) =>
    setSort((prev) =>
      prev?.key === key
        ? { key, dir: prev.dir === 'asc' ? 'desc' : 'asc' }
        : { key, dir: 'asc' },
    );

  return (
    <div>
      <div className="table-toolbar">
        <input
          type="search"
          className="table-search"
          placeholder={searchPlaceholder}
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <span className="muted table-count">{sorted.length} / {rows.length}</span>
      </div>
      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <table>
          <thead>
            <tr>
              {columns.map((c) => {
                const sortable = c.sortable !== false;
                const active = sort?.key === c.key;
                return (
                  <th
                    key={c.key}
                    className={sortable ? 'th-sortable' : undefined}
                    onClick={sortable ? () => toggleSort(c.key) : undefined}
                    aria-sort={active ? (sort!.dir === 'asc' ? 'ascending' : 'descending') : undefined}
                  >
                    {c.header}
                    {sortable && (
                      <span className="sort-ind">{active ? (sort!.dir === 'asc' ? ' ▲' : ' ▼') : ' ⇅'}</span>
                    )}
                  </th>
                );
              })}
            </tr>
          </thead>
          <tbody>
            {sorted.map((row) => (
              <tr key={rowKey(row)}>
                {columns.map((c) => (
                  <td key={c.key} className={c.className}>
                    {c.render ? c.render(row) : c.value(row)}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {loading && <p className="muted">Učitavanje…</p>}
      {!loading && sorted.length === 0 && <p className="muted">{emptyText}</p>}
    </div>
  );
}
