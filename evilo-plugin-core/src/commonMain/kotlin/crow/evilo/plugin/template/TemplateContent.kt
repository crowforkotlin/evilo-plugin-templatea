package crow.evilo.plugin.template

import crow.evilo.model.MediaChapter
import crow.evilo.model.MediaContent
import crow.evilo.model.MediaId
import crow.evilo.model.MediaItem
import crow.evilo.model.MediaKind
import crow.evilo.plugin.api.EviloHostService
import crow.evilo.plugin.api.PluginDescriptor
import crow.evilo.plugin.api.PluginOperation
import crow.evilo.plugin.api.PluginOperationKind
import crow.evilo.plugin.api.PluginResult

class TemplateContent(private val descriptor: PluginDescriptor, private val host: () -> EviloHostService) {
    private val item = MediaItem(
        MediaId(descriptor.id, MediaKind.NOVEL, "storage-demo"),
        "Host storage example",
        "https://example.invalid/storage-demo",
        description = "Local SDK demonstration content, not a real provider.",
    )
    private val chapter = MediaChapter(item.id, "chapter-1", "Persistent plugin settings", item.url)

    fun execute(operation: PluginOperation): PluginResult {
        require(operation.kind == MediaKind.NOVEL) { "The template only supports its local novel example." }
        // Host lookup is deferred until an operation; construction performs no host calls.
        val storage = host()
        return when (operation.operation) {
            PluginOperationKind.SEARCH -> {
                storage.writeValue("last-query", operation.query)
                PluginResult(items = if (operation.page == 1 &&
                    (operation.query.isBlank() || item.title.contains(operation.query, true))) listOf(item) else emptyList())
            }
            PluginOperationKind.DETAILS -> {
                require(operation.item?.id == item.id)
                PluginResult(details = item)
            }
            PluginOperationKind.CHAPTERS -> {
                require(operation.item?.id == item.id)
                PluginResult(chapters = listOf(chapter))
            }
            PluginOperationKind.CONTENT -> {
                val selected = requireNotNull(operation.chapter)
                require(selected.id == chapter.id && selected.itemId == item.id)
                PluginResult(content = MediaContent(text = "This is bundled template demonstration content.\n\n" +
                    "The host owns persistent storage and isolates each signed plugin ID.\n\n" +
                    "Last search query: ${storage.readValue("last-query").orEmpty()}"))
            }
        }
    }
}
