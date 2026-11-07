import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

const backend = process.env.VITE_API_PROXY_TARGET || "http://localhost:8080";

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      "/item": backend,
      "/orders": backend,
      "/chefs": backend,
    },
  },
});
