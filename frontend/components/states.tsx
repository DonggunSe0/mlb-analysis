import { cn } from "@/lib/utils"
import { Loader2, AlertCircle, Inbox } from "lucide-react"

export function LoadingState({ label = "불러오는 중...", className }: { label?: string; className?: string }) {
  return (
    <div
      role="status"
      aria-live="polite"
      className={cn("flex flex-col items-center justify-center gap-3 py-12 text-muted-foreground", className)}
    >
      <Loader2 className="size-6 animate-spin text-primary" aria-hidden="true" />
      <p className="text-sm">{label}</p>
    </div>
  )
}

export function ErrorState({
  message = "데이터를 불러오지 못했습니다. 백엔드 서버 상태를 확인해 주세요.",
  onRetry,
  className,
}: {
  message?: string
  onRetry?: () => void
  className?: string
}) {
  return (
    <div
      role="alert"
      className={cn(
        "flex flex-col items-center justify-center gap-3 rounded-lg border border-primary/30 bg-primary/5 py-12 text-center",
        className,
      )}
    >
      <AlertCircle className="size-6 text-primary" aria-hidden="true" />
      <p className="max-w-sm text-sm text-foreground">{message}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="rounded-md border border-border bg-secondary px-3 py-1.5 text-sm font-medium text-secondary-foreground transition-colors hover:bg-accent focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
        >
          다시 시도
        </button>
      )}
    </div>
  )
}

export function EmptyState({ message, className }: { message: string; className?: string }) {
  return (
    <div className={cn("flex flex-col items-center justify-center gap-3 py-12 text-center text-muted-foreground", className)}>
      <Inbox className="size-6" aria-hidden="true" />
      <p className="max-w-sm text-sm">{message}</p>
    </div>
  )
}
