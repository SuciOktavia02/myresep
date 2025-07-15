package com.example.myresep

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etIngredients: EditText
    private lateinit var etSteps: EditText
    private lateinit var imgPreview: ImageView
    private lateinit var btnSave: Button

    private var selectedImageUri: Uri? = null
    private lateinit var recipeDatabase: RecipeDatabase

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            selectedImageUri = uri
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.kamera)
                .into(imgPreview)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        etIngredients = findViewById(R.id.etIngredients)
        etSteps = findViewById(R.id.etSteps)
        imgPreview = findViewById(R.id.imgPreview)
        btnSave = findViewById(R.id.btnSave)

        // ✅ Tambahkan tombol back
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        recipeDatabase = RecipeDatabase.getDatabase(this)

        imgPreview.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        btnSave.setOnClickListener {
            saveRecipe()
        }
    }

    private fun saveRecipe() {
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val ingredients = etIngredients.text.toString().trim()
        val steps = etSteps.text.toString().trim()

        if (title.isEmpty() || ingredients.isEmpty() || steps.isEmpty()) {
            Toast.makeText(this, "Lengkapi semua data!", Toast.LENGTH_SHORT).show()
            return
        }

        val recipe = RecipeEntity(
            title = title,
            description = description,
            ingredients = ingredients,
            steps = steps,
            imageUri = selectedImageUri?.toString() ?: ""
        )

        lifecycleScope.launch {
            recipeDatabase.recipeDao().insertRecipe(recipe)
            runOnUiThread {
                Toast.makeText(this@AddRecipeActivity, "Resep berhasil disimpan!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
