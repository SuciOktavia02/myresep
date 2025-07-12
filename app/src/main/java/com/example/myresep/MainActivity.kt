package com.example.myresep

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var recipeDatabase: RecipeDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val btnAddRecipe: Button = findViewById(R.id.btnAddRecipe)

        recipeDatabase = RecipeDatabase.getDatabase(this)

        // Inisialisasi Adapter (dengan aksi klik item, edit, dan hapus)
        recipeAdapter = RecipeAdapter(
            onItemClick = { recipe ->
                if (recipe.id != null) {
                    val intent = Intent(this, DetailActivity::class.java).apply {
                        putExtra("id", recipe.id)
                    }
                    startActivity(intent)
                } else {
                    Log.e("MainActivity", "ID resep null atau tidak valid")
                }
            },
            onEditClick = { recipe ->
                val intent = Intent(this, EditRecipeActivity::class.java).apply {
                    putExtra("id", recipe.id)
                }
                startActivity(intent)
            },
            onDeleteClick = { recipe ->
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Hapus Resep")
                    .setMessage("Yakin ingin menghapus ${recipe.title}?")
                    .setPositiveButton("Hapus") { _, _ ->
                        lifecycleScope.launch {
                            recipeDatabase.recipeDao().deleteRecipe(recipe)
                        }
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = recipeAdapter
            setHasFixedSize(true)
        }

        btnAddRecipe.setOnClickListener {
            startActivity(Intent(this, AddRecipeActivity::class.java))
        }

        // Observasi data resep secara real-time dari database
        lifecycleScope.launch {
            recipeDatabase.recipeDao().getAllRecipes().collectLatest { recipes ->
                recipeAdapter.updateData(recipes)
            }
        }
    }
}
