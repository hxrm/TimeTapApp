package com.example.timesheet

import androidx.lifecycle.LiveData
import com.example.timetapwebapp.Timesheet
import com.example.timetapwebapp.TimesheetDao

class TimesheetRepository(private val timesheetDao: TimesheetDao) {

    val allTimesheets: LiveData<List<Timesheet>> = timesheetDao.getAllTimesheets()

    suspend fun insert(timesheet: Timesheet) {
        timesheetDao.insert(timesheet)
    }
}
