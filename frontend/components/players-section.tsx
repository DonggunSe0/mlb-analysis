"use client"

import useSWR from "swr"
import { useState } from "react"
import { fetcher, endpoints, type Player } from "@/lib/api"
import { LoadingState, ErrorState, EmptyState } from "@/components/states"
import { PlayerAvatar } from "@/components/media"
import { cn } from "@/lib/utils"
import { Search } from "lucide-react"

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
  const { data, error, isLoading, mutate } = useSWR<Player>(endpoints.player(playerId), fetcher, {
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
        <div>
          <h2 className="text-xl font-bold tracking-tight text-foreground">{data.fullName}</h2>
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
    </article>
  )
}
