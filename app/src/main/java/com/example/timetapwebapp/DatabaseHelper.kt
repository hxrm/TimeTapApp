package com.example.timetapwebapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "timeTap.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_TIMER = "timer"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TIME_LEFT = "time_left"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTimerTable = ("CREATE TABLE " + TABLE_TIMER + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TIME_LEFT + " INTEGER" + ")")
        db.execSQL(createTimerTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TIMER")
        onCreate(db)
    }

    fun saveTimeLeft(timeLeft: Long): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_TIME_LEFT, timeLeft)
        val result = db.insert(TABLE_TIMER, null, contentValues)
        db.close()
        return result != -1L
    }

    fun getTimeLeft(): Long {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_TIMER, arrayOf(COLUMN_TIME_LEFT), null, null, null, null, null)
        var timeLeft: Long = 0
        if (cursor.moveToFirst()) {
            timeLeft = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIME_LEFT))
        }
        cursor.close()
        db.close()
        return timeLeft
    }

    fun clearTimeLeft() {
        val db = this.writableDatabase
        db.delete(TABLE_TIMER, null, null)
        db.close()
    }
}
