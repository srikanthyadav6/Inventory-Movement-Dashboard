import type { MovementAnalyticsResponse, MovementFilterType, MovementFilters, MovementPageResponse } from './types';

export const PAGE_SIZE = 10;

export const DEFAULT_FILTERS: MovementFilters = {
  from: '2026-02-01',
  to: '2026-05-31',
  type: 'ALL',
};

export const EMPTY_PAGE_RESPONSE: MovementPageResponse = {
  content: [],
  page: 0,
  size: PAGE_SIZE,
  totalElements: 0,
  totalPages: 0,
};

export const EMPTY_ANALYTICS_RESPONSE: MovementAnalyticsResponse = {
  summary: { inQuantity: 0, outQuantity: 0 },
  dailyTotals: [],
};

export const MOVEMENT_TYPE_OPTIONS: MovementFilterType[] = ['ALL', 'IN', 'OUT'];

export const MOVEMENT_COLORS: Record<'IN' | 'OUT', string> = {
  IN: '#1f9d55',
  OUT: '#d97706',
};
