import { useEffect, useMemo, useState } from 'react';
import {
  Area,
  AreaChart,
  CartesianGrid,
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { exportMovementsCsv, fetchMovementAnalytics, fetchMovements } from './api';
import type {
  MovementAnalyticsResponse,
  MovementFilterType,
  MovementFilters,
  MovementPageResponse,
  StockMovement,
} from './types';
import './App.css';

const DEFAULT_FILTERS: MovementFilters = {
  from: '2026-02-01',
  to: '2026-05-31',
  type: 'ALL',
};

const EMPTY_PAGE_RESPONSE: MovementPageResponse = {
  content: [],
  page: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0,
};

const EMPTY_ANALYTICS_RESPONSE: MovementAnalyticsResponse = {
  summary: { inQuantity: 0, outQuantity: 0 },
  dailyTotals: [],
};

const movementTypeOptions: MovementFilterType[] = ['ALL', 'IN', 'OUT'];
const pieColors: Record<'IN' | 'OUT', string> = {
  IN: '#1f9d55',
  OUT: '#d97706',
};

function App() {
  const [filters, setFilters] = useState<MovementFilters>(DEFAULT_FILTERS);
  const [appliedFilters, setAppliedFilters] = useState<MovementFilters>(DEFAULT_FILTERS);
  const [page, setPage] = useState(0);
  const [pageData, setPageData] = useState<MovementPageResponse>(EMPTY_PAGE_RESPONSE);
  const [analytics, setAnalytics] = useState<MovementAnalyticsResponse>(EMPTY_ANALYTICS_RESPONSE);
  const [loading, setLoading] = useState(false);
  const [analyticsLoading, setAnalyticsLoading] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError('');

    fetchMovements(appliedFilters, page, controller.signal)
      .then(setPageData)
      .catch((err: Error) => {
        if (err.name !== 'AbortError') {
          setPageData(EMPTY_PAGE_RESPONSE);
          setError(err.message);
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      });

    return () => controller.abort();
  }, [appliedFilters, page]);

  useEffect(() => {
    const controller = new AbortController();
    setAnalyticsLoading(true);
    setError('');

    fetchMovementAnalytics(appliedFilters, controller.signal)
      .then(setAnalytics)
      .catch((err: Error) => {
        if (err.name !== 'AbortError') {
          setAnalytics(EMPTY_ANALYTICS_RESPONSE);
          setError(err.message);
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setAnalyticsLoading(false);
        }
      });

    return () => controller.abort();
  }, [appliedFilters]);

  const pieData = useMemo(
    () => [
      { name: 'IN', value: analytics.summary.inQuantity },
      { name: 'OUT', value: analytics.summary.outQuantity },
    ].filter((item) => item.value > 0),
    [analytics.summary],
  );

  const totalQuantity = analytics.summary.inQuantity + analytics.summary.outQuantity;
  const canGoBack = page > 0;
  const canGoForward = page + 1 < pageData.totalPages;

  function updateFilter<K extends keyof MovementFilters>(key: K, value: MovementFilters[K]) {
    setFilters((current) => ({ ...current, [key]: value }));
  }

  function applyFilters() {
    setAppliedFilters(filters);
    setPage(0);
  }

  async function handleExport() {
    setExporting(true);
    setError('');
    try {
      await exportMovementsCsv(appliedFilters);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to export CSV.');
    } finally {
      setExporting(false);
    }
  }

  return (
    <main className="app-shell">
      <section className="page-header">
        <div>
          <p className="eyebrow">Warehouse operations</p>
          <h1>Inventory Movement Dashboard</h1>
        </div>
      </section>

      <section className="filters-panel" aria-label="Movement filters">
        <label>
          <span>From</span>
          <input
            required
            type="date"
            value={filters.from}
            max={filters.to}
            onChange={(event) => updateFilter('from', event.target.value)}
          />
        </label>
        <label>
          <span>To</span>
          <input
            required
            type="date"
            value={filters.to}
            min={filters.from}
            onChange={(event) => updateFilter('to', event.target.value)}
          />
        </label>
        <label>
          <span>Movement type</span>
          <select
            value={filters.type}
            onChange={(event) => updateFilter('type', event.target.value as MovementFilterType)}
          >
            {movementTypeOptions.map((option) => (
              <option key={option} value={option}>
                {option === 'ALL' ? 'All' : option}
              </option>
            ))}
          </select>
        </label>
        <button className="apply-button" type="button" onClick={applyFilters}>
          Apply filters
        </button>
        <button className="export-button" type="button" onClick={handleExport} disabled={exporting || loading}>
          {exporting ? 'Exporting...' : 'Export CSV'}
        </button>
      </section>

      {error && <div className="alert">{error}</div>}

      <section className="charts-grid">
        <div className="panel">
          <div className="panel-heading">
            <h2>IN vs OUT Quantity</h2>
          </div>
          {analyticsLoading ? (
            <EmptyState message="Loading chart totals..." />
          ) : totalQuantity === 0 ? (
            <EmptyState message="No quantity totals for the selected filters." />
          ) : (
            <div className="chart-frame">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={pieData} dataKey="value" nameKey="name" outerRadius={100} label>
                    {pieData.map((entry) => (
                      <Cell key={entry.name} fill={pieColors[entry.name as 'IN' | 'OUT']} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(value) => [formatNumber(Number(value)), 'Quantity']} />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>

        <div className="panel panel-wide">
          <div className="panel-heading">
            <h2>Daily Movement Trend</h2>
          </div>
          {analyticsLoading ? (
            <EmptyState message="Loading daily totals..." />
          ) : analytics.dailyTotals.length === 0 ? (
            <EmptyState message="No daily totals for the selected filters." />
          ) : (
            <div className="chart-frame">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={analytics.dailyTotals} margin={{ top: 8, right: 18, left: 8, bottom: 8 }}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" minTickGap={28} />
                  <YAxis />
                  <Tooltip formatter={(value) => formatNumber(Number(value))} />
                  <Legend />
                  <Area
                    type="monotone"
                    dataKey="inQuantity"
                    name="IN"
                    stroke={pieColors.IN}
                    strokeWidth={2}
                    fill={pieColors.IN}
                    fillOpacity={0.12}
                  />
                  <Area
                    type="monotone"
                    dataKey="outQuantity"
                    name="OUT"
                    stroke={pieColors.OUT}
                    strokeWidth={2}
                    fill={pieColors.OUT}
                    fillOpacity={0.12}
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>
      </section>

      <section className="panel">
        <div className="panel-heading table-heading">
          <div>
            <h2>Stock Movements</h2>
            <p>{loading ? 'Loading movements...' : `Total ${formatNumber(pageData.totalElements)}`}</p>
          </div>
          <Pagination
            page={page}
            totalPages={pageData.totalPages}
            canGoBack={canGoBack}
            canGoForward={canGoForward}
            onPrevious={() => setPage((current) => Math.max(0, current - 1))}
            onNext={() => setPage((current) => current + 1)}
          />
        </div>
        <MovementTable rows={pageData.content} loading={loading} />
      </section>
    </main>
  );
}

function Pagination({
  page,
  totalPages,
  canGoBack,
  canGoForward,
  onPrevious,
  onNext,
}: {
  page: number;
  totalPages: number;
  canGoBack: boolean;
  canGoForward: boolean;
  onPrevious: () => void;
  onNext: () => void;
}) {
  return (
    <div className="pagination">
      <button type="button" onClick={onPrevious} disabled={!canGoBack}>
        Previous
      </button>
      <span>
        Page {totalPages === 0 ? 0 : page + 1} of {totalPages}
      </span>
      <button type="button" onClick={onNext} disabled={!canGoForward}>
        Next
      </button>
    </div>
  );
}

function MovementTable({ rows, loading }: { rows: StockMovement[]; loading: boolean }) {
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Date/Time</th>
            <th>SKU</th>
            <th>Movement Type</th>
            <th className="numeric">Quantity</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={row.id}>
              <td>{formatDateTime(row.timestamp)}</td>
              <td>{row.sku}</td>
              <td>
                <span className={`type-pill ${row.movementType.toLowerCase()}`}>{row.movementType}</span>
              </td>
              <td className="numeric">{formatNumber(row.quantity)}</td>
            </tr>
          ))}
          {!loading && rows.length === 0 && (
            <tr>
              <td colSpan={4}>
                <EmptyState message="No movements match the selected filters." />
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

function EmptyState({ message }: { message: string }) {
  return <div className="empty-state">{message}</div>;
}

function formatNumber(value: number) {
  return new Intl.NumberFormat('en-US').format(value);
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat('en-US', {
    dateStyle: 'medium',
    timeStyle: 'short',
    timeZone: 'UTC',
  }).format(new Date(value));
}

export default App;
