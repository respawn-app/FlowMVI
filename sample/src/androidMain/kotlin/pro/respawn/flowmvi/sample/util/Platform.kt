package pro.respawn.flowmvi.sample.util

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
val supportsBlur get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
val supportsDynamicColors get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
val supportsShaders get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
