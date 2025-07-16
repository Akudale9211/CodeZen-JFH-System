import React from 'react';

const InvoiceItem = ({ item, index, handleItemChange }) => (
  <div className="item-row">
    <input name="goodsType" value={item.goodsType} placeholder="Goods Type" onChange={(e) => handleItemChange(index, e)} />
    <input name="hsnCode" value={item.hsnCode} placeholder="HSN Code" onChange={(e) => handleItemChange(index, e)} />
    <input name="noOfBags" type="number" value={item.noOfBags} placeholder="No. of Bags" onChange={(e) => handleItemChange(index, e)} />
    <input name="totalWeight" type="number" value={item.totalWeight} placeholder="Total Weight" onChange={(e) => handleItemChange(index, e)} />
    <input name="rate" type="number" value={item.rate} placeholder="Rate" onChange={(e) => handleItemChange(index, e)} />
  </div>
);

export default InvoiceItem;
