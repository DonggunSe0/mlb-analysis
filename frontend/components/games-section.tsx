"use client"

import useSWR from "swr"
import { useMemo, useState } from "react"
import {
  AUTH_TOKEN_KEY,
  endpoints,
  fetcher,
  fetchPreferences,
  fetchGamePick,
  fetchGamePickSummary,
  fetchMyGamePicks,
  submitGamePick,
  type Game,
  type GamePick,
  type GamePickSummary,
  type UserPreference,
} from "@/lib/api"
import { StatusBadge, getStatusInfo } from "@/components/status-badge"
import { LoadingState, ErrorState, EmptyState } from "@/components/states"
import { TeamLogo } from "@/components/media"
import { cn } from "@/lib/utils"
import { CheckCircle2, Flag, Lock, Sparkles } from "lucide-react"

function todayStr() {
  return new Date().toISOString().slice(0, 10)
}

export function GamesSection({ date, onDateChange }: { date: string; onDateChange: (d: string) => void }) {
  const [token] = useState<string | null>(() =>
    typeof window === "undefined" ? null : localStorage.getItem(AUTH_TOKEN_KEY),
  )
  const [pickVersion, setPickVersion] = useState(0)
  const { data, error, isLoading, mutate } = useSWR<Game[]>(endpoints.games(date), fetcher, {
    revalidateOnFocus: false,
  })
  const { data: preferences } = useSWR<UserPreference>(
    token ? ["preferences", token] : null,
    () => fetchPreferences(token!),
    { revalidateOnFocus: false },
  )

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

      {!isLoading && !error && games.length > 0 && (
        <PickCenter
          games={games}
          token={token}
          favoriteTeamId={preferences?.favoriteTeamId ?? null}
          favoriteTeamName={preferences?.favoriteTeamName ?? null}
          pickVersion={pickVersion}
        />
      )}

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
            <GameCard
              key={g.gamePk}
              game={g}
              favoriteTeamId={preferences?.favoriteTeamId ?? null}
              onPickSaved={() => setPickVersion((value) => value + 1)}
            />
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

function PickCenter({
  games,
  token,
  favoriteTeamId,
  favoriteTeamName,
  pickVersion,
}: {
  games: Game[]
  token: string | null
  favoriteTeamId: number | null
  favoriteTeamName: string | null
  pickVersion: number
}) {
  const { data: myPicks } = useSWR<GamePick[]>(
    token ? ["my-game-picks", token, pickVersion] : null,
    () => fetchMyGamePicks(token!),
    { revalidateOnFocus: false },
  )
  const { data: summaries } = useSWR<GamePickSummary[]>(
    games.length > 0 ? ["game-pick-summaries", games.map((game) => game.gamePk).join(","), pickVersion] : null,
    () => Promise.all(games.map((game) => fetchGamePickSummary(game.gamePk))),
    { revalidateOnFocus: false },
  )

  const picksByGame = useMemo(() => new Map((myPicks ?? []).map((pick) => [pick.gamePk, pick])), [myPicks])
  const summariesByGame = useMemo(() => new Map((summaries ?? []).map((summary) => [summary.gamePk, summary])), [summaries])
  const pickedGames = games.filter((game) => picksByGame.has(game.gamePk))
  const pendingCount = pickedGames.filter((game) => getStatusInfo(game.status).tone !== "final").length
  const successCount = pickedGames.filter((game) => {
    const pick = picksByGame.get(game.gamePk)
    return Boolean(pick && getStatusInfo(game.status).tone === "final" && winningTeamId(game) === pick.pickedTeamId)
  }).length
  const favoriteGames = favoriteTeamId
    ? games.filter((game) => game.homeTeamId === favoriteTeamId || game.awayTeamId === favoriteTeamId)
    : []
  const featuredGames = pickedGames.length > 0 ? pickedGames.slice(0, 3) : games.slice(0, 3)

  return (
    <div className="rounded-xl border border-border bg-card p-5">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wider text-primary">응원 센터</p>
          <h2 className="mt-1 text-lg font-bold text-foreground">내 응원 센터</h2>
          <p className="mt-1 text-sm text-muted-foreground">
            오늘 경기에서 응원할 팀을 고르고 결과를 함께 확인해 보세요.
          </p>
        </div>
        {favoriteGames.length > 0 && (
          <span className="inline-flex items-center gap-1.5 rounded-md bg-primary/10 px-3 py-2 text-xs font-bold text-primary">
            <Flag className="size-3.5" aria-hidden="true" />
            내 팀 경기 {favoriteGames.length}
          </span>
        )}
      </div>

      {!token ? (
        <div className="mt-4 rounded-lg border border-dashed border-border bg-secondary/40 p-4">
          <div className="flex items-start gap-3">
            <span className="flex size-10 shrink-0 items-center justify-center rounded-full bg-secondary text-muted-foreground">
              <Lock className="size-5" aria-hidden="true" />
            </span>
            <div>
              <p className="text-sm font-bold text-foreground">로그인이 필요합니다</p>
              <p className="mt-1 text-sm text-muted-foreground">
                로그인하면 오늘의 응원팀을 한곳에서 모아볼 수 있습니다.
              </p>
            </div>
          </div>
        </div>
      ) : (
        <>
          <div className="mt-5 grid grid-cols-2 gap-3 sm:grid-cols-4">
            <Metric label="오늘 응원" value={String(pickedGames.length)} accent />
            <Metric label="결과 대기" value={String(pendingCount)} />
            <Metric label="응원 성공" value={String(successCount)} />
            <Metric label="내 팀 경기" value={String(favoriteGames.length)} />
          </div>

          {favoriteGames.length > 0 && (
            <div className="mt-4 rounded-lg border border-primary/20 bg-primary/5 px-4 py-3">
              <p className="text-sm font-bold text-foreground">내 팀 경기가 오늘 있어요</p>
              <p className="mt-1 text-xs text-muted-foreground">
                {favoriteTeamName ?? "내 팀"} 경기를 카드에서 찾아 응원팀을 남겨보세요.
              </p>
            </div>
          )}

          {pickedGames.length === 0 ? (
            <div className="mt-4">
              <EmptyState message="아직 저장한 응원팀이 없습니다. 경기 카드에서 응원할 팀을 골라보세요." />
            </div>
          ) : (
            <div className="mt-4 overflow-hidden rounded-lg border border-border">
              <div className="grid grid-cols-[1.4fr_1fr_1fr] gap-3 bg-secondary px-3 py-2 text-xs font-semibold text-muted-foreground">
                <span>경기</span>
                <span>내 응원팀</span>
                <span className="text-right">결과</span>
              </div>
              <div className="divide-y divide-border">
                {pickedGames.slice(0, 4).map((game) => {
                  const pick = picksByGame.get(game.gamePk)!
                  const result = pickResult(game, pick)
                  return (
                    <div key={game.gamePk} className="grid grid-cols-[1.4fr_1fr_1fr] items-center gap-3 px-3 py-2 text-sm">
                      <div className="min-w-0">
                        <p className="truncate font-semibold text-foreground">{game.awayTeam ?? "원정"} @ {game.homeTeam ?? "홈"}</p>
                        <p className="text-xs text-muted-foreground">{getStatusInfo(game.status).label}</p>
                      </div>
                      <span className="truncate text-xs font-semibold text-foreground">{pick.pickedTeamName}</span>
                      <span className={cn("justify-self-end rounded px-2 py-1 text-[11px] font-bold", result.className)}>
                        {result.label}
                      </span>
                    </div>
                  )
                })}
              </div>
            </div>
          )}
        </>
      )}

      <div className="mt-4 grid grid-cols-1 gap-3 md:grid-cols-3">
        {featuredGames.map((game) => {
          const summary = summariesByGame.get(game.gamePk)
          const leader = summary?.teams.find((team) => team.leading) ?? summary?.teams[0]
          return (
            <div key={game.gamePk} className="rounded-lg border border-border bg-secondary/40 p-3">
              <div className="flex items-center gap-2 text-xs font-bold text-foreground">
                <Sparkles className="size-3.5 text-primary" aria-hidden="true" />
                팬 응원 흐름
              </div>
              <p className="mt-2 truncate text-sm font-semibold text-foreground">{game.awayTeam ?? "원정"} @ {game.homeTeam ?? "홈"}</p>
              {leader && summary && summary.totalPicks > 0 ? (
                <p className="mt-1 text-xs text-muted-foreground">
                  {leader.pickedTeamName} 쪽으로 {leader.pickCount}명이 응원 중
                </p>
              ) : (
                <p className="mt-1 text-xs text-muted-foreground">아직 응원 선택이 없습니다.</p>
              )}
            </div>
          )
        })}
      </div>
    </div>
  )
}

function GameCard({
  game,
  favoriteTeamId,
  onPickSaved,
}: {
  game: Game
  favoriteTeamId: number | null
  onPickSaved: () => void
}) {
  const { tone } = getStatusInfo(game.status)
  const isFinal = tone === "final"
  const homeWin = isFinal && (game.homeScore ?? 0) > (game.awayScore ?? 0)
  const awayWin = isFinal && (game.awayScore ?? 0) > (game.homeScore ?? 0)
  const time = new Date(game.gameDate).toLocaleTimeString("ko-KR", { hour: "2-digit", minute: "2-digit" })
  const hasFavoriteTeam = favoriteTeamId === game.homeTeamId || favoriteTeamId === game.awayTeamId

  return (
    <article
      className={cn(
        "rounded-xl border bg-card p-4 transition-colors hover:border-primary/40",
        hasFavoriteTeam ? "border-primary/60 shadow-sm shadow-primary/10" : "border-border",
      )}
    >
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <StatusBadge status={game.status} />
          {hasFavoriteTeam && (
            <span className="rounded bg-primary/10 px-1.5 py-0.5 text-[10px] font-bold text-primary">내 팀</span>
          )}
        </div>
        <span className="font-mono text-xs text-muted-foreground">{time}</span>
      </div>
      <div className="mt-3 space-y-1">
        <TeamRow name={game.awayTeam} teamId={game.awayTeamId} score={game.awayScore} win={awayWin} suffix="원정" />
        <TeamRow name={game.homeTeam} teamId={game.homeTeamId} score={game.homeScore} win={homeWin} suffix="홈" />
      </div>
      <GamePickControls game={game} onPickSaved={onPickSaved} />
    </article>
  )
}

function GamePickControls({ game, onPickSaved }: { game: Game; onPickSaved: () => void }) {
  const [token] = useState<string | null>(() =>
    typeof window === "undefined" ? null : localStorage.getItem(AUTH_TOKEN_KEY),
  )
  const [message, setMessage] = useState<string | null>(null)
  const { data: pick, mutate, isLoading } = useSWR<GamePick | undefined>(
    token ? ["game-pick", game.gamePk, token] : null,
    () => fetchGamePick(token!, game.gamePk),
    { revalidateOnFocus: false },
  )

  async function choose(teamId: number | null, teamName: string | null) {
    if (!token || !teamId || !teamName) return
    setMessage(null)
    try {
      const saved = await submitGamePick(token, game.gamePk, teamId, teamName)
      await mutate(saved, { revalidate: false })
      onPickSaved()
      setMessage(`${teamName} 응원팀이 저장됐습니다.`)
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "응원팀 저장에 실패했습니다.")
    }
  }

  if (!token) {
    return (
      <div className="mt-4 rounded-lg border border-dashed border-border bg-secondary/40 px-3 py-2 text-xs text-muted-foreground">
        <span className="inline-flex items-center gap-1.5">
          <Lock className="size-3.5" aria-hidden="true" />
          올스타 투표에서 로그인하면 경기에서 응원할 팀을 남길 수 있습니다.
        </span>
      </div>
    )
  }

  const options = [
    { id: game.awayTeamId, name: game.awayTeam, label: "원정" },
    { id: game.homeTeamId, name: game.homeTeam, label: "홈" },
  ]

  return (
    <div className="mt-4 border-t border-border pt-3">
      <div className="flex items-center justify-between gap-2">
        <p className="text-xs font-semibold text-muted-foreground">응원 팀 선택</p>
        {pick && (
          <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-primary">
            <CheckCircle2 className="size-3.5" aria-hidden="true" />
            선택됨
          </span>
        )}
      </div>
      <div className="mt-2 grid grid-cols-2 gap-2">
        {options.map((option) => {
          const selected = pick?.pickedTeamId === option.id
          return (
            <button
              key={option.label}
              type="button"
              disabled={!option.id || !option.name || isLoading}
              onClick={() => choose(option.id, option.name)}
              className={cn(
                "min-h-10 rounded-md border px-2 py-2 text-left text-xs font-semibold transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring disabled:cursor-not-allowed disabled:opacity-50",
                selected
                  ? "border-primary bg-primary/10 text-primary"
                  : "border-border bg-secondary text-secondary-foreground hover:bg-accent",
              )}
            >
              <span className="block text-[10px] text-muted-foreground">{option.label}</span>
              <span className="block truncate">{option.name ?? "미정"}</span>
            </button>
          )
        })}
      </div>
      {(message || pick) && (
        <p className="mt-2 truncate text-xs text-muted-foreground">
          {message ?? `${pick?.pickedTeamName} 선택 중`}
        </p>
      )}
    </div>
  )
}

function winningTeamId(game: Game) {
  if ((game.homeScore ?? 0) === (game.awayScore ?? 0)) return null
  return (game.homeScore ?? 0) > (game.awayScore ?? 0) ? game.homeTeamId : game.awayTeamId
}

function pickResult(game: Game, pick: GamePick) {
  if (getStatusInfo(game.status).tone !== "final") {
    return { label: "결과 대기", className: "bg-secondary text-secondary-foreground" }
  }
  return winningTeamId(game) === pick.pickedTeamId
    ? { label: "응원 성공", className: "bg-primary/10 text-primary" }
    : { label: "아쉬운 결과", className: "bg-muted text-muted-foreground" }
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
