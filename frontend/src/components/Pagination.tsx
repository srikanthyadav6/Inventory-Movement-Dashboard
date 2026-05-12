interface PaginationProps {
  page: number;
  totalPages: number;
  canGoBack: boolean;
  canGoForward: boolean;
  onPrevious: () => void;
  onNext: () => void;
}

export function Pagination({
  page,
  totalPages,
  canGoBack,
  canGoForward,
  onPrevious,
  onNext,
}: PaginationProps) {
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
