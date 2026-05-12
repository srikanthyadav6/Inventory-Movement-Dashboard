import { useMemo } from 'react';
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
import { MOVEMENT_COLORS } from '../constants';
import type { MovementAnalyticsResponse } from '../types';
import { formatNumber } from '../utils/format';
import { EmptyState } from './EmptyState';

interface ChartsSectionProps {
  analytics: MovementAnalyticsResponse;
  loading: boolean;
}

export function ChartsSection({ analytics, loading }: ChartsSectionProps) {
  const pieData = useMemo(
    () => [
      { name: 'IN', value: analytics.summary.inQuantity },
      { name: 'OUT', value: analytics.summary.outQuantity },
    ].filter((item) => item.value > 0),
    [analytics.summary],
  );

  const totalQuantity = analytics.summary.inQuantity + analytics.summary.outQuantity;

  return (
    <section className="charts-grid">
      <div className="panel">
        <div className="panel-heading">
          <h2>IN vs OUT Quantity</h2>
        </div>
        {loading ? (
          <EmptyState message="Loading chart totals..." />
        ) : totalQuantity === 0 ? (
          <EmptyState message="No quantity totals for the selected filters." />
        ) : (
          <div className="chart-frame">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={pieData} dataKey="value" nameKey="name" outerRadius={100} label>
                  {pieData.map((entry) => (
                    <Cell key={entry.name} fill={MOVEMENT_COLORS[entry.name as 'IN' | 'OUT']} />
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
        {loading ? (
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
                  stroke={MOVEMENT_COLORS.IN}
                  strokeWidth={2}
                  fill={MOVEMENT_COLORS.IN}
                  fillOpacity={0.12}
                />
                <Area
                  type="monotone"
                  dataKey="outQuantity"
                  name="OUT"
                  stroke={MOVEMENT_COLORS.OUT}
                  strokeWidth={2}
                  fill={MOVEMENT_COLORS.OUT}
                  fillOpacity={0.12}
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>
    </section>
  );
}
