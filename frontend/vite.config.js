import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
          target: 'http://localhost:8080', // Your backend URL
          changeOrigin: true,
          secure: false, // Set to false if you're using HTTP instead of HTTPS
          rewrite: (path) => path.replace(/^\/api/, '') // Forward to the backend without /api
      }
  }
  }
});