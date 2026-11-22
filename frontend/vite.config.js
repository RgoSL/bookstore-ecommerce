import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// O front-end chama /api/... e o Vite encaminha para o Spring Boot (porta 8080),
// removendo o prefixo /api. Assim não há problema de CORS.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
