package com.shopcore.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {
  @Query("SELECT * FROM shop_products ORDER BY groupName ASC, subCategory ASC, name ASC")
  fun observeProducts(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM shop_customers ORDER BY createdAtMillis DESC")
  fun observeCustomers(): Flow<List<CustomerEntity>>

  @Query("SELECT * FROM repair_tickets ORDER BY updatedAtMillis DESC")
  fun observeRepairTickets(): Flow<List<RepairTicketEntity>>

  @Query("SELECT * FROM shop_orders ORDER BY createdAtMillis DESC")
  fun observeOrders(): Flow<List<ShopOrderEntity>>

  @Query("SELECT * FROM cashbook_entries ORDER BY createdAtMillis DESC")
  fun observeCashbookEntries(): Flow<List<CashbookEntryEntity>>

  @Query("SELECT * FROM shop_products ORDER BY groupName ASC, subCategory ASC, name ASC")
  suspend fun getAllProducts(): List<ProductEntity>

  @Query("SELECT * FROM shop_customers ORDER BY createdAtMillis DESC")
  suspend fun getAllCustomers(): List<CustomerEntity>

  @Query("SELECT * FROM repair_tickets ORDER BY updatedAtMillis DESC")
  suspend fun getAllRepairTickets(): List<RepairTicketEntity>

  @Query("SELECT * FROM shop_orders ORDER BY createdAtMillis DESC")
  suspend fun getAllOrders(): List<ShopOrderEntity>

  @Query("SELECT * FROM cashbook_entries ORDER BY createdAtMillis DESC")
  suspend fun getAllCashbookEntries(): List<CashbookEntryEntity>

  @Query("SELECT COUNT(*) FROM shop_products")
  suspend fun getProductCount(): Int

  @Query("SELECT * FROM shop_products WHERE id = :id LIMIT 1")
  suspend fun getProductById(id: String): ProductEntity?

  @Query("SELECT * FROM shop_customers WHERE phone = :phone LIMIT 1")
  suspend fun getCustomerByPhone(phone: String): CustomerEntity?

  @Query("SELECT * FROM shop_customers WHERE id = :id LIMIT 1")
  suspend fun getCustomerById(id: String): CustomerEntity?

  @Query("SELECT * FROM repair_tickets WHERE id = :id LIMIT 1")
  suspend fun getRepairTicketById(id: String): RepairTicketEntity?

  @Query("SELECT * FROM shop_orders WHERE orderCode = :orderCode LIMIT 1")
  suspend fun getOrderByCode(orderCode: String): ShopOrderEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProducts(items: List<ProductEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProduct(item: ProductEntity)

  @Delete
  suspend fun deleteProduct(item: ProductEntity)

  @Query("UPDATE shop_products SET stock = :newStock, updatedAtMillis = :updatedAt WHERE id = :id")
  suspend fun updateProductStock(id: String, newStock: Int, updatedAt: Long)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCustomers(customers: List<CustomerEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCustomer(customer: CustomerEntity)

  @Delete
  suspend fun deleteCustomer(customer: CustomerEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRepairTickets(tickets: List<RepairTicketEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRepairTicket(ticket: RepairTicketEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrders(orders: List<ShopOrderEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrder(order: ShopOrderEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCashbookEntries(entries: List<CashbookEntryEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCashbookEntry(entry: CashbookEntryEntity)

  @Query("DELETE FROM shop_products")
  suspend fun clearProducts()

  @Query("DELETE FROM shop_customers")
  suspend fun clearCustomers()

  @Query("DELETE FROM repair_tickets")
  suspend fun clearRepairTickets()

  @Query("DELETE FROM shop_orders")
  suspend fun clearOrders()

  @Query("DELETE FROM cashbook_entries")
  suspend fun clearCashbook()

  @Query("DELETE FROM shop_products WHERE id IN (:ids)")
  suspend fun deleteProductsByIds(ids: List<String>)

  @Query("DELETE FROM shop_customers WHERE id IN (:ids)")
  suspend fun deleteCustomersByIds(ids: List<String>)

  @Query("DELETE FROM repair_tickets WHERE id IN (:ids)")
  suspend fun deleteRepairTicketsByIds(ids: List<String>)

  @Query("DELETE FROM shop_orders WHERE id IN (:ids)")
  suspend fun deleteOrdersByIds(ids: List<String>)

  @Query("DELETE FROM cashbook_entries WHERE id IN (:ids)")
  suspend fun deleteCashbookEntriesByIds(ids: List<String>)
}
