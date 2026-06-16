export function formatSeconds(seconds: number): string {
  const safeValue = Math.max(0, Math.floor(seconds || 0))
  const hour = String(Math.floor(safeValue / 3600)).padStart(2, '0')
  const minute = String(Math.floor((safeValue % 3600) / 60)).padStart(2, '0')
  const second = String(safeValue % 60).padStart(2, '0')

  return `${hour}:${minute}:${second}`
}
