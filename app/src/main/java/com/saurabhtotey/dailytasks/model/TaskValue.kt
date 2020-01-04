package com.saurabhtotey.dailytasks.model

/**
 * A class that holds the value data for a task and contains the TaskValues of the sub-tasks
 * Is essentially a tree data structure to allow a task to access the values of any of its sub-tasks
 */
class TaskValue(val value: Int, val subTaskValues: List<TaskValue>)
