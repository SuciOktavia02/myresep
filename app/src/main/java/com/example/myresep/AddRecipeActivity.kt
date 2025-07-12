package com.example.myresep

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var editTitle: EditText
    private lateinit var editDescription: EditText
    private lateinit var editIngredients: EditText
    private lateinit var editSteps: EditText
    private lateinit var btnSave: Button
    private lateinit var imagePreview: ImageView

    private var imageUri: String? = null
    private lateinit var recipeDatabase: RecipeDatabase

    private val IMAGE_PICK_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe) // pastikan file xml kamu namanya sama

        // Inisialisasi komponen
        editTitle = findViewById(R.id.etTitle)
        editDescription = findViewById(R.id.etDescription)
        editIngredients = findViewById(R.id.etIngredients)
        editSteps = findViewById(R.id.etSteps)
        btnSave = findViewById(R.id.btnSave)
        imagePreview = findViewById(R.id.imgPreview)

        recipeDatabase = RecipeDatabase.getDatabase(this)

        imagePreview.setOnClickListener {
            pickImageFromGallery()
        }

        btnSave.setOnClickListener {
            val title = editTitle.text.toString()
            val description = editDescription.text.toString()
            val ingredients = editIngredients.text.toString()
            val steps = editSteps.text.toString()

            if (title.isEmpty() || description.isEmpty() || ingredients.isEmpty() || steps.isEmpty()) {
                Toast.makeText(this, "Lengkapi semua data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newRecipe = RecipeEntity(
                title = title,
                description = description,
                ingredients = ingredients,
                steps = steps,
                imageUri = imageUri
            )

            lifecycleScope.launch {
                recipeDatabase.recipeDao().insertRecipe(newRecipe)
                runOnUiThread {
                    Toast.makeText(this@AddRecipeActivity, "Resep ditambahkan", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            val selectedImage = data?.data
            imageUri = selectedImage.toString()
            imagePreview.setImageURI(selectedImage)
        }
        val btnBack: ImageView = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            onBackPressed()
        }
    }
}
