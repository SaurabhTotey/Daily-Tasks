package com.saurabhtotey.dailytasks.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.saurabhtotey.dailytasks.R
import com.saurabhtotey.dailytasks.TaskDataController
import com.saurabhtotey.dailytasks.model.FormType
import com.saurabhtotey.dailytasks.model.Task

/**
 * The main view for this application
 * In its most basic essence, shows a list of tasks
 *
 * Tasks are displayed as titles, descriptions, and a form field for marking some sort of completion info
 * Tasks also display with a button that expands and closes an indented list of sub-tasks that the parent task may consider when evaluating completion
 * TODO: Tasks have a green background when considered complete, red for incomplete, and white for when completion is meaningless in the context of the task
 * Handles the expanding/collapsing of task descriptions when tasks get selected
 * TODO: may eventually handle sorting alphabetically and by completeness
 * TODO: may eventually allow for navigation to another view that shows stats and data
 */
class MainActivity : AppCompatActivity() {

	/**
	 * Main entry point for the app
	 */
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val taskContainer = this.findViewById<LinearLayout>(R.id.TaskContainer)
		TaskDataController.get(this).getPrimaryTasks().forEach { task ->
			val taskView = LayoutInflater.from(this).inflate(R.layout.task, taskContainer, false)
			taskContainer.addView(taskView)
			this.populateTaskView(taskView, task)
		}
	}

	/**
	 * Populates the given task view with data from the given task
	 */
	private fun populateTaskView(taskView: View, task: Task, isSubTask: Boolean = false) {
		taskView.tag = task.name
		val taskTitleView = taskView.findViewById<TextView>(R.id.TaskTitle)
		taskTitleView.text = task.displayName
		taskView.findViewById<TextView>(R.id.TaskFormDescription).text = task.formDescription

		val taskDescriptionView = taskView.findViewById<TextView>(R.id.TaskDescription)
		taskDescriptionView.text = task.description
		taskView.setOnClickListener {
			taskDescriptionView.visibility = if (taskDescriptionView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
		}

		//TODO: link up form behaviour to TaskDataController (both for form initial value and for when form is interacted with)
		if (task.formType == FormType.CHECKBOX) {
			taskView.findViewById<CheckBox>(R.id.TaskCheckBox).visibility = View.VISIBLE
		} else if (task.formType == FormType.POSITIVE_INTEGER) {
			taskView.findViewById<NumberPicker>(R.id.TaskNumberPicker).visibility = View.VISIBLE
		}

		if (isSubTask) {
//			taskTitleView.layoutParams.width -= (15 * this.resources.displayMetrics.density + 0.5f).toInt()
			taskTitleView.layoutParams.width -= (taskView.parent as LinearLayout).paddingStart
		}

		val subTaskContainer = taskView.findViewById<LinearLayout>(R.id.SubTaskContainer)
		task.subTasks.forEach { subTask ->
			val subTaskView = LayoutInflater.from(this).inflate(R.layout.task, subTaskContainer, false)
			subTaskContainer.addView(subTaskView)
			this.populateTaskView(subTaskView, subTask, true)
		}

		if (task.subTasks.isNotEmpty()) {
			val subTaskExpansionButton = taskView.findViewById<ImageButton>(R.id.ExpandSubTasksButton)
			subTaskExpansionButton.visibility = View.VISIBLE
			var isExpanded = false
			subTaskExpansionButton.setOnClickListener() {
				if (isExpanded) {
					subTaskContainer.visibility = View.GONE
					subTaskExpansionButton.setImageDrawable(this.resources.getDrawable(android.R.drawable.arrow_down_float, this.theme))
				} else {
					subTaskContainer.visibility = View.VISIBLE
					subTaskExpansionButton.setImageDrawable(this.resources.getDrawable(android.R.drawable.arrow_up_float, this.theme))
				}
				isExpanded = !isExpanded
			}
		}
	}

}
