/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: {
          DEFAULT: '#1F3A3D',
          light: '#2C4F53',
          dark: '#152A2C',
        },
        marigold: {
          DEFAULT: '#E8A33D',
          light: '#F4C878',
          dark: '#C3821F',
        },
        vermilion: {
          DEFAULT: '#C1440E',
          light: '#E2653A',
        },
        moss: {
          DEFAULT: '#3D8361',
          light: '#5FA37F',
        },
        paper: '#FBFAF8',
        surface: '#FFFFFF',
        hairline: '#E7E2DA',
        ink50: '#6B6560',
        ink30: '#9A948C',
      },
      fontFamily: {
        display: ['Fraunces', 'Georgia', 'serif'],
        sans: ['Inter', 'ui-sans-serif', 'system-ui', 'sans-serif'],
        mono: ['"IBM Plex Mono"', 'ui-monospace', 'monospace'],
      },
      borderRadius: {
        card: '10px',
      },
      boxShadow: {
        card: '0 1px 2px rgba(31,58,61,0.06), 0 1px 8px rgba(31,58,61,0.04)',
      },
    },
  },
  plugins: [],
};
