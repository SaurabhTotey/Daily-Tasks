package com.saurabhtotey.dailytasks.view

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.saurabhtotey.dailytasks.R
import com.saurabhtotey.dailytasks.TaskDataController
import com.saurabhtotey.dailytasks.model.TaskStatus
import com.saurabhtotey.dailytasks.model.primaryTasks
import java.util.*

/**
 * Schedules a notification to happen 3 hours from now
 */
fun scheduleNotification(context: Context) {
	val alarmStartTime = Calendar.getInstance().also {
		it.set(Calendar.MINUTE, 0)
		it.set(Calendar.SECOND, 0)
		it.set(Calendar.MILLISECOND, 0)
		it.add(Calendar.HOUR, 3)
	}
	(context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).setAndAllowWhileIdle(
		AlarmManager.RTC_WAKEUP,
		alarmStartTime.timeInMillis,
		PendingIntent.getBroadcast(
			context,
			0,
			Intent(context, NotificationSender::class.java),
			0
		)
	)
}

/**
 * The broadcast receiver that manages sending notifications
 * Ensures that another notification is scheduled after a notification is sent (so that they are recurring)
 */
class NotificationSender: BroadcastReceiver() {

	private val notificationChannelId = "DailyTasksNotificationChannel"

	/**
	 * The method that is called hourly: sends a notification detailing the amount of remaining tasks
	 * and then schedules a notification for the next hour
	 */
	override fun onReceive(context: Context, intent: Intent?) {

		//Schedules another notification for the next hour because notifications are recurring
		scheduleNotification(context)

		//Leaves if this is only being scheduled because of the phone booting up
		if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
			return
		}

		//Creates the notificationManager and the channel to send notifications on
		val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.createNotificationChannel(NotificationChannel(
			this.notificationChannelId,
			context.getString(R.string.notification_channel_name),
			NotificationManager.IMPORTANCE_DEFAULT).also {
				it.description = context.getString(R.string.notification_channel_description)
				it.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
			}
		)

		//Creates the intent that will open the main activity when the notification is tapped on
		val appOpeningIntent = PendingIntent.getActivity(
			context,
			0,
			Intent(context, MainActivity::class.java).also {
				it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
			},
			0
		)

		val notification = NotificationCompat.Builder(context, this.notificationChannelId)
			.setSmallIcon(R.mipmap.ic_launcher)
			.setContentTitle(context.getString(R.string.notification_title))
			.setPriority(NotificationCompat.PRIORITY_DEFAULT)
			.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
			.setCategory(NotificationCompat.CATEGORY_REMINDER)
			.setOnlyAlertOnce(true)
			.setContentIntent(appOpeningIntent)
			.setAutoCancel(true)

		TaskDataController.initialize(context)

		//Counts incomplete tasks and cancels existing notifications if there are no remaining tasks
		val passingStates = arrayOf(TaskStatus.BEYOND_COMPLETE, TaskStatus.COMPLETE, TaskStatus.COMPLETION_IRRELEVANT)
		val today = Calendar.getInstance()
		val numberOfIncompleteTasks = primaryTasks.count { task ->
			!passingStates.contains(task.evaluateIsCompleted(TaskDataController.getValueFor(task, today)))
		}
		if (numberOfIncompleteTasks == 0) {
			notificationManager.cancel(0)
			return
		}

		//Sends the notification
		notification.setContentText(context.resources.getQuantityString(R.plurals.notification_text, numberOfIncompleteTasks, numberOfIncompleteTasks))
		notificationManager.notify(0, notification.build())
	}

}
