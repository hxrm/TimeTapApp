package com.example.timetapwebapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ActiveProjectsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var projectAdapter: ProjectAdapter
    private val projectList = mutableListOf<ProjectData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_projects)

        recyclerView = findViewById(R.id.recyclerView)
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference.child("Projects")

        setupRecyclerView()
        loadProjects()

        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    projectAdapter.filter(newText)
                }
                return true
            }
        })

        val imageButton = findViewById<ImageButton>(R.id.imageButton)
        imageButton.setOnClickListener {
            val intent = Intent(this, ActivityStartTask::class.java)
            startActivity(intent)
        }

    }

    private fun setupRecyclerView() {
        projectAdapter = ProjectAdapter(this, projectList)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ActiveProjectsActivity)
            adapter = projectAdapter
        }
    }

    private fun loadProjects() {
        val user = firebaseAuth.currentUser
        user?.let { currentUser ->
            val userId = currentUser.uid
            databaseReference.child(userId).orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    projectList.clear()
                    for (projectSnapshot in snapshot.children) {
                        val project = projectSnapshot.getValue(ProjectData::class.java)
                        project?.let { projectList.add(0, it) }
                    }
                    projectAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }
}

data class ProjectData(
    val projectName: String = "",
    val clientName: String = "",
    val deadline: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
