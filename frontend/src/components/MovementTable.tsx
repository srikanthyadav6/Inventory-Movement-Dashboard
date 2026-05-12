import type { StockMovement } from '../types';
import { formatDateTime, formatNumber } from '../utils/format';
import { EmptyState } from './EmptyState';
import { Pagination } from './Pagination';

interface MovementTableProps {
  rows: StockMovement[];
  loading: boolean;
  page: number;
  totalElements: number;
  totalPages: number;
  onPreviousPage: () => void;
  onNextPage: () => void;
}

export function MovementTable({
  rows,
  loading,
  page,
  totalElements,
  totalPages,
  onPreviousPage,
  onNextPage,
}: MovementTableProps) {
  return (
    <section className="panel">
      <div className="panel-heading table-heading">
        <div>
          <h2>Stock Movements</h2>
          <p>{loading ? 'Loading movements...' : `Total ${formatNumber(totalElements)}`}</p>
        </div>
        <Pagination
          page={page}
          totalPages={totalPages}
          canGoBack={page > 0}
          canGoForward={page + 1 < totalPages}
          onPrevious={onPreviousPage}
          onNext={onNextPage}
        />
      </div>
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
    </section>
  );
}
