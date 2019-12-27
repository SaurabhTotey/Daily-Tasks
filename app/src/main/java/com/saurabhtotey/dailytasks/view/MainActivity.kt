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

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val listView = this.findViewById<ListView>(R.id.ListView)
		listView.adapter = TaskAdapter(this)
		listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			val previousInflation = TaskDataController.get(this).inflatedTask
			val selectedTask = listView.adapter.getItem(position) as Task
			if (previousInflation != null) {
				listView.findViewWithTag<RelativeLayout>(previousInflation.name).findViewById<TextView>(R.id.TaskDescription).visibility = View.GONE
				if (selectedTask == previousInflation) {
					TaskDataController.get(this).inflatedTask = null
					return@OnItemClickListener
				}
			}
			listView.findViewWithTag<RelativeLayout>(selectedTask.name).findViewById<TextView>(R.id.TaskDescription).visibility = View.VISIBLE
			TaskDataController.get(this).inflatedTask = selectedTask
		}
	}
}
