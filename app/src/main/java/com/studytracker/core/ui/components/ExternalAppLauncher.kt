package com.studytracker.core.ui.components

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

private const val ANKIDROID_PACKAGE_PREFIX = "com.ichi2.anki"
private const val ANKIDROID_CANONICAL_PACKAGE = "com.ichi2.anki"

data class ExternalLaunchResult(
    val launched: Boolean,
    val packageName: String? = null
)

/**
 * Launch AnkiDroid without assuming it came from one specific store.
 *
 * Official Play, F-Droid and normal GitHub builds use com.ichi2.anki.
 * GitHub Parallel builds may use a package suffix, so launcher activities
 * whose package starts with com.ichi2.anki are accepted as well.
 */
fun launchAnkiDroid(
    context: Context,
    preferredPackage: String? = null
): ExternalLaunchResult {
    val pm = context.packageManager

    val preferred = preferredPackage
        ?.trim()
        ?.removePrefix("package:")
        ?.takeIf { it.isNotBlank() }

    val candidates = linkedSetOf<String>()
    if (preferred != null) candidates += preferred
    candidates += ANKIDROID_CANONICAL_PACKAGE
    candidates += discoverAnkiPackages(pm)

    for (packageName in candidates) {
        val launched = launchPackage(context, pm, packageName)
        if (launched) {
            return ExternalLaunchResult(
                launched = true,
                packageName = packageName
            )
        }
    }

    return ExternalLaunchResult(launched = false)
}

private fun discoverAnkiPackages(pm: PackageManager): List<String> {
    val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }

    val matches = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        pm.queryIntentActivities(
            launcherIntent,
            PackageManager.ResolveInfoFlags.of(0L)
        )
    } else {
        @Suppress("DEPRECATION")
        pm.queryIntentActivities(launcherIntent, 0)
    }

    return matches
        .asSequence()
        .mapNotNull { it.activityInfo?.packageName }
        .filter { it == ANKIDROID_PACKAGE_PREFIX || it.startsWith("$ANKIDROID_PACKAGE_PREFIX.") }
        .distinct()
        .toList()
}

private fun launchPackage(
    context: Context,
    pm: PackageManager,
    packageName: String
): Boolean {
    // First try Android's canonical launch intent.
    val normalLaunch = runCatching {
        pm.getLaunchIntentForPackage(packageName)
    }.getOrNull()

    if (normalLaunch != null) {
        val launched = runCatching {
            normalLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(normalLaunch)
            true
        }.getOrDefault(false)
        if (launched) return true
    }

    // Package visibility may make getLaunchIntentForPackage() return null even
    // when the caller knows the package. Try an explicit launcher intent.
    val explicitLaunch = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
        setPackage(packageName)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    return runCatching {
        context.startActivity(explicitLaunch)
        true
    }.getOrDefault(false)
}
