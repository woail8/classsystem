import { describe, expect, it } from 'vitest'
import { formatDistance, haversineDistance } from '@/utils/distance'

describe('distance utils', () => {
  it('同一点的距离应为 0', () => {
    expect(haversineDistance(30.0, 120.0, 30.0, 120.0)).toBe(0)
  })

  it('不同坐标点应返回正距离', () => {
    expect(haversineDistance(30.0, 120.0, 30.001, 120.001)).toBeGreaterThan(0)
  })

  it('应正确格式化米和公里', () => {
    expect(formatDistance(320)).toBe('320 米')
    expect(formatDistance(1520)).toBe('1.52 公里')
    expect(formatDistance(null)).toBe('--')
  })
})
