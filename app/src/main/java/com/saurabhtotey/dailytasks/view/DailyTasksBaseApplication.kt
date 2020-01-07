package com.saurabhtotey.dailytasks.view

import android.app.*

/**
 * A base application class that is the entry point of the app and represents the whole app
 * Currently only registers notification timing
 */
class DailyTasksBaseApplication : Application() {

	/**
	 * Initializes the app and registers notifications
	 */
	override fun onCreate() {
		super.onCreate()
		scheduleNotification(this)
	}

}
