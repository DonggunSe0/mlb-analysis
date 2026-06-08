import type {
  AllStarBallot,
  AllStarSelection,
  AllStarVoteStatus,
  AuthResponse,
  CurrentUser,
  Game,
  GamePick,
  GamePickSummary,
  NewsItem,
  Player,
  PlayerStats,
  Team,
  TeamPlayer,
  TeamStanding,
  UserPreference,
} from './types'

export class ApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.status = status
  }
}

type GameEnvelope = { games: Game[] }
type TeamEnvelope = { teams: Team[] }
type TeamStandingEnvelope = { standings: TeamStanding[] }
type RosterEnvelope = { players: TeamPlayer[] }
type PlayerSearchEnvelope = { players: Player[] }
type NewsEnvelope = { news: NewsItem[] }
type GamePickListEnvelope = { picks: GamePick[] }

export const AUTH_TOKEN_KEY = 'mlb-analysis-auth-token'

export async function fetcher<T>(url: string): Promise<T> {
  const res = await fetch(url)
  if (!res.ok) {
    throw new ApiError(`요청 실패 (${res.status})`, res.status)
  }

  const data = await res.json()
  return normalizeResponse(url, data) as T
}

export async function apiRequest<T>(url: string, options: RequestInit = {}, token?: string | null): Promise<T> {
  const headers = new Headers(options.headers)
  if (!headers.has('Content-Type') && options.body) headers.set('Content-Type', 'application/json')
  if (token) headers.set('Authorization', `Bearer ${token}`)
  const res = await fetch(url, { ...options, headers })
  if (!res.ok) {
    let message = `요청 실패 (${res.status})`
    try {
      const body = await res.json()
      if (body?.message) message = body.message
    } catch {
      // Keep the default HTTP status message when the error body is not JSON.
    }
    throw new ApiError(message, res.status)
  }
  if (res.status === 204) return undefined as T
  return res.json() as Promise<T>
}

function normalizeResponse(url: string, data: unknown): unknown {
  if (url.startsWith('/api/v1/games')) return Array.isArray(data) ? data : (data as GameEnvelope).games
  if (url.startsWith('/api/v1/teams/standings')) return Array.isArray(data) ? data : (data as TeamStandingEnvelope).standings
  if (url === '/api/v1/teams') return Array.isArray(data) ? data : (data as TeamEnvelope).teams
  if (/^\/api\/v1\/teams\/\d+\/players$/.test(url)) return Array.isArray(data) ? data : (data as RosterEnvelope).players
  if (url.startsWith('/api/v1/players/search')) return Array.isArray(data) ? data : (data as PlayerSearchEnvelope).players
  if (url.startsWith('/api/v1/news')) return Array.isArray(data) ? data : (data as NewsEnvelope).news
  return data
}

export const endpoints = {
  games: (date: string) => `/api/v1/games?date=${encodeURIComponent(date)}`,
  teams: () => '/api/v1/teams',
  standings: (season: string) => `/api/v1/teams/standings?season=${encodeURIComponent(season)}`,
  roster: (teamId: number) => `/api/v1/teams/${teamId}/players`,
  search: (name: string) => `/api/v1/players/search?name=${encodeURIComponent(name)}`,
  player: (playerId: number) => `/api/v1/players/${playerId}`,
  playerStats: (playerId: number, season: string, group = 'hitting') => `/api/v1/players/${playerId}/stats?season=${encodeURIComponent(season)}&group=${encodeURIComponent(group)}`,
  news: (limit = 8) => `/api/v1/news?limit=${limit}`,
  authRegister: () => '/api/v1/auth/register',
  authLogin: () => '/api/v1/auth/login',
  authMe: () => '/api/v1/auth/me',
  allStarStatus: () => '/api/v1/all-star/votes/me',
  allStarVotes: () => '/api/v1/all-star/votes',
  gamePick: (gamePk: number) => `/api/v1/games/${gamePk}/pick`,
  gamePickSummary: (gamePk: number) => `/api/v1/games/${gamePk}/pick-summary`,
  myGamePicks: () => '/api/v1/games/picks/me',
  preferences: () => '/api/v1/users/me/preferences',
}

export function login(email: string, password: string) {
  return apiRequest<AuthResponse>(endpoints.authLogin(), {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
}

export function register(email: string, displayName: string, password: string) {
  return apiRequest<AuthResponse>(endpoints.authRegister(), {
    method: 'POST',
    body: JSON.stringify({ email, displayName, password }),
  })
}

export function currentUser(token: string) {
  return apiRequest<CurrentUser>(endpoints.authMe(), {}, token)
}

export function fetchAllStarStatus(token: string) {
  return apiRequest<AllStarVoteStatus>(endpoints.allStarStatus(), {}, token)
}

export function submitAllStarVote(token: string, selections: AllStarSelection[]) {
  return apiRequest<AllStarBallot>(endpoints.allStarVotes(), {
    method: 'POST',
    body: JSON.stringify({ selections }),
  }, token)
}

export function fetchGamePick(token: string, gamePk: number) {
  return apiRequest<GamePick | undefined>(endpoints.gamePick(gamePk), {}, token)
}

export function submitGamePick(token: string, gamePk: number, gameDate: string, pickedTeamId: number) {
  return apiRequest<GamePick>(endpoints.gamePick(gamePk), {
    method: 'POST',
    body: JSON.stringify({ gameDate, pickedTeamId }),
  }, token)
}

export function fetchGamePickSummary(gamePk: number) {
  return apiRequest<GamePickSummary>(endpoints.gamePickSummary(gamePk))
}

export async function fetchMyGamePicks(token: string) {
  const response = await apiRequest<GamePickListEnvelope>(endpoints.myGamePicks(), {}, token)
  return response.picks
}

export function fetchPreferences(token: string) {
  return apiRequest<UserPreference>(endpoints.preferences(), {}, token)
}

export function updatePreferences(token: string, favoriteTeamId: number) {
  return apiRequest<UserPreference>(endpoints.preferences(), {
    method: 'PUT',
    body: JSON.stringify({ favoriteTeamId }),
  }, token)
}

export type {
  AllStarBallot,
  AllStarSelection,
  AllStarVoteStatus,
  AuthResponse,
  CurrentUser,
  Game,
  GamePick,
  GamePickSummary,
  NewsItem,
  Player,
  PlayerStats,
  Team,
  TeamPlayer,
  TeamStanding,
  UserPreference,
}
