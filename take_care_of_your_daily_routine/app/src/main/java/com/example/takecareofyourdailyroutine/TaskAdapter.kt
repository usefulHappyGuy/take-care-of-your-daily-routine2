package com.example.takecareofyourdailyroutine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.takecareofyourdailyroutine.Task
import android.widget.Button


class TaskAdapter(
    private val taskList: List<Task>,
    private val onTaskClick: (Task) -> Unit,
    private val onTaskComplete: (Task) -> Unit,
    private val onTaskDiscard: (Task) -> Unit // Funkcja do obsługi porzucania zadania
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val taskStatus: TextView = itemView.findViewById(R.id.tvTaskStatus)
        val btnCompleteTask: Button = itemView.findViewById(R.id.btnCompleteTask)
        val btnDiscardTask: Button = itemView.findViewById(R.id.btnDiscardTask)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.taskTitle.text = task.title
        holder.taskStatus.text = if (task.isCompleted) "Zakończone" else "W toku"

        // Obsługa kliknięcia całego elementu
        holder.itemView.setOnClickListener {
            onTaskClick(task)
        }

        // Obsługa przycisku "Zakończ"
        holder.btnCompleteTask.setOnClickListener {
            task.isCompleted = true
            onTaskComplete(task)
        }

        // Obsługa przycisku "Porzuć"
        holder.btnDiscardTask.setOnClickListener {
            onTaskDiscard(task)
            removeTaskFromFirestore(task)
        }
    }

    override fun getItemCount(): Int = taskList.size
}
