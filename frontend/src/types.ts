export type MovementType = 'IN' | 'OUT';
export type MovementFilterType = MovementType | 'ALL';

export interface StockMovement {
  id: string;
  timestamp: string;
  sku: string;
  movementType: MovementType;
  quantity: number;
}

export interface MovementSummary {
  inQuantity: number;
  outQuantity: number;
}

export interface DailyMovementTotal {
  date: string;
  inQuantity: number;
  outQuantity: number;
}

export interface MovementPageResponse {
  content: StockMovement[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  summary: MovementSummary;
  dailyTotals: DailyMovementTotal[];
}

export interface MovementFilters {
  from: string;
  to: string;
  type: MovementFilterType;
}
