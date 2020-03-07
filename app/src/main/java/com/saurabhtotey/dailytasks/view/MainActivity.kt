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
 * Tasks have a different background color based on their completion status
 * TODO: may eventually allow for navigation to another view that shows stats and data
 */
class MainActivity : AppCompatActivity() {

	private var tasksRoot: LinearLayout? = null

	private var dateButton: Button? = null
	private var trackingDate = Calendar.getInstance()
		set(value) {
			field = value
			this.dateButton!!.text = SimpleDateFormat.getDateInstance().format(value.time)
			this.updateTaskViewsForms()
			this.updateTaskViewsByCompletion()
		}

	/**
	 * Main entry point for the app
	 */
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		TaskDataController.initialize(this)

		//Populates the view with tasks
		this.tasksRoot = this.findViewById(R.id.TaskContainer)
		primaryTasks.forEach { task ->
			val taskView = LayoutInflater.from(this).inflate(R.layout.task, this.tasksRoot!!, false)
			this.tasksRoot!!.addView(taskView)
			this.populateTaskView(taskView, task)
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
	 * Populates the given task view with data from the given task
	 * Task depth is how nested the task is as a sub-task:
	 *  0 means the task is a main task, 1 means the task is a sub-task, 2 means the task is a sub-sub-task, and so on
	 */
	private fun populateTaskView(taskView: View, task: Task, taskDepth: Int = 0) {
		//Gives the task view its basic information
		taskView.tag = task.name
		val taskTitleView = taskView.findViewById<TextView>(R.id.TaskTitle)
		val taskDescriptionView = taskView.findViewById<TextView>(R.id.TaskDescription)
		taskTitleView.text = task.displayName
		taskDescriptionView.text = task.description
		taskView.findViewById<TextView>(R.id.TaskFormDescription).text = task.formDescription

		//Shortens sub task title width so that form controls line up
		taskTitleView.layoutParams.width -= taskDepth * (taskView.parent as LinearLayout).paddingStart

		//Creates task form controls and links it up with the TaskDataController to keep data up to date
		if (task.formType == FormType.CHECKBOX) {
			val checkBox = taskView.findViewById<CheckBox>(R.id.TaskCheckBox)
			checkBox.visibility = View.VISIBLE
			checkBox.setOnCheckedChangeListener { _, isChecked ->
				TaskDataController.setValueForTask(task, if (isChecked) 1 else 0, this.trackingDate)
				this.updateTaskViewsByCompletion()
			}
		} else if (task.formType == FormType.POSITIVE_INTEGER) {
			val numberInput = taskView.findViewById<EditText>(R.id.TaskNumberInput)
			numberInput.visibility = View.VISIBLE
			numberInput.addTextChangedListener(object : TextWatcher {
				override fun afterTextChanged(p0: Editable?) {
					val numberInputValue = numberInput.text.toString().toIntOrNull() ?: return
					TaskDataController.setValueForTask(task, numberInputValue, this@MainActivity.trackingDate)
					this@MainActivity.updateTaskViewsByCompletion()
				}
				override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
				override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			})
			numberInput.setOnEditorActionListener { _, actionId, _ ->
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					numberInput.clearFocus()
				}
				return@setOnEditorActionListener false
			}
			numberInput.setOnFocusChangeListener { _, hasFocus ->
				if (!hasFocus && numberInput.text.isBlank()) {
					numberInput.setText("0")
				}
			}
			//TODO: unfocus when keyboard is dismissed: this is difficult and is probably going to need https://stackoverflow.com/questions/3425932/detecting-when-user-has-dismissed-the-soft-keyboard
		}

		//Populates the subTaskContainer with the task's sub-tasks
		val subTaskContainer = taskView.findViewById<LinearLayout>(R.id.SubTaskContainer)
		task.subTasks.forEach { subTask ->
			val subTaskView = LayoutInflater.from(this).inflate(R.layout.task, subTaskContainer, false)
			subTaskContainer.addView(subTaskView)
			this.populateTaskView(subTaskView, subTask, taskDepth + 1)
		}
	}

	/**
	 * Updates the appearance for all taskViews based off of their completion
	 */
	private fun updateTaskViewsByCompletion() {
		Task.values().forEach { task ->
			val view = this.tasksRoot!!.findViewWithTag<RelativeLayout>(task.name)
			val completion = task.evaluateIsCompleted(TaskDataController.getValueFor(task, this.trackingDate))
			view.background = this.resources.getDrawable(
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
	 */
	private fun updateTaskViewsForms() {
		Task.values().forEach { task ->
			val view = this.tasksRoot!!.findViewWithTag<RelativeLayout>(task.name)
			if (task.formType == FormType.CHECKBOX) {
				val checkBox = view.findViewById<CheckBox>(R.id.TaskCheckBox)
				val newValue = TaskDataController.getValueFor(task, this.trackingDate).value > 0
				if (checkBox.isChecked != newValue) {
					checkBox.isChecked = newValue
				}
			} else if (task.formType == FormType.POSITIVE_INTEGER) {
				val editText = view.findViewById<EditText>(R.id.TaskNumberInput)
				val newValue = "${TaskDataController.getValueFor(task, this.trackingDate).value}"
				if (editText.text.toString() != newValue) {
					editText.setText("${TaskDataController.getValueFor(task, this.trackingDate).value}")
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
