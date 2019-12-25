package com.saurabhtotey.dailytasks.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.saurabhtotey.dailytasks.R
import com.saurabhtotey.dailytasks.TaskDataController
import com.saurabhtotey.dailytasks.model.Task

/**
 * An adapter that returns views for each given task
 */
class TaskAdapter(context: Context) : ArrayAdapter<Task>(context, 0, TaskDataController.get(context).getPrimaryTasks()) {

	/**
	 * There are 2 different types of views we can be using
	 */
	override fun getViewTypeCount(): Int {
		return 2
	}

	/**
	 * Gets a view for the item at the given position
	 */
	override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
		if (convertView != null) {
			return convertView
		}

		val task = this.getItem(position)!!
		val convertView = LayoutInflater.from(this.context).inflate(
			if (TaskDataController.get(this.context).inflatedTask == task) {
				R.layout.main_task_inflated
			} else {
				R.layout.main_task
			}
		, parent)

		//TODO: give the view a form

		//TODO: put in the sub tasks

		return convertView

	}

}
