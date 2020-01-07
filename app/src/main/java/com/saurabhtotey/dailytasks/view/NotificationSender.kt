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
import java.util.*

/**
 * Schedules a notification for the next hour
 */
fun scheduleNotification(context: Context) {
	val alarmStartTime = Calendar.getInstance().also {
		it.set(Calendar.MINUTE, 0)
		it.set(Calendar.SECOND, 0)
		it.set(Calendar.MILLISECOND, 0)
		it.add(Calendar.HOUR, 1)
	}
	(context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).setExactAndAllowWhileIdle(
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

		//Schedules another notification for the next hour because notifications are recurring
		scheduleNotification(context)

		//Counts incomplete tasks and doesn't send a notification if there are no remaining tasks
		val numberOfIncompleteTasks = TaskDataController.get(context).getPrimaryTasks().count { task ->
			task.evaluateIsCompleted(TaskDataController.get(context).getValueFor(task, Calendar.getInstance().time)) == false
		}
		if (numberOfIncompleteTasks == 0) {
			return
		}

		//Sends the notification
		notification.setContentText(context.resources.getQuantityString(R.plurals.notification_text, numberOfIncompleteTasks, numberOfIncompleteTasks))
		notificationManager.notify(0, notification.build())
	}

}
