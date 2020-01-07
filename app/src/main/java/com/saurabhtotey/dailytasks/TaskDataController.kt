package com.saurabhtotey.dailytasks

import android.content.Context
import com.saurabhtotey.dailytasks.model.Task
import com.saurabhtotey.dailytasks.model.TaskValue
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * A singleton that acts as a controller if taken in an MVC context
 * Manages data flow and recording data to file and reading data from file
 * The context that is passed in is only used to initialize file IO: any correct context object should work
 */
class TaskDataController private constructor(context: Context) {

	/**
	 * Handles ensuring that this class is a singleton
	 */
	companion object {
		@Volatile private var INSTANCE: TaskDataController? = null
		fun get(context: Context): TaskDataController {
			return INSTANCE ?: synchronized(this) {
				INSTANCE ?: TaskDataController(context).also { INSTANCE = it }
			}
		}
	}

	private val file = File(context.filesDir, "TaskData.json")
	private var fileData: JSONArray
	private var currentDayTasksDataIndex = -1
		set(value) {
			field = value
			this.currentDayTasksData = this.fileData[field] as JSONObject
		}
	private var currentDayTasksData = JSONObject()

	/**
	 * Parses the data file
	 * Creates data file if it doesn't exist
	 */
	init {
		if (!this.file.exists()) {
			this.file.createNewFile()
			this.file.writeText("[]")
		}
		this.fileData = JSONArray(this.file.readLines().joinToString("\n"))
		this.initializeDayData(Calendar.getInstance().time)
	}

	/**
	 * Ensures that data for the given day exists
	 */
	fun initializeDayData(date: Date) {
		val currentDate = this.getDateString(date)
		val indexOfCurrentDate = (0 until this.fileData.length()).firstOrNull { (this.fileData[it] as JSONObject).getString("date") == currentDate }
		if (indexOfCurrentDate == null) {
			this.currentDayTasksData = JSONObject()
			this.currentDayTasksData.put("date", currentDate)
			this.currentDayTasksData.put("data", JSONObject())
			(this.fileData.length() downTo 1).forEach { i -> this.fileData.put(i, this.fileData.get(i - 1)) }
			this.fileData.put(0, this.currentDayTasksData)
			this.currentDayTasksDataIndex = 0
			(this.fileData.length() - 1 downTo 1).forEach { i ->
				if (((this.fileData[i] as JSONObject)["data"] as JSONObject).length() == 0) {
					this.fileData.remove(i)
				}
			}
		} else {
			this.currentDayTasksDataIndex = indexOfCurrentDate
		}
	}

	/**
	 * Returns the given date in a machine-parsable string
	 */
	private fun getDateString(date: Date): String {
		return SimpleDateFormat("ddMMyyyy", Locale.US).format(date)
	}

	/**
	 * Updates all JSON objects and ensures that the file is up-to-date
	 * Should be called whenever currentDayTasksData is updated or fileData is updated
	 * Changes from currentDayTasksData take highest precedence (eg. will overwrite changes made in fileData if they differ)
	 */
	private fun updateFileData() {
		this.fileData.put(this.currentDayTasksDataIndex, this.currentDayTasksData)
		file.writeText(fileData.toString())
	}

	/**
	 * Gets the stored value for the given task: defaults to 0
	 */
	fun getValueFor(task: Task): TaskValue {
		var value = 0
		if ((this.currentDayTasksData["data"] as JSONObject).keys().asSequence().contains(task.name)) {
			value = (this.currentDayTasksData["data"] as JSONObject).getInt(task.name)
		}
		return TaskValue(value, task.subTasks.map { getValueFor(it) })
	}

	/**
	 * Updates the given task to have the given value
	 * If a value is set to 0, the task is just removed from the data as an omission defaults to 0
	 */
	fun setValueForTask(task: Task, value: Int) {
		if (value == 0) {
			(this.currentDayTasksData["data"] as JSONObject).remove(task.name)
		} else {
			(this.currentDayTasksData["data"] as JSONObject).put(task.name, value)
		}
		this.updateFileData()
	}

	/**
	 * Filters all the tasks and gets all non-sub-tasks
	 */
	fun getPrimaryTasks(): Array<Task> {
		val subTasks = Task.values().flatMap { it.subTasks.toList() }
		return Task.values().filter { !subTasks.contains(it) }.toTypedArray()
	}

}
