package com.example.timetapwebapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProjectViewModel : ViewModel() {
    private val _projectTitle = MutableLiveData<String>()
    val projectTitle: LiveData<String> get() = _projectTitle

    fun setProjectTitle(title: String) {
        _projectTitle.value = title
    }
}
