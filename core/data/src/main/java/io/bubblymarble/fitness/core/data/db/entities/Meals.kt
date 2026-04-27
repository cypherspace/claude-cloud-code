package io.bubblymarble.fitness.core.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "ingredient")
data class IngredientEntity(
    @PrimaryKey val id: String,
    val name: String,
    val brand: String? = null,
    val barcode: String? = null,
    val source: String,
    val servingGrams: Double,
    val kcalPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatPer100g: Double,
    val fibrePer100g: Double? = null,
)

@Entity(
    tableName = "meal",
    indices = [Index("eatenAtEpochMs")]
)
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mealType: String,
    val eatenAtEpochMs: Long,
    val notes: String? = null,
    val photoUri: String? = null,
)

@Entity(
    tableName = "meal_item",
    foreignKeys = [
        ForeignKey(
            entity = MealEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.RESTRICT
        ),
    ],
    indices = [Index("mealId"), Index("ingredientId")]
)
data class MealItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealId: Long,
    val ingredientId: String,
    val grams: Double,
)
