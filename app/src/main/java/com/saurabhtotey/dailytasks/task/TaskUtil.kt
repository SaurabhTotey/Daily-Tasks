package com.saurabhtotey.dailytasks.task

/**
 * Filters all the tasks and gets all non-sub-tasks
 */
fun getPrimaryTasks(): Array<Task> {
	val subTasks = Task.values().flatMap { it.subTasks.toList() }
	return Task.values().filter { !subTasks.contains(it) }.toTypedArray()
}

//TODO: make methods for serializing and deserializing task data
