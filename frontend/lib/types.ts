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

export type TeamStanding = {
  teamId: number | null
  teamName: string | null
  season: string
  leagueName: string | null
  divisionName: string | null
  divisionRank: number | null
  leagueRank: number | null
  gamesPlayed: number | null
  wins: number | null
  losses: number | null
  winningPercentage: string | null
  gamesBack: string | null
  wildCardGamesBack: string | null
  runDifferential: number | null
  divisionLeader: boolean | null
}

export type Player = {
  id: number
  fullName: string
  birthCountry: string | null
  currentAge: number | null
  primaryPosition: string | null
  batSide: string | null
  pitchHand: string | null
  headshotUrl?: string | null
}

export type PlayerStats = {
  playerId: number
  season: string
  group: string
  gamesPlayed: number | null
  plateAppearances: number | null
  atBats: number | null
  runs: number | null
  hits: number | null
  doubles: number | null
  triples: number | null
  homeRuns: number | null
  rbi: number | null
  baseOnBalls: number | null
  intentionalWalks: number | null
  strikeOuts: number | null
  avg: string | null
  obp: string | null
  slg: string | null
  ops: string | null
  stolenBases: number | null
  caughtStealing: number | null
  stolenBasePercentage: string | null
  totalBases: number | null
  hitByPitch: number | null
  groundIntoDoublePlay: number | null
  sacBunts: number | null
  sacFlies: number | null
  numberOfPitches: number | null
  babip: string | null
  groundOuts: number | null
  airOuts: number | null
  gamesStarted: number | null
  era: string | null
  inningsPitched: string | null
  wins: number | null
  losses: number | null
  saves: number | null
  holds: number | null
  blownSaves: number | null
  earnedRuns: number | null
  whip: string | null
  battersFaced: number | null
  gamesPitched: number | null
  completeGames: number | null
  shutouts: number | null
  strikePercentage: string | null
  wildPitches: number | null
  pitchesPerInning: string | null
  strikeoutWalkRatio: string | null
  strikeoutsPer9Inn: string | null
  walksPer9Inn: string | null
  hitsPer9Inn: string | null
  homeRunsPer9: string | null
}

export type NewsItem = {
  title: string | null
  link: string | null
  summary: string | null
  publishedAt: string | null
}

export type CurrentUser = {
  id: number
  email: string
  displayName: string
}

export type AuthResponse = {
  token: string
  expiresAt: string
  user: CurrentUser
}

export type AllStarSelection = {
  positionKey: string
  playerId: number
  playerName: string
  teamName: string | null
}

export type AllStarBallot = {
  id: number
  voteDate: string
  createdAt: string
  selections: AllStarSelection[]
}

export type AllStarVoteStatus = {
  canVote: boolean
  voteDate: string
  ballot: AllStarBallot | null
}
