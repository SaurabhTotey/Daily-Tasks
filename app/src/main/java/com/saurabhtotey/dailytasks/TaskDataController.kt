package com.saurabhtotey.dailytasks

import com.saurabhtotey.dailytasks.task.Task
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * A singleton that acts as a controller if taken in an MVC context
 * Manages data flow and recording data to file and reading data from file
 */
object TaskDataController {

	private val file = File("SaurabhToteyTaskData.json")
	private var fileData: JSONArray
	private var currentDayTasksData: JSONObject

	/**
	 * Reads from the file to check if data from this day exists already
	 * If no data on file for today, create an entry
	 * Guarantees that today's entry is the first entry of fileData
	 * TODO: this code won't work well if the app is left open between days: it will need to be re-run
	 */
	init {
		if (!file.exists()) {
			file.createNewFile()
			file.writeText("[]")
		}

		fileData = JSONArray(file.readLines().joinToString("\n"))
		val currentDate = this.getDateString()

		if (fileData.length() == 0 || (fileData[0] as JSONObject).getString("date") != currentDate) {
			this.currentDayTasksData = JSONObject()
			this.currentDayTasksData.put("date", currentDate)
			this.currentDayTasksData.put("data", JSONObject()) //TODO: this should be a map from Task enum name to list of integer values
			(fileData.length() downTo 1).forEach { i -> fileData.put(i, fileData.get(i - 1)) }
			this.handleUpdate()
		} else {
			this.currentDayTasksData = fileData.get(0) as JSONObject
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
	 */
	private fun handleUpdate() {
		this.fileData.put(0, this.currentDayTasksData)
		file.writeText(fileData.toString())
	}

	/**
	 * Gets the stored value for the given task: defaults to 0
	 */
	fun getValueFor(task: Task): Int {
		return 0 //TODO:
	}

	/**
	 * Updates the given task to have the given value
	 */
	fun setValueForTask(task: Task, value: Int) {
		//TODO:
	}

}
