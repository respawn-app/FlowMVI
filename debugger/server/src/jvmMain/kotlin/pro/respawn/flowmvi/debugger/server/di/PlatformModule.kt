package pro.respawn.flowmvi.debugger.server.di

import org.koin.dsl.module
import pro.respawn.flowmvi.debugger.server.platform.FileManager
import pro.respawn.flowmvi.debugger.server.platform.JvmFileManager

actual val platformModule = module {
    single<FileManager> { JvmFileManager() }
}
