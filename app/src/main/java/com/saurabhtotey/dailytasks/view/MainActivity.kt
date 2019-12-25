package com.saurabhtotey.dailytasks.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ListView
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
			TaskDataController.get(this).inflatedTask = listView.adapter.getItem(position) as Task?
			listView.invalidate() //forces listView to redraw
		}
	}
}
