package com.marketyardbill.marketyardbill.dao;

import com.marketyardbill.marketyardbill.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    Optional<Inventory> findByGoodsTypeAndHsnCode(String goodsType, String hsnCode);

}
