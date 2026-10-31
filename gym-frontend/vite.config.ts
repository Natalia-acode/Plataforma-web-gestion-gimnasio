import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// El backend solo permite CORS desde http://localhost:5173, por eso el puerto es fijo.
export default defineConfig({
  plugins: [react()],
  server: { port: 5173, strictPort: true },
})
