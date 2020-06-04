package com.saurabhtotey.dailytasks.model

/**
 * An enumeration of all the daily tasks I must complete
 *
 * displayName is how the task will be displayed to the user
 * description is a more in-depth explanation of what the task entails or requires
 * formType takes in a FormType enumeration to specify how the user will input data for the task
 * formDescription describes the units or inputs for the form
 * evaluateIsCompleted is a function that takes in form data (more on form data below) and evaluates whether the task counts as completed or not (null return means completion is irrelevant: it isn't completed or not completed)
 * subTasks is an array of tasks that will visually appear to be a subordinate task to the current task or will visually appear to be hierarchically below or included as a part of the current task
 *
 * The actual caps name for the enumeration values is what is recorded when task data is written to file, along with integers describing their data
 * A series of integers is recorded as a part of task data: the first integer corresponds to the data of the current task while subsequent integers represent the data for the subTasks in the same order as the subTasks are listed in the task data
 * These integers are what gets passed to evaluateIsCompleted
 *
 * A checkbox will record values as either 0s or 1s for the corresponding boolean values
 * A positive integer will record values as its integer
 * An empty form type (NONE) will record as always 0s
 *
 * TODO: consider passing in a date along with the array of integers to evaluateIsComplete so that the evaluation can account for the date
 * TODO: ideally these tasks would be encoded in some external format like a JSON file so they are more configurable, but that is difficult because tasks also need to specify their completion evaluation logic
 */
enum class Task(val displayName: String, val description: String, val formType: FormType = FormType.CHECKBOX, val formDescription: String = "", val evaluateIsCompleted: (TaskValue) -> TaskStatus = { if (it.value > 0) TaskStatus.COMPLETE else TaskStatus.INCOMPLETE }, val subTasks: Array<Task> = arrayOf()) {
	MEDITATE(
		"Meditate",
		"Meditate for at least 5 minutes. Do not do this at the expense of sleep.",
		FormType.POSITIVE_INTEGER,
		"minutes",
		generateCompletionFunctionBasedOnThresholds(15, 5, null, TaskStatus.INCOMPLETE)
	),
	SHOWER(
		"Shower",
		"Take a shower."
	),
	BRUSH_TEETH(
		"Brush Teeth",
		"Brush your teeth at least twice today.",
		FormType.POSITIVE_INTEGER,
		"times",
		generateCompletionFunctionBasedOnThresholds(null, 2, 1, TaskStatus.INCOMPLETE)
	),
	PRACTICE(
		"Practice Piano",
		"Spend at least 30 minutes practicing the piano. If possible, also practice cello.",
		FormType.POSITIVE_INTEGER,
		"minutes",
		generateCompletionFunctionBasedOnThresholds(60, 30, 1, TaskStatus.INCOMPLETE)
	),
	EAT_HEALTHY("Eat Healthy Meals", "Eat healthy meals.", FormType.POSITIVE_INTEGER, "meals", { if (it.value >= 2) TaskStatus.BEYOND_COMPLETE else if (it.value == 1) TaskStatus.COMPLETE else TaskStatus.INCOMPLETE }),
	EAT_MISCELLANEOUS("Eat Meals", "Eat meals.", FormType.POSITIVE_INTEGER, "meals", { TaskStatus.COMPLETION_IRRELEVANT }),
	EAT(
		"Eat",
		"Eat at least two meals today. At least one of those meals needs to be healthy.",
		FormType.NONE,
		"",
		{
			val healthyMeals = it.subTaskValues[0].value
			val totalMeals = it.subTaskValues[0].value + it.subTaskValues[1].value
			if (healthyMeals >= 2)
				TaskStatus.BEYOND_COMPLETE
			else if (healthyMeals == 1 && totalMeals >= 2)
				TaskStatus.COMPLETE
			else if (totalMeals > 0)
				TaskStatus.IN_PROGRESS_OR_ATTEMPTED
			else
				TaskStatus.INCOMPLETE
		},
		arrayOf(EAT_HEALTHY, EAT_MISCELLANEOUS)
	),
	EXERCISE(
		"Exercise",
		"Exercise for at least 30 minutes.",
		FormType.POSITIVE_INTEGER,
		"minutes",
		generateCompletionFunctionBasedOnThresholds(60, 30, 1, TaskStatus.INCOMPLETE)
	),
	COMMIT(
		"Commit Code",
		"Commit a change on a git repository."
	),
	LEARN_PHYSICS(
		"Learn New Physics",
		"Learn some new physics that you didn't know before."
	),
	SOLVE_PROBLEM(
		"Solve a Problem",
		"Solve any sort of problem. It could be homework, a personal problem, or anything else."
	),
	TALK_NEW("Meet Someone", "Meet and talk with someone you don't know.", FormType.CHECKBOX, "", { if (it.value > 0) TaskStatus.COMPLETE else TaskStatus.COMPLETION_IRRELEVANT }),
	TALK_QUESTION("Ask a Question", "Ask anyone any sort of question. It could be clarification, or asking for help, or anything else.", FormType.CHECKBOX, "", { if (it.value > 0) TaskStatus.COMPLETE else TaskStatus.COMPLETION_IRRELEVANT }),
	TALK_SHARE("Share", "Share a story or a joke or any other sort of information about yourself.", FormType.CHECKBOX, "", { if (it.value > 0) TaskStatus.COMPLETE else TaskStatus.COMPLETION_IRRELEVANT }),
	TALK(
		"Talk",
		"Either meet a new person, ask any sort of question, or share something about yourself.",
		FormType.NONE,
		"",
		{
			val subTaskValues = it.subTaskValues.sumBy { subTaskValue -> subTaskValue.value }
			if (subTaskValues >= 2) TaskStatus.BEYOND_COMPLETE else if (subTaskValues == 1) TaskStatus.COMPLETE else TaskStatus.INCOMPLETE
		},
		arrayOf(TALK_NEW, TALK_QUESTION, TALK_SHARE)
	),
	JOURNAL(
		"Write in Journal",
		"Write about your day in your journal. Think a little bit about what has happened today and what you have done or not done."
	),
	PLANK(
		"Plank",
		"Plank with any plank form for at least 3 minutes (180 seconds), but go for 4 minutes if possible (240 seconds).",
		FormType.POSITIVE_INTEGER,
		"seconds",
		generateCompletionFunctionBasedOnThresholds(240, 180, 1, TaskStatus.INCOMPLETE)
	)
}
