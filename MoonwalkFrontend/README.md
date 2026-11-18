# Moonwalk Frontend

React frontend for browsing the Moonwalk menu, creating an order, tracking active order preparation countdowns, and managing chefs.

## Run locally

Use Node.js 18 or newer.

1. Start the Spring Boot backend on `http://localhost:8080`.
2. Ensure at least one chef has been registered. The order service needs a chef to assign preparation work.
3. From this folder, install dependencies and start Vite:

   ```sh
   npm install
   npm run dev
   ```

Vite proxies `/item`, `/orders`, and `/chefs` requests to the backend at `http://localhost:8080`.

Use **Manage chefs** in the menu page header to open `/chefs`, where you can view, add, and delete kitchen staff. Vite serves the frontend route while proxying chef API requests under `/chefs/` to Spring Boot.

Set `VITE_API_PROXY_TARGET` to change the Vite proxy target, or set `VITE_API_BASE_URL` to use a direct API base URL from the browser.

Orders can contain each menu item once because the backend identifies an order item by the combination of order ID and item ID.
