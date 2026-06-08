"use client"

import useSWR from "swr"
import { useState } from "react"
import { fetcher, endpoints, type Player, type PlayerStats } from "@/lib/api"
import { LoadingState, ErrorState, EmptyState } from "@/components/states"
import { PlayerAvatar } from "@/components/media"
import { cn } from "@/lib/utils"
import { BarChart3, Search, TrendingUp } from "lucide-react"

export function PlayersSection() {
  const [input, setInput] = useState("")
  const [query, setQuery] = useState("")
  const [selectedId, setSelectedId] = useState<number | null>(null)

  const {
    data: results,
    error,
    isLoading,
    mutate,
  } = useSWR<Player[]>(query ? endpoints.search(query) : null, fetcher, {
    revalidateOnFocus: false,
  })

  function submit(e: React.FormEvent) {
    e.preventDefault()
    setSelectedId(null)
    setQuery(input.trim())
  }

  const list = results ?? []

  return (
    <section aria-labelledby="players-heading" className="space-y-6">
      <div>
        <p className="text-xs font-semibold uppercase tracking-wider text-primary">PLAYERS</p>
        <h1 id="players-heading" className="mt-1 text-2xl font-bold tracking-tight text-foreground">
          선수 검색
        </h1>
        <p className="mt-1 text-sm text-muted-foreground">
          선수 이름(영문)으로 검색하세요. 예: Mike Trout, Shohei Ohtani
        </p>
      </div>

      <form onSubmit={submit} className="flex max-w-xl gap-2">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" aria-hidden="true" />
          <input
            type="search"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="선수 이름 입력"
            aria-label="선수 이름 입력"
            className="w-full rounded-md border border-input bg-secondary py-2.5 pl-9 pr-3 text-sm text-foreground placeholder:text-muted-foreground focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
          />
        </div>
        <button
          type="submit"
          disabled={!input.trim()}
          className="rounded-md bg-primary px-5 py-2.5 text-sm font-semibold text-primary-foreground transition-colors hover:bg-primary/90 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring disabled:cursor-not-allowed disabled:opacity-50"
        >
          검색
        </button>
      </form>

      {!query && (
        <EmptyState message="검색어를 입력하고 검색 버튼을 누르면 결과가 표시됩니다." />
      )}

      {query && (
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-[1fr_1fr]">
          <div>
            {isLoading && <LoadingState label="선수를 검색하는 중..." />}
            {error && (
              <ErrorState message="선수 검색에 실패했습니다. 잠시 후 다시 시도해 주세요." onRetry={() => mutate()} />
            )}
            {!isLoading && !error && list.length === 0 && (
              <EmptyState message="검색 결과가 없습니다. 선수 이름을 다시 입력해 주세요." />
            )}
            {!isLoading && !error && list.length > 0 && (
              <div className="space-y-2">
                <p className="text-xs text-muted-foreground">
                  검색 결과 <span className="font-mono font-semibold text-foreground">{list.length}</span>명
                </p>
                <ul className="space-y-2">
                  {list.map((p) => {
                    const isActive = selectedId === p.id
                    return (
                      <li key={p.id}>
                        <button
                          onClick={() => setSelectedId(p.id)}
                          aria-pressed={isActive}
                          className={cn(
                            "flex w-full items-center gap-4 rounded-lg border p-4 text-left transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring",
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
                              {p.birthCountry ? ` · ${p.birthCountry}` : ""}
                            </p>
                          </div>
                        </button>
                      </li>
                    )
                  })}
                </ul>
              </div>
            )}
          </div>

          <div className="lg:sticky lg:top-20 lg:self-start">
            <PlayerDetailPanel playerId={selectedId} />
          </div>
        </div>
      )}
    </section>
  )
}

function PlayerDetailPanel({ playerId }: { playerId: number | null }) {
  if (!playerId) {
    return (
      <div className="rounded-xl border border-dashed border-border bg-card/50 p-8">
        <EmptyState message="검색 결과에서 선수를 선택하면 상세 정보가 표시됩니다." />
      </div>
    )
  }
  return <PlayerDetail playerId={playerId} key={playerId} />
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
  } = useSWR<PlayerStats>(endpoints.playerStats(playerId, currentSeason), fetcher, {
    revalidateOnFocus: false,
  })

  if (isLoading) {
    return (
      <div className="rounded-xl border border-border bg-card p-6">
        <LoadingState label="선수 정보를 불러오는 중..." />
      </div>
    )
  }
  if (error || !data) {
    return (
      <div className="rounded-xl border border-border bg-card p-6">
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
    <article className="overflow-hidden rounded-xl border border-border bg-card">
      <div className="flex items-center gap-4 border-b border-border bg-secondary/40 p-6">
        <PlayerAvatar playerId={data.id} name={data.fullName} size={240} className="size-16 shrink-0 text-lg" />
        <div className="min-w-0">
          <p className="text-xs font-semibold uppercase tracking-wider text-primary">Player profile</p>
          <h2 className="truncate text-xl font-bold tracking-tight text-foreground">{data.fullName}</h2>
          <p className="mt-0.5 font-mono text-xs text-muted-foreground">ID #{data.id}</p>
        </div>
      </div>
      <dl className="grid grid-cols-2 gap-px bg-border">
        {facts.map((f) => (
          <div key={f.label} className="bg-card p-4">
            <dt className="text-xs text-muted-foreground">{f.label}</dt>
            <dd className="mt-1 text-sm font-semibold text-foreground">{f.value}</dd>
          </div>
        ))}
      </dl>
      <SeasonHittingStats
        season={currentSeason}
        stats={stats}
        isLoading={statsLoading}
        hasError={Boolean(statsError)}
        onRetry={() => mutateStats()}
      />
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
    <section aria-labelledby="player-stats-heading" className="border-t border-border p-6">
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

      <div className="rounded-xl border border-border">
        <div className="flex items-center gap-2 border-b border-border bg-secondary/40 px-4 py-3 text-sm font-semibold text-foreground">
          <TrendingUp className="size-4 text-primary" aria-hidden="true" />
          주요 타격 성적 전체 보기
        </div>
        <dl className="grid grid-cols-2 gap-px bg-border sm:grid-cols-3">
          {rows.map(([label, value]) => (
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
