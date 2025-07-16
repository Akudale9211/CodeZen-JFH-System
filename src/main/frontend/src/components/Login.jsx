import React, { useState } from "react";
import "./style/Login.css";

export default function Login({ onLogin }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });
      if (response.ok) {
        const data = await response.json();
        onLogin(data.token);
      } else {
        setError("Invalid credentials");
      }
    } catch (err) {
      setError("Login error");
    }
  };

  return (
    <div
      className="login-container"
      style={{
        minHeight: "100vh",
        background: "linear-gradient(to right, #e0f7fa, #b2ebf2)",
        display: "flex",
        flexDirection: "column",
        justifyContent: "center",
        alignItems: "center",
        position: "relative",
        paddingBottom: "60px",
      }}
    >
      <div className="login-box" style={{ animation: "fadeIn 1s" }}>

        <h1 style={{ color: "#0056b3", marginBottom: 8 }}>Welcome to Jarad Farm House</h1>
        <h2 style={{ color: "#555", fontWeight: 400, marginBottom: 24 }}>Sign in to continue</h2>
        {error && <p style={{ color: "red" }}>{error}</p>}
        <form onSubmit={handleSubmit}>
          <input
            type="text"
            placeholder="Username"
            required
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="login-input"
          />
          <input
            type="password"
            placeholder="Password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="login-input"
          />
          <button type="submit" className="login-btn">Login</button>
        </form>
      </div>
      <footer
        style={{
          position: "fixed",
          bottom: 0,
          left: 0,
          width: "100%",
          backgroundColor: "#fff8f8",
          color: "#b10000",
          fontWeight: "bold",
          fontSize: "13px",
          textAlign: "center",
          padding: "10px 15px",
          borderTop: "1px solid #b10000",
          boxShadow: "0 -2px 5px rgba(177, 0, 0, 0.2)",
          zIndex: 1000,
        }}
      >
        ⚠️ This software is licensed for use by a single authorized user only. Sharing access or credentials may result in data corruption, unauthorized access, or permanent data loss.
      </footer>
    </div>
  );
}