package com.example.myresep

import android.view.*
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecipeAdapter(
    private var recipeList: List<RecipeEntity> = emptyList(),
    private val onItemClick: (RecipeEntity) -> Unit,
    private val onEditClick: (RecipeEntity) -> Unit,
    private val onDeleteClick: (RecipeEntity) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgRecipe: ImageView = itemView.findViewById(R.id.imgRecipe)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val btnMenu: ImageView = itemView.findViewById(R.id.btnMenu)

        fun bind(recipe: RecipeEntity) {
            tvTitle.text = recipe.title
            tvDescription.text = recipe.description

            Glide.with(itemView.context)
                .load(recipe.imageUri)
                .placeholder(R.drawable.ic_placeholder)
                .into(imgRecipe)

            itemView.setOnClickListener {
                onItemClick(recipe)
            }

            btnMenu.setOnClickListener {
                showPopupMenu(it, recipe)
            }
        }

        private fun showPopupMenu(view: View, recipe: RecipeEntity) {
            val popup = PopupMenu(view.context, view)
            popup.inflate(R.menu.menu_list)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_edit -> {
                        onEditClick(recipe)
                        true
                    }
                    R.id.menu_delete -> {
                        onDeleteClick(recipe)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipeList[position])
    }

    override fun getItemCount(): Int = recipeList.size

    fun updateData(newList: List<RecipeEntity>) {
        recipeList = newList
        notifyDataSetChanged()
    }
}
