/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        mlb: {
          navy: '#07111f',
          panel: '#101b2d',
          card: '#f8fafc',
          red: '#d71920',
          blue: '#0b3d91',
          gold: '#f5b301',
        },
      },
      boxShadow: {
        scoreboard: '0 20px 45px rgba(3, 7, 18, 0.18)',
      },
    },
  },
  plugins: [],
}
