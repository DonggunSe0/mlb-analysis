"use client"

import useSWR from "swr"
import { useMemo } from "react"
import { fetcher, endpoints, type Game } from "@/lib/api"
import { StatusBadge, getStatusInfo } from "@/components/status-badge"
import { LoadingState, ErrorState, EmptyState } from "@/components/states"
import { TeamLogo } from "@/components/media"
import { cn } from "@/lib/utils"

function todayStr() {
  return new Date().toISOString().slice(0, 10)
}

export function GamesSection({ date, onDateChange }: { date: string; onDateChange: (d: string) => void }) {
  const { data, error, isLoading, mutate } = useSWR<Game[]>(endpoints.games(date), fetcher, {
    revalidateOnFocus: false,
  })

  const games = useMemo(() => data ?? [], [data])

  const stats = useMemo(() => {
    const totalRuns = games.reduce((sum, g) => sum + (g.homeScore ?? 0) + (g.awayScore ?? 0), 0)
    const buckets: Record<string, number> = {}
    for (const g of games) {
      const { label } = getStatusInfo(g.status)
      buckets[label] = (buckets[label] ?? 0) + 1
    }
    return { totalRuns, buckets }
  }, [games])

  const formattedDate = useMemo(() => {
    const d = new Date(date + "T00:00:00")
    return d.toLocaleDateString("ko-KR", { year: "numeric", month: "long", day: "numeric", weekday: "long" })
  }, [date])

  return (
    <section aria-labelledby="games-heading" className="space-y-6">
      {/* 히어로 / 요약 */}
      <div className="rounded-xl border border-border bg-card p-6">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div>
            <p className="text-xs font-semibold uppercase tracking-wider text-primary">SCOREBOARD</p>
            <h1 id="games-heading" className="mt-1 text-2xl font-bold tracking-tight text-balance text-foreground">
              오늘의 경기
            </h1>
            <p className="mt-1 text-sm text-muted-foreground">{formattedDate}</p>
          </div>
          <div className="flex flex-col gap-1.5">
            <label htmlFor="game-date" className="text-xs font-medium text-muted-foreground">
              날짜 선택
            </label>
            <div className="flex items-center gap-2">
              <input
                id="game-date"
                type="date"
                value={date}
                max="2030-12-31"
                onChange={(e) => onDateChange(e.target.value)}
                className="rounded-lg border border-input bg-secondary px-3 py-2 text-sm text-foreground focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
              />
              <button
                onClick={() => onDateChange(todayStr())}
                className="rounded-md border border-border bg-secondary px-3 py-2 text-sm font-medium text-secondary-foreground transition-colors hover:bg-accent focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
              >
                오늘
              </button>
            </div>
          </div>
        </div>

        {/* 핵심 지표 */}
        <div className="mt-6 grid grid-cols-2 gap-3 sm:grid-cols-4">
          <Metric label="총 경기" value={isLoading ? "—" : String(games.length)} />
          <Metric label="총 득점" value={isLoading ? "—" : String(stats.totalRuns)} />
          <Metric
            label="경기 중"
            value={isLoading ? "—" : String(stats.buckets["경기 중"] ?? 0)}
            accent
          />
          <Metric label="경기 종료" value={isLoading ? "—" : String(stats.buckets["경기 종료"] ?? 0)} />
        </div>
      </div>

      {/* 상태 분포 시각화 */}
      {!isLoading && !error && games.length > 0 && <StatusDistribution buckets={stats.buckets} total={games.length} />}

      {/* 스코어보드 */}
      {isLoading && <LoadingState label="경기 데이터를 불러오는 중..." />}
      {error && (
        <ErrorState
          message="경기 데이터를 불러오지 못했습니다. 백엔드 서버 상태를 확인해 주세요."
          onRetry={() => mutate()}
        />
      )}
      {!isLoading && !error && games.length === 0 && (
        <EmptyState message="해당 날짜에 예정된 경기가 없습니다. 다른 날짜를 선택해 주세요." />
      )}
      {!isLoading && !error && games.length > 0 && (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
          {games.map((g) => (
            <GameCard key={g.gamePk} game={g} />
          ))}
        </div>
      )}
    </section>
  )
}

function Metric({ label, value, accent }: { label: string; value: string; accent?: boolean }) {
  return (
    <div className="rounded-lg border border-border bg-secondary/50 px-4 py-3">
      <p className="text-xs text-muted-foreground">{label}</p>
      <p className={cn("mt-1 font-mono text-2xl font-bold tabular-nums", accent ? "text-primary" : "text-foreground")}>
        {value}
      </p>
    </div>
  )
}

function StatusDistribution({ buckets, total }: { buckets: Record<string, number>; total: number }) {
  const entries = Object.entries(buckets).sort((a, b) => b[1] - a[1])
  const colors: Record<string, string> = {
    "경기 중": "bg-primary",
    "경기 종료": "bg-muted-foreground",
    "경기 예정": "bg-chart-2",
    "연기/취소": "bg-chart-3",
  }
  return (
    <div className="rounded-xl border border-border bg-card p-5">
      <h2 className="text-sm font-semibold text-foreground">경기 상태 분포</h2>
      <div className="mt-3 flex h-3 w-full overflow-hidden rounded-full bg-secondary" role="img" aria-label="경기 상태 비율">
        {entries.map(([label, count]) => (
          <div
            key={label}
            className={cn(colors[label] ?? "bg-chart-5")}
            style={{ width: `${(count / total) * 100}%` }}
            title={`${label} ${count}경기`}
          />
        ))}
      </div>
      <ul className="mt-3 flex flex-wrap gap-x-4 gap-y-1.5">
        {entries.map(([label, count]) => (
          <li key={label} className="flex items-center gap-1.5 text-xs text-muted-foreground">
            <span className={cn("size-2.5 rounded-sm", colors[label] ?? "bg-chart-5")} aria-hidden="true" />
            {label} <span className="font-mono font-semibold text-foreground">{count}</span>
          </li>
        ))}
      </ul>
    </div>
  )
}

function GameCard({ game }: { game: Game }) {
  const { tone } = getStatusInfo(game.status)
  const isFinal = tone === "final"
  const homeWin = isFinal && (game.homeScore ?? 0) > (game.awayScore ?? 0)
  const awayWin = isFinal && (game.awayScore ?? 0) > (game.homeScore ?? 0)
  const time = new Date(game.gameDate).toLocaleTimeString("ko-KR", { hour: "2-digit", minute: "2-digit" })

  return (
    <article className="rounded-xl border border-border bg-card p-4 transition-colors hover:border-primary/40">
      <div className="flex items-center justify-between">
        <StatusBadge status={game.status} />
        <span className="font-mono text-xs text-muted-foreground">{time}</span>
      </div>
      <div className="mt-3 space-y-1">
        <TeamRow name={game.awayTeam} teamId={game.awayTeamId} score={game.awayScore} win={awayWin} suffix="원정" />
        <TeamRow name={game.homeTeam} teamId={game.homeTeamId} score={game.homeScore} win={homeWin} suffix="홈" />
      </div>
    </article>
  )
}

function TeamRow({
  name,
  teamId,
  score,
  win,
  suffix,
}: {
  name: string | null
  teamId: number | null
  score: number | null
  win: boolean
  suffix: string
}) {
  return (
    <div className="flex items-center justify-between gap-3 rounded-lg px-2 py-1.5">
      <div className="flex min-w-0 items-center gap-2.5">
        <TeamLogo teamId={teamId} name={name} className="size-7 shrink-0" />
        <span className={cn("truncate text-sm", win ? "font-bold text-foreground" : "text-foreground/90")}>
          {name ?? "미정"}
        </span>
        <span className="shrink-0 rounded bg-secondary px-1.5 py-0.5 text-[10px] font-medium text-muted-foreground">
          {suffix}
        </span>
      </div>
      <span
        className={cn(
          "font-mono text-lg font-bold tabular-nums",
          score === null ? "text-muted-foreground" : win ? "text-primary" : "text-foreground",
        )}
      >
        {score ?? "-"}
      </span>
    </div>
  )
}
