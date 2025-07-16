import React, { useEffect, useState } from "react";
import axios from "axios";

const itemOptions = [
  { label: "Gahu", hsnCode: "1001" },
  { label: "Bajara", hsnCode: "1008" },
  { label: "Nachani", hsnCode: "1008" },
  { label: "Jawar", hsnCode: "1008" },
  { label: "Moong", hsnCode: "0713" },
  { label: "Harbhara", hsnCode: "0713" },
  { label: "Turr", hsnCode: "0713" },
  { label: "Rice", hsnCode: "1006" },
  { label: "Makka", hsnCode: "1005" },
  { label: "Udid", hsnCode: "0713" },
  { label: "Broken Rice", hsnCode: "1006" },
  { label: "Other", hsnCode: "" },
];

export default function EditInvoice({ invoiceData, onNavigate }) {
  const [form, setForm] = useState({
    id: "",
    invoiceType: "",
    customerName: "",
    customerAddress: "",
    customerGstNumber: "",
    customerContact: "",
    email: "",
    invoiceDate: "",
    vehicleNumber: "",
    transportCost: "",
    wages: "",
    discount: "",
    applyGst: false,
    applyCgst: false,
    invoiceItems: [],
  });

  const [pdfUrl, setPdfUrl] = useState(null);
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    if (invoiceData) {
      setForm({
        id: invoiceData.id || "",
        invoiceType: invoiceData.invoiceType || "",
        customerName: invoiceData.fullName || "",
        customerAddress: invoiceData.address || "",
        customerGstNumber: invoiceData.gstNumber || "",
        customerContact: invoiceData.contactNumber || "",
        email: invoiceData.email || "",
        invoiceDate: invoiceData.invoiceDate || "",
        vehicleNumber: invoiceData.vehicleNumber || "",
        transportCost: invoiceData.transportCost || "",
        wages: invoiceData.wages || "",
        discount: invoiceData.discount || "",
        applyGst: invoiceData.gstApplicable || false,
        applyCgst: invoiceData.cgstApplicable || false,
        invoiceItems: (invoiceData.items || []).map((item) => ({
          goodsType: item.goodsType || "",
          hsnCode: item.hsnCode || "",
          noOfBags: item.noOfBags || "",
          weightPerBag: item.weightPerBag || "",
          rate: item.rate || "",
        })),
      });
    }
  }, [invoiceData]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleItemChange = (index, field, value) => {
    const updatedItems = [...form.invoiceItems];
    if (field === "goodsType") {
      updatedItems[index][field] = value;
      const selected = itemOptions.find((opt) => opt.label === value);
      updatedItems[index]["hsnCode"] = selected?.hsnCode || "";
    } else {
      updatedItems[index][field] = value;
    }
    setForm({ ...form, invoiceItems: updatedItems });
  };

  const addItem = () => {
    setForm((prev) => ({
      ...prev,
      invoiceItems: [
        ...prev.invoiceItems,
        { goodsType: "", hsnCode: "", noOfBags: "", weightPerBag: "", rate: "" },
      ],
    }));
  };

  const removeItem = (index) => {
    const updatedItems = form.invoiceItems.filter((_, i) => i !== index);
    setForm({ ...form, invoiceItems: updatedItems });
  };

  const handleSubmit = async () => {
    const payload = {
      id: form.id,
      fullName: form.customerName,
      address: form.customerAddress,
      gstNumber: form.customerGstNumber,
      contactNumber: form.customerContact,
      email: form.email,
      invoiceDate: form.invoiceDate,
      vehicleNumber: form.vehicleNumber,
      transportCost: parseFloat(form.transportCost),
      wages: parseFloat(form.wages),
      discount: form.invoiceType.toLowerCase() === "buy" ? 0 : parseFloat(form.discount) || 0,
      gstApplicable: form.applyGst,
      cgstApplicable: form.applyCgst,
      items: form.invoiceItems.map((item) => ({
        goodsType: item.goodsType,
        hsnCode: item.hsnCode,
        noOfBags: parseInt(item.noOfBags),
        weightPerBag: parseFloat(item.weightPerBag),
        rate: parseFloat(item.rate),
      })),
    };

    try {
      const response = await axios.put(`/api/invoices/${form.id}`, payload, {
        responseType: "blob",
      });

      const file = new Blob([response.data], { type: "application/pdf" });
      const fileURL = URL.createObjectURL(file);
      setPdfUrl(fileURL);
      setShowModal(true);
    } catch (error) {
      console.error("Error updating invoice:", error);
      alert("Failed to update invoice.");
    }
  };

  const isBuyInvoice = form.invoiceType?.toLowerCase() === "buy";

  return (
    <div className="invoice-form">
      <style>{`
        .invoice-form {
          padding: 1rem;
          font-size: 14px;
          font-family: sans-serif;
        }
        .section {
          border: 1px solid #ccc;
          border-radius: 4px;
          margin-bottom: 1rem;
          padding: 1rem;
        }
        .section-title {
          font-weight: bold;
          margin-bottom: 0.5rem;
          border-bottom: 1px solid #ccc;
          padding-bottom: 0.25rem;
        }
        .form-grid {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
          gap: 0.75rem;
        }
        .form-group {
          display: flex;
          flex-direction: column;
        }
        .form-group label {
          font-weight: 600;
          font-size: 13px;
          margin-bottom: 0.2rem;
        }
        input, select {
          padding: 6px 10px;
          font-size: 13px;
          border: 1px solid #aaa;
          border-radius: 4px;
        }
        .tax-section {
          margin-top: 0.5rem;
          font-size: 13px;
        }
        .item-row {
          display: flex;
          flex-wrap: wrap;
          gap: 0.5rem;
          margin-top: 0.5rem;
          align-items: center;
        }
        .add-btn, .remove-btn, .submit-btn {
          margin-top: 0.5rem;
          padding: 6px 12px;
          font-size: 13px;
          border: 1px solid #ccc;
          border-radius: 4px;
          background-color: white;
          cursor: pointer;
        }
        .add-btn:hover, .remove-btn:hover, .submit-btn:hover {
          background-color: #f0f0f0;
        }
        .modal {
          position: fixed;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: rgba(0,0,0,0.6);
          display: flex;
          align-items: center;
          justify-content: center;
          z-index: 1000;
        }
        .modal-content {
          background: #fff;
          padding: 1rem;
          width: 90%;
          max-width: 800px;
          height: 90%;
          border-radius: 8px;
          position: relative;
          display: flex;
          flex-direction: column;
        }
        .modal-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
        }
        .modal-body {
          flex: 1;
          margin-top: 10px;
          overflow: hidden;
        }
        .close-btn {
          background: red;
          color: #fff;
          border: none;
          font-weight: bold;
          padding: 0.25rem 0.5rem;
          border-radius: 4px;
          cursor: pointer;
        }
      `}</style>

      {/* Customer Details */}
      <div className="section">
        <div className="section-title">Customer Details</div>
        <div className="form-grid">
          <div className="form-group">
            <label>Full Name</label>
            <input name="customerName" value={form.customerName} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>Address</label>
            <input name="customerAddress" value={form.customerAddress} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>GST Number</label>
            <input name="customerGstNumber" value={form.customerGstNumber} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>Contact Number</label>
            <input name="customerContact" value={form.customerContact} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>Email</label>
            <input name="email" value={form.email} onChange={handleChange} />
          </div>
        </div>
      </div>

      {/* Invoice Details */}
      <div className="section">
        <div className="section-title">Invoice Details</div>
        <div className="form-grid">
          <div className="form-group">
            <label>Invoice Date</label>
            <input type="date" name="invoiceDate" value={form.invoiceDate} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>Vehicle Number</label>
            <input name="vehicleNumber" value={form.vehicleNumber} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>Transport Cost</label>
            <input name="transportCost" type="number" value={form.transportCost} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label>Labour Cost</label>
            <input name="wages" type="number" value={form.wages} onChange={handleChange} />
          </div>
          {!isBuyInvoice && (
            <div className="form-group">
              <label>Discount (%)</label>
              <input name="discount" type="number" value={form.discount} onChange={handleChange} />
            </div>
          )}
        </div>
        <div className="tax-section">
          <label>
            <input type="checkbox" name="applyGst" checked={form.applyGst} onChange={handleChange} /> Apply GST
          </label>
          <label>
            <input type="checkbox" name="applyCgst" checked={form.applyCgst} onChange={handleChange} /> Apply CGST
          </label>
        </div>
      </div>

      {/* Items Section */}
      <div className="section">
        <div className="section-title">Items</div>
        {form.invoiceItems.map((item, index) => (
          <div key={index} className="item-row">
            <select value={item.goodsType} onChange={(e) => handleItemChange(index, "goodsType", e.target.value)}>
              <option value="">Select Item</option>
              {itemOptions.map((opt, i) => (
                <option key={i} value={opt.label}>{opt.label}</option>
              ))}
            </select>
            <input
              placeholder="HSN Code"
              value={item.hsnCode}
              onChange={(e) => handleItemChange(index, "hsnCode", e.target.value)}
              disabled={item.goodsType !== "Other"}
            />
            <input
              placeholder="No. of Bags"
              type="number"
              value={item.noOfBags}
              onChange={(e) => handleItemChange(index, "noOfBags", e.target.value)}
            />
            <input
              placeholder="Weight per Bag"
              type="number"
              value={item.weightPerBag}
              onChange={(e) => handleItemChange(index, "weightPerBag", e.target.value)}
            />
            <input
              placeholder="Rate"
              type="number"
              value={item.rate}
              onChange={(e) => handleItemChange(index, "rate", e.target.value)}
            />
            <button type="button" className="remove-btn" onClick={() => removeItem(index)}>✕</button>
          </div>
        ))}
        <button type="button" className="add-btn" onClick={addItem}>+ Add Item</button>
      </div>

      <button className="submit-btn" onClick={handleSubmit}>Update Invoice</button>

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
                style={{ border: "1px solid #ccc" }}
              ></iframe>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
