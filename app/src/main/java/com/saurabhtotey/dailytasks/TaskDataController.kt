package com.saurabhtotey.dailytasks

import android.content.Context
import com.saurabhtotey.dailytasks.model.Task
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
	private var currentDayTasksData: JSONObject = JSONObject()

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
		this.initializeNewDayData()
	}

	/**
	 * Gets task data for today from fileData
	 * If no data on file for today, create an entry
	 * Guarantees that today's entry is the first entry of fileData
	 * TODO: must be called again if the app is open but the date changes
	 */
	private fun initializeNewDayData() {
		val currentDate = this.getDateString()
		if (this.fileData.length() == 0 || (this.fileData[0] as JSONObject).getString("date") != currentDate) {
			this.currentDayTasksData = JSONObject()
			this.currentDayTasksData.put("date", currentDate)
			this.currentDayTasksData.put("data", JSONObject())
			(this.fileData.length() downTo 1).forEach { i -> this.fileData.put(i, this.fileData.get(i - 1)) }
			this.handleUpdate()
		} else {
			this.currentDayTasksData = this.fileData.get(0) as JSONObject
		}
	}

	/**
	 * Returns today's date in a machine-parsable string
	 */
	private fun getDateString(): String {
		return SimpleDateFormat("ddMMyyyy", Locale.US).format(Calendar.getInstance().time)
	}

	/**
	 * Updates all JSON objects and ensures that the file is up-to-date
	 * Should be called whenever currentDayTasksData is updated or fileData is updated
	 * Changes from currentDayTasksData take highest precedence (eg. will overwrite changes made in fileData if they differ)
	 */
	private fun handleUpdate() {
		this.fileData.put(0, this.currentDayTasksData)
		file.writeText(fileData.toString())
	}

	/**
	 * Gets the stored value for the given task: defaults to 0
	 */
	fun getValueFor(task: Task): Int {
		if ((this.currentDayTasksData["data"] as JSONObject).keys().asSequence().contains(task.name)) {
			return (this.currentDayTasksData["data"] as JSONObject).getInt(task.name)
		}
		return 0
	}

	/**
	 * Given a target task, creates an array of relevant values for the target task
	 * First element of the array is the value of the task itself, and the subsequent values are the values for the sub-tasks in the order they are listed as sub-tasks
	 */
	fun getValuesForTask(task: Task): Array<Int> {
		return Array(task.subTasks.size + 1) { i -> this.getValueFor(if (i == 0) task else task.subTasks[i - 1]) }
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
	}

	/**
	 * Filters all the tasks and gets all non-sub-tasks
	 */
	fun getPrimaryTasks(): Array<Task> {
		val subTasks = Task.values().flatMap { it.subTasks.toList() }
		return Task.values().filter { !subTasks.contains(it) }.toTypedArray()
	}

}
