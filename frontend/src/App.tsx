import { ChartsSection } from './components/ChartsSection';
import { FiltersPanel } from './components/FiltersPanel';
import { MovementTable } from './components/MovementTable';
import { useMovementDashboard } from './hooks/useMovementDashboard';
import './App.css';

function App() {
  const dashboard = useMovementDashboard();

  return (
    <main className="app-shell">
      <section className="page-header">
        <div>
          <p className="eyebrow">Warehouse operations</p>
          <h1>Inventory Movement Dashboard</h1>
        </div>
      </section>

      <FiltersPanel
        filters={dashboard.filters}
        loading={dashboard.loading}
        exporting={dashboard.exporting}
        onFilterChange={dashboard.updateFilter}
        onApply={dashboard.applyFilters}
        onExport={dashboard.exportCsv}
      />

      {dashboard.error && <div className="alert">{dashboard.error}</div>}

      <ChartsSection analytics={dashboard.analytics} loading={dashboard.analyticsLoading} />

      <MovementTable
        rows={dashboard.pageData.content}
        loading={dashboard.loading}
        page={dashboard.page}
        totalElements={dashboard.pageData.totalElements}
        totalPages={dashboard.pageData.totalPages}
        onPreviousPage={() => dashboard.setPage((current) => Math.max(0, current - 1))}
        onNextPage={() => dashboard.setPage((current) => current + 1)}
      />
    </main>
  );
}

export default App;
