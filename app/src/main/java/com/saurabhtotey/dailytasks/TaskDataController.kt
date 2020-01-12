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
	}

	private fun getIndexForDayDataFor(date: Calendar): Int {
		val currentDate = this.getDateString(date)
		val indexOfCurrentDate = (0 until this.fileData.length()).firstOrNull { (this.fileData[it] as JSONObject).getString("date") == currentDate }
		if (indexOfCurrentDate != null) {
			return indexOfCurrentDate
		}
		val currentDayTasksData = JSONObject()
		currentDayTasksData.put("date", currentDate)
		currentDayTasksData.put("data", JSONObject())
		(this.fileData.length() downTo 1).forEach { i -> this.fileData.put(i, this.fileData.get(i - 1)) }
		this.fileData.put(0, currentDayTasksData)
		(this.fileData.length() - 1 downTo 1).forEach { i ->
			if (((this.fileData[i] as JSONObject)["data"] as JSONObject).length() == 0) {
				this.fileData.remove(i)
			}
		}
		this.file.writeText(this.fileData.toString())
		return 0
	}

	private fun getDayDataFor(date: Calendar): JSONObject {
		return (this.fileData[this.getIndexForDayDataFor(date)] as JSONObject)["data"] as JSONObject
	}

	/**
	 * Returns the given date in a machine-parsable string
	 */
	private fun getDateString(date: Calendar): String {
		return SimpleDateFormat("ddMMyyyy", Locale.US).format(date.time)
	}

	/**
	 * Gets the stored value for the given task: defaults to 0
	 */
	fun getValueFor(task: Task, date: Calendar): TaskValue {
		var value = 0
		if (this.getDayDataFor(date).keys().asSequence().contains(task.name)) {
			value = this.getDayDataFor(date).getInt(task.name)
		}
		return TaskValue(value, task.subTasks.map { getValueFor(it, date) })
	}

	/**
	 * Updates the given task to have the given value
	 * If a value is set to 0, the task is just removed from the data as an omission defaults to 0
	 */
	fun setValueForTask(task: Task, value: Int, date: Calendar) {
		val updatedData = this.getDayDataFor(date)
		if (value == 0) {
			updatedData.remove(task.name)
		} else {
			updatedData.put(task.name, value)
		}
		val fullDayObjectIndex = this.getIndexForDayDataFor(date)
		val fullDayObject = this.fileData[fullDayObjectIndex] as JSONObject
		fullDayObject.put("data", updatedData)
		this.fileData.put(fullDayObjectIndex, fullDayObject)
		this.file.writeText(this.fileData.toString())
	}

}
