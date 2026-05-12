import { useEffect, useState } from 'react';
import { exportMovementsCsv, fetchMovementAnalytics, fetchMovements } from '../api';
import { DEFAULT_FILTERS, EMPTY_ANALYTICS_RESPONSE, EMPTY_PAGE_RESPONSE } from '../constants';
import type { MovementAnalyticsResponse, MovementFilters, MovementPageResponse } from '../types';

export function useMovementDashboard() {
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

  function updateFilter<K extends keyof MovementFilters>(key: K, value: MovementFilters[K]) {
    setFilters((current) => ({ ...current, [key]: value }));
  }

  function applyFilters() {
    setAppliedFilters(filters);
    setPage(0);
  }

  async function exportCsv() {
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

  return {
    filters,
    updateFilter,
    applyFilters,
    exportCsv,
    page,
    setPage,
    pageData,
    analytics,
    loading,
    analyticsLoading,
    exporting,
    error,
  };
}
