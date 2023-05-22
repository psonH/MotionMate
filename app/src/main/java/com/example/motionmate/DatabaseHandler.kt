package com.example.motionmate

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

const val DATABASE_NAME = "ActivityDB"
const val TABLE_NAME = "Activity"

const val COL_ID = "id"
const val COL_DAY = "Day"
const val COL_TIME = "Time"
const val COL_DURATION = "Duration"
const val COL_ACTIVITY = "Name"

class DatabaseHandler (var context: Context): SQLiteOpenHelper(context, DATABASE_NAME,null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = ("CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY, " +
                COL_DAY + " VARCHAR(50)," +
                COL_TIME + " VARCHAR(50)," +
                COL_DURATION + " VARCHAR(50)," +
                COL_ACTIVITY + " VARCHAR(50))")

        db?.execSQL(createTable)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {

    }

    fun insertData (day: String, time:String, duration: String, activity: String) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_DAY, day)
        cv.put(COL_TIME, time)
        cv.put(COL_DURATION, duration)
        cv.put(COL_ACTIVITY, activity)

        var result = db.insert(TABLE_NAME, null, cv)
        if (result == (-1).toLong()) {
            Toast.makeText(context, "Failed to insert data!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Database Updated!", Toast.LENGTH_SHORT).show()
        }
    }
}