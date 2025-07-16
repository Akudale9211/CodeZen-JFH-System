import React, { useState } from "react";
import Login from "./components/Login";
import Header from "./components/Header";
import InvoiceForm from "./components/InvoiceForm";
import ViewInvoices from "./components/ViewInvoices";
import EditInvoice from "./components/EditInvoice";
import BuyForm from "./components/BuyForm";
import InventoryReport from "./components/InventoryReport";
import AdminPanel from "./components/AdminPanel";

export default function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [currentPage, setCurrentPage] = useState("home");
  const [invoiceToEdit, setInvoiceToEdit] = useState(null);

  const handleLogin = (token) => {
    localStorage.setItem("jwtToken", token);
    setIsLoggedIn(true);
    setCurrentPage("create");
  };

  const handleLogout = () => {
    localStorage.removeItem("jwtToken");
    setIsLoggedIn(false);
    setCurrentPage("home");
    setInvoiceToEdit(null);
  };

  const handleLocalBackup = () => {
    fetch("/api/backup/local", {
      headers: { Authorization: `Bearer ${localStorage.getItem("jwtToken")}` }
    })
      .then(res => res.ok ? alert("Local backup successful!") : alert("Backup failed!"));
  };

  const handleGoogleDriveBackup = () => {
    fetch("/api/backup/google-drive", {
      headers: { Authorization: `Bearer ${localStorage.getItem("jwtToken")}` }
    })
      .then(res => res.ok ? alert("Google Drive backup successful!") : alert("Backup failed!"));
  };

  const renderPage = () => {
    if (!isLoggedIn) {
      return <Login onLogin={handleLogin} />;
    }

    switch (currentPage) {
      case "create":
        return <InvoiceForm onNavigate={setCurrentPage} />;
      case "buy":
        return <BuyForm onNavigate={setCurrentPage} />;
      case "edit":
        return (
          <EditInvoice
            invoiceData={invoiceToEdit}
            onNavigate={setCurrentPage}
          />
        );
      case "view":
        return (
          <ViewInvoices
            onNavigate={setCurrentPage}
            setInvoiceToEdit={setInvoiceToEdit}
          />
        );
      case "report":
        return <InventoryReport onNavigate={setCurrentPage} />;
      case "admin":
        return (
          <AdminPanel
            onLocalBackup={handleLocalBackup}
            onGoogleDriveBackup={handleGoogleDriveBackup}
          />
        );
      default:
        return <InvoiceForm onNavigate={setCurrentPage} />;
    }
  };

  return (
    <div>
      {isLoggedIn && (
        <Header
          onHomeClick={() => setCurrentPage("create")}
          onLogoutClick={handleLogout}
          onNavigate={setCurrentPage}
        />
      )}
      {renderPage()}
    </div>
  );
}