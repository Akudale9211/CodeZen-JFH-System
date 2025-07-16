import React from "react";
import "./style/Header.css";

export default function Header({ onNavigate, onLogoutClick }) {
  return (
    <header className="header">
      <h1 className="header-title">🧾 Jarad Farm House</h1>
      <nav className="header-buttons">
        <button className="btn nav-btn" onClick={() => onNavigate("create")}>Sell Invoice</button>
        <button className="btn nav-btn" onClick={() => onNavigate("buy")}>Buy Invoice</button>
        <button className="btn nav-btn" onClick={() => onNavigate("view")}>View Invoices</button>
        <button className="btn nav-btn" onClick={() => onNavigate("report")}>Inventory Report</button>
        <button className="btn nav-btn" onClick={() => onNavigate("admin")}>BackUp</button>
        <button className="btn logout" onClick={onLogoutClick}>Logout</button>
      </nav>
    </header>
  );
}
