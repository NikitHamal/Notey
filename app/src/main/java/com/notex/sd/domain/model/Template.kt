package com.notex.sd.domain.model

import java.util.UUID

/**
 * Represents a note template for quick note creation.
 * Templates enable users to start with pre-defined structures.
 */
data class Template(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val icon: TemplateIcon,
    val category: TemplateCategory,
    val titleTemplate: String = "",
    val contentTemplate: String = "",
    val color: NoteColor = NoteColor.DEFAULT,
    val isBuiltIn: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val usageCount: Int = 0
) {
    /**
     * Creates a Note from this template.
     */
    fun createNote(customTitle: String? = null): Note {
        val title = customTitle ?: titleTemplate
        return Note.create(
            title = title,
            content = contentTemplate,
            color = color
        )
    }
}

/**
 * Categories for organizing templates.
 */
enum class TemplateCategory(val displayName: String) {
    PERSONAL("Personal"),
    WORK("Work"),
    STUDY("Study"),
    CREATIVE("Creative"),
    PLANNING("Planning"),
    CUSTOM("Custom")
}

/**
 * Icons available for templates.
 */
enum class TemplateIcon(val iconName: String) {
    NOTE("Note"),
    MEETING("Meeting"),
    TODO("Todo"),
    JOURNAL("Journal"),
    IDEA("Idea"),
    PROJECT("Project"),
    RECIPE("Recipe"),
    TRAVEL("Travel"),
    BOOK("Book"),
    CODE("Code"),
    WORKOUT("Workout"),
    SHOPPING("Shopping"),
    GOAL("Goal"),
    HABIT("Habit"),
    CUSTOM("Custom")
}

/**
 * Built-in templates that come with the app.
 */
object BuiltInTemplates {

    val meetingNotes = Template(
        id = "template_meeting",
        name = "Meeting Notes",
        description = "Structured meeting notes with agenda, attendees, and action items",
        icon = TemplateIcon.MEETING,
        category = TemplateCategory.WORK,
        titleTemplate = "Meeting: ",
        contentTemplate = """## Attendees
-

## Agenda
1.

## Discussion Notes


## Action Items
- [ ]

## Next Steps
""",
        color = NoteColor.BLUE
    )

    val dailyJournal = Template(
        id = "template_journal",
        name = "Daily Journal",
        description = "Reflect on your day with gratitude and goals",
        icon = TemplateIcon.JOURNAL,
        category = TemplateCategory.PERSONAL,
        titleTemplate = "Journal - ",
        contentTemplate = """## Today I'm grateful for
-

## How I'm feeling
-

## What happened today


## Tomorrow I want to
-

## Reflections
""",
        color = NoteColor.YELLOW
    )

    val projectPlan = Template(
        id = "template_project",
        name = "Project Plan",
        description = "Plan and track your project from start to finish",
        icon = TemplateIcon.PROJECT,
        category = TemplateCategory.WORK,
        titleTemplate = "Project: ",
        contentTemplate = """## Overview
Brief description of the project.

## Goals
- [ ]

## Milestones
- [ ] Phase 1:
- [ ] Phase 2:
- [ ] Phase 3:

## Resources Needed
-

## Timeline
- Start:
- End:

## Notes
""",
        color = NoteColor.PURPLE
    )

    val ideaBrainstorm = Template(
        id = "template_idea",
        name = "Idea Brainstorm",
        description = "Capture and develop your creative ideas",
        icon = TemplateIcon.IDEA,
        category = TemplateCategory.CREATIVE,
        titleTemplate = "Idea: ",
        contentTemplate = """## The Idea
Describe your idea here.

## Why it matters
-

## How it could work


## Potential challenges
-

## Next steps to explore
- [ ]

## Related ideas
- [[]]
""",
        color = NoteColor.ORANGE
    )

    val weeklyReview = Template(
        id = "template_weekly",
        name = "Weekly Review",
        description = "Review your week and plan for the next",
        icon = TemplateIcon.GOAL,
        category = TemplateCategory.PLANNING,
        titleTemplate = "Week Review - ",
        contentTemplate = """## Wins This Week
-

## Challenges Faced
-

## Lessons Learned
-

## Goals for Next Week
- [ ]
- [ ]
- [ ]

## Notes & Thoughts
""",
        color = NoteColor.GREEN
    )

    val bookNotes = Template(
        id = "template_book",
        name = "Book Notes",
        description = "Capture insights from books you read",
        icon = TemplateIcon.BOOK,
        category = TemplateCategory.STUDY,
        titleTemplate = "Book: ",
        contentTemplate = """## Book Info
- **Author:**
- **Genre:**
- **Started:**
- **Finished:**

## Key Takeaways
1.
2.
3.

## Favorite Quotes
>

## How it applies to my life


## Related Books/Notes
- [[]]
""",
        color = NoteColor.TEAL
    )

    val codeSnippet = Template(
        id = "template_code",
        name = "Code Snippet",
        description = "Save useful code with context",
        icon = TemplateIcon.CODE,
        category = TemplateCategory.WORK,
        titleTemplate = "Code: ",
        contentTemplate = """## Language
-

## Purpose
What this code does.

## Code
```

```

## Usage Example
```

```

## Notes
-
""",
        color = NoteColor.GRAY
    )

    val quickNote = Template(
        id = "template_quick",
        name = "Quick Note",
        description = "Simple note for quick thoughts",
        icon = TemplateIcon.NOTE,
        category = TemplateCategory.PERSONAL,
        titleTemplate = "",
        contentTemplate = "",
        color = NoteColor.DEFAULT
    )

    val allTemplates = listOf(
        quickNote,
        meetingNotes,
        dailyJournal,
        projectPlan,
        ideaBrainstorm,
        weeklyReview,
        bookNotes,
        codeSnippet
    )

    fun getByCategory(category: TemplateCategory): List<Template> {
        return allTemplates.filter { it.category == category }
    }

    fun getById(id: String): Template? {
        return allTemplates.find { it.id == id }
    }
}
