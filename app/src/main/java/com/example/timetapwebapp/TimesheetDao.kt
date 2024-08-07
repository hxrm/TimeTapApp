package com.example.timetapwebapp

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TimesheetDao {

    @Query("SELECT * FROM timesheets")
    fun getAllTimesheets(): LiveData<List<Timesheet>>

    @Insert
    suspend fun insert(timesheet: Timesheet)
}
