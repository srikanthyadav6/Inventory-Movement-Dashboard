import { MOVEMENT_TYPE_OPTIONS } from '../constants';
import type { MovementFilterType, MovementFilters } from '../types';

interface FiltersPanelProps {
  filters: MovementFilters;
  loading: boolean;
  exporting: boolean;
  onFilterChange: <K extends keyof MovementFilters>(key: K, value: MovementFilters[K]) => void;
  onApply: () => void;
  onExport: () => void;
}

export function FiltersPanel({
  filters,
  loading,
  exporting,
  onFilterChange,
  onApply,
  onExport,
}: FiltersPanelProps) {
  return (
    <section className="filters-panel" aria-label="Movement filters">
      <label>
        <span>From</span>
        <input
          required
          type="date"
          value={filters.from}
          max={filters.to}
          onChange={(event) => onFilterChange('from', event.target.value)}
        />
      </label>
      <label>
        <span>To</span>
        <input
          required
          type="date"
          value={filters.to}
          min={filters.from}
          onChange={(event) => onFilterChange('to', event.target.value)}
        />
      </label>
      <label>
        <span>Movement type</span>
        <select
          value={filters.type}
          onChange={(event) => onFilterChange('type', event.target.value as MovementFilterType)}
        >
          {MOVEMENT_TYPE_OPTIONS.map((option) => (
            <option key={option} value={option}>
              {option === 'ALL' ? 'All' : option}
            </option>
          ))}
        </select>
      </label>
      <button className="apply-button" type="button" onClick={onApply}>
        Apply filters
      </button>
      <button className="export-button" type="button" onClick={onExport} disabled={exporting || loading}>
        {exporting ? 'Exporting...' : 'Export CSV'}
      </button>
    </section>
  );
}
