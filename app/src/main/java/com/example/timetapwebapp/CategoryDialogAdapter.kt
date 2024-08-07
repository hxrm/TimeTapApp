
package com.example.timetapwebapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryDialogAdapter(
    private val categories: List<String>, // Adjusted type to List<String>
    private val onCategorySelected: (String) -> Unit
) : RecyclerView.Adapter<CategoryDialogAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtCategory: TextView = itemView.findViewById(R.id.txtCategory)

        fun bind(category: String) {
            txtCategory.text = category
            itemView.setOnClickListener {
                onCategorySelected(category)
            }
        }
    }
}