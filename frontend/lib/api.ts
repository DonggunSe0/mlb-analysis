import type { Game, Player, Team, TeamPlayer } from './types'

export class ApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.status = status
  }
}

type GameEnvelope = { games: Omit<Game, 'homeTeamId' | 'awayTeamId'>[] }
type TeamEnvelope = { teams: Team[] }
type RosterEnvelope = { players: TeamPlayer[] }
type PlayerSearchEnvelope = { players: Player[] }

export async function fetcher<T>(url: string): Promise<T> {
  const res = await fetch(url)
  if (!res.ok) {
    throw new ApiError(`요청 실패 (${res.status})`, res.status)
  }

  const data = await res.json()
  return normalizeResponse(url, data) as T
}

function normalizeResponse(url: string, data: unknown): unknown {
  if (url.startsWith('/api/v1/games')) {
    const games = Array.isArray(data) ? data : (data as GameEnvelope).games
    return games.map((game) => ({
      ...game,
      homeTeamId: 'homeTeamId' in game ? game.homeTeamId : null,
      awayTeamId: 'awayTeamId' in game ? game.awayTeamId : null,
    }))
  }

  if (url === '/api/v1/teams') {
    return Array.isArray(data) ? data : (data as TeamEnvelope).teams
  }

  if (/^\/api\/v1\/teams\/\d+\/players$/.test(url)) {
    return Array.isArray(data) ? data : (data as RosterEnvelope).players
  }

  if (url.startsWith('/api/v1/players/search')) {
    return Array.isArray(data) ? data : (data as PlayerSearchEnvelope).players
  }

  return data
}

export const endpoints = {
  games: (date: string) => `/api/v1/games?date=${encodeURIComponent(date)}`,
  teams: () => '/api/v1/teams',
  roster: (teamId: number) => `/api/v1/teams/${teamId}/players`,
  search: (name: string) => `/api/v1/players/search?name=${encodeURIComponent(name)}`,
  player: (playerId: number) => `/api/v1/players/${playerId}`,
}

export type { Game, Player, Team, TeamPlayer }
