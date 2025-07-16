import React, { useState, useEffect } from "react";
import "./style/InvoiceForm.css";

const itemOptions = [
  { label: "Gahu", hsnCode: "1001" },
  { label: "Bajara", hsnCode: "1008" },
  { label: "Nachani", hsnCode: "1008" },
  { label: "Jawar", hsnCode: "1008" },
  { label: "Moong", hsnCode: "0713" },
  { label: "Harbhara ", hsnCode: "0713" },
  { label: "Turr ", hsnCode: "0713" },
  { label: "Rice", hsnCode: "1006" },
  { label: "Makka", hsnCode: "1005" },
  { label: "Udid", hsnCode: "0713" },
  { label: "Broken Rice", hsnCode: "1006" },
  { label: "Other", hsnCode: "" },
];

const initialItem = {
  goodsType: "",
  hsnCode: "",
  noOfBags: "",
  weightPerBag: "",
  rate: "",
  customGoodsType: "",
};

const getLocalTodayISO = () => {
  const tzoffset = new Date().getTimezoneOffset() * 60000;
  return new Date(Date.now() - tzoffset).toISOString().split("T")[0];
};

export default function InvoiceForm() {
  const today = getLocalTodayISO();

  const [form, setForm] = useState({
    fullName: "",
    address: "",
    email: "",
    gstNumber: "",
    contactNumber: "",
    transportCost: "",
    wages: "",
    vehicleNumber: "",
    invoiceDate: "",
    items: [{ ...initialItem }],
    gstApplicable: true,
    cgstApplicable: true,
  });

  const [pdfBlob, setPdfBlob] = useState(null);
  const [showPreview, setShowPreview] = useState(false);

  // Handle simple input changes
  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  // Handle checkboxes
  const handleCheckboxChange = (e) => {
    const { name, checked } = e.target;
    setForm((prev) => ({ ...prev, [name]: checked }));
  };

  // Handle item input changes
  const handleItemChange = (index, e) => {
    const { name, value } = e.target;
    setForm((prev) => {
      const newItems = [...prev.items];
      if (name === "goodsType") {
        newItems[index].goodsType = value;
        if (value === "Other") {
          newItems[index].hsnCode = "";
          newItems[index].customGoodsType = "";
        } else {
          const selected = itemOptions.find((item) => item.label === value);
          newItems[index].hsnCode = selected ? selected.hsnCode : "";
          newItems[index].customGoodsType = "";
        }
      } else {
        newItems[index][name] = value;
      }
      return { ...prev, items: newItems };
    });
  };

  // Add new item
  const addItem = () => {
    setForm((prev) => ({ ...prev, items: [...prev.items, { ...initialItem }] }));
  };

  // Remove item
  const removeItem = (index) => {
    setForm((prev) => ({
      ...prev,
      items: prev.items.filter((_, i) => i !== index),
    }));
  };

  // Submit form
  const handleSubmit = async (e) => {
    e.preventDefault();

    // Use today's date if none selected
    const invoiceDate = form.invoiceDate || today;

    const payload = {
      ...form,
      invoiceDate,
      transportCost: parseFloat(form.transportCost) || 0,
      wages: parseFloat(form.wages) || 0,
      items: form.items.map((item) => ({
        goodsType: item.goodsType === "Other" ? item.customGoodsType : item.goodsType,
        hsnCode: item.hsnCode,
        noOfBags: parseInt(item.noOfBags) || 0,
        weightPerBag: parseFloat(item.weightPerBag) || 0,
        rate: parseFloat(item.rate) || 0,
      })),
    };

    try {
      const response = await fetch("/api/buyInvoices", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (!response.ok) throw new Error("Invoice generation failed");

      const blob = await response.blob();
      setPdfBlob(blob);
      setShowPreview(true);
    } catch (err) {
      alert("Failed to create invoice");
      console.error(err);
    }
  };

  // Cleanup URL object on blob change or unmount
  useEffect(() => {
    return () => {
      if (pdfBlob) {
        URL.revokeObjectURL(pdfBlob);
      }
    };
  }, [pdfBlob]);

  return (
    <div className="invoice-form-container">
      {!showPreview ? (
        <>
          <h1 style={{ textAlign: "center", marginBottom: "20px" }}>Buy Invoice</h1>
          <form onSubmit={handleSubmit} className="invoice-form">
            <fieldset>
              <legend>Customer Details</legend>
              <div className="form-grid">
                <div className="form-group">
                  <label htmlFor="fullName">Full Name</label>
                  <input
                    id="fullName"
                    type="text"
                    name="fullName"
                    value={form.fullName}
                    onChange={handleChange}
                    required
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="address">Address</label>
                  <input
                    id="address"
                    type="text"
                    name="address"
                    value={form.address}
                    onChange={handleChange}
                    required
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="gstNumber">GST Number</label>
                  <input
                    id="gstNumber"
                    type="text"
                    name="gstNumber"
                    value={form.gstNumber}
                    onChange={handleChange}
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="contactNumber">Contact Number</label>
                  <input
                    id="contactNumber"
                    type="tel"
                    name="contactNumber"
                    value={form.contactNumber}
                    onChange={handleChange}
                    required
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="email">Email</label>
                  <input
                    id="email"
                    type="email"
                    name="email"
                    value={form.email}
                    onChange={handleChange}
                  />
                </div>
              </div>
            </fieldset>

            <fieldset>
              <legend>Invoice Details</legend>
              <div className="form-grid">
                <div className="form-group">
                  <label htmlFor="invoiceDate">Invoice Date</label>
                  <input
                    id="invoiceDate"
                    type="date"
                    name="invoiceDate"
                    value={form.invoiceDate}
                    onChange={handleChange}
                    max={today}
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="vehicleNumber">Vehicle Number</label>
                  <input
                    id="vehicleNumber"
                    type="text"
                    name="vehicleNumber"
                    value={form.vehicleNumber}
                    onChange={handleChange}
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="transportCost">Transport Cost</label>
                  <input
                    id="transportCost"
                    type="number"
                    step="0.01"
                    name="transportCost"
                    value={form.transportCost}
                    onChange={handleChange}
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="wages">Labour Cost</label>
                  <input
                    id="wages"
                    type="number"
                    step="0.01"
                    name="wages"
                    value={form.wages}
                    onChange={handleChange}
                  />
                </div>
              </div>

              <div className="tax-section">
                <label>
                  <input
                    type="checkbox"
                    name="gstApplicable"
                    checked={form.gstApplicable}
                    onChange={handleCheckboxChange}
                  />
                  Apply GST
                </label>
                <label>
                  <input
                    type="checkbox"
                    name="cgstApplicable"
                    checked={form.cgstApplicable}
                    onChange={handleCheckboxChange}
                  />
                  Apply CGST
                </label>
              </div>
            </fieldset>

            <fieldset>
              <legend>Items</legend>
              {form.items.map((item, index) => (
                <div key={index} className="item-row">
                  <select
                    name="goodsType"
                    value={item.goodsType}
                    onChange={(e) => handleItemChange(index, e)}
                    required
                  >
                    <option value="">Select Item</option>
                    {itemOptions.map((opt) => (
                      <option key={opt.label} value={opt.label}>
                        {opt.label}
                      </option>
                    ))}
                  </select>

                  {item.goodsType === "Other" ? (
                    <>
                      <input
                        type="text"
                        name="customGoodsType"
                        placeholder="Enter Other Item"
                        value={item.customGoodsType}
                        onChange={(e) => handleItemChange(index, e)}
                        required
                      />
                      <input
                        type="text"
                        name="hsnCode"
                        placeholder="Enter HSN Code"
                        value={item.hsnCode}
                        onChange={(e) => handleItemChange(index, e)}
                        required
                      />
                    </>
                  ) : (
                    <input
                      type="text"
                      name="hsnCode"
                      placeholder="HSN Code"
                      value={item.hsnCode}
                      readOnly
                      disabled
                    />
                  )}

                  <input
                    type="number"
                    name="noOfBags"
                    placeholder="Quantity"
                    value={item.noOfBags}
                    onChange={(e) => handleItemChange(index, e)}
                    required
                  />
                  <input
                    type="number"
                    step="0.01"
                    name="weightPerBag"
                    placeholder="Net Weight"
                    value={item.weightPerBag}
                    onChange={(e) => handleItemChange(index, e)}
                    required
                  />
                  <input
                    type="number"
                    step="0.01"
                    name="rate"
                    placeholder="Rate As Per Quintels"
                    value={item.rate}
                    onChange={(e) => handleItemChange(index, e)}
                    required
                  />

                  <button
                    type="button"
                    className="remove-btn"
                    onClick={() => removeItem(index)}
                    disabled={form.items.length === 1}
                  >
                    &times;
                  </button>
                </div>
              ))}
              <button type="button" className="add-btn" onClick={addItem}>
                + Add Item
              </button>
            </fieldset>

            <button type="submit" className="submit-btn">
              Create Invoice
            </button>
          </form>
        </>
      ) : (
        <div className="pdf-preview">
          <div className="pdf-actions">
            <button onClick={() => setShowPreview(false)}>✏️ Edit Invoice</button>
            <button
              onClick={() => {
                const iframe = document.getElementById("pdf-frame");
                if (iframe) {
                  iframe.contentWindow.focus();
                  iframe.contentWindow.print();
                }
              }}
            >
              🖨️ Print Invoice
            </button>
          </div>
          <iframe
            id="pdf-frame"
            src={pdfBlob ? URL.createObjectURL(pdfBlob) : ""}
            title="Invoice PDF"
            width="100%"
            height="600px"
            style={{ border: "none" }}
          />
        </div>
      )}
    </div>
  );
}
