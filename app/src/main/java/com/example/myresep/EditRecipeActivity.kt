package com.example.myresep

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class EditRecipeActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 2
    private var selectedImageUri: Uri? = null

    private lateinit var imagePreview: ImageView
    private lateinit var editTitle: EditText
    private lateinit var editDescription: EditText
    private lateinit var editIngredients: EditText
    private lateinit var editSteps: EditText
    private lateinit var btnSave: Button
    private lateinit var recipeDatabase: RecipeDatabase

    private var recipeId: Int = -1

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe) // reuse layout

        // Inisialisasi view
        imagePreview = findViewById(R.id.imageView)
        editTitle = findViewById(R.id.editTitle)
        editDescription = findViewById(R.id.editDescription)
        editIngredients = findViewById(R.id.editIngredients)
        editSteps = findViewById(R.id.editSteps)
        btnSave = findViewById(R.id.btnSave)

        recipeDatabase = RecipeDatabase.getDatabase(this)

        // Ambil data dari intent
        recipeId = intent.getIntExtra("id", -1)

        // Ambil data dari database berdasarkan ID
        lifecycleScope.launch {
            val recipe = recipeDatabase.recipeDao().getRecipeById(recipeId)
            recipe?.let {
                editTitle.setText(it.title)
                editDescription.setText(it.description)
                editIngredients.setText(it.ingredients)
                editSteps.setText(it.steps)

                if (!it.imageUri.isNullOrEmpty()) {
                    selectedImageUri = Uri.parse(it.imageUri)
                    imagePreview.setImageURI(selectedImageUri)
                }
            }
        }

        // Klik gambar → buka galeri
        imagePreview.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        // Klik Simpan → update Room
        btnSave.setOnClickListener {
            val updatedTitle = editTitle.text.toString().trim()
            val updatedDesc = editDescription.text.toString().trim()
            val updatedIngredients = editIngredients.text.toString().trim()
            val updatedSteps = editSteps.text.toString().trim()
            val updatedImageUri = selectedImageUri?.toString()

            if (updatedTitle.isNotEmpty() && updatedDesc.isNotEmpty()
                && updatedIngredients.isNotEmpty() && updatedSteps.isNotEmpty()
            ) {
                val updatedRecipe = RecipeEntity(
                    id = recipeId,
                    title = updatedTitle,
                    description = updatedDesc,
                    ingredients = updatedIngredients,
                    steps = updatedSteps,
                    imageUri = updatedImageUri
                )

                lifecycleScope.launch {
                    recipeDatabase.recipeDao().updateRecipe(updatedRecipe)
                    runOnUiThread {
                        Toast.makeText(this@EditRecipeActivity, "Resep berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } else {
                Toast.makeText(this, "Lengkapi semua data!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            imagePreview.setImageURI(selectedImageUri)
        }
    }
}
