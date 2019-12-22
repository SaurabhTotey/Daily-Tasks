package com.saurabhtotey.dailytasks.task

/**
 * An enumeration of the different types of ways a task can be filled out
 * CHECKBOX should be used for boolean values
 * POSITIVE_INTEGER should be used for tasks that take in a positive integer that allows 0
 * NONE should be used for tasks that their completion off of some other criteria (eg. sub-tasks)
 */
enum class FormType {
	CHECKBOX, POSITIVE_INTEGER, NONE
}
