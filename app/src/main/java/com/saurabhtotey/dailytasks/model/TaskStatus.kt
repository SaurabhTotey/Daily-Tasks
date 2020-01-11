package com.saurabhtotey.dailytasks.model

/**
 * An enumeration of the types of statuses that a task may be in
 * Ideally, by the end of the day, all tasks are either COMPLETION_IRRELEVANT, COMPLETE, or BEYOND_COMPLETE
 */
enum class TaskStatus {
	BEYOND_COMPLETE, COMPLETE, IN_PROGRESS_OR_ATTEMPTED, INCOMPLETE, COMPLETION_IRRELEVANT
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
