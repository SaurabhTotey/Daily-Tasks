package com.saurabhtotey.dailytasks.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.saurabhtotey.dailytasks.R
import com.saurabhtotey.dailytasks.TaskDataController
import com.saurabhtotey.dailytasks.model.Task

/**
 * An adapter that returns views for each given task
 * Tasks are displayed as titles, descriptions, and a form field for marking some sort of completion info
 * Tasks also display with an indented list of sub-tasks that the parent task may consider when evaluating completion
 * TODO: Tasks have a green background when considered complete, red for incomplete, and white for when completion is meaningless in the context of the task
 * Task descriptions expand or collapse whenever the task is selected, but that is handled by the listView in MainActivity
 */
class TaskAdapter(context: Context) : ArrayAdapter<Task>(context, 0, TaskDataController.get(context).getPrimaryTasks()) {

	/**
	 * Makes a view for the item at the given position
	 */
	override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
		//Gets the task and inflates a main_task layout for it
		val task = this.getItem(position)!!
		val taskView = LayoutInflater.from(this.context).inflate(R.layout.main_task, parent, false)

		//TODO: use view holder pattern (do this last once everything else is done); see https://dzone.com/articles/optimizing-your-listview

		//Populates the main_task view with the task's information
		taskView.tag = task.name
		taskView.findViewById<TextView>(R.id.TaskTitle).text = task.displayName
		taskView.findViewById<TextView>(R.id.TaskDescription).text = task.description

		//TODO: put in the sub tasks

		//TODO: put in completion forms for primary and sub tasks

		return taskView
	}

}
