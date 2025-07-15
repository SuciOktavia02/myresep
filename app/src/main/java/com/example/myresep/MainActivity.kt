package com.example.myresep

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var recipeDatabase: RecipeDatabase
    private lateinit var searchView: SearchView
    private var allRecipes: List<RecipeEntity> = emptyList() // Untuk menyimpan semua resep

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Toast.makeText(this, "Selamat datang di My Recipes", Toast.LENGTH_SHORT).show()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val btnTambahResep = findViewById<TextView>(R.id.btnTambahResep)
        searchView = findViewById(R.id.searchView)

        recipeDatabase = RecipeDatabase.getDatabase(this)

        recipeAdapter = RecipeAdapter(
            onItemClick = { recipe ->
                if (recipe.id != null) {
                    val intent = Intent(this, DetailActivity::class.java).apply {
                        putExtra("id", recipe.id)
                    }
                    startActivity(intent)
                } else {
                    Log.e("MainActivity", "ID resep null")
                }
            },
            onEditClick = { recipe ->
                val intent = Intent(this, EditRecipeActivity::class.java).apply {
                    putExtra("id", recipe.id)
                }
                startActivity(intent)
            },
            onDeleteClick = { recipe ->
                AlertDialog.Builder(this)
                    .setTitle("Hapus Resep")
                    .setMessage("Yakin hapus ${recipe.title}?")
                    .setPositiveButton("Hapus") { _, _ ->
                        lifecycleScope.launch {
                            recipeDatabase.recipeDao().deleteRecipe(recipe)
                        }
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            },
            onShareClick = { recipe ->
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_SUBJECT, "Yuk Coba Resep ${recipe.title}!")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "🍽️ ${recipe.title}\n\n${recipe.description}\n\nDapatkan resep lainnya di aplikasi MyResep! 😋"
                    )
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, "Bagikan resep lewat..."))
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = recipeAdapter
            setHasFixedSize(true)
        }

        btnTambahResep.setOnClickListener {
            startActivity(Intent(this, AddRecipeActivity::class.java))
        }

        // Ambil semua resep & simpan untuk fitur search
        lifecycleScope.launch {
            recipeDatabase.recipeDao().getAllRecipes().collectLatest { recipes ->
                allRecipes = recipes
                recipeAdapter.updateData(recipes)
            }
        }

        // Fitur Search
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false // biar langsung filter saat diketik
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = if (newText.isNullOrBlank()) {
                    allRecipes
                } else {
                    allRecipes.filter {
                        it.title.contains(newText, ignoreCase = true)
                    }
                }
                recipeAdapter.updateData(filtered)
                return true
            }
        })
    }
}
