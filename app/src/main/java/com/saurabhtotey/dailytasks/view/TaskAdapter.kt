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
 */
class TaskAdapter(context: Context) : ArrayAdapter<Task>(context, 0, TaskDataController.get(context).getPrimaryTasks()) {

	/**
	 * Makes a view for the item at the given position
	 */
	override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
		val task = this.getItem(position)!!

		val taskView = LayoutInflater
			.from(this.context)
			.inflate(
				if (TaskDataController.get(this.context).inflatedTask == task) R.layout.main_task_inflated else R.layout.main_task,
				parent,
				false
			)

		taskView.findViewById<TextView>(R.id.TaskTitle).text = task.displayName
		taskView.findViewById<TextView?>(R.id.TaskDescription)?.text = task.description

		//TODO: put in the sub tasks

		//TODO: put in completion forms for primary and sub tasks

		return taskView
	}

}
