/** @type {import('next').NextConfig} */
const backendUrl = process.env.MLB_BACKEND_URL ?? 'http://localhost:8080'

const nextConfig = {
  images: {
    unoptimized: true,
  },
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: `${backendUrl}/api/:path*`,
      },
    ]
  },
}

export default nextConfig
