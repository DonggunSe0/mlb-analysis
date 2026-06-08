export type Game = {
  gamePk: number
  gameDate: string
  status: string | null
  homeTeam: string | null
  awayTeam: string | null
  homeScore: number | null
  awayScore: number | null
}

export type GameListResponse = {
  games: Game[]
}

export type Team = {
  id: number
  name: string
  abbreviation: string | null
  teamName: string | null
  locationName: string | null
  leagueName: string | null
  divisionName: string | null
  venueName: string | null
  active: boolean | null
}

export type TeamListResponse = {
  teams: Team[]
}

export type TeamPlayer = {
  playerId: number | null
  fullName: string | null
  jerseyNumber: string | null
  position: string | null
}

export type TeamPlayerListResponse = {
  teamId: number
  players: TeamPlayer[]
}

export type Player = {
  id: number
  fullName: string
  birthCountry: string | null
  currentAge: number | null
  primaryPosition: string | null
  batSide: string | null
  pitchHand: string | null
}

export type PlayerSearchResponse = {
  name: string
  players: Player[]
}

async function request<T>(path: string): Promise<T> {
  const response = await fetch(path)
  if (!response.ok) {
    throw new Error(`API 요청 실패 (${response.status})`)
  }
  return response.json() as Promise<T>
}

export function fetchGames(date: string) {
  return request<GameListResponse>(`/api/v1/games?date=${encodeURIComponent(date)}`)
}

export function fetchTeams() {
  return request<TeamListResponse>('/api/v1/teams')
}

export function fetchTeamPlayers(teamId: number) {
  return request<TeamPlayerListResponse>(`/api/v1/teams/${teamId}/players`)
}

export function searchPlayers(name: string) {
  return request<PlayerSearchResponse>(`/api/v1/players/search?name=${encodeURIComponent(name)}`)
}
