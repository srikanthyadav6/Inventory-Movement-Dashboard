export function formatNumber(value: number) {
  return new Intl.NumberFormat('en-US').format(value);
}

export function formatDateTime(value: string) {
  return new Intl.DateTimeFormat('en-US', {
    dateStyle: 'medium',
    timeStyle: 'short',
    timeZone: 'UTC',
  }).format(new Date(value));
}
