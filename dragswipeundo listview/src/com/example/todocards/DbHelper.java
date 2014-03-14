package com.example.todocards;

import java.util.ArrayList;
import java.util.List;

import com.example.todocards.Task;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {
	
private static final int DATABASE_VERSION = 1;
	
	//db name
	private static final String DATABASE_NAME = "taskManager";
	
	//db table name
	private static final String TABLE_TASKS = "tasks";
	
	//db table column names
	private static final String KEY_ID = "id";
	private static final String KEY_TASKNAME= "taskName";
	private static final String KEY_STATUS = "status";
	private static final String KEY_POSITION = "position";
	
	
	
	public DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_TASKS + " ( "
				+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ KEY_TASKNAME + " TEXT, "
				+ KEY_STATUS + " INTEGER, "
				+ KEY_POSITION + " INTEGER)";
		db.execSQL(sql);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
		// create tables again
		onCreate(db);
	}
	
	public void addTask(Task task) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(KEY_TASKNAME, task.getTaskName());
		// task status can be 0 for not done, 1 for done
		values.put(KEY_STATUS, task.getStatus());
		// position in listview
		values.put(KEY_POSITION, task.getPosition());
		
		//inserting row
		db.insert(TABLE_TASKS, null, values);
		db.close();
	}
	
	public List<Task> getAllTasks() {
		List<Task> taskList = new ArrayList<Task>();
		// select all query
		
		String selectQuery = "SELECT * FROM " + TABLE_TASKS + " ORDER BY POSITION ASC";   // <---- change to this when you fix task.getPosition
		//String selectQuery = "SELECT * FROM " + TABLE_TASKS;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		//loop through all rows and adding to list
		if(cursor.moveToFirst()) {
			do {
				Task task = new Task();
				task.setId(cursor.getInt(0));
				task.setTaskName(cursor.getString(1));
				task.setStatus(cursor.getInt(2));
				task.setPosition(cursor.getInt(3));
				// adding to list
				taskList.add(task);
				} while (cursor.moveToNext());
		}
		
		return taskList;
	}
	
	public void updateTask(Task task) {
		// updating table row
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_TASKNAME, task.getTaskName());
		values.put(KEY_STATUS, task.getStatus());
		values.put(KEY_POSITION, task.getPosition());
		db.update(TABLE_TASKS, values, KEY_ID + " = ?", 
				new String[]{String.valueOf(task.getId())});
	}
	
	// will this work?
	public void deleteTask(Task task) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		
		db.delete(TABLE_TASKS, //table name
                KEY_ID+" = ?",  // selections
                new String[] { String.valueOf(task.getId()) }); //selections arg
		 // 3. close
        db.close();
        
        Log.d("deleteTask", task.getTaskName());
        // Log.d("deleteTask("+KEY_ID+")", task.toString());
        // Log.d("remainingTasks", getAllTasks());
	}
	
}



