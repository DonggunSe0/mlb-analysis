"use client"

import useSWR from "swr"
import { useMemo, useState } from "react"
import { fetcher, endpoints, type Team, type TeamPlayer } from "@/lib/api"
import { LoadingState, ErrorState, EmptyState } from "@/components/states"
import { TeamLogo } from "@/components/media"
import { cn } from "@/lib/utils"
import { Search } from "lucide-react"

export function TeamsSection() {
  const { data, error, isLoading, mutate } = useSWR<Team[]>(endpoints.teams(), fetcher, {
    revalidateOnFocus: false,
  })
  const [selected, setSelected] = useState<Team | null>(null)
  const [query, setQuery] = useState("")

  const teams = useMemo(() => data ?? [], [data])

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

  return (
    <section aria-labelledby="teams-heading" className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wider text-primary">TEAMS</p>
          <h1 id="teams-heading" className="mt-1 text-2xl font-bold tracking-tight text-foreground">
            MLB 팀 목록
          </h1>
          <p className="mt-1 text-sm text-muted-foreground">팀을 선택하면 선수 명단을 확인할 수 있습니다.</p>
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
                    return (
                      <button
                        key={t.id}
                        onClick={() => setSelected(t)}
                        aria-pressed={isActive}
                        className={cn(
                          "flex items-center gap-3 rounded-lg border p-3 text-left transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring",
                          isActive
                            ? "border-primary bg-primary/10"
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
                          <p className="truncate text-sm font-semibold text-foreground">{t.name}</p>
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
            <RosterPanel team={selected} />
          </div>
        </div>
      )}
    </section>
  )
}

function RosterPanel({ team }: { team: Team | null }) {
  if (!team) {
    return (
      <div className="rounded-xl border border-dashed border-border bg-card/50 p-8">
        <EmptyState message="왼쪽에서 팀을 선택하면 선수 명단이 여기에 표시됩니다." />
      </div>
    )
  }
  return <Roster team={team} key={team.id} />
}

function Roster({ team }: { team: Team }) {
  const { data, error, isLoading, mutate } = useSWR<TeamPlayer[]>(endpoints.roster(team.id), fetcher, {
    revalidateOnFocus: false,
  })
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
                    <td className="px-3 py-2 font-medium text-foreground">{p.fullName ?? "이름 없음"}</td>
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
    </div>
  )
}
