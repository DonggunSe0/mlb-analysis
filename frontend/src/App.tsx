import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import {
  fetchGames,
  fetchTeamPlayers,
  fetchTeams,
  searchPlayers,
  type Game,
  type Player,
  type Team,
  type TeamPlayer,
} from './api'

function todayText() {
  return new Date().toISOString().slice(0, 10)
}

type LoadState = 'idle' | 'loading' | 'success' | 'error'

function App() {
  const [date, setDate] = useState(todayText())
  const [games, setGames] = useState<Game[]>([])
  const [gamesState, setGamesState] = useState<LoadState>('loading')
  const [gamesError, setGamesError] = useState('')

  const [teams, setTeams] = useState<Team[]>([])
  const [teamsState, setTeamsState] = useState<LoadState>('loading')
  const [selectedTeamId, setSelectedTeamId] = useState<number | null>(null)

  const [roster, setRoster] = useState<TeamPlayer[]>([])
  const [rosterState, setRosterState] = useState<LoadState>('idle')

  const [searchName, setSearchName] = useState('Mike Trout')
  const [players, setPlayers] = useState<Player[]>([])
  const [selectedPlayer, setSelectedPlayer] = useState<Player | null>(null)
  const [searchState, setSearchState] = useState<LoadState>('idle')
  const [searchError, setSearchError] = useState('')

  useEffect(() => {
    fetchGames(date)
      .then((response) => {
        setGames(response.games)
        setGamesState('success')
      })
      .catch(() => {
        setGamesError('경기 데이터를 불러오지 못했습니다. 백엔드 서버와 외부 MLB API 상태를 확인해 주세요.')
        setGamesState('error')
      })
  }, [date])

  useEffect(() => {
    fetchTeams()
      .then((response) => {
        setTeams(response.teams)
        setRosterState(response.teams[0]?.id ? 'loading' : 'idle')
        setSelectedTeamId((current) => current ?? response.teams[0]?.id ?? null)
        setTeamsState('success')
      })
      .catch(() => setTeamsState('error'))
  }, [])

  useEffect(() => {
    if (!selectedTeamId) return
    fetchTeamPlayers(selectedTeamId)
      .then((response) => {
        setRoster(response.players)
        setRosterState('success')
      })
      .catch(() => setRosterState('error'))
  }, [selectedTeamId])

  function handleDateChange(nextDate: string) {
    setGamesState('loading')
    setGamesError('')
    setDate(nextDate)
  }

  function handleTeamSelect(teamId: number) {
    setRosterState('loading')
    setSelectedTeamId(teamId)
  }

  async function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const keyword = searchName.trim()
    if (!keyword) return

    setSearchState('loading')
    setSearchError('')
    try {
      const response = await searchPlayers(keyword)
      setPlayers(response.players)
      setSelectedPlayer(response.players[0] ?? null)
      setSearchState('success')
    } catch {
      setSearchError('선수 검색에 실패했습니다. 이름을 다시 확인하거나 잠시 후 재시도해 주세요.')
      setSearchState('error')
    }
  }

  const selectedTeam = teams.find((team) => team.id === selectedTeamId) ?? null
  const completedGames = games.filter((game) => game.homeScore !== null || game.awayScore !== null).length
  const totalRuns = games.reduce((sum, game) => sum + (game.homeScore ?? 0) + (game.awayScore ?? 0), 0)

  return (
    <main className="min-h-screen bg-mlb-navy text-slate-950">
      <Header />

      <section className="mx-auto grid w-full max-w-7xl grid-cols-[1.35fr_0.65fr] gap-6 px-6 pb-10 pt-8 max-lg:grid-cols-1">
        <HeroSummary date={date} games={games} completedGames={completedGames} totalRuns={totalRuns} />
        <QuickPanel teamsCount={teams.length} selectedTeam={selectedTeam} selectedPlayer={selectedPlayer} />
      </section>

      <section id="games" className="mx-auto grid w-full max-w-7xl grid-cols-[1fr_360px] gap-6 px-6 pb-8 max-lg:grid-cols-1">
        <section className="section-card overflow-hidden">
          <div className="flex items-center justify-between gap-4 border-b border-slate-200 bg-slate-50 px-6 py-5">
            <div>
              <p className="eyebrow">Today&apos;s slate</p>
              <h2 className="mt-1 text-2xl font-black tracking-tight">오늘의 경기</h2>
            </div>
            <label className="flex items-center gap-2 text-sm font-bold text-slate-600">
              날짜
              <input
                type="date"
                className="focus-ring rounded-xl border border-slate-300 bg-white px-3 py-2 text-slate-900"
                value={date}
                onChange={(event) => handleDateChange(event.target.value)}
              />
            </label>
          </div>
          <StateBlock state={gamesState} error={gamesError} empty={games.length === 0} emptyText="이 날짜에는 표시할 경기 데이터가 없습니다.">
            <div className="grid grid-cols-2 gap-4 p-5 max-xl:grid-cols-1">
              {games.map((game) => (
                <GameCard key={game.gamePk} game={game} />
              ))}
            </div>
          </StateBlock>
        </section>

        <section className="section-card p-6">
          <p className="eyebrow">Visualization</p>
          <h2 className="mt-1 text-2xl font-black tracking-tight">경기 상태 분석</h2>
          <StatusDistribution games={games} />
          <div className="mt-6 rounded-2xl bg-mlb-navy p-5 text-white">
            <p className="text-sm text-slate-300">총 득점</p>
            <p className="mt-2 text-5xl font-black text-white">{totalRuns}</p>
            <p className="mt-2 text-sm text-slate-300">선택 날짜 기준 양 팀 합산 점수</p>
          </div>
        </section>
      </section>

      <section id="teams" className="mx-auto grid w-full max-w-7xl grid-cols-[380px_1fr] gap-6 px-6 pb-8 max-lg:grid-cols-1">
        <section className="section-card overflow-hidden">
          <div className="border-b border-slate-200 bg-slate-50 px-6 py-5">
            <p className="eyebrow">Teams</p>
            <h2 className="mt-1 text-2xl font-black tracking-tight">팀 탐색</h2>
          </div>
          <StateBlock state={teamsState} empty={teams.length === 0} emptyText="팀 데이터를 찾을 수 없습니다.">
            <div className="max-h-[620px] overflow-auto p-3">
              {teams.map((team) => (
                <button
                  key={team.id}
                  type="button"
                  onClick={() => handleTeamSelect(team.id)}
                  className={`focus-ring mb-2 w-full rounded-xl border px-4 py-3 text-left transition ${
                    selectedTeamId === team.id
                      ? 'border-mlb-red bg-red-50 text-mlb-red'
                      : 'border-slate-200 bg-white hover:border-slate-300 hover:bg-slate-50'
                  }`}
                >
                  <div className="flex items-center justify-between gap-3">
                    <span className="font-black">{team.name}</span>
                    <span className="rounded-full bg-slate-900 px-2 py-1 text-xs font-black text-white">{team.abbreviation ?? 'MLB'}</span>
                  </div>
                  <p className="mt-1 text-sm text-slate-500">{team.divisionName ?? team.leagueName ?? '리그 정보 없음'}</p>
                </button>
              ))}
            </div>
          </StateBlock>
        </section>

        <section className="section-card overflow-hidden">
          <div className="flex items-start justify-between gap-4 border-b border-slate-200 bg-slate-50 px-6 py-5">
            <div>
              <p className="eyebrow">Roster</p>
              <h2 className="mt-1 text-2xl font-black tracking-tight">{selectedTeam?.name ?? '팀'} 선수 명단</h2>
              <p className="mt-1 text-sm text-slate-500">{selectedTeam?.venueName ?? '팀을 선택하면 선수 목록을 불러옵니다.'}</p>
            </div>
            <PositionChart roster={roster} />
          </div>
          <StateBlock state={rosterState} empty={roster.length === 0} emptyText="선수 명단이 비어 있습니다.">
            <RosterTable roster={roster} />
          </StateBlock>
        </section>
      </section>

      <section id="players" className="mx-auto grid w-full max-w-7xl grid-cols-[420px_1fr] gap-6 px-6 pb-12 max-lg:grid-cols-1">
        <section className="section-card p-6">
          <p className="eyebrow">Player finder</p>
          <h2 className="mt-1 text-2xl font-black tracking-tight">선수 이름 검색</h2>
          <form className="mt-5 flex gap-2" onSubmit={handleSearch}>
            <label className="sr-only" htmlFor="player-search">선수 이름</label>
            <input
              id="player-search"
              className="focus-ring min-w-0 flex-1 rounded-xl border border-slate-300 px-4 py-3"
              value={searchName}
              onChange={(event) => setSearchName(event.target.value)}
              placeholder="예: Mike Trout"
            />
            <button
              className="focus-ring rounded-xl bg-mlb-red px-5 py-3 font-black text-white disabled:cursor-not-allowed disabled:bg-slate-300"
              disabled={!searchName.trim() || searchState === 'loading'}
              type="submit"
            >
              검색
            </button>
          </form>
          <StateBlock state={searchState} error={searchError} empty={searchState === 'success' && players.length === 0} emptyText="검색 결과가 없습니다.">
            <div className="mt-5 space-y-2">
              {players.map((player) => (
                <button
                  key={player.id}
                  type="button"
                  onClick={() => setSelectedPlayer(player)}
                  className={`focus-ring w-full rounded-xl border px-4 py-3 text-left transition ${
                    selectedPlayer?.id === player.id ? 'border-mlb-red bg-red-50' : 'border-slate-200 hover:bg-slate-50'
                  }`}
                >
                  <p className="font-black">{player.fullName}</p>
                  <p className="text-sm text-slate-500">{player.primaryPosition ?? '포지션 없음'} · {player.birthCountry ?? '국가 정보 없음'}</p>
                </button>
              ))}
            </div>
          </StateBlock>
        </section>

        <section className="section-card p-6">
          <p className="eyebrow">Player profile</p>
          <h2 className="mt-1 text-2xl font-black tracking-tight">선수 상세</h2>
          {selectedPlayer ? <PlayerDetail player={selectedPlayer} /> : <EmptyMessage text="선수 이름을 검색하고 결과를 선택하세요." />}
        </section>
      </section>
    </main>
  )
}

function Header() {
  return (
    <header className="sticky top-0 z-20 border-b border-white/10 bg-mlb-navy/95 px-6 py-4 text-white backdrop-blur">
      <div className="mx-auto flex max-w-7xl items-center justify-between gap-6">
        <a href="#games" className="focus-ring flex items-center gap-3 rounded-lg">
          <span className="grid h-10 w-10 place-items-center rounded bg-mlb-red text-xl font-black">M</span>
          <div>
            <p className="text-lg font-black leading-none">MLB Analysis</p>
            <p className="text-xs text-slate-300">Korean sports data MVP</p>
          </div>
        </a>
        <nav className="flex gap-2 text-sm font-bold text-slate-200">
          <a className="focus-ring rounded-full px-4 py-2 hover:bg-white/10" href="#games">오늘의 경기</a>
          <a className="focus-ring rounded-full px-4 py-2 hover:bg-white/10" href="#teams">팀</a>
          <a className="focus-ring rounded-full px-4 py-2 hover:bg-white/10" href="#players">선수 검색</a>
        </nav>
      </div>
    </header>
  )
}

function HeroSummary({ date, games, completedGames, totalRuns }: { date: string; games: Game[]; completedGames: number; totalRuns: number }) {
  return (
    <section className="rounded-3xl bg-gradient-to-br from-white via-slate-50 to-red-50 p-8 shadow-scoreboard">
      <p className="eyebrow">MLB Analysis MVP</p>
      <h1 className="mt-3 max-w-3xl text-5xl font-black leading-tight tracking-tight text-slate-950">
        오늘의 MLB 흐름을 한눈에 보는 스포츠 데이터 허브
      </h1>
      <p className="mt-4 max-w-2xl text-base leading-7 text-slate-600">
        경기 스코어보드, 팀 로스터, 선수 이름 검색을 백엔드 MLB Stats API 연동으로 제공합니다.
      </p>
      <div className="mt-8 grid grid-cols-3 gap-4">
        <Metric label="기준 날짜" value={date} />
        <Metric label="경기 수" value={`${games.length}경기`} />
        <Metric label="종료/득점" value={`${completedGames} / ${totalRuns}`} />
      </div>
    </section>
  )
}

function QuickPanel({ teamsCount, selectedTeam, selectedPlayer }: { teamsCount: number; selectedTeam: Team | null; selectedPlayer: Player | null }) {
  return (
    <aside className="rounded-3xl border border-white/10 bg-mlb-panel p-6 text-white shadow-scoreboard">
      <p className="text-xs font-black uppercase tracking-[0.28em] text-red-300">Live board</p>
      <h2 className="mt-2 text-2xl font-black">빠른 요약</h2>
      <div className="mt-6 space-y-4">
        <DarkStat label="등록 팀" value={`${teamsCount}개`} />
        <DarkStat label="선택 팀" value={selectedTeam?.name ?? '선택 전'} />
        <DarkStat label="검색 선수" value={selectedPlayer?.fullName ?? '검색 전'} />
      </div>
    </aside>
  )
}

function GameCard({ game }: { game: Game }) {
  const homeWon = (game.homeScore ?? -1) > (game.awayScore ?? -1)
  const awayWon = (game.awayScore ?? -1) > (game.homeScore ?? -1)
  return (
    <article className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-center justify-between gap-3">
        <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-black text-slate-600">#{game.gamePk}</span>
        <span className="rounded-full bg-red-50 px-3 py-1 text-xs font-black text-mlb-red">{game.status ?? '상태 없음'}</span>
      </div>
      <div className="mt-5 space-y-3">
        <ScoreLine team={game.awayTeam ?? 'Away'} score={game.awayScore} winner={awayWon} />
        <ScoreLine team={game.homeTeam ?? 'Home'} score={game.homeScore} winner={homeWon} />
      </div>
      <p className="mt-4 text-xs text-slate-500">{new Date(game.gameDate).toLocaleString('ko-KR')}</p>
    </article>
  )
}

function ScoreLine({ team, score, winner }: { team: string; score: number | null; winner: boolean }) {
  return (
    <div className={`flex items-center justify-between rounded-xl px-4 py-3 ${winner ? 'bg-slate-950 text-white' : 'bg-slate-50 text-slate-700'}`}>
      <span className="font-black">{team}</span>
      <span className="text-2xl font-black">{score ?? '-'}</span>
    </div>
  )
}

function StatusDistribution({ games }: { games: Game[] }) {
  const rows = useMemo(() => {
    const counts = games.reduce<Record<string, number>>((acc, game) => {
      const status = game.status ?? '상태 없음'
      acc[status] = (acc[status] ?? 0) + 1
      return acc
    }, {})
    return Object.entries(counts).sort((a, b) => b[1] - a[1])
  }, [games])

  if (rows.length === 0) return <EmptyMessage text="시각화할 경기 데이터가 없습니다." />

  return (
    <div className="mt-5 space-y-4">
      {rows.map(([status, count]) => {
        const width = `${Math.max(8, (count / games.length) * 100)}%`
        return (
          <div key={status}>
            <div className="mb-1 flex justify-between text-sm font-bold text-slate-600">
              <span>{status}</span>
              <span>{count}경기</span>
            </div>
            <div className="h-3 overflow-hidden rounded-full bg-slate-100">
              <div className="h-full rounded-full bg-mlb-red" style={{ width }} />
            </div>
          </div>
        )
      })}
    </div>
  )
}

function PositionChart({ roster }: { roster: TeamPlayer[] }) {
  const top = useMemo(() => {
    const counts = roster.reduce<Record<string, number>>((acc, player) => {
      const position = player.position ?? '미정'
      acc[position] = (acc[position] ?? 0) + 1
      return acc
    }, {})
    return Object.entries(counts).sort((a, b) => b[1] - a[1]).slice(0, 3)
  }, [roster])

  return (
    <div className="hidden min-w-48 space-y-2 xl:block">
      {top.map(([position, count]) => (
        <div key={position} className="flex items-center justify-between rounded-full bg-white px-3 py-1 text-xs font-black text-slate-600">
          <span>{position}</span>
          <span>{count}</span>
        </div>
      ))}
    </div>
  )
}

function RosterTable({ roster }: { roster: TeamPlayer[] }) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full border-collapse text-left text-sm">
        <thead className="bg-slate-950 text-white">
          <tr>
            <th className="px-5 py-3">선수</th>
            <th className="px-5 py-3">등번호</th>
            <th className="px-5 py-3">포지션</th>
            <th className="px-5 py-3">ID</th>
          </tr>
        </thead>
        <tbody>
          {roster.map((player, index) => (
            <tr key={`${player.playerId ?? 'unknown'}-${index}`} className="border-b border-slate-100 last:border-0">
              <td className="px-5 py-3 font-bold">{player.fullName ?? '이름 없음'}</td>
              <td className="px-5 py-3">{player.jerseyNumber ?? '-'}</td>
              <td className="px-5 py-3">{player.position ?? '-'}</td>
              <td className="px-5 py-3 text-slate-500">{player.playerId ?? '-'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

function PlayerDetail({ player }: { player: Player }) {
  return (
    <div className="mt-5 grid grid-cols-[220px_1fr] gap-6 max-lg:grid-cols-1">
      <div className="rounded-3xl bg-gradient-to-br from-mlb-red to-mlb-blue p-6 text-white">
        <p className="text-sm font-bold text-white/80">#{player.id}</p>
        <h3 className="mt-3 text-4xl font-black leading-tight">{player.fullName}</h3>
        <p className="mt-4 rounded-full bg-white/15 px-4 py-2 text-sm font-bold">{player.primaryPosition ?? '포지션 없음'}</p>
      </div>
      <div className="grid grid-cols-2 gap-4 max-md:grid-cols-1">
        <Metric label="국가" value={player.birthCountry ?? '-'} />
        <Metric label="나이" value={player.currentAge === null ? '-' : `${player.currentAge}세`} />
        <Metric label="타격" value={player.batSide ?? '-'} />
        <Metric label="투구" value={player.pitchHand ?? '-'} />
      </div>
    </div>
  )
}

function StateBlock({ state, error, empty, emptyText, children }: { state: LoadState; error?: string; empty: boolean; emptyText: string; children: React.ReactNode }) {
  if (state === 'loading') return <div className="p-6 text-sm font-bold text-slate-500">데이터를 불러오는 중입니다...</div>
  if (state === 'error') return <div className="m-5 rounded-2xl border border-red-200 bg-red-50 p-5 text-sm font-bold text-red-700">{error ?? '데이터를 불러오지 못했습니다.'}</div>
  if (empty) return <EmptyMessage text={emptyText} />
  return <>{children}</>
}

function EmptyMessage({ text }: { text: string }) {
  return <div className="m-5 rounded-2xl border border-dashed border-slate-300 bg-slate-50 p-6 text-sm font-bold text-slate-500">{text}</div>
}

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-4">
      <p className="text-xs font-bold uppercase tracking-widest text-slate-500">{label}</p>
      <p className="mt-2 text-2xl font-black text-slate-950">{value}</p>
    </div>
  )
}

function DarkStat({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
      <p className="text-xs font-bold uppercase tracking-widest text-slate-400">{label}</p>
      <p className="mt-2 text-xl font-black text-white">{value}</p>
    </div>
  )
}

export default App
