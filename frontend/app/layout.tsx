import type { Metadata } from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: 'MLB Analysis — 메이저리그 데이터 대시보드',
  description: 'MLB Stats API 기반 경기 일정, 스코어보드, 팀·선수 데이터를 빠르게 탐색하는 스포츠 대시보드',
  icons: {
    icon: [
      {
        url: '/icon-light-32x32.png',
        media: '(prefers-color-scheme: light)',
      },
      {
        url: '/icon-dark-32x32.png',
        media: '(prefers-color-scheme: dark)',
      },
      {
        url: '/icon.svg',
        type: 'image/svg+xml',
      },
    ],
    apple: '/apple-icon.png',
  },
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <html lang="ko" className="bg-background">
      <body className="bg-background font-sans antialiased">{children}</body>
    </html>
  )
}
