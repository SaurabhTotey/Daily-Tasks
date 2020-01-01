package com.saurabhtotey.dailytasks.view

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
import androidx.core.app.NotificationCompat
import com.saurabhtotey.dailytasks.R
import com.saurabhtotey.dailytasks.TaskDataController
import com.saurabhtotey.dailytasks.model.FormType
import com.saurabhtotey.dailytasks.model.Task
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

	private val alarmManager get() = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
	private val notificationChannelId = "DailyTasksNotificationChannel"

	/**
	 * Main entry point for the app
	 */
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		this.initializeTaskDataControllerUpdateOnNewDay()
		this.initializeHourlyNotificationSending()

		//Populates the view with tasks
		val taskContainer = this.findViewById<LinearLayout>(R.id.TaskContainer)
		TaskDataController.get(this).getPrimaryTasks().forEach { task ->
			val taskView = LayoutInflater.from(this).inflate(R.layout.task, taskContainer, false)
			taskContainer.addView(taskView)
			this.populateTaskView(taskView, task)
		}
		this.updateTaskViews()
	}

	/**
	 * Sets up a daily action that first triggers in midnight in the future and then triggers daily where TaskDataController gets data for the day
	 * TODO: this doesn't work: create an application class that handles this one-time initialization
	 */
	private fun initializeTaskDataControllerUpdateOnNewDay() {
		class TaskDataControllerUpdater: BroadcastReceiver() {
			override fun onReceive(context: Context, intent: Intent) {
				TaskDataController.get(context).initializeNewDayData()
				this@MainActivity.recreate()
			}
		}
		val alarmStartTime = Calendar.getInstance().also {
			it.set(Calendar.HOUR, 0)
			it.set(Calendar.MINUTE, 0)
			it.set(Calendar.SECOND, 0)
			it.set(Calendar.MILLISECOND, 0)
			it.add(Calendar.DATE, 1)
		}
		this.alarmManager.setRepeating(
			AlarmManager.RTC,
			alarmStartTime.timeInMillis,
			AlarmManager.INTERVAL_DAY,
			PendingIntent.getBroadcast(
				this,
				0,
				Intent(this, TaskDataControllerUpdater::class.java),
				0
			)
		)
	}

	/**
	 * Sets up notifications that send hourly
	 * TODO: this doesn't work: create an application class that handles this one-time initialization
	 * TODO: look at https://stackoverflow.com/questions/36902667/how-to-schedule-notification-in-android
	 */
	private fun initializeHourlyNotificationSending() {
		val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		val notificationChannel = NotificationChannel(this.notificationChannelId, this.getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_DEFAULT).also {
			it.description = this.getString(R.string.notification_channel_description)
			it.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
		}
		notificationManager.createNotificationChannel(notificationChannel)
		val appOpeningIntent = PendingIntent.getActivity(
			this,
			0,
			Intent(this, MainActivity::class.java).also {
				it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
			},
			0
		)
		val notification = NotificationCompat.Builder(this, this.notificationChannelId)
			.setSmallIcon(R.mipmap.ic_launcher)
			.setContentTitle(this.getString(R.string.notification_title))
			.setPriority(NotificationCompat.PRIORITY_DEFAULT)
			.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
			.setCategory(NotificationCompat.CATEGORY_REMINDER)
			.setOnlyAlertOnce(true)
			.setContentIntent(appOpeningIntent)
			.setAutoCancel(true)
		class NotificationSender: BroadcastReceiver() {
			override fun onReceive(context: Context?, intent: Intent?) {
				val numberOfIncompleteTasks = TaskDataController.get(this@MainActivity).getPrimaryTasks().count { task ->
					task.evaluateIsCompleted(TaskDataController.get(this@MainActivity).getValuesForTask(task)) == false
				}
				if (numberOfIncompleteTasks == 0) {
					return
				}
				notification.setContentText(this@MainActivity.resources.getQuantityString(R.plurals.notification_text, numberOfIncompleteTasks, numberOfIncompleteTasks))
				notificationManager.notify(0, notification.build())
			}
		}
		val alarmStartTime = Calendar.getInstance().also {
			it.set(Calendar.MINUTE, 0)
			it.set(Calendar.SECOND, 0)
			it.set(Calendar.MILLISECOND, 0)
			it.add(Calendar.HOUR, 1)
		}
		this.alarmManager.setRepeating(
			AlarmManager.RTC_WAKEUP,
			alarmStartTime.timeInMillis,
			AlarmManager.INTERVAL_HOUR,
			PendingIntent.getBroadcast(
				this,
				0,
				Intent(this, NotificationSender::class.java),
				0
			)
		)
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

		//Implements that when tasks are clicked, they toggle the visibility of their descriptions
		taskView.setOnClickListener {
			taskDescriptionView.visibility = if (taskDescriptionView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
		}

		//Creates task form controls and links it up with the TaskDataController to keep data up to date
		if (task.formType == FormType.CHECKBOX) {
			val checkBox = taskView.findViewById<CheckBox>(R.id.TaskCheckBox)
			checkBox.visibility = View.VISIBLE
			checkBox.isChecked = TaskDataController.get(this).getValueFor(task) > 0
			checkBox.setOnCheckedChangeListener { _, isChecked ->
				TaskDataController.get(this).setValueForTask(task, if (isChecked) 1 else 0)
				this.updateTaskViews()
			}
		} else if (task.formType == FormType.POSITIVE_INTEGER) {
			val numberInput = taskView.findViewById<EditText>(R.id.TaskNumberInput)
			numberInput.visibility = View.VISIBLE
			numberInput.setText("${TaskDataController.get(this).getValueFor(task)}")
			numberInput.addTextChangedListener(object : TextWatcher {
				override fun afterTextChanged(p0: Editable?) {
					val numberInputValue = numberInput.text.toString().toIntOrNull() ?: return
					TaskDataController.get(this@MainActivity).setValueForTask(task, numberInputValue)
					this@MainActivity.updateTaskViews()
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

		//Adds a button to expand and collapse the subTaskContainer if the task has sub-tasks
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

	/**
	 * Updates the appearance for all taskViews based off of their completion
	 */
	private fun updateTaskViews() {
		Task.values().forEach { task ->
			val completion = task.evaluateIsCompleted(TaskDataController.get(this).getValuesForTask(task))
			this.findViewById<LinearLayout>(R.id.TaskContainer).findViewWithTag<RelativeLayout>(task.name).background = this.resources.getDrawable(
				when (completion) {
					null -> R.color.taskCompletionIrrelevant
					true -> R.color.taskComplete
					false -> R.color.taskIncomplete
				},
				this.theme
			)
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
