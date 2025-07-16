import React, { useState } from "react";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import DialogActions from "@mui/material/DialogActions";
import Button from "@mui/material/Button";
import './style/AdminPanel.css';

export default function AdminPanel() {
  const [dialogOpen, setDialogOpen] = useState(false);
  const [dialogMsg, setDialogMsg] = useState("");

  const showDialog = (msg) => {
    setDialogMsg(msg);
    setDialogOpen(true);
  };

  const handleLocalBackup = async () => {
    try {
      const res = await fetch("http://localhost:8080/api/history/export-to-file");
      if (res.ok) {
        showDialog("Local backup successful!");
      } else {
        showDialog("Local backup failed!");
      }
    } catch (err) {
      showDialog("Error during local backup!");
    }
  };

  const handleGoogleDriveBackup = async () => {
    try {
      const res = await fetch("http://localhost:8080/api/history/upload");
      if (res.ok) {
        showDialog("Google Drive backup successful!");
      } else {
        showDialog("Google Drive backup failed!");
      }
    } catch (err) {
      showDialog("Error during Google Drive backup!");
    }
  };

  return (
    <div className="admin-panel-container">
      <h2 className="admin-panel-title">BackUp</h2>
      <button className="admin-panel-btn" onClick={handleLocalBackup}>
        Take Backup on Local Drive
      </button>
      <button className="admin-panel-btn" onClick={handleGoogleDriveBackup}>
        Backup on Google Drive
      </button>
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)}>
        <DialogTitle>Notification</DialogTitle>
        <DialogContent>{dialogMsg}</DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>OK</Button>
        </DialogActions>
      </Dialog>
    </div>
  );
}