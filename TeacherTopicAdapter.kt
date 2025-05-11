package com.example.indokuistampilan2

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class TeacherTopicAdapter(
    private val context: Context,
    private val topicList: MutableList<Topic>,
    private val onItemClick: (Topic) -> Unit
) : RecyclerView.Adapter<TeacherTopicAdapter.TopicViewHolder>() {

    class TopicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.tvNamaTopik)
        val descText: TextView = itemView.findViewById(R.id.tvDeskripsi)
        val deadlineText: TextView = itemView.findViewById(R.id.tvDeadline)
        val deleteButton: ImageButton = itemView.findViewById(R.id.topic_delete_button)
        val topicMenuButton: ImageButton = itemView.findViewById(R.id.topic_menu_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_teacher_topic, parent, false)
        return TopicViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val topic = topicList[position]
        holder.titleText.text = topic.nama_topik
        holder.descText.text = topic.deskripsi
        holder.deadlineText.text = topic.tenggat_waktu

        // Set the onClickListener for the delete button
        holder.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(topic.id, position)
        }

        // Add the click listener for the menu button (topic_menu_button)
        holder.topicMenuButton.setOnClickListener {
            Log.d("TeacherTopicAdapter", "Topic clicked: ${topic.nama_topik}, Topic ID: ${topic.id}")

            if (topic.id != 0) {
                val intent = Intent(context, edit_kuis::class.java)
                intent.putExtra("topic_id", topic.id) // Pass the topic.id to the next activity
                context.startActivity(intent)
            } else {
                Log.e("TeacherTopicAdapter", "Invalid Topic ID: ${topic.id}")
            }
        }

        // Add the click listener for the topic item (navigate to edit_kuis page)
        holder.itemView.setOnClickListener {
            onItemClick(topic)
        }
    }

    override fun getItemCount(): Int = topicList.size

    // Method to show a confirmation dialog before deleting
    private fun showDeleteConfirmationDialog(topicId: Int, position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Topic")
        builder.setMessage("Anda yakin ingin menghapus topik ini?")
        builder.setPositiveButton("Yes") { dialog, which ->
            deleteTopicFromServer(topicId, position)
        }
        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    // Method to send the delete request to the server
    private fun deleteTopicFromServer(topicId: Int, position: Int) {
        val url = "http://192.168.8.137/indokuis/delete_topic.php" // Your delete endpoint

        val params = HashMap<String, String>()
        params["topic_id"] = topicId.toString()

        val requestQueue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(Method.POST, url,
            { response ->
                // Handle the response from the server
                Log.d("TeacherTopicAdapter", "Delete response: $response")
                if (response.contains("success")) {
                    // If the topic was successfully deleted, remove it from the list
                    topicList.removeAt(position)
                    notifyItemRemoved(position)
                    Toast.makeText(context, "Topic berhasil dihapus", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("TeacherTopicAdapter", "Failed to delete topic")
                    Toast.makeText(context, "Failed to delete topic", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("TeacherTopicAdapter", "Error deleting topic: ${error.localizedMessage}")
                Toast.makeText(context, "Error deleting topic", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return params
            }
        }

        // Add the request to the Volley request queue
        requestQueue.add(stringRequest)
    }
}

