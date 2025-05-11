package com.example.indokuistampilan2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TopicAdapter(private val topicList: MutableList<Topic>) : RecyclerView.Adapter<TopicAdapter.TopicViewHolder>() {

    class TopicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.nama_topik)
        val descText: TextView = itemView.findViewById(R.id.deskripsi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_topic, parent, false)
        return TopicViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val topic = topicList[position]
        holder.titleText.text = topic.nama_topik
        holder.descText.text = topic.deskripsi
    }

    override fun getItemCount(): Int = topicList.size

    // This method will update the topic list and notify the adapter
    fun updateTopics(newTopicList: List<Topic>) {
        topicList.clear()               // Clear the current list
        topicList.addAll(newTopicList)  // Add the new list
        notifyDataSetChanged()          // Notify the adapter to refresh the view
    }
}
