import React from "react";
import { createRoot } from "react-dom/client";
import App from "./App.jsx";
import ChefManagement from "./ChefManagement.jsx";
import "./styles.css";

const isChefPage = window.location.pathname.replace(/\/+$/, "") === "/chefs";

createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    {isChefPage ? <ChefManagement /> : <App />}
  </React.StrictMode>,
);
