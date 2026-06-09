"use client"

import useSWR from "swr"
import { useEffect, useId, useMemo, useRef, useState } from "react"
import { fetcher, endpoints, type Player, type PlayerBrowseResponse, type PlayerStats, type Team } from "@/lib/api"
import { LoadingState, ErrorState, EmptyState } from "@/components/states"
import { PlayerAvatar } from "@/components/media"
import { cn } from "@/lib/utils"
import { BarChart3, Search, TrendingUp, X } from "lucide-react"

const PLAYER_PAGE_SIZE = 20

type PlayerFilters = {
  country: string
  teamId: string
  position: string
}

export function PlayersSection() {
  const [input, setInput] = useState("")
  const [query, setQuery] = useState("")
  const [filters, setFilters] = useState<PlayerFilters>({ country: "", teamId: "", position: "" })
  const [page, setPage] = useState(0)
  const [selectedId, setSelectedId] = useState<number | null>(null)

  const browseUrl = useMemo(
    () => endpoints.players({
      page,
      size: PLAYER_PAGE_SIZE,
      q: query,
      country: filters.country,
      teamId: filters.teamId,
      position: filters.position,
    }),
    [filters.country, filters.position, filters.teamId, page, query],
  )

  const {
    data: browse,
    error,
    isLoading,
    mutate,
  } = useSWR<PlayerBrowseResponse>(browseUrl, fetcher, {
    revalidateOnFocus: false,
  })
  const { data: teams } = useSWR<Team[]>(endpoints.teams(), fetcher, {
    revalidateOnFocus: false,
  })

  function submit(e: React.FormEvent) {
    e.preventDefault()
    setSelectedId(null)
    setPage(0)
    setQuery(input.trim())
  }

  function updateFilter<K extends keyof PlayerFilters>(key: K, value: PlayerFilters[K]) {
    setSelectedId(null)
    setPage(0)
    setFilters((prev) => ({ ...prev, [key]: value }))
  }

  function clearFilters() {
    setInput("")
    setQuery("")
    setPage(0)
    setSelectedId(null)
    setFilters({ country: "", teamId: "", position: "" })
  }

  const list = browse?.players ?? []
  const teamNameById = useMemo(() => new Map((teams ?? []).map((team) => [team.id, team.name])), [teams])
  const filterTeams = useMemo(() => {
    const ids = new Set((browse?.teams ?? []).map((team) => team.id))
    const fromTeamEndpoint = (teams ?? [])
      .filter((team) => ids.has(team.id))
      .map((team) => ({ id: team.id, name: team.name }))
    if (fromTeamEndpoint.length > 0) return fromTeamEndpoint
    return browse?.teams ?? []
  }, [browse?.teams, teams])
  const hasActiveFilters = Boolean(query || filters.country || filters.teamId || filters.position)

  return (
    <section aria-labelledby="players-heading" className="space-y-6">
      <div>
        <p className="text-xs font-semibold uppercase tracking-wider text-primary">PLAYERS</p>
        <h1 id="players-heading" className="mt-1 text-2xl font-bold tracking-tight text-foreground">
          선수 탐색
        </h1>
        <p className="mt-1 text-sm text-muted-foreground">
          검색어 없이도 A-Z 순으로 20명씩 둘러보고, 국가·팀·포지션 필터로 좁혀볼 수 있습니다.
        </p>
      </div>

      <div className="space-y-3 rounded-2xl border border-border bg-card p-4 shadow-sm">
        <form onSubmit={submit} className="flex flex-col gap-2 sm:flex-row">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" aria-hidden="true" />
            <input
              type="search"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="선수 이름 입력 (선택)"
              aria-label="선수 이름 입력"
              className="w-full rounded-md border border-input bg-secondary py-2.5 pl-9 pr-3 text-sm text-foreground placeholder:text-muted-foreground focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
            />
          </div>
          <button
            type="submit"
            className="rounded-md bg-primary px-5 py-2.5 text-sm font-semibold text-primary-foreground transition-colors hover:bg-primary/90 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
          >
            검색/적용
          </button>
          {hasActiveFilters && (
            <button
              type="button"
              onClick={clearFilters}
              className="rounded-md border border-border bg-secondary px-4 py-2.5 text-sm font-semibold text-secondary-foreground transition-colors hover:bg-accent focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
            >
              초기화
            </button>
          )}
        </form>

        <div className="grid grid-cols-1 gap-2 md:grid-cols-3">
          <label className="space-y-1 text-xs font-medium text-muted-foreground">
            국가
            <select
              value={filters.country}
              onChange={(e) => updateFilter("country", e.target.value)}
              className="w-full rounded-md border border-input bg-secondary px-3 py-2 text-sm text-foreground"
            >
              <option value="">전체 국가</option>
              {(browse?.countries ?? []).map((country) => <option key={country} value={country}>{country}</option>)}
            </select>
          </label>
          <label className="space-y-1 text-xs font-medium text-muted-foreground">
            팀
            <select
              value={filters.teamId}
              onChange={(e) => updateFilter("teamId", e.target.value)}
              className="w-full rounded-md border border-input bg-secondary px-3 py-2 text-sm text-foreground"
            >
              <option value="">전체 팀</option>
              {filterTeams.map((team) => <option key={team.id} value={String(team.id)}>{team.name ?? `팀 #${team.id}`}</option>)}
            </select>
          </label>
          <label className="space-y-1 text-xs font-medium text-muted-foreground">
            포지션
            <select
              value={filters.position}
              onChange={(e) => updateFilter("position", e.target.value)}
              className="w-full rounded-md border border-input bg-secondary px-3 py-2 text-sm text-foreground"
            >
              <option value="">전체 포지션</option>
              {(browse?.positions ?? []).map((position) => <option key={position} value={position}>{position}</option>)}
            </select>
          </label>
        </div>
      </div>

      <div className="min-w-0">
        {isLoading && <LoadingState label="선수 목록을 불러오는 중..." />}
        {error && (
          <ErrorState message="선수 목록을 불러오지 못했습니다. 백엔드 서버 또는 MLB API 상태를 확인해 주세요." onRetry={() => mutate()} />
        )}
        {!isLoading && !error && list.length === 0 && (
          <EmptyState message="조건에 맞는 선수가 없습니다. 검색어나 필터를 조정해 주세요." />
        )}
        {!isLoading && !error && list.length > 0 && browse && (
          <div className="space-y-4">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <p className="text-xs text-muted-foreground">
                A-Z 선수 목록 <span className="font-mono font-semibold text-foreground">{browse.totalElements}</span>명 · {browse.size}명씩 보기 · 선수를 누르면 상세 팝업이 열립니다.
              </p>
              <div className="flex items-center gap-2 text-xs text-muted-foreground">
                <button
                  type="button"
                  disabled={browse.first}
                  onClick={() => setPage((prev) => Math.max(0, prev - 1))}
                  className="rounded-md border border-border bg-secondary px-3 py-1.5 font-semibold text-secondary-foreground disabled:cursor-not-allowed disabled:opacity-50"
                >
                  이전
                </button>
                <span className="font-mono font-semibold text-foreground">{browse.totalPages === 0 ? 0 : browse.page + 1} / {browse.totalPages}</span>
                <button
                  type="button"
                  disabled={browse.last}
                  onClick={() => setPage((prev) => prev + 1)}
                  className="rounded-md border border-border bg-secondary px-3 py-1.5 font-semibold text-secondary-foreground disabled:cursor-not-allowed disabled:opacity-50"
                >
                  다음
                </button>
              </div>
            </div>
            <ul className="grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-3">
              {list.map((p) => {
                const isActive = selectedId === p.id
                return (
                  <li key={p.id}>
                    <button
                      onClick={() => setSelectedId(p.id)}
                      aria-haspopup="dialog"
                      aria-pressed={isActive}
                      className={cn(
                        "flex h-full w-full items-center gap-4 rounded-xl border p-4 text-left shadow-sm transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring",
                        isActive
                          ? "border-primary bg-primary/10"
                          : "border-border bg-card hover:border-primary/40 hover:bg-accent",
                      )}
                    >
                      <PlayerAvatar playerId={p.id} name={p.fullName} size={180} className="size-16 shrink-0 text-sm" />
                      <div className="min-w-0">
                        <p className="truncate text-base font-semibold text-foreground">{p.fullName}</p>
                        <p className="mt-1 truncate text-sm text-muted-foreground">
                          {p.primaryPosition ?? "포지션 정보 없음"}
                          {teamLabel(p, teamNameById) ? ` · ${teamLabel(p, teamNameById)}` : ""}
                          {p.birthCountry ? ` · ${p.birthCountry}` : ""}
                        </p>
                        <p className="mt-2 text-xs font-medium text-primary">상세 보기</p>
                      </div>
                    </button>
                  </li>
                )
              })}
            </ul>
          </div>
        )}
      </div>

      <PlayerDetailDialog playerId={selectedId} onClose={() => setSelectedId(null)} />
    </section>
  )
}

export function PlayerDetailDialog({ playerId, onClose }: { playerId: number | null; onClose: () => void }) {
  const titleId = useId()
  const closeButtonRef = useRef<HTMLButtonElement>(null)

  useEffect(() => {
    if (!playerId) return

    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = "hidden"
    closeButtonRef.current?.focus()

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") onClose()
    }

    window.addEventListener("keydown", handleKeyDown)
    return () => {
      document.body.style.overflow = previousOverflow
      window.removeEventListener("keydown", handleKeyDown)
    }
  }, [playerId, onClose])

  if (!playerId) return null

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby={titleId}
      className="fixed inset-0 z-50 flex items-end justify-center bg-foreground/45 p-0 backdrop-blur-sm sm:items-center sm:p-6"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) onClose()
      }}
    >
      <div className="max-h-[92vh] w-full overflow-hidden rounded-t-3xl border border-border bg-card shadow-2xl sm:max-w-3xl sm:rounded-3xl">
        <div className="flex items-center justify-between gap-3 border-b border-border bg-card/95 px-5 py-4 backdrop-blur">
          <div>
            <p className="text-xs font-semibold uppercase tracking-wider text-primary">Player detail</p>
            <h2 id={titleId} className="text-lg font-bold text-foreground">선수 상세보기</h2>
          </div>
          <button
            ref={closeButtonRef}
            type="button"
            onClick={onClose}
            aria-label="선수 상세보기 닫기"
            className="rounded-full border border-border bg-secondary p-2 text-muted-foreground transition-colors hover:bg-accent hover:text-foreground focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
          >
            <X className="size-5" aria-hidden="true" />
          </button>
        </div>
        <div className="max-h-[calc(92vh-4.5rem)] overflow-y-auto overscroll-contain">
          <PlayerDetail playerId={playerId} />
        </div>
      </div>
    </div>
  )
}

function PlayerDetail({ playerId }: { playerId: number }) {
  const currentSeason = String(new Date().getFullYear())
  const { data, error, isLoading, mutate } = useSWR<Player>(endpoints.player(playerId), fetcher, {
    revalidateOnFocus: false,
  })
  const {
    data: stats,
    error: statsError,
    isLoading: statsLoading,
    mutate: mutateStats,
  } = useSWR<PlayerStats>(endpoints.playerStats(playerId, currentSeason, "hitting"), fetcher, {
    revalidateOnFocus: false,
  })
  const {
    data: pitchingStats,
    error: pitchingStatsError,
    isLoading: pitchingStatsLoading,
    mutate: mutatePitchingStats,
  } = useSWR<PlayerStats>(endpoints.playerStats(playerId, currentSeason, "pitching"), fetcher, {
    revalidateOnFocus: false,
  })

  if (isLoading) {
    return (
      <div className="p-6">
        <LoadingState label="선수 정보를 불러오는 중..." />
      </div>
    )
  }
  if (error || !data) {
    return (
      <div className="p-6">
        <ErrorState message="선수 정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요." onRetry={() => mutate()} />
      </div>
    )
  }

  const facts: { label: string; value: string }[] = [
    { label: "주 포지션", value: data.primaryPosition ?? "정보 없음" },
    { label: "나이", value: data.currentAge != null ? `${data.currentAge}세` : "정보 없음" },
    { label: "출신 국가", value: data.birthCountry ?? "정보 없음" },
    { label: "타격", value: data.batSide ?? "정보 없음" },
    { label: "투구", value: data.pitchHand ?? "정보 없음" },
  ]

  return (
    <article>
      <div className="relative border-b border-border bg-gradient-to-br from-primary/12 via-card to-secondary p-6">
        <div className="flex flex-col gap-5 sm:flex-row sm:items-end">
          <PlayerAvatar playerId={data.id} name={data.fullName} size={320} className="size-24 shrink-0 border-4 border-card text-2xl shadow-sm" />
          <div className="min-w-0 flex-1">
            <p className="text-xs font-semibold uppercase tracking-wider text-primary">Player profile</p>
            <h3 className="mt-1 text-3xl font-black tracking-tight text-foreground sm:truncate">{data.fullName}</h3>
            <div className="mt-3 flex flex-wrap gap-2 text-xs font-medium">
              <span className="rounded-full bg-card/85 px-3 py-1 text-foreground shadow-sm">ID #{data.id}</span>
              <span className="rounded-full bg-primary/10 px-3 py-1 text-primary">{data.primaryPosition ?? "포지션 정보 없음"}</span>
              <span className="rounded-full bg-secondary px-3 py-1 text-secondary-foreground">{data.birthCountry ?? "국가 정보 없음"}</span>
            </div>
          </div>
        </div>
      </div>
      <dl className="grid grid-cols-2 gap-3 p-4 sm:grid-cols-3">
        {facts.map((f) => (
          <div key={f.label} className="rounded-xl border border-border bg-secondary/40 p-4">
            <dt className="text-xs text-muted-foreground">{f.label}</dt>
            <dd className="mt-1 text-sm font-semibold text-foreground">{f.value}</dd>
          </div>
        ))}
      </dl>
      <div className="space-y-0">
        <SeasonHittingStats
          season={currentSeason}
          stats={stats}
          isLoading={statsLoading}
          hasError={Boolean(statsError)}
          onRetry={() => mutateStats()}
        />
        <SeasonPitchingStats
          season={currentSeason}
          stats={pitchingStats}
          isLoading={pitchingStatsLoading}
          hasError={Boolean(pitchingStatsError)}
          onRetry={() => mutatePitchingStats()}
        />
      </div>
    </article>
  )
}

function SeasonHittingStats({
  season,
  stats,
  isLoading,
  hasError,
  onRetry,
}: {
  season: string
  stats: PlayerStats | undefined
  isLoading: boolean
  hasError: boolean
  onRetry: () => void
}) {
  const hasStats = Boolean(stats && (stats.gamesPlayed != null || stats.atBats != null || stats.ops != null))

  return (
    <section aria-labelledby="player-stats-heading" className="border-t border-border p-4 sm:p-6">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <p className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-primary">
            <BarChart3 className="size-4" aria-hidden="true" />
            Season hitting
          </p>
          <h3 id="player-stats-heading" className="mt-1 text-lg font-bold text-foreground">
            {season} 현재 타격 기록
          </h3>
          <p className="mt-1 text-xs text-muted-foreground">MLB Stats API의 시즌 hitting 기록 기준입니다.</p>
        </div>
        {hasStats && (
          <span className="rounded-full border border-primary/25 bg-primary/10 px-3 py-1 text-xs font-semibold text-primary">
            G {formatStat(stats?.gamesPlayed)} · PA {formatStat(stats?.plateAppearances)}
          </span>
        )}
      </div>

      {isLoading && <LoadingState label="현재 시즌 타격 기록을 불러오는 중..." className="py-8" />}
      {hasError && (
        <ErrorState
          message="선수 성적을 불러오지 못했습니다. 백엔드 서버 또는 MLB API 상태를 확인해 주세요."
          onRetry={onRetry}
          className="mt-4 py-8"
        />
      )}
      {!isLoading && !hasError && !hasStats && (
        <EmptyState message="현재 시즌에 표시할 타격 기록이 없습니다." className="py-8" />
      )}
      {!isLoading && !hasError && hasStats && stats && <HittingStatsTable stats={stats} />}
    </section>
  )
}

function SeasonPitchingStats({
  season,
  stats,
  isLoading,
  hasError,
  onRetry,
}: {
  season: string
  stats: PlayerStats | undefined
  isLoading: boolean
  hasError: boolean
  onRetry: () => void
}) {
  const hasStats = Boolean(stats && (stats.gamesPitched != null || stats.era != null || stats.inningsPitched != null))

  return (
    <section aria-labelledby="player-pitching-heading" className="border-t border-border p-4 sm:p-6">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <p className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-primary">
            <BarChart3 className="size-4" aria-hidden="true" />
            Season pitching
          </p>
          <h3 id="player-pitching-heading" className="mt-1 text-lg font-bold text-foreground">
            {season} 현재 투수 기록
          </h3>
          <p className="mt-1 text-xs text-muted-foreground">투수 출전 기록이 없으면 비어있게 표시됩니다.</p>
        </div>
        {hasStats && (
          <span className="rounded-full border border-primary/25 bg-primary/10 px-3 py-1 text-xs font-semibold text-primary">
            ERA {formatStat(stats?.era)} · WHIP {formatStat(stats?.whip)}
          </span>
        )}
      </div>

      {isLoading && <LoadingState label="현재 시즌 투수 기록을 불러오는 중..." className="py-8" />}
      {hasError && (
        <ErrorState
          message="투수 성적을 불러오지 못했습니다. 백엔드 서버 또는 MLB API 상태를 확인해 주세요."
          onRetry={onRetry}
          className="mt-4 py-8"
        />
      )}
      {!isLoading && !hasError && !hasStats && <EmptyState message="현재 시즌에 표시할 투수 기록이 없습니다." className="py-8" />}
      {!isLoading && !hasError && hasStats && stats && <PitchingStatsTable stats={stats} />}
    </section>
  )
}

function PitchingStatsTable({ stats }: { stats: PlayerStats }) {
  const headline = [
    { label: "ERA", value: stats.era, hint: "평균자책" },
    { label: "WHIP", value: stats.whip, hint: "이닝당 출루" },
    { label: "K/9", value: stats.strikeoutsPer9Inn, hint: "9이닝당 삼진" },
    { label: "IP", value: stats.inningsPitched, hint: "이닝" },
  ]
  const rows = [
    ["경기", stats.gamesPitched],
    ["선발", stats.gamesStarted],
    ["승", stats.wins],
    ["패", stats.losses],
    ["세이브", stats.saves],
    ["홀드", stats.holds],
    ["블론", stats.blownSaves],
    ["이닝", stats.inningsPitched],
    ["자책점", stats.earnedRuns],
    ["피안타", stats.hits],
    ["피홈런", stats.homeRuns],
    ["볼넷", stats.baseOnBalls],
    ["삼진", stats.strikeOuts],
    ["타자 상대", stats.battersFaced],
    ["완투", stats.completeGames],
    ["완봉", stats.shutouts],
    ["스트라이크%", stats.strikePercentage],
    ["폭투", stats.wildPitches],
    ["투구/이닝", stats.pitchesPerInning],
    ["K/BB", stats.strikeoutWalkRatio],
    ["BB/9", stats.walksPer9Inn],
    ["H/9", stats.hitsPer9Inn],
    ["HR/9", stats.homeRunsPer9],
  ] as const
  const visibleRows = rows.filter(([, value]) => value != null && value !== "")

  return (
    <div className="mt-5 space-y-5">
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        {headline.map((item) => (
          <div key={item.label} className="rounded-xl border border-border bg-secondary/50 p-4">
            <dt className="text-xs font-semibold text-muted-foreground">{item.label}</dt>
            <dd className="mt-1 text-2xl font-black tracking-tight text-foreground">{formatStat(item.value)}</dd>
            <p className="mt-1 text-[11px] text-muted-foreground">{item.hint}</p>
          </div>
        ))}
      </div>
      <dl className="grid overflow-hidden rounded-xl border border-border bg-border sm:grid-cols-3">
        {visibleRows.map(([label, value]) => (
          <div key={label} className="bg-card px-4 py-3">
            <dt className="text-xs text-muted-foreground">{label}</dt>
            <dd className="mt-1 font-mono text-sm font-semibold text-foreground">{formatStat(value)}</dd>
          </div>
        ))}
      </dl>
    </div>
  )
}

function HittingStatsTable({ stats }: { stats: PlayerStats }) {
  const headline = [
    { label: "AVG", value: stats.avg, hint: "타율" },
    { label: "OPS", value: stats.ops, hint: "출루율+장타율" },
    { label: "HR", value: stats.homeRuns, hint: "홈런" },
    { label: "RBI", value: stats.rbi, hint: "타점" },
  ]
  const rows = [
    ["경기", stats.gamesPlayed],
    ["타석", stats.plateAppearances],
    ["타수", stats.atBats],
    ["득점", stats.runs],
    ["안타", stats.hits],
    ["2루타", stats.doubles],
    ["3루타", stats.triples],
    ["홈런", stats.homeRuns],
    ["타점", stats.rbi],
    ["볼넷", stats.baseOnBalls],
    ["고의4구", stats.intentionalWalks],
    ["삼진", stats.strikeOuts],
    ["출루율", stats.obp],
    ["장타율", stats.slg],
    ["OPS", stats.ops],
    ["총루타", stats.totalBases],
    ["도루", stats.stolenBases],
    ["도루 실패", stats.caughtStealing],
    ["도루 성공률", stats.stolenBasePercentage],
    ["사구", stats.hitByPitch],
    ["병살타", stats.groundIntoDoublePlay],
    ["희생번트", stats.sacBunts],
    ["희생플라이", stats.sacFlies],
    ["투구 수", stats.numberOfPitches],
    ["BABIP", stats.babip],
    ["땅볼 아웃", stats.groundOuts],
    ["뜬공 아웃", stats.airOuts],
  ] as const

  const visibleRows = rows.filter(([, value]) => value != null && value !== "")

  return (
    <div className="mt-5 space-y-5">
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        {headline.map((item) => (
          <div key={item.label} className="rounded-xl border border-border bg-secondary/50 p-4">
            <dt className="text-xs font-semibold text-muted-foreground">{item.label}</dt>
            <dd className="mt-1 flex items-baseline gap-1 text-2xl font-black tracking-tight text-foreground">
              {formatStat(item.value)}
            </dd>
            <p className="mt-1 text-[11px] text-muted-foreground">{item.hint}</p>
          </div>
        ))}
      </div>

      <div className="overflow-hidden rounded-xl border border-border">
        <div className="sticky top-0 z-10 flex items-center gap-2 border-b border-border bg-secondary/95 px-4 py-3 text-sm font-semibold text-foreground backdrop-blur">
          <TrendingUp className="size-4 text-primary" aria-hidden="true" />
          주요 타격 성적 전체 보기
        </div>
        <dl className="grid max-h-80 grid-cols-2 gap-px overflow-y-auto overscroll-contain bg-border sm:grid-cols-3">
          {visibleRows.map(([label, value]) => (
            <div key={label} className="bg-card px-4 py-3">
              <dt className="text-xs text-muted-foreground">{label}</dt>
              <dd className="mt-1 font-mono text-sm font-semibold text-foreground">{formatStat(value)}</dd>
            </div>
          ))}
        </dl>
      </div>
    </div>
  )
}

function formatStat(value: number | string | null | undefined) {
  if (value == null || value === "") return "-"
  return String(value)
}


function teamLabel(player: Player, teamNameById: Map<number, string>) {
  if (player.currentTeamName) return player.currentTeamName
  if (player.currentTeamId == null) return null
  return teamNameById.get(player.currentTeamId) ?? `팀 #${player.currentTeamId}`
}
