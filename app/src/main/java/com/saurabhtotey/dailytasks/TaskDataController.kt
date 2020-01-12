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
 * TODO: doc for singleton usage except for in NotificationSender
 */
class TaskDataController constructor(context: Context) {

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
	private val fileData: JSONArray
	private var trackingDateDataIndex = -1

	var trackingDate = Calendar.getInstance()
		set(value) {
			field = value
			val dateString = this.getDateString(value)
			val indexOfCurrentDate = (0 until this.fileData.length()).firstOrNull { (this.fileData[it] as JSONObject).getString("date") == dateString }
			if (indexOfCurrentDate != null) {
				this.trackingDateDataIndex = indexOfCurrentDate
				return
			}
			val currentDayTasksData = JSONObject()
			currentDayTasksData.put("date", dateString)
			currentDayTasksData.put("data", JSONObject())
			(this.fileData.length() downTo 1).forEach { i -> this.fileData.put(i, this.fileData.get(i - 1)) }
			this.fileData.put(0, currentDayTasksData)
			(this.fileData.length() - 1 downTo 1).forEach { i ->
				if (((this.fileData[i] as JSONObject)["data"] as JSONObject).length() == 0) {
					this.fileData.remove(i)
				}
			}
			this.file.writeText(this.fileData.toString())
			this.trackingDateDataIndex = 0
		}

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
		this.trackingDate = Calendar.getInstance()
	}

	/**
	 * Gets the task data (with date info stripped out) for the current trackingDate
	 * Is given in key-value pairs where keys are task names and values are integers
	 * Data is a copy and not a reference
	 */
	private fun getDayData(): JSONObject {
		return (this.fileData[this.trackingDateDataIndex] as JSONObject)["data"] as JSONObject
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
	fun getValueFor(task: Task): TaskValue {
		var value = 0
		if (this.getDayData().keys().asSequence().contains(task.name)) {
			value = this.getDayData().getInt(task.name)
		}
		return TaskValue(value, task.subTasks.map { getValueFor(it) })
	}

	/**
	 * Updates the given task to have the given value
	 * If a value is set to 0, the task is just removed from the data as an omission defaults to 0
	 */
	fun setValueForTask(task: Task, value: Int) {
		val updatedData = this.getDayData()
		if (value == 0) {
			updatedData.remove(task.name)
		} else {
			updatedData.put(task.name, value)
		}
		val updatedDayObject = this.fileData[this.trackingDateDataIndex] as JSONObject
		updatedDayObject.put("data", updatedData)
		this.fileData.put(this.trackingDateDataIndex, updatedDayObject)
		this.file.writeText(this.fileData.toString())
	}

}
