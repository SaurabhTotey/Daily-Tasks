package com.saurabhtotey.dailytasks.model

/**
 * An enumeration of the types of statuses that a task may be in
 * Ideally, by the end of the day, all tasks are either COMPLETION_IRRELEVANT, COMPLETE, or BEYOND_COMPLETE
 */
enum class TaskStatus {
	BEYOND_COMPLETE, COMPLETE, IN_PROGRESS_OR_ATTEMPTED, INCOMPLETE, COMPLETION_IRRELEVANT
}
