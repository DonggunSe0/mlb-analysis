"use client"

import useSWR from "swr"
import { useMemo, useState } from "react"
import {
  endpoints,
  fetchAllStarResults,
  fetchAllStarStatus,
  submitAllStarVote,
  type AllStarSelection,
  type AllStarVoteResults,
  type AllStarVoteStatus,
  type Player,
  fetcher,
} from "@/lib/api"
import { EmptyState, ErrorState, LoadingState } from "@/components/states"
import { PlayerAvatar } from "@/components/media"
import { cn } from "@/lib/utils"
import { BarChart3, CheckCircle2, Lock, Search, Trophy } from "lucide-react"

const POSITIONS = [
  { key: "P", label: "투수", x: "50%", y: "42%" },
  { key: "C", label: "포수", x: "50%", y: "82%" },
  { key: "1B", label: "1루수", x: "78%", y: "58%" },
  { key: "2B", label: "2루수", x: "64%", y: "32%" },
  { key: "3B", label: "3루수", x: "22%", y: "58%" },
  { key: "SS", label: "유격수", x: "36%", y: "32%" },
  { key: "LF", label: "좌익수", x: "18%", y: "18%" },
  { key: "CF", label: "중견수", x: "50%", y: "10%" },
  { key: "RF", label: "우익수", x: "82%", y: "18%" },
  { key: "DH", label: "지명타자", x: "50%", y: "62%" },
]

export function AllStarVoteSection({ token }: { token: string | null }) {
  const [selectedPosition, setSelectedPosition] = useState("P")
  const [playerQuery, setPlayerQuery] = useState("")
  const [submittedMessage, setSubmittedMessage] = useState<string | null>(null)
  const [selections, setSelections] = useState<Record<string, AllStarSelection>>({})


  const { data: voteStatus, error: statusError, isLoading: statusLoading, mutate: mutateStatus } = useSWR<AllStarVoteStatus>(
    token ? ["allstar-status", token] : null,
    () => fetchAllStarStatus(token!),
    { revalidateOnFocus: false },
  )
  const {
    data: voteResults,
    error: resultsError,
    isLoading: resultsLoading,
    mutate: mutateResults,
  } = useSWR<AllStarVoteResults>(
    endpoints.allStarResults(),
    fetchAllStarResults,
    { revalidateOnFocus: false },
  )

  const { data: players, isLoading: playerLoading } = useSWR<Player[]>(
    playerQuery.trim() ? endpoints.search(playerQuery.trim()) : null,
    fetcher,
    { revalidateOnFocus: false },
  )

  const selectedList = useMemo(() => Object.values(selections), [selections])

  function pickPlayer(player: Player) {
    const position = POSITIONS.find((item) => item.key === selectedPosition)
    if (!position) return
    setSelections((prev) => ({
      ...prev,
      [selectedPosition]: {
        positionKey: selectedPosition,
        playerId: player.id,
        playerName: player.fullName,
        teamName: player.primaryPosition,
      },
    }))
  }

  async function submitVote() {
    if (!token || selectedList.length === 0) return
    setSubmittedMessage(null)
    try {
      const ballot = await submitAllStarVote(token, selectedList)
      setSubmittedMessage(`${ballot.voteDate} 올스타 투표가 저장됐습니다.`)
      await mutateStatus()
      await mutateResults()
    } catch (error) {
      setSubmittedMessage(error instanceof Error ? error.message : "투표 저장에 실패했습니다.")
    }
  }

  return (
    <section aria-labelledby="all-star-heading" className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wider text-primary">ALL-STAR</p>
          <h1 id="all-star-heading" className="mt-1 text-2xl font-bold tracking-tight text-foreground">올스타 투표</h1>
          <p className="mt-1 text-sm text-muted-foreground">야구장 포메이션에서 포지션을 고르고 선수를 검색해 하루 한 번 저장합니다.</p>
        </div>
      </div>

      <AllStarResultsPanel
        results={voteResults}
        isLoading={resultsLoading}
        error={resultsError}
        onRetry={() => mutateResults()}
      />

      {!token && (
        <div className="rounded-2xl border border-dashed border-border bg-card/60 p-5">
          <div className="flex items-start gap-3">
            <span className="flex size-10 shrink-0 items-center justify-center rounded-full bg-secondary text-muted-foreground">
              <Lock className="size-5" aria-hidden="true" />
            </span>
            <div>
              <p className="text-sm font-bold text-foreground">투표 참여는 로그인이 필요합니다</p>
              <p className="mt-1 text-sm text-muted-foreground">결과는 누구나 볼 수 있고, 선수 선택과 저장은 상단 로그인/회원가입 후 진행됩니다.</p>
            </div>
          </div>
        </div>
      )}

      {token && (
        <div className="space-y-5">
          <div className="rounded-2xl border border-border bg-card p-4">
            {statusLoading && <LoadingState label="투표 상태를 확인하는 중..." />}
            {statusError && <ErrorState message="투표 상태를 확인하지 못했습니다. login profile 백엔드와 DB 상태를 확인해 주세요." onRetry={() => mutateStatus()} />}
            {voteStatus?.ballot && (
              <div className="flex items-start gap-3 text-sm">
                <CheckCircle2 className="mt-0.5 size-5 text-primary" />
                <div>
                  <p className="font-bold text-foreground">오늘은 이미 투표했습니다.</p>
                  <p className="mt-1 text-muted-foreground">{voteStatus.voteDate} 기준 하루 1회 제한이 적용됩니다.</p>
                </div>
              </div>
            )}
            {!voteStatus?.ballot && <p className="text-sm text-muted-foreground">오늘 투표 가능 상태입니다.</p>}
          </div>

          <div className="grid grid-cols-1 gap-6 lg:grid-cols-[1.1fr_0.9fr]">
            <div className="relative aspect-[4/3] overflow-hidden rounded-3xl border border-emerald-200 bg-emerald-100 p-4 shadow-inner">
              <div className="absolute inset-x-[18%] bottom-[12%] top-[18%] rotate-45 border-4 border-amber-200 bg-emerald-200/60" />
              <div className="absolute left-1/2 top-[82%] size-7 -translate-x-1/2 -translate-y-1/2 rotate-45 bg-amber-100" />
              {POSITIONS.map((position) => {
                const selection = selections[position.key] ?? voteStatus?.ballot?.selections.find((item) => item.positionKey === position.key)
                return (
                  <button
                    key={position.key}
                    type="button"
                    disabled={Boolean(voteStatus?.ballot)}
                    onClick={() => setSelectedPosition(position.key)}
                    className={cn(
                      "absolute min-w-24 -translate-x-1/2 -translate-y-1/2 rounded-xl border bg-card/95 px-3 py-2 text-center text-xs shadow-sm transition-colors",
                      selectedPosition === position.key ? "border-primary text-primary" : "border-border text-foreground",
                    )}
                    style={{ left: position.x, top: position.y }}
                  >
                    <span className="block font-black">{position.label}</span>
                    <span className="mt-1 block truncate text-[11px] text-muted-foreground">{selection?.playerName ?? "선택"}</span>
                  </button>
                )
              })}
            </div>

            <div className="space-y-4">
              <div className="rounded-2xl border border-border bg-card p-4">
                <p className="text-sm font-bold text-foreground">현재 포지션: {POSITIONS.find((p) => p.key === selectedPosition)?.label}</p>
                <div className="relative mt-3">
                  <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                  <input value={playerQuery} onChange={(e) => setPlayerQuery(e.target.value)} placeholder="선수 이름 검색" className="w-full rounded-md border border-input bg-secondary py-2 pl-9 pr-3 text-sm" />
                </div>
                {playerLoading && <LoadingState label="선수 검색 중..." className="py-6" />}
                {players && players.length > 0 && (
                  <ul className="mt-3 max-h-72 space-y-2 overflow-y-auto">
                    {players.map((player) => (
                      <li key={player.id}>
                        <button disabled={Boolean(voteStatus?.ballot)} onClick={() => pickPlayer(player)} className="flex w-full items-center gap-3 rounded-lg border border-border p-2 text-left hover:bg-accent disabled:opacity-50">
                          <PlayerAvatar playerId={player.id} name={player.fullName} className="size-10 shrink-0" />
                          <span className="min-w-0 flex-1">
                            <span className="block truncate text-sm font-semibold">{player.fullName}</span>
                            <span className="block truncate text-xs text-muted-foreground">{player.primaryPosition ?? "포지션 정보 없음"}</span>
                          </span>
                        </button>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
              <div className="rounded-2xl border border-border bg-card p-4">
                <p className="font-bold text-foreground">선택 선수 {selectedList.length}/10</p>
                {selectedList.length === 0 ? <EmptyState message="포지션을 고르고 선수를 추가하세요." /> : (
                  <ul className="mt-3 space-y-1 text-sm">
                    {selectedList.map((item) => <li key={item.positionKey} className="flex justify-between gap-3"><span>{item.positionKey}</span><span className="font-semibold">{item.playerName}</span></li>)}
                  </ul>
                )}
                <button disabled={!voteStatus?.canVote || selectedList.length === 0} onClick={submitVote} className="mt-4 w-full rounded-md bg-primary px-4 py-2 text-sm font-bold text-primary-foreground disabled:cursor-not-allowed disabled:opacity-50">오늘 투표 저장</button>
                {submittedMessage && <p className="mt-3 text-sm text-muted-foreground">{submittedMessage}</p>}
              </div>
            </div>
          </div>
        </div>
      )}
    </section>
  )
}

function AllStarResultsPanel({
  results,
  isLoading,
  error,
  onRetry,
}: {
  results: AllStarVoteResults | undefined
  isLoading: boolean
  error: unknown
  onRetry: () => void
}) {
  const positions = results?.positions ?? []

  return (
    <div className="rounded-2xl border border-border bg-card p-5 shadow-sm">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wider text-primary">LIVE RESULTS</p>
          <h2 className="mt-1 text-lg font-bold text-foreground">오늘 팬 선택 흐름</h2>
          <p className="mt-1 text-sm text-muted-foreground">{results?.voteDate ?? "오늘"} 기준 집계</p>
        </div>
        <span className="inline-flex items-center gap-1.5 rounded-md bg-secondary px-3 py-2 text-xs font-bold text-secondary-foreground">
          <BarChart3 className="size-3.5" aria-hidden="true" />
          {results?.totalBallots ?? 0}표
        </span>
      </div>

      {isLoading && <LoadingState label="투표 결과를 불러오는 중..." className="py-8" />}
      {Boolean(error) && <ErrorState message="투표 결과를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요." onRetry={onRetry} />}
      {!isLoading && !error && positions.length === 0 && (
        <div className="mt-4">
          <EmptyState message="아직 집계된 투표가 없습니다." />
        </div>
      )}
      {!isLoading && !error && positions.length > 0 && (
        <div className="mt-4 grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-3">
          {positions.map((position) => {
            const leader = position.candidates.find((candidate) => candidate.leading)
            const topCandidate = leader ?? position.candidates[0]
            return (
              <div key={position.positionKey} className="rounded-xl border border-border bg-secondary/40 p-4">
                <div className="flex items-center justify-between gap-2">
                  <p className="text-sm font-bold text-foreground">{positionLabel(position.positionKey)}</p>
                  {leader && <Trophy className="size-4 text-primary" aria-hidden="true" />}
                </div>
                {topCandidate ? (
                  <>
                    <p className="mt-3 truncate text-sm font-semibold text-foreground">{topCandidate.playerName}</p>
                    <p className="mt-1 truncate text-xs text-muted-foreground">{topCandidate.teamName ?? "팀 정보 없음"}</p>
                    <div className="mt-3 h-2 overflow-hidden rounded-full bg-background">
                      <div className="h-full rounded-full bg-primary" style={{ width: `${topCandidate.votePercentage}%` }} />
                    </div>
                    <p className="mt-2 text-xs text-muted-foreground">
                      {topCandidate.voteCount}표 · {topCandidate.votePercentage}%
                    </p>
                    <ul className="mt-3 max-h-40 space-y-1 overflow-y-auto border-t border-border pt-3">
                      {position.candidates.map((candidate) => (
                        <li key={`${position.positionKey}-${candidate.playerId}-${candidate.playerName}`} className="grid grid-cols-[1fr_auto] gap-2 text-xs">
                          <span className="truncate text-muted-foreground">{candidate.playerName}</span>
                          <span className="font-semibold text-foreground">{candidate.voteCount}표</span>
                        </li>
                      ))}
                    </ul>
                  </>
                ) : (
                  <p className="mt-3 text-xs text-muted-foreground">선택 대기 중</p>
                )}
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}

function positionLabel(positionKey: string) {
  return POSITIONS.find((position) => position.key === positionKey)?.label ?? positionKey
}
