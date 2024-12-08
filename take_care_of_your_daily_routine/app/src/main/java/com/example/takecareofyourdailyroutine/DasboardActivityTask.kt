package com.example.takecareofyourdailyroutine

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.takecareofyourdailyroutine.databinding.ActivityDashboardTaskBinding
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class DashboardTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardTaskBinding
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskList: MutableList<Task>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicjalizacja RecyclerView
        binding.rvTaskList.layoutManager = LinearLayoutManager(this)

        // Pobieranie danych z Firestore
        taskList = mutableListOf()
        taskAdapter = TaskAdapter(
            taskList,
            onTaskClick = { task ->
                Toast.makeText(this, "Kliknięto na zadanie: ${task.title}", Toast.LENGTH_SHORT).show()
            },
            onTaskComplete = { task ->
                task.isCompleted = !task.isCompleted
                updateTaskInFirestore(task)
                taskAdapter.notifyItemChanged(taskList.indexOf(task))
            },
            onTaskDiscard = { task ->
                val position = taskList.indexOf(task)
                if (position != -1) {
                    taskList.removeAt(position)
                    taskAdapter.notifyItemRemoved(position)
                    removeTaskFromFirestore(task)  // Ta linia usuwa zadanie z Firestore
                }
            }
        )
        binding.rvTaskList.adapter = taskAdapter

        // Pobieranie zadań z Firestore
        loadTasksFromFirestore()

        // Obsługa kliknięcia przycisku fabAddTask
        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog { task ->
                // Dodaj zadanie do listy
                taskList.add(task)
                taskAdapter.notifyItemInserted(taskList.size - 1)

                // Dodaj zadanie do Firestore
                addTaskToFirestore(task)
            }
        }

        binding.btnHome.setOnClickListener {
            val intent = Intent(this, DashboardActivityHome::class.java)
            startActivity(intent)
        }
        binding.btnSettings.setOnClickListener {
            val intent = Intent(this, DashboardActivitySettings::class.java)
            startActivity(intent)
        }
    }

    // Funkcja do pobierania zadań z Firestore
    private fun loadTasksFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val taskListFromFirestore = mutableListOf<Task>()
                    val tasks = documentSnapshot.get("tasks") as? List<Map<String, Any>> ?: emptyList()

                    for (task in tasks) {
                        val id = task["id"] as? Int ?: continue
                        val title = task["title"] as? String ?: continue
                        val description = task["description"] as? String ?: continue
                        val isCompleted = task["isCompleted"] as? Boolean ?: false

                        taskListFromFirestore.add(Task(id, title, description, isCompleted))
                    }

                    // Aktualizowanie listy zadań w UI
                    taskList.clear()
                    taskList.addAll(taskListFromFirestore)
                    taskAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Błąd podczas pobierania zadań", e)
                }
        }
    }

    // Funkcja do pokazania dialogu dodawania zadania
    private fun showAddTaskDialog(onTaskAdded: (Task) -> Unit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val titleEditText: EditText = dialogView.findViewById(R.id.etTaskTitle)
        val descriptionEditText: EditText = dialogView.findViewById(R.id.etTaskDescription)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Dodaj nowe zadanie")
            .setView(dialogView)
            .setPositiveButton("Dodaj") { _, _ ->
                val title = titleEditText.text.toString()
                val description = descriptionEditText.text.toString()
                if (title.isNotEmpty() && description.isNotEmpty()) {
                    val task = Task(0, title, description, false)
                    onTaskAdded(task)
                } else {
                    Toast.makeText(this, "Proszę wprowadzić tytuł i opis zadania.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Anuluj", null)
            .create()

        dialog.show()
    }

    // Funkcja do aktualizacji zadania w Firestore
    private fun updateTaskInFirestore(task: Task) {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val taskMap = hashMapOf(
                "id" to task.id,
                "title" to task.title,
                "description" to task.description,
                "isCompleted" to task.isCompleted
            )

            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val tasks = documentSnapshot.get("tasks") as? MutableList<Map<String, Any>> ?: mutableListOf()
                    val updatedTasks = tasks.map {
                        if (it["id"] == task.id) taskMap else it
                    }
                    db.collection("users").document(uid)
                        .update("tasks", updatedTasks)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Zadanie zaktualizowane.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Błąd podczas aktualizacji zadania", e)
                        }
                }
        }
    }

    // Funkcja do usuwania zadania z Firestore
    private fun removeTaskFromFirestore(task: Task) {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val tasks = documentSnapshot.get("tasks") as? MutableList<Map<String, Any>> ?: mutableListOf()

                    // Filtruj, aby usunąć tylko zadanie o danym id
                    val updatedTasks = tasks.filterNot { it["id"] == task.id }

                    db.collection("users").document(uid)
                        .update("tasks", updatedTasks)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Zadanie usunięte.")

                            // Po udanym usunięciu, zaktualizuj lokalną listę i odśwież UI
                            val position = taskList.indexOf(task)
                            if (position != -1) {
                                taskList.removeAt(position)
                                taskAdapter.notifyItemRemoved(position)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Błąd podczas usuwania zadania", e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Błąd podczas pobierania danych", e)
                }
        }
    }

    // Funkcja do ponownego załadowania danych z Firestore
    private fun reloadTasksFromFirestore() {
        taskList.clear()
        loadTasksFromFirestore()  // ponowne załadowanie zadań z Firestore
    }

}
