package com.example.myresep

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    /**
     * Mengambil semua resep, urut dari terbaru ke terlama.
     * Menggunakan Flow agar update di database langsung muncul ke UI.
     */
    @Query("SELECT * FROM recipes ORDER BY id DESC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    /**
     * Mengambil 1 resep berdasarkan ID.
     */
    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    suspend fun getRecipeById(id: Int): RecipeEntity?

    /**
     * Insert resep baru.
     * Mengembalikan ID yang di-generate Room.
     */
    @Insert
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)

    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipesOnce(): List<RecipeEntity>

}