// MLB 공식 CDN 이미지 헬퍼
// 팀 로고와 선수 헤드샷 URL을 생성합니다.

export function teamLogoUrl(teamId: number | null | undefined): string | null {
  if (!teamId) return null
  return `https://www.mlbstatic.com/team-logos/${teamId}.svg`
}

export function playerHeadshotUrl(playerId: number | null | undefined, size = 120): string | null {
  if (!playerId) return null
  return `https://midfield.mlbstatic.com/v1/people/${playerId}/spots/${size}`
}

export function initials(name: string | null | undefined): string {
  if (!name) return "?"
  const parts = name.trim().split(/\s+/)
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase()
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
}
