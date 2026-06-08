import { cn } from "@/lib/utils"

type Tone = "live" | "final" | "scheduled" | "neutral"

const toneStyles: Record<Tone, string> = {
  live: "bg-primary/15 text-primary border-primary/40",
  final: "bg-muted text-muted-foreground border-border",
  scheduled: "bg-chart-2/15 text-chart-2 border-chart-2/40",
  neutral: "bg-secondary text-secondary-foreground border-border",
}

// 상태 문자열을 한국어 라벨 + 톤으로 매핑 (색상만으로 전달하지 않음)
export function getStatusInfo(status: string | null): { label: string; tone: Tone } {
  const s = (status ?? "").toLowerCase()
  if (s.includes("final") || s.includes("completed") || s.includes("game over")) {
    return { label: "경기 종료", tone: "final" }
  }
  if (s.includes("in progress") || s.includes("live") || s.includes("warmup")) {
    return { label: "경기 중", tone: "live" }
  }
  if (s.includes("scheduled") || s.includes("pre-game") || s.includes("preview")) {
    return { label: "경기 예정", tone: "scheduled" }
  }
  if (s.includes("postponed") || s.includes("cancelled") || s.includes("suspended")) {
    return { label: "연기/취소", tone: "neutral" }
  }
  return { label: status ?? "정보 없음", tone: "neutral" }
}

export function StatusBadge({ status, className }: { status: string | null; className?: string }) {
  const { label, tone } = getStatusInfo(status)
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 rounded-full border px-2.5 py-0.5 text-xs font-medium",
        toneStyles[tone],
        className,
      )}
    >
      {tone === "live" && (
        <span className="relative flex size-2">
          <span className="absolute inline-flex size-full animate-ping rounded-full bg-primary opacity-75" />
          <span className="relative inline-flex size-2 rounded-full bg-primary" />
        </span>
      )}
      {label}
    </span>
  )
}
