package crow.evilo.plugin.template

import crow.evilo.plugin.api.EviloHostService
import crow.evilo.plugin.api.EviloPluginService
import crow.wasmline.Wasmline
import crow.wasmline.bind
import crow.wasmline.link

fun main() {
    val runtime = Wasmline.get()
    runtime.bind(EviloPluginService::class, TemplatePlugin { runtime.link<EviloHostService>() })
}
