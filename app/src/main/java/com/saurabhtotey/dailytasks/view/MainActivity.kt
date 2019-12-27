package com.saurabhtotey.dailytasks.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import com.saurabhtotey.dailytasks.R
import com.saurabhtotey.dailytasks.TaskDataController
import com.saurabhtotey.dailytasks.model.Task

/**
 * The main view for this application
 * In its most basic essence, shows a list of tasks
 * Handles the listView and expanding/collapsing task descriptions when tasks get selected
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

		//Gets the listView and sets its adapter
		val listView = this.findViewById<ListView>(R.id.ListView)
		listView.adapter = TaskAdapter(this)

		//Allows descriptions to expand and collapse when selected
		listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			//Finds what was previously expanded and what was just selected
			val previousExpansion = TaskDataController.get(this).expandedTask
			val selectedTask = listView.adapter.getItem(position) as Task
			//If something else was previously expanded, collapse it
			if (previousExpansion != null) {
				listView.findViewWithTag<RelativeLayout>(previousExpansion.name).findViewById<TextView>(R.id.TaskDescription).visibility = View.GONE
				//Since the user selected something that was already expanded and has now been collapsed, mark nothing as expanded and leave
				if (selectedTask == previousExpansion) {
					TaskDataController.get(this).expandedTask = null
					return@OnItemClickListener
				}
			}
			//Expand what the user selected and mark that task as expanded
			listView.findViewWithTag<RelativeLayout>(selectedTask.name).findViewById<TextView>(R.id.TaskDescription).visibility = View.VISIBLE
			TaskDataController.get(this).expandedTask = selectedTask
		}
	}
}
