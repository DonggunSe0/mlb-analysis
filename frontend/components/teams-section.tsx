"use client"

import useSWR from "swr"
import { useMemo, useState } from "react"
import {
  AUTH_TOKEN_KEY,
  endpoints,
  fetcher,
  fetchPreferences,
  updatePreferences,
  type Team,
  type TeamPlayer,
  type TeamStanding,
  type UserPreference,
} from "@/lib/api"
import { LoadingState, ErrorState, EmptyState } from "@/components/states"
import { TeamLogo } from "@/components/media"
import { cn } from "@/lib/utils"
import { Search, Trophy } from "lucide-react"
import { PlayerDetailDialog } from "@/components/players-section"

export function TeamsSection() {
  const season = new Date().getFullYear().toString()
  const [token] = useState<string | null>(() =>
    typeof window === "undefined" ? null : localStorage.getItem(AUTH_TOKEN_KEY),
  )
  const { data, error, isLoading, mutate } = useSWR<Team[]>(endpoints.teams(), fetcher, {
    revalidateOnFocus: false,
  })
  const { data: preferences, mutate: mutatePreferences } = useSWR<UserPreference>(
    token ? ["preferences", token] : null,
    () => fetchPreferences(token!),
    { revalidateOnFocus: false },
  )
  const {
    data: standingsData,
    error: standingsError,
    isLoading: standingsLoading,
    mutate: mutateStandings,
  } = useSWR<TeamStanding[]>(endpoints.standings(season), fetcher, {
    revalidateOnFocus: false,
  })
  const [selected, setSelected] = useState<Team | null>(null)
  const [query, setQuery] = useState("")

  const teams = useMemo(() => data ?? [], [data])
  const standings = useMemo(() => standingsData ?? [], [standingsData])

  const grouped = useMemo(() => {
    const filtered = teams.filter(
      (t) =>
        t.name.toLowerCase().includes(query.toLowerCase()) ||
        (t.abbreviation ?? "").toLowerCase().includes(query.toLowerCase()),
    )
    const map = new Map<string, Team[]>()
    for (const t of filtered) {
      const key = t.divisionName ?? "기타"
      if (!map.has(key)) map.set(key, [])
      map.get(key)!.push(t)
    }
    return Array.from(map.entries()).sort((a, b) => a[0].localeCompare(b[0]))
  }, [teams, query])

  async function setFavoriteTeam(team: Team) {
    if (!token) return
    await mutatePreferences(updatePreferences(token, team.id, team.name), { revalidate: false })
  }

  return (
    <section aria-labelledby="teams-heading" className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wider text-primary">TEAMS</p>
          <h1 id="teams-heading" className="mt-1 text-2xl font-bold tracking-tight text-foreground">
            MLB 팀 목록
          </h1>
          <p className="mt-1 text-sm text-muted-foreground">팀 순위와 선수 명단을 한 화면에서 확인할 수 있습니다.</p>
          {preferences?.favoriteTeamName && (
            <p className="mt-2 text-xs font-semibold text-primary">내 팀: {preferences.favoriteTeamName}</p>
          )}
        </div>
        <div className="relative">
          <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" aria-hidden="true" />
          <input
            type="search"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="팀 이름 검색"
            aria-label="팀 이름 검색"
            className="w-56 rounded-md border border-input bg-secondary py-2 pl-9 pr-3 text-sm text-foreground placeholder:text-muted-foreground focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
          />
        </div>
      </div>

      <StandingsBoard
        season={season}
        standings={standings}
        isLoading={standingsLoading}
        error={standingsError}
        onRetry={() => mutateStandings()}
        onSelectTeam={(teamId) => {
          const team = teams.find((candidate) => candidate.id === teamId)
          if (team) setSelected(team)
        }}
      />

      {isLoading && <LoadingState label="팀 목록을 불러오는 중..." />}
      {error && (
        <ErrorState message="팀 목록을 불러오지 못했습니다. 백엔드 서버 상태를 확인해 주세요." onRetry={() => mutate()} />
      )}
      {!isLoading && !error && teams.length > 0 && grouped.length === 0 && (
        <EmptyState message="검색 결과가 없습니다. 팀 이름을 다시 입력해 주세요." />
      )}

      {!isLoading && !error && grouped.length > 0 && (
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-[1fr_1.1fr]">
          {/* 팀 목록 */}
          <div className="space-y-5">
            {grouped.map(([division, list]) => (
              <div key={division}>
                <h2 className="mb-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                  {division}
                </h2>
                <div className="grid grid-cols-1 gap-2 sm:grid-cols-2">
                  {list.map((t) => {
                    const isActive = selected?.id === t.id
                    const isFavorite = preferences?.favoriteTeamId === t.id
                    return (
                      <button
                        key={t.id}
                        onClick={() => setSelected(t)}
                        aria-pressed={isActive}
                        className={cn(
                          "flex items-center gap-3 rounded-lg border p-3 text-left transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring",
                          isActive
                            ? "border-primary bg-primary/10"
                            : isFavorite
                              ? "border-primary/50 bg-primary/5"
                            : "border-border bg-card hover:border-primary/40 hover:bg-accent",
                        )}
                      >
                        <span
                          className={cn(
                            "flex size-10 shrink-0 items-center justify-center rounded-full",
                            isActive ? "bg-primary/10" : "bg-secondary",
                          )}
                        >
                          <TeamLogo
                            teamId={t.id}
                            name={t.name}
                            abbr={t.abbreviation}
                            className="size-7"
                          />
                        </span>
                        <div className="min-w-0">
                          <p className="flex items-center gap-2 truncate text-sm font-semibold text-foreground">
                            <span className="truncate">{t.name}</span>
                            {isFavorite && <span className="shrink-0 rounded bg-primary/10 px-1.5 py-0.5 text-[10px] font-bold text-primary">내 팀</span>}
                          </p>
                          <p className="truncate text-xs text-muted-foreground">{t.leagueName ?? "리그 정보 없음"}</p>
                        </div>
                      </button>
                    )
                  })}
                </div>
              </div>
            ))}
          </div>

          {/* 선택된 팀 명단 */}
          <div className="lg:sticky lg:top-20 lg:self-start">
            <RosterPanel
              team={selected}
              isFavorite={Boolean(selected && preferences?.favoriteTeamId === selected.id)}
              canSetFavorite={Boolean(token)}
              onSetFavorite={setFavoriteTeam}
            />
          </div>
        </div>
      )}
    </section>
  )
}

function StandingsBoard({
  season,
  standings,
  isLoading,
  error,
  onRetry,
  onSelectTeam,
}: {
  season: string
  standings: TeamStanding[]
  isLoading: boolean
  error: unknown
  onRetry: () => void
  onSelectTeam: (teamId: number) => void
}) {
  const grouped = useMemo(() => {
    const map = new Map<string, TeamStanding[]>()
    for (const row of standings) {
      const key = row.divisionName ?? row.leagueName ?? "기타"
      if (!map.has(key)) map.set(key, [])
      map.get(key)!.push(row)
    }
    return Array.from(map.entries())
      .map(([division, rows]) => [
        division,
        [...rows].sort((a, b) => (a.divisionRank ?? 999) - (b.divisionRank ?? 999)),
      ] as const)
      .sort((a, b) => a[0].localeCompare(b[0]))
  }, [standings])

  return (
    <div className="rounded-2xl border border-border bg-card shadow-sm">
      <div className="flex flex-wrap items-center justify-between gap-3 border-b border-border p-4">
        <div className="flex items-center gap-3">
          <span className="flex size-10 items-center justify-center rounded-full bg-primary/10 text-primary">
            <Trophy className="size-5" aria-hidden="true" />
          </span>
          <div>
            <h2 className="text-lg font-bold tracking-tight text-foreground">{season} MLB 팀 순위</h2>
            <p className="text-xs text-muted-foreground">디비전별 정규시즌 승률과 게임차를 보여줍니다.</p>
          </div>
        </div>
        {!isLoading && !error && standings.length > 0 && (
          <span className="rounded-full bg-secondary px-3 py-1 text-xs font-medium text-secondary-foreground">
            {standings.length}개 팀
          </span>
        )}
      </div>
      <div className="p-4">
        {isLoading && <LoadingState label="팀 순위를 불러오는 중..." />}
        {Boolean(error) && <ErrorState message="팀 순위를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요." onRetry={onRetry} />}
        {!isLoading && !error && standings.length === 0 && <EmptyState message="표시할 팀 순위가 없습니다." />}
        {!isLoading && !error && grouped.length > 0 && (
          <div className="grid grid-cols-1 gap-4 xl:grid-cols-2">
            {grouped.map(([division, rows]) => (
              <div key={division} className="overflow-hidden rounded-xl border border-border">
                <div className="flex items-center justify-between bg-secondary px-3 py-2">
                  <h3 className="text-sm font-semibold text-secondary-foreground">{division}</h3>
                  <span className="text-xs text-muted-foreground">{rows[0]?.leagueName ?? ""}</span>
                </div>
                <div className="overflow-x-auto">
                  <table className="w-full min-w-[34rem] text-sm">
                    <caption className="sr-only">{division} 순위</caption>
                    <thead>
                      <tr className="border-b border-border text-left text-xs text-muted-foreground">
                        <th scope="col" className="w-10 px-3 py-2 text-center font-medium">순위</th>
                        <th scope="col" className="px-3 py-2 font-medium">팀</th>
                        <th scope="col" className="w-14 px-2 py-2 text-right font-medium">승</th>
                        <th scope="col" className="w-14 px-2 py-2 text-right font-medium">패</th>
                        <th scope="col" className="w-16 px-2 py-2 text-right font-medium">승률</th>
                        <th scope="col" className="w-16 px-2 py-2 text-right font-medium">게임차</th>
                        <th scope="col" className="w-16 px-2 py-2 text-right font-medium">득실</th>
                      </tr>
                    </thead>
                    <tbody>
                      {rows.map((row) => (
                        <tr key={`${division}-${row.teamId ?? row.teamName}`} className="border-b border-border/50 last:border-0 hover:bg-accent">
                          <td className="px-3 py-2 text-center font-semibold text-foreground">{row.divisionRank ?? "-"}</td>
                          <td className="px-3 py-2">
                            <button
                              type="button"
                              onClick={() => row.teamId && onSelectTeam(row.teamId)}
                              disabled={!row.teamId}
                              className="flex max-w-48 items-center gap-2 rounded text-left font-medium text-foreground hover:text-primary disabled:cursor-default disabled:hover:text-foreground"
                            >
                              {row.teamId && (
                                <TeamLogo teamId={row.teamId} name={row.teamName ?? "팀"} className="size-6 shrink-0" />
                              )}
                              <span className="truncate">{row.teamName ?? "팀 정보 없음"}</span>
                              {row.divisionLeader && (
                                <span className="rounded bg-primary/10 px-1.5 py-0.5 text-[10px] font-bold text-primary">1위</span>
                              )}
                            </button>
                          </td>
                          <td className="px-2 py-2 text-right tabular-nums text-foreground">{row.wins ?? "-"}</td>
                          <td className="px-2 py-2 text-right tabular-nums text-muted-foreground">{row.losses ?? "-"}</td>
                          <td className="px-2 py-2 text-right tabular-nums text-foreground">{row.winningPercentage ?? "-"}</td>
                          <td className="px-2 py-2 text-right tabular-nums text-muted-foreground">{row.gamesBack ?? "-"}</td>
                          <td className="px-2 py-2 text-right tabular-nums text-muted-foreground">{row.runDifferential ?? "-"}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

function RosterPanel({
  team,
  isFavorite,
  canSetFavorite,
  onSetFavorite,
}: {
  team: Team | null
  isFavorite: boolean
  canSetFavorite: boolean
  onSetFavorite: (team: Team) => void
}) {
  if (!team) {
    return (
      <div className="rounded-xl border border-dashed border-border bg-card/50 p-8">
        <EmptyState message="왼쪽에서 팀을 선택하면 선수 명단이 여기에 표시됩니다." />
      </div>
    )
  }
  return (
    <Roster
      team={team}
      key={team.id}
      isFavorite={isFavorite}
      canSetFavorite={canSetFavorite}
      onSetFavorite={onSetFavorite}
    />
  )
}

function Roster({
  team,
  isFavorite,
  canSetFavorite,
  onSetFavorite,
}: {
  team: Team
  isFavorite: boolean
  canSetFavorite: boolean
  onSetFavorite: (team: Team) => void
}) {
  const { data, error, isLoading, mutate } = useSWR<TeamPlayer[]>(endpoints.roster(team.id), fetcher, {
    revalidateOnFocus: false,
  })
  const [selectedPlayerId, setSelectedPlayerId] = useState<number | null>(null)
  const players = data ?? []

  return (
    <div className="rounded-xl border border-border bg-card">
      <div className="flex items-center gap-3 border-b border-border p-4">
        <TeamLogo teamId={team.id} name={team.name} abbr={team.abbreviation} className="size-11 shrink-0" />
        <div className="min-w-0">
          <h2 className="truncate text-lg font-bold tracking-tight text-foreground">{team.name}</h2>
          <p className="mt-0.5 truncate text-xs text-muted-foreground">
            {team.locationName ?? ""} · {team.venueName ?? "구장 정보 없음"}
          </p>
        </div>
        <button
          type="button"
          disabled={!canSetFavorite || isFavorite}
          onClick={() => onSetFavorite(team)}
          className={cn(
            "ml-auto shrink-0 rounded-md border px-3 py-2 text-xs font-bold transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring disabled:cursor-not-allowed",
            isFavorite
              ? "border-primary bg-primary/10 text-primary"
              : "border-border bg-secondary text-secondary-foreground hover:bg-accent disabled:opacity-50",
          )}
        >
          {isFavorite ? "내 팀" : "내 팀으로 설정"}
        </button>
      </div>
      <div className="p-2">
        {isLoading && <LoadingState label="선수 명단을 불러오는 중..." />}
        {error && (
          <ErrorState
            message="선수 명단을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요."
            onRetry={() => mutate()}
          />
        )}
        {!isLoading && !error && players.length === 0 && (
          <EmptyState message="등록된 선수 명단이 없습니다." />
        )}
        {!isLoading && !error && players.length > 0 && (
          <div className="max-h-[28rem] overflow-y-auto">
            <table className="w-full text-sm">
              <caption className="sr-only">{team.name} 선수 명단</caption>
              <thead className="sticky top-0 bg-card">
                <tr className="border-b border-border text-left text-xs text-muted-foreground">
                  <th scope="col" className="w-12 px-3 py-2 font-medium">
                    번호
                  </th>
                  <th scope="col" className="px-3 py-2 font-medium">
                    선수
                  </th>
                  <th scope="col" className="w-16 px-3 py-2 text-right font-medium">
                    포지션
                  </th>
                </tr>
              </thead>
              <tbody>
                {players.map((p, i) => (
                  <tr key={p.playerId ?? i} className="border-b border-border/50 last:border-0 hover:bg-accent">
                    <td className="px-3 py-2 font-mono text-muted-foreground">{p.jerseyNumber ?? "-"}</td>
                    <td className="px-3 py-2 font-medium text-foreground">
                      {p.playerId ? (
                        <button
                          type="button"
                          onClick={() => setSelectedPlayerId(p.playerId)}
                          className="rounded text-left font-semibold hover:text-primary focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
                        >
                          {p.fullName ?? "이름 없음"}
                        </button>
                      ) : (
                        p.fullName ?? "이름 없음"
                      )}
                    </td>
                    <td className="px-3 py-2 text-right">
                      <span className="inline-flex items-center rounded bg-secondary px-1.5 py-0.5 text-xs font-medium text-secondary-foreground">
                        {p.position ?? "-"}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
      <PlayerDetailDialog playerId={selectedPlayerId} onClose={() => setSelectedPlayerId(null)} />
    </div>
  )
}
