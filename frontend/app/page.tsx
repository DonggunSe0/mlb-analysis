"use client"

import { useState } from "react"
import { SiteHeader, type Section } from "@/components/site-header"
import { GamesSection } from "@/components/games-section"
import { TeamsSection } from "@/components/teams-section"
import { PlayersSection } from "@/components/players-section"
import { NewsSection } from "@/components/news-section"
import { AllStarVoteSection } from "@/components/all-star-vote-section"
import { AUTH_TOKEN_KEY } from "@/lib/api"

function todayStr() {
  return new Date().toISOString().slice(0, 10)
}

export default function Page() {
  const [section, setSection] = useState<Section>("games")
  const [date, setDate] = useState(todayStr())
  const [token, setToken] = useState<string | null>(() =>
    typeof window === "undefined" ? null : localStorage.getItem(AUTH_TOKEN_KEY),
  )

  function handleAuthChange(nextToken: string | null) {
    setToken(nextToken)
  }

  return (
    <div className="min-h-screen bg-background">
      <SiteHeader active={section} token={token} onAuthChange={handleAuthChange} onSelect={setSection} />
      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6">
        {section === "games" && <GamesSection date={date} onDateChange={setDate} />}
        {section === "teams" && <TeamsSection />}
        {section === "players" && <PlayersSection />}
        {section === "news" && <NewsSection />}
        {section === "allstar" && <AllStarVoteSection token={token} />}
      </main>
      <footer className="border-t border-border">
        <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6">
          <p className="text-xs text-muted-foreground">
            데이터 출처: MLB Stats API · 본 대시보드는 포트폴리오/학습용 MVP입니다.
          </p>
        </div>
      </footer>
    </div>
  )
}
