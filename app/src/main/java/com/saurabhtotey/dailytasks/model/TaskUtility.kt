package com.saurabhtotey.dailytasks.model

/**
 * Filters all the tasks and gets all non-sub-tasks
 */
val primaryTasks: Array<Task> get() {
	val subTasks = Task.values().flatMap { it.subTasks.toList() }
	return Task.values().filter { !subTasks.contains(it) }.toTypedArray()
}

/**
 * A function that allows for easy generation of a evaluateComplete function for Tasks
 * Allows tasks to specify thresholds for each TaskStatus (or null if that TaskStatus value shouldn't be achievable)
 * Assumes that non-null thresholds are given in descending order
 */
fun generateCompletionFunctionBasedOnThresholds(beyondCompleteThreshold: Int?, completeThreshold: Int?, inProgressOrAttemptedThreshold: Int?, defaultValue: TaskStatus) : (TaskValue) -> TaskStatus {
	return {
		if (beyondCompleteThreshold != null && it.value >= beyondCompleteThreshold) {
			TaskStatus.BEYOND_COMPLETE
		} else if (completeThreshold != null && it.value >= completeThreshold) {
			TaskStatus.COMPLETE
		} else if (inProgressOrAttemptedThreshold != null && it.value >= inProgressOrAttemptedThreshold) {
			TaskStatus.IN_PROGRESS_OR_ATTEMPTED
		} else {
			defaultValue
		}
	}
}
