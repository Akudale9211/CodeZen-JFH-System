import React, { useEffect, useState } from "react";
import axios from "axios";
import "./style/InventoryReport.css"; // ✅ Custom CSS import

const InventoryReport = () => {
  const [report, setReport] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    axios.get("/api/inventory/report")
      .then(res => {
        setReport(res.data);
        setLoading(false);
      })
      .catch(err => {
        console.error("Error loading inventory report", err);
        setLoading(false);
      });
  }, []);

const downloadPdf = () => {
  axios.get("/api/inventory/report/pdf", {
    responseType: "blob", // Important
  })
  .then((res) => {
    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", "InventoryReport.pdf");
    document.body.appendChild(link);
    link.click();
    link.remove();
  })
  .catch((err) => {
    console.error("Failed to download PDF", err);
    alert("Could not download the PDF. Please try again later.");
  });
};


  return (
    <div className="inventory-container">
      <div className="inventory-header">
        <h2>📦 Inventory Report</h2>
        <button onClick={downloadPdf}>Download PDF</button>
      </div>

      {loading ? (
        <p className="loading-text">Loading...</p>
      ) : (
        <div className="inventory-table-wrapper">
          <table className="inventory-table">
            <thead>
              <tr>
                <th>Goods Type</th>
                <th>HSN Code</th>
                <th className="text-right">Bags</th>
                <th className="text-right">Weight (kg)</th>
                <th>Last Updated</th>
              </tr>
            </thead>
            <tbody>
              {report.length === 0 ? (
                <tr>
                  <td colSpan="5" className="no-data">No inventory data available.</td>
                </tr>
              ) : (
                report.map((item, index) => (
                  <tr key={index}>
                    <td>{item.goodsType}</td>
                    <td>{item.hsnCode}</td>
                    <td className="text-right">{item.availableBags}</td>
                    <td className="text-right">{item.availableWeight}</td>
                    <td>{new Date(item.lastUpdated).toLocaleString()}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default InventoryReport;
