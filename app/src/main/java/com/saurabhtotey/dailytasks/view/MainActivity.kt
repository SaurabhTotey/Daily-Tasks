package com.saurabhtotey.dailytasks.view

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.saurabhtotey.dailytasks.R
import com.saurabhtotey.dailytasks.TaskDataController
import com.saurabhtotey.dailytasks.model.FormType
import com.saurabhtotey.dailytasks.model.Task
import com.saurabhtotey.dailytasks.model.TaskStatus
import com.saurabhtotey.dailytasks.model.primaryTasks
import java.text.SimpleDateFormat
import java.util.*

/**
 * The main view for this application
 * In its most basic essence, shows a list of tasks
 *
 * Tasks are displayed as titles, descriptions, and a form field for marking some sort of completion info
 * Tasks also display with a button that expands and closes an indented list of sub-tasks that the parent task may consider when evaluating completion
 * Tasks have a green background when considered complete, red for incomplete, and white for when completion is meaningless in the context of the task
 * Handles the expanding/collapsing of task descriptions when tasks get selected
 * TODO: may eventually allow for navigation to another view that shows stats and data
 */
class MainActivity : AppCompatActivity() {

	private var taskViews = listOf<TaskView>()

	private var dateButton: Button? = null
	private var trackingDate = Calendar.getInstance()
		set(value) {
			field = value
			this.dateButton!!.text = SimpleDateFormat.getDateInstance().format(value.time)
			this.updateTaskViewsForms()
			this.updateTaskViewsByCompletion()
		}

	private var expandSubTasksButton: ImageButton? = null
	private var numberOfExpandedDescriptions = 0
	private var numberOfExpandedSubTasks = 0
		set(value) {
			field = value
			this.expandSubTasksButton!!.setImageDrawable(this.resources.getDrawable(
				if (value == 0) android.R.drawable.arrow_down_float else android.R.drawable.arrow_up_float,
				this.theme
			))
		}

	/**
	 * Main entry point for the app
	 */
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		fun makeValueChangeCallback(task: Task): (Int) -> Unit {
			return {
				TaskDataController.get(this).setValueForTask(task, it, this.trackingDate)
				this.updateTaskViewsByCompletion()
			}
		}
		this.taskViews = primaryTasks.flatMap {
			TaskView(
				it,
				0,
				this.findViewById(R.id.TaskContainer),
				makeValueChangeCallback(it),
				{ isExpanded -> this.numberOfExpandedDescriptions += if (isExpanded) 1 else -1 },
				{ isExpanded -> this.numberOfExpandedSubTasks += if (isExpanded) 1 else -1 },
				this
			).taskViews.toList()
		}

		//Sets up the expand/collapse all descriptions and expand/collapse all sub-tasks buttons
		this.expandSubTasksButton = this.findViewById(R.id.ExpandAllSubTasksButton)
		this.expandSubTasksButton!!.setOnClickListener {
			val shouldExpand = this.numberOfExpandedSubTasks == 0
			this.taskViews.forEach { it.isSubTaskContainerExpanded = shouldExpand }
		}
		this.findViewById<Button>(R.id.ExpandAllDescriptionsButton).setOnClickListener {
			val shouldExpand = this.numberOfExpandedDescriptions == 0
			this.taskViews.forEach { it.isDescriptionExpanded = shouldExpand }
		}

		//Sets up functionality for the dateButton
		this.dateButton = this.findViewById(R.id.DateButton)
		this.trackingDate = Calendar.getInstance()
		this.dateButton!!.setOnClickListener {
			DatePickerDialog(
				this,
				{_, year, month, day ->
					var newDate = Calendar.getInstance()
					newDate.set(Calendar.YEAR, year)
					newDate.set(Calendar.MONTH, month)
					newDate.set(Calendar.DATE, day)
					if (newDate > Calendar.getInstance()) {
						newDate = Calendar.getInstance() //Do not allow editing for the future
					}
					this.trackingDate = newDate
				},
				this.trackingDate.get(Calendar.YEAR),
				this.trackingDate.get(Calendar.MONTH),
				this.trackingDate.get(Calendar.DATE)
			).show()
		}
	}

	/**
	 * Updates the appearance for all taskViews based off of their completion
	 * TODO: make TaskView handle this
	 */
	private fun updateTaskViewsByCompletion() {
		this.taskViews.forEach {
			val completion = it.task.evaluateIsCompleted(TaskDataController.get(this).getValueFor(it.task, this.trackingDate))
			it.taskView.background = this.resources.getDrawable(
				when (completion) {
					TaskStatus.BEYOND_COMPLETE -> R.color.taskBeyondComplete
					TaskStatus.COMPLETE -> R.color.taskComplete
					TaskStatus.IN_PROGRESS_OR_ATTEMPTED -> R.color.taskInProgressOrAttempted
					TaskStatus.INCOMPLETE -> R.color.taskIncomplete
					TaskStatus.COMPLETION_IRRELEVANT -> R.color.taskCompletionIrrelevant
				},
				this.theme
			)
		}
	}

	/**
	 * Updates the forms for all taskViews
	 * TODO: make this better
	 */
	private fun updateTaskViewsForms() {
		this.taskViews.forEach {
			if (it.task.formType == FormType.CHECKBOX) {
				val newValue = TaskDataController.get(this).getValueFor(it.task, this.trackingDate).value > 0
				if (it.checkBox!!.isChecked != newValue) {
					it.checkBox!!.isChecked = newValue
				}
			} else if (it.task.formType == FormType.POSITIVE_INTEGER) {
				val newValue = "${TaskDataController.get(this).getValueFor(it.task, this.trackingDate).value}"
				if (it.numberInput!!.text.toString() != newValue) {
					it.numberInput!!.setText(newValue)
				}
			}
		}
	}

	/**
	 * Un-focuses any EditTexts if they were focused but a tap or click occurred outside of their rectangle
	 * Copied from https://stackoverflow.com/a/28939113
	 */
	override fun dispatchTouchEvent(event: MotionEvent): Boolean {
		if (event.action == MotionEvent.ACTION_DOWN) {
			val currentFocus = this.currentFocus
			if (currentFocus is EditText) {
				val outRect = Rect()
				currentFocus.getGlobalVisibleRect(outRect)
				if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
					currentFocus.clearFocus()
					(getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(currentFocus.getWindowToken(), 0)
				}
			}
		}
		return super.dispatchTouchEvent(event)
	}

}
