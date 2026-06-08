"use client"

import { cn } from "@/lib/utils"

export type Section = "games" | "teams" | "players" | "news" | "allstar"

const items: { id: Section; label: string }[] = [
  { id: "games", label: "오늘의 경기" },
  { id: "teams", label: "팀" },
  { id: "players", label: "선수 검색" },
  { id: "news", label: "뉴스" },
  { id: "allstar", label: "올스타 투표" },
]

export function SiteHeader({ active, onSelect }: { active: Section; onSelect: (s: Section) => void }) {
  return (
    <header className="sticky top-0 z-30 border-b border-border bg-sidebar/95 backdrop-blur supports-[backdrop-filter]:bg-sidebar/80">
      <div className="mx-auto flex h-16 max-w-7xl items-center gap-6 px-4 sm:px-6">
        <div className="flex items-center gap-2.5">
          <span className="flex size-8 items-center justify-center rounded-md bg-primary text-sm font-bold text-primary-foreground">
            MLB
          </span>
          <div className="leading-tight">
            <p className="text-sm font-bold tracking-tight text-foreground">MLB Analysis</p>
            <p className="text-[11px] text-muted-foreground">메이저리그 데이터 대시보드</p>
          </div>
        </div>

        <nav aria-label="주요 섹션" className="ml-auto flex items-center gap-1">
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
      </div>
    </header>
  )
}
