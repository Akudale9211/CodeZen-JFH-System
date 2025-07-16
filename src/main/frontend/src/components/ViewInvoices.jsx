import React, { useState } from "react";
import axios from "axios";
import "./style/ViewInvoices.css";

export default function ViewInvoices({ onNavigate, setInvoiceToEdit }) {
  const [invoices, setInvoices] = useState([]);
  const [date, setDate] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [selectedInvoiceId, setSelectedInvoiceId] = useState(null);
  const [pdfUrl, setPdfUrl] = useState(null);
  const [showModal, setShowModal] = useState(false);

  const fetchAllInvoices = async () => {
    setLoading(true);
    setError("");
    setSelectedInvoiceId(null);
    try {
      const response = await axios.get("/api/invoices");
      setInvoices(response.data);
    } catch (err) {
      console.error("Failed to fetch all invoices:", err);
      setError("Failed to fetch all invoices.");
    } finally {
      setLoading(false);
    }
  };

  const fetchInvoicesAfterDate = async () => {
    if (!date) {
      setError("Please select a date.");
      return;
    }
    setLoading(true);
    setError("");
    setSelectedInvoiceId(null);
    try {
      const response = await axios.get(`/api/invoices/after-date?date=${date}`);
      setInvoices(response.data);
    } catch (err) {
      console.error("Failed to fetch invoices for the selected date:", err);
      setError("Failed to fetch invoices for the selected date.");
    } finally {
      setLoading(false);
    }
  };

  const fetchAllBuyInvoices = async () => {
    setLoading(true);
    setError("");
    setSelectedInvoiceId(null);
    try {
      const response = await axios.get("/api/buyInvoices");
      setInvoices(response.data);
    } catch (err) {
      console.error("Failed to fetch all buy invoices:", err);
      setError("Failed to fetch all buying invoices.");
    } finally {
      setLoading(false);
    }
  };

  const fetchBuyInvoicesAfterDate = async () => {
    if (!date) {
      setError("Please select a date.");
      return;
    }
    setLoading(true);
    setError("");
    setSelectedInvoiceId(null);
    try {
      const response = await axios.get(`/api/buyInvoices/after-date?date=${date}`);
      setInvoices(response.data);
    } catch (err) {
      console.error("Failed to fetch buy invoices for the selected date:", err);
      setError("Failed to fetch buy invoices for the selected date.");
    } finally {
      setLoading(false);
    }
  };

  const handlePrint = async (invoiceId) => {
    try {
      const response = await axios.get(`/api/invoices/print/${invoiceId}`, {
        responseType: "blob",
      });
      const file = new Blob([response.data], { type: "application/pdf" });
      const fileURL = URL.createObjectURL(file);
      setPdfUrl(fileURL);
      setShowModal(true);
      setSelectedInvoiceId(invoiceId);
    } catch (err) {
      alert("Failed to load invoice PDF for printing.");
      console.error(err);
    }
  };

  return (
    <div className="container">
      <style>{`
        .modal {
          position: fixed;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: rgba(0,0,0,0.6);
          display: flex;
          justify-content: center;
          align-items: center;
          z-index: 999;
        }

        .modal-content {
          background: white;
          border-radius: 8px;
          width: 90%;
          max-width: 800px;
          height: 90%;
          position: relative;
          display: flex;
          flex-direction: column;
        }

        .modal-header {
          padding: 10px;
          display: flex;
          justify-content: space-between;
          align-items: center;
          border-bottom: 1px solid #ccc;
        }

        .modal-body {
          flex: 1;
          overflow: hidden;
        }

        .close-btn {
          background: red;
          color: white;
          border: none;
          padding: 5px 10px;
          border-radius: 4px;
          cursor: pointer;
        }
      `}</style>

      <h2 className="title">Invoice Viewer</h2>

      <div className="controls">
        <div className="button-group">
          <button className="button blue" onClick={fetchAllInvoices}>Get All Sale Invoices</button>
          <button className="button blue" onClick={fetchAllBuyInvoices}>Get All Buy Invoices</button>
        </div>

        <div className="date-picker-group">
          <label htmlFor="datePicker" className="date-label">Get Invoices On or After Date</label>
          <input
            id="datePicker"
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
            className="date-input"
          />
        </div>

        <div className="button-group">
          <button className="button blue" onClick={fetchInvoicesAfterDate} disabled={!date}>Sales Invoices</button>
          <button className="button blue" onClick={fetchBuyInvoicesAfterDate} disabled={!date}>Purches Invoices</button>
        </div>
      </div>

      {loading && <p className="loading">Loading...</p>}
      {error && <p className="error">{error}</p>}

      {invoices.length > 0 ? (
        <div className="table-container">
          <table className="invoice-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Customer</th>
                <th>Vehicle</th>
                <th>Transport</th>
                <th>Wages</th>
                <th>SGST / CGST</th>
                <th>Total</th>
                <th>Final</th>
                <th>Items</th>
                <th>Print</th>
                <th>Edit</th>
                <th>Delete</th>
              </tr>
            </thead>
            <tbody>
              {invoices.map((invoice) => (
                <tr key={invoice.id}>
                  <td>{invoice.id}</td>
                  <td>{invoice.customerName}</td>
                  <td>{invoice.vehicleNumber}</td>
                  <td>₹{(invoice.transportCost ?? 0).toFixed(2)}</td>
                  <td>₹{(invoice.wages ?? 0).toFixed(2)}</td>
                  <td>₹{(invoice.sgst ?? 0).toFixed(2)} / ₹{(invoice.cgst ?? 0).toFixed(2)}</td>
                  <td>₹{(invoice.totalAmount ?? 0).toFixed(2)}</td>
                  <td>₹{(invoice.finalAmount ?? 0).toFixed(2)}</td>
                  <td>
                    {(invoice.invoiceItems || []).map((item, i) => (
                      <div key={i} className="item">
                        {item.goodsType}, {item.noOfBags} bags, {(item.totalWeight ?? 0).toFixed(2)} kg @ ₹
                        {(item.rate ?? 0).toFixed(2)}
                      </div>
                    ))}
                  </td>
                  <td>
                    <button className="button blue" onClick={() => handlePrint(invoice.id)}>Print</button>
                  </td>
                  <td>
                    <button
                      className="button blue"
                      onClick={async () => {
                        try {
                          const response = await axios.get(`/api/invoices/${invoice.id}`);
                          setInvoiceToEdit(response.data);
                          onNavigate("edit");
                        } catch (error) {
                          alert("Failed to fetch invoice details for editing.");
                          console.error(error);
                        }
                      }}
                    >
                      Edit
                    </button>
                  </td>
                  <td>
                    <button
                      className="button red"
                      onClick={async () => {
                        if (window.confirm("Are you sure you want to delete this invoice?")) {
                          try {
                            await axios.delete(`/api/invoices/${invoice.id}`);
                            setInvoices(invoices.filter(inv => inv.id !== invoice.id));
                          } catch (error) {
                            alert("Failed to delete invoice.");
                            console.error(error);
                          }
                        }
                      }}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        !loading && <p className="no-data">No invoices found.</p>
      )}

      {/* Modal for PDF */}
      {showModal && (
        <div className="modal">
          <div className="modal-content">
            <div className="modal-header">
              <h3>Invoice Preview</h3>
              <button className="close-btn" onClick={() => setShowModal(false)}>X</button>
            </div>
            <div className="modal-body">
              <iframe
                src={pdfUrl}
                width="100%"
                height="100%"
                title="Invoice PDF"
                style={{ border: "none" }}
              ></iframe>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
