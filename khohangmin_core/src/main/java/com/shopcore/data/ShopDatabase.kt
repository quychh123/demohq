package com.shopcore.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    ProductEntity::class,
    CustomerEntity::class,
    RepairTicketEntity::class,
    ShopOrderEntity::class,
    CashbookEntryEntity::class,
  ],
  version = 1,
  exportSchema = false,
)
abstract class ShopDatabase : RoomDatabase() {
  abstract fun dao(): ShopDao

  companion object {
    @Volatile private var instance: ShopDatabase? = null

    fun getInstance(context: Context): ShopDatabase {
      return instance
        ?: synchronized(this) {
          instance
            ?: Room.databaseBuilder(
                context.applicationContext,
                ShopDatabase::class.java,
                "hq_kho_sync.db",
              )
              .build()
              .also { instance = it }
        }
    }
  }
}
