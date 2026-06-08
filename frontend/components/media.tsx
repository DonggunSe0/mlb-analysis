"use client"

import { useState } from "react"
import { cn } from "@/lib/utils"
import { teamLogoUrl, playerHeadshotUrl, initials } from "@/lib/images"

/** 팀 로고. 로드 실패 시 팀 약어/이니셜로 폴백 */
export function TeamLogo({
  teamId,
  name,
  abbr,
  className,
}: {
  teamId: number | null | undefined
  name: string | null | undefined
  abbr?: string | null
  className?: string
}) {
  const [failed, setFailed] = useState(false)
  const url = teamLogoUrl(teamId)
  const fallback = abbr ?? initials(name)

  if (!url || failed) {
    return (
      <span
        className={cn(
          "flex items-center justify-center rounded-full bg-secondary text-[11px] font-bold text-secondary-foreground",
          className,
        )}
        aria-hidden="true"
      >
        {fallback}
      </span>
    )
  }

  return (
    <img
      src={url || "/placeholder.svg"}
      alt={name ? `${name} 로고` : "팀 로고"}
      width={40}
      height={40}
      loading="lazy"
      crossOrigin="anonymous"
      onError={() => setFailed(true)}
      className={cn("object-contain", className)}
    />
  )
}

/** 선수 헤드샷. 로드 실패 시 이니셜로 폴백 */
export function PlayerAvatar({
  playerId,
  name,
  size = 120,
  className,
}: {
  playerId: number | null | undefined
  name: string | null | undefined
  size?: number
  className?: string
}) {
  const [failed, setFailed] = useState(false)
  const url = playerHeadshotUrl(playerId, size)

  if (!url || failed) {
    return (
      <span
        className={cn(
          "flex items-center justify-center rounded-full bg-accent font-bold text-accent-foreground",
          className,
        )}
        aria-hidden="true"
      >
        {initials(name)}
      </span>
    )
  }

  return (
    <img
      src={url || "/placeholder.svg"}
      alt={name ? `${name} 선수 사진` : "선수 사진"}
      loading="lazy"
      crossOrigin="anonymous"
      onError={() => setFailed(true)}
      className={cn("rounded-full bg-secondary object-cover", className)}
    />
  )
}
