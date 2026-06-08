"use client"

import useSWR from "swr"
import { endpoints, fetcher, type NewsItem } from "@/lib/api"
import { EmptyState, ErrorState, LoadingState } from "@/components/states"
import { Newspaper, ExternalLink } from "lucide-react"

export function NewsSection() {
  const { data, error, isLoading, mutate } = useSWR<NewsItem[]>(endpoints.news(10), fetcher, {
    revalidateOnFocus: false,
  })
  const news = data ?? []

  return (
    <section aria-labelledby="news-heading" className="space-y-6">
      <div>
        <p className="text-xs font-semibold uppercase tracking-wider text-primary">NEWS</p>
        <h1 id="news-heading" className="mt-1 text-2xl font-bold tracking-tight text-foreground">MLB 뉴스</h1>
        <p className="mt-1 text-sm text-muted-foreground">MLB 공식 RSS 기반 최신 기사입니다.</p>
      </div>
      {isLoading && <LoadingState label="MLB 뉴스를 불러오는 중..." />}
      {error && <ErrorState message="뉴스를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요." onRetry={() => mutate()} />}
      {!isLoading && !error && news.length === 0 && <EmptyState message="표시할 뉴스가 없습니다." />}
      {!isLoading && !error && news.length > 0 && (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          {news.map((item, index) => (
            <article key={`${item.link ?? item.title}-${index}`} className="rounded-2xl border border-border bg-card p-5 shadow-sm transition-colors hover:border-primary/40">
              <div className="flex items-start gap-3">
                <span className="flex size-10 shrink-0 items-center justify-center rounded-full bg-primary/10 text-primary">
                  <Newspaper className="size-5" aria-hidden="true" />
                </span>
                <div className="min-w-0 flex-1">
                  <h2 className="text-lg font-bold leading-snug text-foreground">{item.title ?? "제목 없음"}</h2>
                  <p className="mt-2 line-clamp-3 text-sm leading-6 text-muted-foreground">{item.summary ?? "요약 없음"}</p>
                  <div className="mt-4 flex flex-wrap items-center justify-between gap-3">
                    <span className="text-xs text-muted-foreground">{item.publishedAt ?? "발행일 정보 없음"}</span>
                    {item.link && (
                      <a href={item.link} target="_blank" rel="noreferrer" className="inline-flex items-center gap-1 rounded-md bg-secondary px-3 py-1.5 text-xs font-semibold text-secondary-foreground hover:bg-accent focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring">
                        기사 보기 <ExternalLink className="size-3" aria-hidden="true" />
                      </a>
                    )}
                  </div>
                </div>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  )
}
