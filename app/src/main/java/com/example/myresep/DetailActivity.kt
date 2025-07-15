package com.example.myresep

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var imageView: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvIngredients: TextView
    private lateinit var stepsContainer: LinearLayout
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button

    private var recipeId: Int = -1
    private lateinit var recipeDatabase: RecipeDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        btnBack = findViewById(R.id.btnBack)
        imageView = findViewById(R.id.imageView)
        tvTitle = findViewById(R.id.tvTitle)
        tvDescription = findViewById(R.id.tvDescription)
        tvIngredients = findViewById(R.id.tvIngredients)
        stepsContainer = findViewById(R.id.stepsContainer)
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        recipeId = intent.getIntExtra("id", -1)
        if (recipeId == -1) {
            Toast.makeText(this, "ID resep tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        recipeDatabase = RecipeDatabase.getDatabase(this)
        loadRecipeDetails()

        btnEdit.setOnClickListener {
            val intent = Intent(this, EditRecipeActivity::class.java).apply {
                putExtra("id", recipeId)
            }
            startActivity(intent)
        }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Hapus Resep")
                .setMessage("Yakin ingin menghapus resep ini?")
                .setPositiveButton("Hapus") { _, _ -> deleteRecipe() }
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

                    stepsContainer.removeAllViews()
                    val stepsList = recipe.steps.split("\n").filter { it.isNotBlank() }
                    if (stepsList.isEmpty()) {
                        val textView = TextView(this@DetailActivity).apply {
                            text = "Tidak ada langkah"
                            setPadding(8, 8, 8, 8)
                        }
                        stepsContainer.addView(textView)
                    } else {
                        stepsList.forEach { step ->
                            val checkBox = CheckBox(this@DetailActivity).apply {
                                text = step.trim()
                                textSize = 14f
                                setTextColor(resources.getColor(android.R.color.black))
                                layoutParams = ViewGroup.MarginLayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                ).apply { setMargins(0, 8, 0, 8) }
                            }
                            stepsContainer.addView(checkBox)
                        }
                    }

                    // Gunakan try-catch saat akses gambar URI
                    try {
                        if (!recipe.imageUri.isNullOrEmpty()) {
                            val uri = Uri.parse(recipe.imageUri)
                            imageView.setImageURI(uri)
                        } else {
                            imageView.setImageResource(R.drawable.ic_placeholder)
                        }
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                        imageView.setImageResource(R.drawable.ic_placeholder)
                        Toast.makeText(this@DetailActivity, "Tidak bisa akses gambar", Toast.LENGTH_SHORT).show()
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
        loadRecipeDetails()
    }
}
