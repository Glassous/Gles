package com.glassous.gles.ui.manage

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.glassous.gles.R

class UrlAdapter(
    private val urls: MutableList<String>,
    private val onItemClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<UrlAdapter.UrlViewHolder>() {

    class UrlViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUrl: TextView = itemView.findViewById(R.id.tv_url)
        val btnDelete: Button = itemView.findViewById(R.id.btn_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrlViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_url, parent, false)
        return UrlViewHolder(view)
    }

    override fun onBindViewHolder(holder: UrlViewHolder, position: Int) {
        val url = urls[position]
        holder.tvUrl.text = url
        holder.itemView.setOnClickListener(null)
        // 点击网址跳转
        holder.tvUrl.setOnClickListener {
            Log.d("UrlAdapter", "tvUrl clicked: $url")
            onItemClick(url)
        }
        // 点击删除按钮删除
        holder.btnDelete.setOnClickListener {
            Log.d("UrlAdapter", "btnDelete clicked: $url")
            onDeleteClick(url)
        }
    }

    override fun getItemCount(): Int {
        return urls.size
    }

    fun updateData(newUrls: List<String>) {
        urls.clear()
        urls.addAll(newUrls)
        notifyDataSetChanged()
    }
}