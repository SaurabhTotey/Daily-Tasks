package com.saurabhtotey.dailytasks.view

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.saurabhtotey.dailytasks.R
import com.saurabhtotey.dailytasks.TaskDataController
import java.util.*

/**
 * A base application class that is the entry point of the app and represents the whole app
 * Currently only registers notification timing and controller updates in onCreate
 */
class DailyTasksBaseApplication : Application() {

	private val notificationChannelId = "DailyTasksNotificationChannel"
	private var currentActivity: Activity? = null

	/**
	 * Initializes the app and registers timed events
	 */
	override fun onCreate() {
		super.onCreate()

		this.registerActivityLifecycleCallbacks(object: ActivityLifecycleCallbacks {
			override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
				this@DailyTasksBaseApplication.currentActivity = activity
			}
			override fun onActivityPaused(activity: Activity) {}
			override fun onActivityStarted(activity: Activity) {}
			override fun onActivityDestroyed(activity: Activity) {}
			override fun onActivitySaveInstanceState(activity: Activity, savedInstanceState: Bundle) {}
			override fun onActivityStopped(activity: Activity) {}
			override fun onActivityResumed(activity: Activity) {}
		})

		val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
		this.initializeTaskDataControllerUpdateOnNewDay(alarmManager)
		this.initializeHourlyNotificationSending(alarmManager)
	}

	/**
	 * Sets up a daily action that first triggers in midnight in the future and then triggers daily where TaskDataController gets data for the day
	 * TODO: this doesn't work
	 */
	private fun initializeTaskDataControllerUpdateOnNewDay(alarmManager: AlarmManager) {
		class TaskDataControllerUpdater: BroadcastReceiver() {
			override fun onReceive(context: Context, intent: Intent) {
				TaskDataController.get(context).initializeNewDayData()
				this@DailyTasksBaseApplication.currentActivity?.recreate()
			}
		}
		val alarmStartTime = Calendar.getInstance().also {
			it.set(Calendar.HOUR, 0)
			it.set(Calendar.MINUTE, 0)
			it.set(Calendar.SECOND, 0)
			it.set(Calendar.MILLISECOND, 0)
			it.add(Calendar.DATE, 1)
		}
		alarmManager.setRepeating(
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
	 * TODO: this doesn't work
	 * TODO: look at https://stackoverflow.com/questions/36902667/how-to-schedule-notification-in-android
	 */
	private fun initializeHourlyNotificationSending(alarmManager: AlarmManager) {
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
				val numberOfIncompleteTasks = TaskDataController.get(this@DailyTasksBaseApplication).getPrimaryTasks().count { task ->
					task.evaluateIsCompleted(TaskDataController.get(this@DailyTasksBaseApplication).getValueFor(task)) == false
				}
				if (numberOfIncompleteTasks == 0) {
					return
				}
				notification.setContentText(this@DailyTasksBaseApplication.resources.getQuantityString(R.plurals.notification_text, numberOfIncompleteTasks, numberOfIncompleteTasks))
				notificationManager.notify(0, notification.build())
			}
		}
		val alarmStartTime = Calendar.getInstance().also {
			it.set(Calendar.MINUTE, 0)
			it.set(Calendar.SECOND, 0)
			it.set(Calendar.MILLISECOND, 0)
			it.add(Calendar.HOUR, 1)
		}
		alarmManager.setRepeating(
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

}
