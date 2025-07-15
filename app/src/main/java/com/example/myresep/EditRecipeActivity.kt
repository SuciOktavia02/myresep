package com.example.myresep

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class EditRecipeActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etIngredients: EditText
    private lateinit var etSteps: EditText
    private lateinit var imgPreview: ImageView
    private lateinit var btnSave: Button

    private var recipeId: Int = -1
    private var currentImageUri: Uri? = null

    private lateinit var recipeDatabase: RecipeDatabase

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            currentImageUri = uri
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_placeholder)
                .into(imgPreview)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe) // pakai layout add_recipe

        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        etIngredients = findViewById(R.id.etIngredients)
        etSteps = findViewById(R.id.etSteps)
        imgPreview = findViewById(R.id.imgPreview)
        btnSave = findViewById(R.id.btnSave)

        // ✅ Tambahkan back button
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        recipeDatabase = RecipeDatabase.getDatabase(this)

        recipeId = intent.getIntExtra("id", -1)
        if (recipeId != -1) {
            loadRecipeDetails()
        } else {
            Toast.makeText(this, "ID Resep tidak valid", Toast.LENGTH_SHORT).show()
            finish()
        }

        imgPreview.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSave.setOnClickListener {
            saveEditedRecipe()
        }
    }

    private fun loadRecipeDetails() {
        lifecycleScope.launch {
            val recipe = recipeDatabase.recipeDao().getRecipeById(recipeId)
            recipe?.let {
                runOnUiThread {
                    etTitle.setText(it.title)
                    etDescription.setText(it.description)
                    etIngredients.setText(it.ingredients)
                    etSteps.setText(it.steps)

                    if (!it.imageUri.isNullOrEmpty()) {
                        currentImageUri = Uri.parse(it.imageUri)
                        Glide.with(this@EditRecipeActivity)
                            .load(currentImageUri)
                            .placeholder(R.drawable.ic_placeholder)
                            .into(imgPreview)
                    } else {
                        imgPreview.setImageResource(R.drawable.ic_placeholder)
                    }
                }
            }
        }
    }

    private fun saveEditedRecipe() {
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val ingredients = etIngredients.text.toString().trim()
        val steps = etSteps.text.toString().trim()

        if (title.isEmpty() || ingredients.isEmpty() || steps.isEmpty()) {
            Toast.makeText(this, "Lengkapi semua data!", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedRecipe = RecipeEntity(
            id = recipeId,
            title = title,
            description = description,
            ingredients = ingredients,
            steps = steps,
            imageUri = currentImageUri?.toString() ?: ""
        )

        lifecycleScope.launch {
            recipeDatabase.recipeDao().updateRecipe(updatedRecipe)
            runOnUiThread {
                Toast.makeText(this@EditRecipeActivity, "Resep berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
