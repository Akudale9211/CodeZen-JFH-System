import React, { useState } from "react";
import "./style/InvoiceForm.css";

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

const initialItem = {
  goodsType: "",
  hsnCode: "",
  noOfBags: "",
  weightPerBag: "",
  rate: "",
  customGoodsType: "",
};

const getLocalDateISO = () => {
  const tzoffset = new Date().getTimezoneOffset() * 60000;
  return new Date(Date.now() - tzoffset).toISOString().split("T")[0];
};

export default function InvoiceForm() {
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
    discount: "",
    items: [{ ...initialItem }],
    gstApplicable: true,
    cgstApplicable: true,
  });

  const [pdfBlob, setPdfBlob] = useState(null);
  const [showPreview, setShowPreview] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleCheckboxChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.checked });
  };

  const handleItemChange = (index, e) => {
    const { name, value } = e.target;
    const newItems = [...form.items];

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

    setForm({ ...form, items: newItems });
  };

  const addItem = () => {
    setForm({ ...form, items: [...form.items, { ...initialItem }] });
  };

  const removeItem = (index) => {
    const newItems = form.items.filter((_, i) => i !== index);
    setForm({ ...form, items: newItems });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const currentDate = getLocalDateISO();

    const payload = {
      ...form,
      invoiceDate: form.invoiceDate || currentDate,
      transportCost: parseFloat(form.transportCost) || 0,
      wages: parseFloat(form.wages) || 0,
      discount: parseFloat(form.discount) || 0,
      items: form.items.map((item) => ({
        goodsType: item.goodsType === "Other" ? item.customGoodsType : item.goodsType,
        hsnCode: item.hsnCode,
        noOfBags: parseInt(item.noOfBags) || 0,
        weightPerBag: parseFloat(item.weightPerBag) || 0,
        rate: parseFloat(item.rate) || 0,
      })),
    };

    try {
      const res = await fetch("/api/invoices", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (!res.ok) throw new Error("Invoice generation failed");

      const blob = await res.blob();
      setPdfBlob(blob);
      setShowPreview(true);
    } catch (error) {
      alert("Failed to create invoice");
      console.error(error);
    }
  };

  return (
    <div className="invoice-form-container">
      {!showPreview ? (
        <>
            <h1 style={{ textAlign: "center", marginBottom: "8px" }}>Sell Invoice</h1>
          <form onSubmit={handleSubmit} className="invoice-form">
            <fieldset>
              <legend>Customer Details</legend>
              <div className="form-grid">
                {[
                  { label: "Full Name", name: "fullName" },
                  { label: "Address", name: "address" },
                  { label: "GST Number", name: "gstNumber" },
                  { label: "Contact Number", name: "contactNumber", type: "tel" },
                  { label: "Email", name: "email", type: "email" },
                ].map(({ label, name, type = "text" }) => (
                  <div className="form-group" key={name}>
                    <label>{label}</label>
                    <input type={type} name={name} value={form[name]} onChange={handleChange} required={name !== "email" && name !== "gstNumber"} />
                  </div>
                ))}
              </div>
            </fieldset>

            <fieldset>
              <legend>Invoice Details</legend>
              <div className="form-grid">
                {[
                  { label: "Invoice Date", name: "invoiceDate", type: "date", max: getLocalDateISO() },
                  { label: "Vehicle Number", name: "vehicleNumber" },
                  { label: "Transport Cost", name: "transportCost", type: "number" },
                  { label: "Labour Cost", name: "wages", type: "number" },
                  { label: "Discount", name: "discount", type: "number" },
                ].map(({ label, name, type = "text", max }) => (
                  <div className="form-group" key={name}>
                    <label>{label}</label>
                    <input
                      type={type}
                      name={name}
                      value={form[name]}
                      onChange={handleChange}
                      {...(max && { max })}
                    />
                  </div>
                ))}
              </div>
              <div className="tax-section">
                <label>
                  <input type="checkbox" name="gstApplicable" checked={form.gstApplicable} onChange={handleCheckboxChange} /> Apply GST
                </label>
                <label>
                  <input type="checkbox" name="cgstApplicable" checked={form.cgstApplicable} onChange={handleCheckboxChange} /> Apply CGST
                </label>
              </div>
            </fieldset>

           <fieldset>
             <legend>Items</legend>
{/*              <div className="item-grid-labels"> */}
{/*                <span>Goods Type</span> */}
{/*                <span>HSN Code</span> */}
{/*                <span>No. of Bags</span> */}
{/*                <span>Weight/Bag</span> */}
{/*                <span>Rate</span> */}
{/*                <span>Action</span> */}
{/*              </div> */}

             {form.items.map((item, index) => (
               <div className="item-row" key={index}>
                 <select
                   name="goodsType"
                   value={item.goodsType}
                   onChange={(e) => handleItemChange(index, e)}
                   required
                   title="Select the type of goods"
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
                       title="Enter name of other item"
                     />
                     <input
                       type="text"
                       name="hsnCode"
                       placeholder="Enter HSN Code"
                       value={item.hsnCode}
                       onChange={(e) => handleItemChange(index, e)}
                       required
                       title="Enter HSN code for custom item"
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
                     title="Auto-filled HSN code"
                   />
                 )}

                 <input
                   type="number"
                   name="noOfBags"
                   placeholder="No. of Bags"
                   value={item.noOfBags}
                   onChange={(e) => handleItemChange(index, e)}
                   required
                   title="Enter number of bags"
                 />

                 <input
                   type="number"
                   step="0.01"
                   name="weightPerBag"
                   placeholder="Weight per Bag"
                   value={item.weightPerBag}
                   onChange={(e) => handleItemChange(index, e)}
                   required
                   title="Enter weight per bag in kg"
                 />

                 <input
                   type="number"
                   step="0.01"
                   name="rate"
                   placeholder="Rate As Per KG"
                   value={item.rate}
                   onChange={(e) => handleItemChange(index, e)}
                   required
                   title="Rate per kg"
                 />

                 <button
                   type="button"
                   className="remove-btn"
                   onClick={() => removeItem(index)}
                   disabled={form.items.length === 1}
                   title="Remove item"
                 >
                   &times;
                 </button>
               </div>
             ))}

             <button
               type="button"
               className="add-btn"
               onClick={addItem}
               title="Add another item"
             >
               + Add Item
             </button>
           </fieldset>


            <button type="submit" className="submit-btn">Create Invoice</button>
          </form>
        </>
      ) : (
        <div className="pdf-preview">
          <div className="pdf-actions">
            <button onClick={() => setShowPreview(false)}>✏️ Edit Invoice</button>
            <button onClick={() => {
              const iframe = document.getElementById("pdf-frame");
              if (iframe) {
                iframe.contentWindow.focus();
                iframe.contentWindow.print();
              }
            }}>🖨️ Print Invoice</button>
          </div>
          <iframe
            id="pdf-frame"
            src={URL.createObjectURL(pdfBlob)}
            title="Invoice PDF"
            width="100%"
            height="600px"
            style={{ border: "none" }}
          ></iframe>
        </div>
      )}
    </div>
  );
}
