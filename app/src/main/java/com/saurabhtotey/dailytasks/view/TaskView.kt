package com.saurabhtotey.dailytasks.view

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.saurabhtotey.dailytasks.R
import com.saurabhtotey.dailytasks.model.FormType
import com.saurabhtotey.dailytasks.model.Task

/**
 * A class that represents a view for a task
 * Holds all relevant child views and sub-task TaskViews
 * Takes in what task the view is supposed to represent, its depth (0 means its a main task, 1 means sub-task, 2 means sub-sub-task, so on), its parent, and the context
 * Constructing a TaskView will add it to its parent automatically thereby modifying it
 */
class TaskView(val task: Task, taskDepth: Int, parent: LinearLayout, private var context: Context) {

	private val taskView = LayoutInflater.from(this.context).inflate(R.layout.task, parent, false)
	private val titleView: TextView
	private val descriptionView: TextView
	private val formDescriptionView: TextView
	private val subTaskContainer: LinearLayout
	private val subTaskExpansionButton: ImageButton
	private val subTaskViews: Array<TaskView>

	/**
	 * The view will only either have checkbox or numberInput or neither, but never both
	 * Which is not null will reflect this.task.formType
	 */
	private var checkBox: CheckBox? = null
	private var numberInput: EditText? = null

	var isDescriptionExpanded = false
		set(value) {
			this.descriptionView.visibility = if (value) View.VISIBLE else View.GONE
			field = value
		}
	var isSubTaskContainerExpanded = false
		set(value) {
			this.subTaskExpansionButton.setImageDrawable(this.context.resources.getDrawable(
				if (value) android.R.drawable.arrow_up_float else android.R.drawable.arrow_down_float,
				this.context.theme
			))
			this.subTaskContainer.visibility = if (value) View.VISIBLE else View.GONE
			field = value
		}

	/**
	 * Initializes all child views and fills out task information into the view
	 * Also links up view behaviours to relevant children
	 */
	init {
		//Adds the view to its parent and makes it easily findable by setting its tag to its task's name
		parent.addView(taskView)
		this.taskView.tag = this.task.name

		//Finds all relevant child views
		this.titleView = this.taskView.findViewById(R.id.TaskTitle)
		this.descriptionView = this.taskView.findViewById(R.id.TaskDescription)
		this.formDescriptionView = this.taskView.findViewById(R.id.TaskFormDescription)
		this.subTaskContainer = this.taskView.findViewById(R.id.SubTaskContainer)
		this.subTaskExpansionButton = this.taskView.findViewById(R.id.ExpandSubTasksButton)

		//Fills in task information
		this.titleView.text = this.task.displayName
		this.descriptionView.text = this.task.description
		this.formDescriptionView.text = this.task.formDescription

		//Shortens title based on task depth so that form controls can be aligned
		this.titleView.layoutParams.width -= taskDepth * parent.paddingStart

		//Toggles the description's visibility when the view is tapped on
		this.taskView.setOnClickListener { this.isDescriptionExpanded = !this.isDescriptionExpanded }

		//TODO:
		if (this.task.formType == FormType.CHECKBOX) {
			this.checkBox = this.taskView.findViewById(R.id.TaskCheckBox)
			this.checkBox!!.visibility = View.VISIBLE
			this.checkBox!!.setOnCheckedChangeListener { _, isChecked -> /*TODO:*/ }
		} else if (this.task.formType == FormType.POSITIVE_INTEGER) {
			this.numberInput = this.taskView.findViewById(R.id.TaskNumberInput)
			this.numberInput!!.visibility = View.VISIBLE
			this.numberInput!!.addTextChangedListener(object : TextWatcher {
				override fun afterTextChanged(text: Editable?) {
					val numberInputValue = text.toString().toIntOrNull() ?: return
					/*TODO:*/
				}
				override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
				override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			})
			this.numberInput!!.setOnEditorActionListener { _, actionId, _ ->
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					this.numberInput!!.clearFocus()
				}
				false
			}
			this.numberInput!!.setOnFocusChangeListener { _, hasFocus ->
				if (!hasFocus && this.numberInput!!.text.isBlank()) {
					this.numberInput!!.setText("0")
				}
			}
			//TODO: unfocus when keyboard is dismissed: this is difficult and is probably going to need https://stackoverflow.com/questions/3425932/detecting-when-user-has-dismissed-the-soft-keyboard
		}

		//Makes TaskViews for the sub-tasks and makes the expansion button toggle sub-task visibility if there are sub-tasks
		this.subTaskViews = this.task.subTasks.map { TaskView(it, taskDepth + 1, this.subTaskContainer, this.context) }.toTypedArray()
		if (this.task.subTasks.isNotEmpty()) {
			this.subTaskExpansionButton.visibility = View.VISIBLE
			this.subTaskExpansionButton.setOnClickListener { this.isSubTaskContainerExpanded = !this.isSubTaskContainerExpanded }
		}
	}

}
