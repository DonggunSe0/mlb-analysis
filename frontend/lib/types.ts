export type Game = {
  gamePk: number
  gameDate: string
  status: string | null
  homeTeam: string | null
  awayTeam: string | null
  homeTeamId: number | null
  awayTeamId: number | null
  homeScore: number | null
  awayScore: number | null
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

export type TeamPlayer = {
  playerId: number | null
  fullName: string | null
  jerseyNumber: string | null
  position: string | null
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
