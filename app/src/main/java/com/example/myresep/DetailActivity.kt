package com.example.myresep

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvIngredients: TextView
    private lateinit var tvSteps: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button

    private var recipeId: Int = -1
    private lateinit var recipeDatabase: RecipeDatabase

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Inisialisasi view
        imageView = findViewById(R.id.imageView)
        tvTitle = findViewById(R.id.tvTitle)
        tvDescription = findViewById(R.id.tvDescription)
        tvIngredients = findViewById(R.id.tvIngredients)
        tvSteps = findViewById(R.id.tvSteps)
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)

        // Ambil ID dari intent
        recipeId = intent.getIntExtra("id", -1)
        if (recipeId == -1) {
            Toast.makeText(this, "ID resep tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        recipeDatabase = RecipeDatabase.getDatabase(this)
        loadRecipeDetails()

        // Tombol edit
        btnEdit.setOnClickListener {
            val intent = Intent(this, EditRecipeActivity::class.java).apply {
                putExtra("id", recipeId)
            }
            startActivity(intent)
        }

        // Tombol hapus
        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Hapus Resep")
                .setMessage("Yakin ingin menghapus resep ini?")
                .setPositiveButton("Hapus") { _: DialogInterface, _: Int ->
                    deleteRecipe()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun loadRecipeDetails() {
        lifecycleScope.launch {
            val recipe = recipeDatabase.recipeDao().getRecipeById(recipeId)
            runOnUiThread {
                if (recipe != null) {
                    tvTitle.text = recipe.title
                    tvDescription.text = recipe.description
                    tvIngredients.text = recipe.ingredients.ifEmpty { "Tidak ada bahan" }
                    tvSteps.text = recipe.steps.ifEmpty { "Tidak ada langkah" }

                    if (!recipe.imageUri.isNullOrEmpty()) {
                        imageView.setImageURI(Uri.parse(recipe.imageUri))
                    } else {
                        imageView.setImageResource(R.drawable.ic_placeholder)
                    }
                } else {
                    Toast.makeText(this@DetailActivity, "Resep tidak ditemukan", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun deleteRecipe() {
        lifecycleScope.launch {
            val recipe = recipeDatabase.recipeDao().getRecipeById(recipeId)
            if (recipe != null) {
                recipeDatabase.recipeDao().deleteRecipe(recipe)
                runOnUiThread {
                    Toast.makeText(this@DetailActivity, "Resep dihapus", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadRecipeDetails() // refresh data saat kembali dari edit
    }
}
