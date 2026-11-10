package crow.evilo.plugin.template

import crow.evilo.plugin.api.EviloHostService
import crow.evilo.plugin.api.EviloPluginService
import crow.evilo.plugin.api.PluginJson
import crow.evilo.plugin.api.PluginOperation
import crow.evilo.plugin.api.PluginStep
import crow.evilo.plugin.build.releaseDescriptor

class TemplatePlugin(host: () -> EviloHostService) : EviloPluginService {
    private val content = TemplateContent(releaseDescriptor, host)

    override fun describe(): String = PluginJson.encodeToString(releaseDescriptor)

    override fun start(operationJson: String): String {
        val operation = PluginJson.decodeFromString<PluginOperation>(operationJson)
        return PluginJson.encodeToString(PluginStep(result = content.execute(operation)))
    }

    override fun resume(operationJson: String, context: String, responseJson: String): String =
        error("The self-contained template never requests network I/O.")
}
