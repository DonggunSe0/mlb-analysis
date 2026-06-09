"use client"

import { useState } from "react"
import { AUTH_TOKEN_KEY, login, register, type AuthResponse } from "@/lib/api"
import { cn } from "@/lib/utils"
import { LogIn, UserPlus, X } from "lucide-react"

export type Section = "games" | "teams" | "players" | "news" | "allstar"

const items: { id: Section; label: string }[] = [
  { id: "games", label: "오늘의 경기" },
  { id: "teams", label: "팀" },
  { id: "players", label: "선수 검색" },
  { id: "news", label: "뉴스" },
  { id: "allstar", label: "올스타 투표" },
]

export function SiteHeader({
  active,
  token,
  onAuthChange,
  onSelect,
}: {
  active: Section
  token: string | null
  onAuthChange: (token: string | null) => void
  onSelect: (s: Section) => void
}) {
  const [authMode, setAuthMode] = useState<"login" | "register" | null>(null)

  function logout() {
    localStorage.removeItem(AUTH_TOKEN_KEY)
    onAuthChange(null)
  }

  return (
    <header className="sticky top-0 z-30 border-b border-border bg-sidebar/95 backdrop-blur supports-[backdrop-filter]:bg-sidebar/80">
      <div className="mx-auto flex min-h-16 max-w-7xl flex-wrap items-center gap-3 px-4 py-3 sm:gap-6 sm:px-6">
        <div className="flex items-center gap-2.5">
          <span className="flex size-8 items-center justify-center rounded-md bg-primary text-sm font-bold text-primary-foreground">
            MLB
          </span>
          <div className="leading-tight">
            <p className="text-sm font-bold tracking-tight text-foreground">MLB Analysis</p>
            <p className="text-[11px] text-muted-foreground">메이저리그 데이터 대시보드</p>
          </div>
        </div>

        <nav aria-label="주요 섹션" className="ml-auto flex max-w-full items-center gap-1 overflow-x-auto">
          {items.map((item) => (
            <button
              key={item.id}
              onClick={() => onSelect(item.id)}
              aria-current={active === item.id ? "page" : undefined}
              className={cn(
                "relative rounded-md px-3 py-2 text-sm font-medium transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring",
                active === item.id
                  ? "text-foreground"
                  : "text-muted-foreground hover:bg-accent hover:text-foreground",
              )}
            >
              {item.label}
              {active === item.id && (
                <span className="absolute inset-x-2 -bottom-px h-0.5 rounded-full bg-primary" />
              )}
            </button>
          ))}
        </nav>
        <div className="flex items-center gap-2 border-l border-border pl-3">
          {token ? (
            <button
              type="button"
              onClick={logout}
              className="rounded-md border border-border bg-secondary px-3 py-2 text-sm font-semibold text-secondary-foreground transition-colors hover:bg-accent focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
            >
              로그아웃
            </button>
          ) : (
            <>
              <button
                type="button"
                onClick={() => setAuthMode("login")}
                className="inline-flex items-center gap-1.5 rounded-md border border-border bg-secondary px-3 py-2 text-sm font-semibold text-secondary-foreground transition-colors hover:bg-accent focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
              >
                <LogIn className="size-4" aria-hidden="true" />
                로그인
              </button>
              <button
                type="button"
                onClick={() => setAuthMode("register")}
                className="inline-flex items-center gap-1.5 rounded-md bg-primary px-3 py-2 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary/90 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
              >
                <UserPlus className="size-4" aria-hidden="true" />
                회원가입
              </button>
            </>
          )}
        </div>
      </div>
      {authMode && (
        <AuthDialog
          initialMode={authMode}
          onClose={() => setAuthMode(null)}
          onAuthenticated={(nextToken) => {
            onAuthChange(nextToken)
            setAuthMode(null)
          }}
        />
      )}
    </header>
  )
}

function AuthDialog({
  initialMode,
  onAuthenticated,
  onClose,
}: {
  initialMode: "login" | "register"
  onAuthenticated: (token: string) => void
  onClose: () => void
}) {
  const [mode, setMode] = useState(initialMode)
  const [email, setEmail] = useState("")
  const [displayName, setDisplayName] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function submitAuth(event: React.FormEvent) {
    event.preventDefault()
    setError(null)
    setIsSubmitting(true)
    try {
      const response: AuthResponse = mode === "login"
        ? await login(email, password)
        : await register(email, displayName || email.split("@")[0], password)
      localStorage.setItem(AUTH_TOKEN_KEY, response.token)
      onAuthenticated(response.token)
      setPassword("")
    } catch (authError) {
      setError(authError instanceof Error ? authError.message : "로그인 서버에 연결하지 못했습니다.")
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-start justify-center bg-background/60 px-4 pt-20 backdrop-blur-sm">
      <form onSubmit={submitAuth} className="w-full max-w-md rounded-xl border border-border bg-card p-5 shadow-xl">
        <div className="flex items-center justify-between gap-3">
          <div>
            <p className="text-xs font-semibold uppercase tracking-wider text-primary">ACCOUNT</p>
            <h2 className="mt-1 text-lg font-bold text-foreground">{mode === "login" ? "로그인" : "회원가입"}</h2>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-md p-2 text-muted-foreground transition-colors hover:bg-accent hover:text-foreground focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
            aria-label="닫기"
          >
            <X className="size-4" aria-hidden="true" />
          </button>
        </div>
        <div className="mt-4 grid gap-3">
          <input
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            type="email"
            placeholder="email@example.com"
            className="rounded-md border border-input bg-secondary px-3 py-2 text-sm text-foreground"
            required
          />
          {mode === "register" && (
            <input
              value={displayName}
              onChange={(event) => setDisplayName(event.target.value)}
              placeholder="표시 이름"
              className="rounded-md border border-input bg-secondary px-3 py-2 text-sm text-foreground"
            />
          )}
          <input
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            type="password"
            placeholder="비밀번호"
            className="rounded-md border border-input bg-secondary px-3 py-2 text-sm text-foreground"
            required
          />
          {error && <p className="text-sm text-destructive">{error}</p>}
          <button
            disabled={isSubmitting}
            className="rounded-md bg-primary px-4 py-2 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary/90 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {isSubmitting ? "처리 중..." : mode === "login" ? "로그인" : "회원가입"}
          </button>
          <button
            type="button"
            onClick={() => {
              setMode(mode === "login" ? "register" : "login")
              setError(null)
            }}
            className="text-sm font-semibold text-primary"
          >
            {mode === "login" ? "계정이 없으면 회원가입" : "이미 계정이 있으면 로그인"}
          </button>
        </div>
      </form>
    </div>
  )
}
