"use client"

import useSWR from "swr"
import { useMemo, useState } from "react"
import {
  endpoints,
  AUTH_TOKEN_KEY,
  fetchAllStarStatus,
  login,
  register,
  submitAllStarVote,
  type AllStarSelection,
  type AllStarVoteStatus,
  type AuthResponse,
  type Player,
  fetcher,
} from "@/lib/api"
import { EmptyState, ErrorState, LoadingState } from "@/components/states"
import { PlayerAvatar } from "@/components/media"
import { cn } from "@/lib/utils"
import { CheckCircle2, Lock, Search, UserPlus } from "lucide-react"

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

export function AllStarVoteSection() {
  const [token, setToken] = useState<string | null>(() =>
    typeof window === "undefined" ? null : localStorage.getItem(AUTH_TOKEN_KEY),
  )
  const [authMode, setAuthMode] = useState<"login" | "register">("login")
  const [authError, setAuthError] = useState<string | null>(null)
  const [email, setEmail] = useState("")
  const [displayName, setDisplayName] = useState("")
  const [password, setPassword] = useState("")
  const [selectedPosition, setSelectedPosition] = useState("P")
  const [playerQuery, setPlayerQuery] = useState("")
  const [submittedMessage, setSubmittedMessage] = useState<string | null>(null)
  const [selections, setSelections] = useState<Record<string, AllStarSelection>>({})


  const { data: voteStatus, error: statusError, isLoading: statusLoading, mutate: mutateStatus } = useSWR<AllStarVoteStatus>(
    token ? ["allstar-status", token] : null,
    () => fetchAllStarStatus(token!),
    { revalidateOnFocus: false },
  )

  const { data: players, isLoading: playerLoading } = useSWR<Player[]>(
    playerQuery.trim() ? endpoints.search(playerQuery.trim()) : null,
    fetcher,
    { revalidateOnFocus: false },
  )

  const selectedList = useMemo(() => Object.values(selections), [selections])

  async function handleAuth(e: React.FormEvent) {
    e.preventDefault()
    setAuthError(null)
    try {
      const response: AuthResponse = authMode === "login"
        ? await login(email, password)
        : await register(email, displayName || email.split("@")[0], password)
      localStorage.setItem(AUTH_TOKEN_KEY, response.token)
      setToken(response.token)
      setPassword("")
    } catch (error) {
      setAuthError(error instanceof Error ? error.message : "로그인 서버에 연결하지 못했습니다. backend를 login profile로 실행해 주세요.")
    }
  }

  function logout() {
    localStorage.removeItem(AUTH_TOKEN_KEY)
    setToken(null)
    setSelections({})
  }

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
        {token && (
          <button onClick={logout} className="rounded-md border border-border bg-secondary px-3 py-2 text-sm font-semibold text-secondary-foreground hover:bg-accent">로그아웃</button>
        )}
      </div>

      {!token && (
        <form onSubmit={handleAuth} className="max-w-xl rounded-2xl border border-border bg-card p-5 shadow-sm">
          <div className="flex items-center gap-2 text-sm font-bold text-foreground">
            {authMode === "login" ? <Lock className="size-4" /> : <UserPlus className="size-4" />}
            {authMode === "login" ? "로그인 후 투표하기" : "회원가입 후 투표하기"}
          </div>
          <div className="mt-4 grid gap-3">
            <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" placeholder="email@example.com" className="rounded-md border border-input bg-secondary px-3 py-2 text-sm" required />
            {authMode === "register" && <input value={displayName} onChange={(e) => setDisplayName(e.target.value)} placeholder="표시 이름" className="rounded-md border border-input bg-secondary px-3 py-2 text-sm" />}
            <input value={password} onChange={(e) => setPassword(e.target.value)} type="password" placeholder="비밀번호" className="rounded-md border border-input bg-secondary px-3 py-2 text-sm" required />
            {authError && <p className="text-sm text-destructive">{authError}</p>}
            <button className="rounded-md bg-primary px-4 py-2 text-sm font-bold text-primary-foreground">{authMode === "login" ? "로그인" : "회원가입"}</button>
            <button type="button" onClick={() => setAuthMode(authMode === "login" ? "register" : "login")} className="text-sm font-semibold text-primary">
              {authMode === "login" ? "계정이 없으면 회원가입" : "이미 계정이 있으면 로그인"}
            </button>
          </div>
        </form>
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
