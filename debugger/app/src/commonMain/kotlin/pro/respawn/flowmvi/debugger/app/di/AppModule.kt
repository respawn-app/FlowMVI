package pro.respawn.flowmvi.debugger.app.di

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

@OptIn(ExperimentalSerializationApi::class)
val appModule = module {
    single {
        Json {
            prettyPrint = true
            allowTrailingComma = true
            coerceInputValues = true
            decodeEnumsCaseInsensitive = true
            explicitNulls = false
            ignoreUnknownKeys = true
        }
    }
}
