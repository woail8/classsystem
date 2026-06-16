export function haversineDistance(
  lat1: number,
  lng1: number,
  lat2: number,
  lng2: number
): number {
  const toRad = (value: number) => (value * Math.PI) / 180
  const earthRadius = 6371000

  const diffLat = toRad(lat2 - lat1)
  const diffLng = toRad(lng2 - lng1)

  const a =
    Math.sin(diffLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(diffLng / 2) ** 2

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return earthRadius * c
}

export function formatDistance(distance: number | null): string {
  if (distance == null) return '--'
  if (distance < 1000) return `${Math.round(distance)} 米`
  return `${(distance / 1000).toFixed(2)} 公里`
}
