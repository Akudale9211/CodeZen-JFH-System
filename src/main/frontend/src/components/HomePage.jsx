import React from "react";
import "./style/HomePage.css";

export default function HomePage({ onNavigate }) {
  return (
    <div className="home-container">
      <h2>Welcome</h2>
      <div className="home-buttons">
        <button onClick={() => onNavigate("create")}>Create New Invoice</button>
{/*         <button onClick={() => onNavigate("edit")}>Edit Existing Invoice</button> */}
        <button onClick={() => onNavigate("view")}>View All Invoices</button>
      </div>
    </div>
  );
}
