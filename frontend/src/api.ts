import type { MovementAnalyticsResponse, MovementFilters, MovementPageResponse } from './types';
import { PAGE_SIZE } from './constants';

function buildMovementParams(filters: MovementFilters, page?: number) {
  const params = new URLSearchParams({
    from: filters.from,
    to: filters.to,
  });

  if (filters.type !== 'ALL') {
    params.set('type', filters.type);
  }

  if (page !== undefined) {
    params.set('page', String(page));
    params.set('size', String(PAGE_SIZE));
  }

  return params;
}

export async function fetchMovements(filters: MovementFilters, page: number, signal?: AbortSignal) {
  const params = buildMovementParams(filters, page);
  const response = await fetch(`/api/movements?${params.toString()}`, { signal });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || 'Unable to load stock movements.');
  }

  return response.json() as Promise<MovementPageResponse>;
}

export async function fetchMovementAnalytics(filters: MovementFilters, signal?: AbortSignal) {
  const params = buildMovementParams(filters);
  const response = await fetch(`/api/movements/analytics?${params.toString()}`, { signal });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || 'Unable to load movement analytics.');
  }

  return response.json() as Promise<MovementAnalyticsResponse>;
}

export async function exportMovementsCsv(filters: MovementFilters) {
  const params = buildMovementParams(filters);
  params.set('export', 'true');

  const response = await fetch(`/api/movements?${params.toString()}`);
  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || 'Unable to export CSV.');
  }

  const blob = await response.blob();
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `stock-movements-${filters.from}-to-${filters.to}.csv`;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}
