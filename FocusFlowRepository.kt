package com.focusflow.app.data

import android.content.Context
import android.content.SharedPreferences
import com.focusflow.app.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FocusFlowRepository(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("focusflow_preferences", Context.MODE_PRIVATE)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // Onboarding State
    fun isOnboardingCompleted(): Boolean = prefs.getBoolean(KEY_ONBOARDING_DONE, false)

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, completed).apply()
    }

    // Goal
    fun getUserGoal(): String = prefs.getString(KEY_USER_GOAL, "Master Deep Focus & Eliminate Doomscrolling") ?: "Master Deep Focus"

    fun setUserGoal(goal: String) {
        prefs.edit().putString(KEY_USER_GOAL, goal).apply()
    }

    // User Profile
    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "Dinesh") ?: "Dinesh"

    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun getUserBio(): String = prefs.getString(KEY_USER_BIO, "Conquering distractions & mastering flow") ?: "Conquering distractions & mastering flow"

    fun setUserBio(bio: String) {
        prefs.edit().putString(KEY_USER_BIO, bio).apply()
    }

    fun getUserAvatar(): String = prefs.getString(KEY_USER_AVATAR, "⚡") ?: "⚡"

    fun setUserAvatar(avatar: String) {
        prefs.edit().putString(KEY_USER_AVATAR, avatar).apply()
    }

    fun getUserJoinedDate(): String {
        var date = prefs.getString(KEY_USER_JOINED_DATE, null)
        if (date == null) {
            val nowCal = Calendar.getInstance()
            val month = SimpleDateFormat("MMMM yyyy", Locale.US).format(nowCal.time)
            date = "Joined $month"
            prefs.edit().putString(KEY_USER_JOINED_DATE, date).apply()
        }
        return date
    }

    // PIN Security with SHA-256 Hashing
    fun hashPin(pin: String): String {
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(pin.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            pin
        }
    }

    fun getLockPin(): String = prefs.getString(KEY_LOCK_PIN, "1234") ?: "1234"

    fun setLockPin(pin: String) {
        prefs.edit().putString(KEY_LOCK_PIN, hashPin(pin)).apply()
    }

    fun verifyLockPin(enteredPin: String): Boolean {
        val stored = getLockPin()
        return stored == hashPin(enteredPin) || stored == enteredPin
    }

    // Focus Coins
    fun getFocusCoins(): Int = prefs.getInt(KEY_FOCUS_COINS, 100)

    // Custom Timer Durations
    fun getCustomFocusMinutes(): Int = prefs.getInt(KEY_CUSTOM_FOCUS_MINS, 25)

    fun setCustomFocusMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_CUSTOM_FOCUS_MINS, minutes).apply()
    }

    fun getCustomBreakMinutes(): Int = prefs.getInt(KEY_CUSTOM_BREAK_MINS, 5)

    fun setCustomBreakMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_CUSTOM_BREAK_MINS, minutes).apply()
    }

    fun addFocusCoins(amount: Int) {
        val current = getFocusCoins()
        prefs.edit().putInt(KEY_FOCUS_COINS, current + amount).apply()
    }

    fun deductFocusCoins(amount: Int): Boolean {
        val current = getFocusCoins()
        if (current >= amount) {
            prefs.edit().putInt(KEY_FOCUS_COINS, current - amount).apply()
            return true
        }
        return false
    }

    // Regained Screen Time (Minutes)
    fun getSavedScreenTimeMinutes(): Int = prefs.getInt(KEY_SAVED_SCREENTIME, 0)

    fun addSavedScreenTime(minutes: Int) {
        val current = getSavedScreenTimeMinutes()
        prefs.edit().putInt(KEY_SAVED_SCREENTIME, current + minutes).apply()
    }

    // Reels & Shorts Traps Intercepted
    fun getReelsBlockedCount(): Int = prefs.getInt(KEY_REELS_BLOCKED_COUNT, 19)

    fun incrementReelsBlocked() {
        val current = getReelsBlockedCount()
        prefs.edit().putInt(KEY_REELS_BLOCKED_COUNT, current + 1).apply()
        addSavedScreenTime(3) // Each blocked reel saves ~3 minutes of mindless swiping
    }

    // Mindful Interventions Resisted
    fun getInterventionsAccepted(): Int = prefs.getInt(KEY_INTERVENTIONS_ACCEPTED, 14)

    fun incrementInterventionsAccepted() {
        val current = getInterventionsAccepted()
        prefs.edit().putInt(KEY_INTERVENTIONS_ACCEPTED, current + 1).apply()
    }

    // Dopamine Score (0 - 100)
    fun getDopamineScore(): Int {
        val base = 80
        val bonus = (getStreakDays() * 2 + getReelsBlockedCount() / 2 + getInterventionsAccepted()).coerceAtMost(19)
        return (base + bonus).coerceIn(60, 99)
    }

    // Zen Bonsai Focus Garden
    fun getBonsaiStage(): Int = prefs.getInt(KEY_BONSAI_STAGE, 3)

    fun getBonsaiWaterPoints(): Int = prefs.getInt(KEY_BONSAI_WATER_POINTS, 65)

    fun getTotalTreesGrown(): Int = prefs.getInt(KEY_TOTAL_TREES_GROWN, 4)

    fun addBonsaiWater(points: Int): Boolean {
        var currentPoints = getBonsaiWaterPoints() + points
        var currentStage = getBonsaiStage()
        var leveledUp = false

        if (currentPoints >= 100) {
            if (currentStage < 5) {
                currentStage++
                currentPoints -= 100
                leveledUp = true
            } else {
                currentPoints = 100
            }
        }
        prefs.edit()
            .putInt(KEY_BONSAI_STAGE, currentStage)
            .putInt(KEY_BONSAI_WATER_POINTS, currentPoints)
            .apply()
        return leveledUp
    }

    fun harvestAndPlantBonsai(): Boolean {
        val currentStage = getBonsaiStage()
        if (currentStage >= 4) {
            val total = getTotalTreesGrown() + 1
            prefs.edit()
                .putInt(KEY_TOTAL_TREES_GROWN, total)
                .putInt(KEY_BONSAI_STAGE, 1)
                .putInt(KEY_BONSAI_WATER_POINTS, 20)
                .apply()
            addFocusCoins(50) // Harvesting bonus!
            return true
        }
        return false
    }

    fun getBonsaiStatus(): com.focusflow.app.model.ZenBonsaiStatus {
        val stage = getBonsaiStage()
        val (title, emoji) = when (stage) {
            1 -> "Sprout of Intention" to "🌱"
            2 -> "Young Jade Bonsai" to "🌿"
            3 -> "Deep Focus Evergreen" to "🪴"
            4 -> "Blossoming Sakura" to "🌳"
            else -> "Golden Master Bonsai" to "🌸"
        }
        return com.focusflow.app.model.ZenBonsaiStatus(
            stage = stage,
            title = title,
            emoji = emoji,
            waterPoints = getBonsaiWaterPoints(),
            maxWaterPoints = 100,
            totalTreesGrown = getTotalTreesGrown(),
            healthPercent = (90 + getStreakDays().coerceAtMost(10)).coerceIn(80, 100)
        )
    }

    // Coding Challenges
    fun getSolvedChallengeIds(): Set<Int> {
        val set = prefs.getStringSet(KEY_SOLVED_CHALLENGES, null) ?: return emptySet()
        return set.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun recordSolvedChallenge(id: Int) {
        val current = getSolvedChallengeIds().toMutableSet()
        current.add(id)
        prefs.edit().putStringSet(KEY_SOLVED_CHALLENGES, current.map { it.toString() }.toSet()).apply()
    }

    // Short-form Blocker Settings (Regain-style)
    fun getShortFormConfig(): ShortFormBlockerConfig {
        return ShortFormBlockerConfig(
            blockInstagramReels = prefs.getBoolean(KEY_BLOCK_REELS, true),
            blockYouTubeShorts = prefs.getBoolean(KEY_BLOCK_SHORTS, true),
            blockSnapchatSpotlight = prefs.getBoolean(KEY_BLOCK_SPOTLIGHT, true),
            blockWebDoomscrolling = prefs.getBoolean(KEY_BLOCK_WEB_DOOM, true)
        )
    }

    fun saveShortFormConfig(config: ShortFormBlockerConfig) {
        prefs.edit()
            .putBoolean(KEY_BLOCK_REELS, config.blockInstagramReels)
            .putBoolean(KEY_BLOCK_SHORTS, config.blockYouTubeShorts)
            .putBoolean(KEY_BLOCK_SPOTLIGHT, config.blockSnapchatSpotlight)
            .putBoolean(KEY_BLOCK_WEB_DOOM, config.blockWebDoomscrolling)
            .apply()
    }

    // Soundscape
    fun getActiveSoundscape(): FocusSoundscape {
        val name = prefs.getString(KEY_SOUNDSCAPE, FocusSoundscape.SILENT.name)
        return try {
            FocusSoundscape.valueOf(name ?: FocusSoundscape.SILENT.name)
        } catch (e: Exception) {
            FocusSoundscape.SILENT
        }
    }

    fun saveActiveSoundscape(soundscape: FocusSoundscape) {
        prefs.edit().putString(KEY_SOUNDSCAPE, soundscape.name).apply()
    }

    private var cachedLauncherMap: Map<String, String>? = null

    fun getInstalledLauncherPackagesMap(forceRefresh: Boolean = false): Map<String, String> {
        if (!forceRefresh) {
            cachedLauncherMap?.let { return it }
        }
        val pm = context.packageManager
        val launcherIntent = android.content.Intent(android.content.Intent.ACTION_MAIN, null).apply {
            addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        }
        val map = mutableMapOf<String, String>()
        try {
            val resolveList = pm.queryIntentActivities(launcherIntent, 0)
            for (info in resolveList) {
                val pkg = info.activityInfo?.packageName ?: continue
                if (pkg != context.packageName) {
                    val label = info.loadLabel(pm)?.toString() ?: pkg
                    map[pkg] = label
                }
            }
        } catch (_: Exception) {}
        cachedLauncherMap = map
        return map
    }

    fun isPackageInstalled(packageName: String): Boolean {
        if (packageName.isBlank()) return false
        val launcher = getInstalledLauncherPackagesMap()
        if (launcher.containsKey(packageName)) return true
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    // Blocked Apps
    fun getBlockedApps(): List<BlockedApp> {
        val json = prefs.getString(KEY_BLOCKED_APPS, null)
        val launcherMap = getInstalledLauncherPackagesMap()
        if (json != null) {
            try {
                val array = JSONArray(json)
                val list = mutableListOf<BlockedApp>()
                var hadUninstalled = false
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val pkg = obj.optString("packageName", "com.unknown.app")
                    // STRICT CHECK: Skip any app not installed on the user's phone!
                    if (!launcherMap.containsKey(pkg) && !isPackageInstalled(pkg)) {
                        hadUninstalled = true
                        continue
                    }
                    val origBlocked = obj.optBoolean("isBlocked", true)
                    // Educational apps are permanently immune, YouTube whole-app block is driven by user shield rule, while Shorts is intercepted via short-form blocker
                    val isBlocked = if (isEducationalApp(pkg)) {
                        false
                    } else if (pkg == "com.google.android.youtube") {
                        isPackageInBlockedSet("com.google.android.youtube")
                    } else origBlocked
                    list.add(
                        BlockedApp(
                            name = obj.getString("name"),
                            packageName = pkg,
                            category = obj.getString("category"),
                            iconEmoji = obj.getString("iconEmoji"),
                            isBlocked = isBlocked,
                            dailyLimitMinutes = obj.optInt("dailyLimitMinutes", 15),
                            usedMinutesToday = obj.optInt("usedMinutesToday", 0),
                            opensToday = obj.optInt("opensToday", 0),
                            maxOpensPerDay = obj.optInt("maxOpensPerDay", 20),
                            isHardLock = obj.optBoolean("isHardLock", false)
                        )
                    )
                }
                if (hadUninstalled || list.isEmpty()) {
                    if (list.isEmpty()) {
                        val fresh = defaultBlockedApps()
                        saveBlockedApps(fresh)
                        return fresh
                    } else {
                        saveBlockedApps(list)
                    }
                }
                return list
            } catch (e: Exception) {
                // fallback
            }
        }
        val defaults = defaultBlockedApps()
        saveBlockedApps(defaults)
        return defaults
    }

    fun saveBlockedApps(apps: List<BlockedApp>) {
        val array = JSONArray()
        for (app in apps) {
            val obj = JSONObject().apply {
                put("name", app.name)
                put("packageName", app.packageName)
                put("category", app.category)
                put("iconEmoji", app.iconEmoji)
                put("isBlocked", app.isBlocked)
                put("dailyLimitMinutes", app.dailyLimitMinutes)
                put("usedMinutesToday", app.usedMinutesToday)
                put("opensToday", app.opensToday)
                put("maxOpensPerDay", app.maxOpensPerDay)
                put("isHardLock", app.isHardLock)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_BLOCKED_APPS, array.toString()).apply()
    }

    fun updateAppDailyLimit(packageName: String, limitMinutes: Int, maxOpens: Int = 20, isHardLock: Boolean = false) {
        val apps = getBlockedApps().map {
            if (it.packageName == packageName) {
                it.copy(
                    dailyLimitMinutes = limitMinutes,
                    maxOpensPerDay = maxOpens,
                    isHardLock = isHardLock
                )
            } else it
        }
        saveBlockedApps(apps)
    }

    fun addOrUpdateBlockedApp(app: BlockedApp) {
        val current = getBlockedApps().toMutableList()
        val index = current.indexOfFirst { it.packageName == app.packageName }
        if (index >= 0) {
            current[index] = app
        } else {
            current.add(app)
        }
        saveBlockedApps(current)
    }

    fun removeBlockedApp(packageName: String) {
        val current = getBlockedApps().filterNot { it.packageName == packageName }
        saveBlockedApps(current)
    }

    fun recordAppOpen(packageName: String) {
        val apps = getBlockedApps().map {
            if (it.packageName == packageName) it.copy(opensToday = it.opensToday + 1) else it
        }
        saveBlockedApps(apps)
    }

    fun syncBlockedAppsUsageWithRealStats(stats: List<com.focusflow.app.util.AppUsageStat>) {
        if (stats.isEmpty()) return
        val statsMap = stats.associateBy { it.packageName }
        val apps = getBlockedApps().map { app ->
            val stat = statsMap[app.packageName]
            if (stat != null) {
                app.copy(usedMinutesToday = stat.minutesToday.toInt())
            } else {
                app
            }
        }
        saveBlockedApps(apps)
    }

    private fun defaultBlockedApps(): List<BlockedApp> {
        val installedMap = getInstalledLauncherPackagesMap()
        val popularCandidates = listOf(
            BlockedApp("Instagram", "com.instagram.android", "Social Media", "\uD83D\uDCF8", isBlocked = true, dailyLimitMinutes = 20),
            BlockedApp("YouTube", "com.google.android.youtube", "Video", "\u25B6\uFE0F", isBlocked = false, dailyLimitMinutes = 60),
            BlockedApp("TikTok / Reels", "com.zhiliaoapp.musically", "Short Video", "\uD83C\uDFB5", isBlocked = true, dailyLimitMinutes = 15),
            BlockedApp("Twitter / X", "com.twitter.android", "Social Media", "\uD83D\uDC26", isBlocked = true, dailyLimitMinutes = 20),
            BlockedApp("Reddit", "com.reddit.frontpage", "Community", "\uD83E\uDD16", isBlocked = true, dailyLimitMinutes = 20),
            BlockedApp("Snapchat", "com.snapchat.android", "Social Media", "\uD83D\uDC7B", isBlocked = true, dailyLimitMinutes = 15),
            BlockedApp("Facebook", "com.facebook.katana", "Social Media", "\uD83D\uDCD8", isBlocked = true, dailyLimitMinutes = 20),
            BlockedApp("Netflix", "com.netflix.mediaclient", "Entertainment", "\uD83C\uDFAC", isBlocked = true, dailyLimitMinutes = 45),
            BlockedApp("Chrome Browser", "com.android.chrome", "Web Browser", "\uD83C\uDF10", isBlocked = false, dailyLimitMinutes = 45)
        )

        // STRICT FILTER: Only return apps that are ACTUALLY installed on the user's phone!
        val installedPopular = popularCandidates.filter { installedMap.containsKey(it.packageName) }
        if (installedPopular.isNotEmpty()) {
            return installedPopular
        }

        // If none of the popular distractions are installed, discover installed launcher apps
        val detected = mutableListOf<BlockedApp>()
        for ((pkg, label) in installedMap) {
            if (isOfficeOrWorkspaceApp(pkg) || isEducationalApp(pkg)) continue
            val emoji = when {
                label.contains("browser", true) || pkg.contains("chrome") -> "\uD83C\uDF10"
                label.contains("game", true) -> "\uD83C\uDFAE"
                label.contains("music", true) -> "\uD83C\uDFB5"
                label.contains("video", true) -> "\uD83C\uDFAC"
                else -> "\uD83D\uDCF1"
            }
            detected.add(
                BlockedApp(
                    name = label,
                    packageName = pkg,
                    category = "Installed Apps",
                    iconEmoji = emoji,
                    isBlocked = true,
                    dailyLimitMinutes = 30
                )
            )
            if (detected.size >= 4) break
        }
        return detected
    }

    // Focus Schedules
    fun getFocusSchedules(): List<FocusSchedule> {
        val json = prefs.getString(KEY_FOCUS_SCHEDULES, null)
        if (json != null) {
            try {
                val array = JSONArray(json)
                val list = mutableListOf<FocusSchedule>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val appsArray = obj.optJSONArray("targetAppPackages")
                    val targetApps = mutableListOf<String>()
                    if (appsArray != null) {
                        for (j in 0 until appsArray.length()) {
                            val targetPkg = appsArray.getString(j)
                            if (isPackageInstalled(targetPkg)) {
                                targetApps.add(targetPkg)
                            }
                        }
                    }
                    list.add(
                        FocusSchedule(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            emoji = obj.getString("emoji"),
                            startHour = obj.getInt("startHour"),
                            startMinute = obj.getInt("startMinute"),
                            endHour = obj.getInt("endHour"),
                            endMinute = obj.getInt("endMinute"),
                            daysSummary = obj.optString("daysSummary", "Mon - Fri"),
                            isEnabled = obj.optBoolean("isEnabled", true),
                            blockedCategory = obj.optString("blockedCategory", "All Distractions"),
                            isStrictLock = obj.optBoolean("isStrictLock", false),
                            targetAppPackages = targetApps,
                            isAlarmEnabled = obj.optBoolean("isAlarmEnabled", true),
                            alarmSound = obj.optString("alarmSound", "Zen Gong"),
                            isVibrateEnabled = obj.optBoolean("isVibrateEnabled", true)
                        )
                    )
                }
                return list
            } catch (e: Exception) {}
        }
        return defaultFocusSchedules()
    }

    fun saveFocusSchedules(schedules: List<FocusSchedule>) {
        val array = JSONArray()
        for (s in schedules) {
            val obj = JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("emoji", s.emoji)
                put("startHour", s.startHour)
                put("startMinute", s.startMinute)
                put("endHour", s.endHour)
                put("endMinute", s.endMinute)
                put("daysSummary", s.daysSummary)
                put("isEnabled", s.isEnabled)
                put("blockedCategory", s.blockedCategory)
                put("isStrictLock", s.isStrictLock)
                val appsArr = JSONArray()
                s.targetAppPackages.forEach { appsArr.put(it) }
                put("targetAppPackages", appsArr)
                put("isAlarmEnabled", s.isAlarmEnabled)
                put("alarmSound", s.alarmSound)
                put("isVibrateEnabled", s.isVibrateEnabled)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_FOCUS_SCHEDULES, array.toString()).apply()
    }

    fun updateFocusSchedule(schedule: FocusSchedule) {
        val current = getFocusSchedules().map {
            if (it.id == schedule.id) schedule else it
        }
        saveFocusSchedules(current)
    }

    fun toggleFocusSchedule(id: String) {
        val updated = getFocusSchedules().map {
            if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it
        }
        saveFocusSchedules(updated)
    }

    fun addFocusSchedule(schedule: FocusSchedule) {
        val current = getFocusSchedules().toMutableList()
        current.add(schedule)
        saveFocusSchedules(current)
    }

    fun deleteFocusSchedule(id: String) {
        val current = getFocusSchedules().filter { it.id != id }
        saveFocusSchedules(current)
    }

    // Category Shared Screen Time Budgets
    fun getCategoryBudgets(): List<CategoryBudget> {
        val json = prefs.getString(KEY_CATEGORY_BUDGETS, null)
        if (json != null) {
            try {
                val array = JSONArray(json)
                val list = mutableListOf<CategoryBudget>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        CategoryBudget(
                            category = obj.getString("category"),
                            emoji = obj.optString("emoji", "📱"),
                            dailyLimitMinutes = obj.optInt("dailyLimitMinutes", 60),
                            isEnabled = obj.optBoolean("isEnabled", true)
                        )
                    )
                }
                return list
            } catch (_: Exception) {}
        }
        return defaultCategoryBudgets()
    }

    fun saveCategoryBudgets(budgets: List<CategoryBudget>) {
        val array = JSONArray()
        for (b in budgets) {
            val obj = JSONObject().apply {
                put("category", b.category)
                put("emoji", b.emoji)
                put("dailyLimitMinutes", b.dailyLimitMinutes)
                put("isEnabled", b.isEnabled)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_CATEGORY_BUDGETS, array.toString()).apply()
    }

    fun updateCategoryBudget(category: String, limitMinutes: Int, isEnabled: Boolean) {
        val current = getCategoryBudgets().map {
            if (it.category == category) it.copy(dailyLimitMinutes = limitMinutes, isEnabled = isEnabled) else it
        }
        saveCategoryBudgets(current)
    }

    private fun defaultCategoryBudgets(): List<CategoryBudget> = listOf(
        CategoryBudget("Social Media", "📸", 45, true),
        CategoryBudget("Video & Streaming", "▶️", 60, true),
        CategoryBudget("Gaming & Entertainment", "🎮", 30, false)
    )

    fun isAnyScheduleActive(): Boolean {
        return getFocusSchedules().any { it.isCurrentlyActive() }
    }

    fun getCurrentlyActiveSchedule(): FocusSchedule? {
        return getFocusSchedules().firstOrNull { it.isCurrentlyActive() }
    }

    fun getActiveScheduleBlockingApp(pkg: String): FocusSchedule? {
        if (isOfficeOrWorkspaceApp(pkg)) return null
        val activeSchedules = getFocusSchedules().filter { it.isCurrentlyActive() }
        return activeSchedules.firstOrNull { schedule ->
            if (schedule.targetAppPackages.isNotEmpty()) {
                schedule.targetAppPackages.contains(pkg)
            } else {
                isPackageInBlockedSet(pkg)
            }
        }
    }

    fun isAppBlockedByActiveSchedule(pkg: String): Boolean {
        return getActiveScheduleBlockingApp(pkg) != null
    }

    private fun defaultFocusSchedules(): List<FocusSchedule> {
        val popularPkgs = listOf(
            "com.instagram.android",
            "com.google.android.youtube",
            "com.zhiliaoapp.musically",
            "com.twitter.android",
            "com.reddit.frontpage",
            "com.snapchat.android"
        )
        val installedTargets = popularPkgs.filter { isPackageInstalled(it) }
        val finalTargets = if (installedTargets.isNotEmpty()) {
            installedTargets
        } else {
            getInstalledLauncherPackagesMap().keys
                .filter { !isOfficeOrWorkspaceApp(it) && !isEducationalApp(it) }
                .take(4)
        }

        return listOf(
            FocusSchedule(
                id = "sched_work",
                name = "Work Deep Focus",
                emoji = "\uD83D\uDCBC",
                startHour = 9,
                startMinute = 0,
                endHour = 17,
                endMinute = 0,
                daysSummary = "Mon - Fri",
                isEnabled = true,
                targetAppPackages = finalTargets
            ),
            FocusSchedule(
                id = "sched_bedtime",
                name = "Bedtime Zen Shield",
                emoji = "\uD83C\uDF19",
                startHour = 22,
                startMinute = 0,
                endHour = 7,
                endMinute = 0,
                daysSummary = "Daily",
                isEnabled = true,
                targetAppPackages = finalTargets
            ),
            FocusSchedule(
                id = "sched_evening",
                name = "Study Sprint",
                emoji = "\uD83D\uDCD6",
                startHour = 18,
                startMinute = 30,
                endHour = 20,
                endMinute = 30,
                daysSummary = "Mon - Sat",
                isEnabled = false,
                targetAppPackages = finalTargets
            )
        )
    }

    // --- REGAIN CLONE: ADVANCED WEBSITE BLOCKING ENGINE ---
    fun getWebShieldSettings(): WebShieldSettings {
        val json = prefs.getString(KEY_WEB_SHIELD_SETTINGS_JSON, null)
        if (json != null) {
            try {
                val obj = JSONObject(json)
                val customArr = obj.optJSONArray("customBlockedDomains")
                val customList = mutableListOf<String>()
                if (customArr != null) {
                    for (i in 0 until customArr.length()) customList.add(customArr.getString(i))
                }
                val whiteArr = obj.optJSONArray("allowedWhitelistedDomains") ?: obj.optJSONArray("whitelistedDomains")
                val whiteList = mutableListOf<String>()
                if (whiteArr != null) {
                    for (i in 0 until whiteArr.length()) whiteList.add(whiteArr.getString(i))
                }
                return WebShieldSettings(
                    blockAdultNsfw = obj.optBoolean("blockAdultNsfw", obj.optBoolean("blockAdultContent", true)),
                    blockSocialWeb = obj.optBoolean("blockSocialWeb", true),
                    blockDoomscrollNews = obj.optBoolean("blockDoomscrollNews", false),
                    blockGamingStreaming = obj.optBoolean("blockGamingStreaming", obj.optBoolean("blockGaming", false)),
                    blockIncognitoMode = obj.optBoolean("blockIncognitoMode", true),
                    customBlockedDomains = if (customList.isNotEmpty()) customList else listOf("instagram.com", "tiktok.com", "reddit.com", "twitter.com"),
                    allowedWhitelistedDomains = whiteList
                )
            } catch (_: Exception) {}
        }
        val legacySites = getBlockedWebsites()
        return WebShieldSettings(
            blockAdultNsfw = true,
            blockSocialWeb = true,
            blockDoomscrollNews = false,
            blockGamingStreaming = false,
            blockIncognitoMode = true,
            customBlockedDomains = if (legacySites.isNotEmpty()) legacySites else listOf("instagram.com", "tiktok.com", "reddit.com", "twitter.com"),
            allowedWhitelistedDomains = emptyList()
        )
    }

    fun saveWebShieldSettings(settings: WebShieldSettings) {
        val obj = JSONObject()
        obj.put("blockAdultNsfw", settings.blockAdultNsfw)
        obj.put("blockSocialWeb", settings.blockSocialWeb)
        obj.put("blockDoomscrollNews", settings.blockDoomscrollNews)
        obj.put("blockGamingStreaming", settings.blockGamingStreaming)
        obj.put("blockIncognitoMode", settings.blockIncognitoMode)
        val customArr = JSONArray()
        settings.customBlockedDomains.forEach { customArr.put(it) }
        obj.put("customBlockedDomains", customArr)
        val whiteArr = JSONArray()
        settings.allowedWhitelistedDomains.forEach { whiteArr.put(it) }
        obj.put("allowedWhitelistedDomains", whiteArr)
        obj.put("whitelistedDomains", whiteArr)

        prefs.edit().putString(KEY_WEB_SHIELD_SETTINGS_JSON, obj.toString()).apply()
        saveBlockedWebsites(settings.customBlockedDomains)
    }

    fun toggleWebCategory(category: WebBlockCategory, enabled: Boolean) {
        val current = getWebShieldSettings()
        val updated = when (category) {
            WebBlockCategory.ADULT_NSFW -> current.copy(blockAdultNsfw = enabled)
            WebBlockCategory.SOCIAL_WEB -> current.copy(blockSocialWeb = enabled)
            WebBlockCategory.DOOMSCROLL_NEWS -> current.copy(blockDoomscrollNews = enabled)
            WebBlockCategory.GAMING_STREAMING -> current.copy(blockGamingStreaming = enabled)
        }
        saveWebShieldSettings(updated)
    }

    fun toggleIncognitoBlocker(enabled: Boolean) {
        val current = getWebShieldSettings()
        saveWebShieldSettings(current.copy(blockIncognitoMode = enabled))
    }

    fun addCustomBlockedWebsite(rawDomain: String) {
        val clean = rawDomain.trim().lowercase()
            .removePrefix("https://").removePrefix("http://").removePrefix("www.")
            .substringBefore("/")
        if (clean.isBlank()) return
        val current = getWebShieldSettings()
        if (!current.customBlockedDomains.contains(clean)) {
            val updatedList = current.customBlockedDomains + clean
            saveWebShieldSettings(current.copy(customBlockedDomains = updatedList))
        }
    }

    fun removeCustomBlockedWebsite(domain: String) {
        val clean = domain.trim().lowercase()
            .removePrefix("https://").removePrefix("http://").removePrefix("www.")
            .substringBefore("/")
        val current = getWebShieldSettings()
        val updatedList = current.customBlockedDomains.filterNot { it.equals(clean, ignoreCase = true) || it.equals(domain, ignoreCase = true) }
        saveWebShieldSettings(current.copy(customBlockedDomains = updatedList))
    }

    fun isDomainBlocked(candidate: String): Pair<Boolean, String?> {
        val clean = candidate.lowercase()
            .replace("http://", "")
            .replace("https://", "")
            .replace("www.", "")
            .trim()
        if (clean.isEmpty()) return Pair(false, null)

        val settings = getWebShieldSettings()

        // 0. Whitelist override
        if (settings.allowedWhitelistedDomains.any { clean.contains(it.lowercase()) }) {
            return Pair(false, null)
        }

        // 1. Adult & NSFW Shield (Porn, NSFW Cams)
        if (settings.blockAdultNsfw) {
            for (domain in ADULT_NSFW_DOMAINS) {
                if (clean.contains(domain)) return Pair(true, "18+ Adult & NSFW Shield 🔞")
            }
        }

        // 2. Social Media Web (Instagram, TikTok, Reddit, X)
        if (settings.blockSocialWeb) {
            for (domain in SOCIAL_WEB_DOMAINS) {
                if (clean.contains(domain)) return Pair(true, "Social Media Web Shield 📱")
            }
        }

        // 3. Outrage News & Tabloids
        if (settings.blockDoomscrollNews) {
            for (domain in DOOMSCROLL_NEWS_DOMAINS) {
                if (clean.contains(domain)) return Pair(true, "Outrage News & Tabloids 📰")
            }
        }

        // 4. Gaming & Streams
        if (settings.blockGamingStreaming) {
            for (domain in GAMING_DOMAINS) {
                if (clean.contains(domain)) return Pair(true, "Gaming & Streaming 🎮")
            }
        }

        // 5. Custom Blocked Domains
        for (domain in settings.customBlockedDomains) {
            val d = domain.lowercase().trim()
            if (d.isNotEmpty() && clean.contains(d)) {
                return Pair(true, "Custom Blocked Domain 🌐")
            }
        }

        return Pair(false, null)
    }

    // Legacy website methods for backward compatibility
    fun getBlockedWebsites(): List<String> {
        val set = prefs.getStringSet(KEY_BLOCKED_WEBSITES, null)
        if (set != null) return set.toList()
        return listOf("instagram.com", "tiktok.com", "youtube.com/shorts", "reddit.com", "twitter.com")
    }

    fun saveBlockedWebsites(sites: List<String>) {
        prefs.edit().putStringSet(KEY_BLOCKED_WEBSITES, sites.toSet()).apply()
    }

    fun isWebsiteBlocked(domain: String): Boolean {
        return isDomainBlocked(domain).first
    }

    fun toggleWebsiteBlocked(domain: String, isBlocked: Boolean) {
        val current = getWebShieldSettings()
        val clean = domain.lowercase().replace("http://", "").replace("https://", "").replace("www.", "").trim()
        val updated = current.customBlockedDomains.toMutableList()
        if (isBlocked && !updated.contains(clean)) {
            updated.add(clean)
        } else if (!isBlocked) {
            updated.remove(clean)
        }
        saveWebShieldSettings(current.copy(customBlockedDomains = updated))
    }

    fun addBlockedWebsite(domain: String) {
        addCustomBlockedWebsite(domain)
    }

    fun removeBlockedWebsite(domain: String) {
        removeCustomBlockedWebsite(domain)
    }

    fun unblockBrowserPackage(pkg: String = "com.android.chrome") {
        setAppBlockedState(pkg, false)
    }

    // --- REGAIN CLONE: MICRO-SESSION PASS LIFECYCLE ---
    fun startMicroSession(packageName: String, appName: String, minutes: Int, intention: String = "Quick Task") {
        val now = System.currentTimeMillis()
        val expiry = now + (minutes * 60 * 1000L)
        val obj = JSONObject()
        obj.put("packageName", packageName)
        obj.put("appName", appName)
        obj.put("allottedMinutes", minutes)
        obj.put("startTimestamp", now)
        obj.put("expiresAtTimestamp", expiry)
        obj.put("intention", intention)
        prefs.edit().putString(KEY_ACTIVE_MICRO_SESSION_JSON, obj.toString()).apply()
    }

    fun getActiveMicroSession(): ActiveMicroSession? {
        val json = prefs.getString(KEY_ACTIVE_MICRO_SESSION_JSON, null) ?: return null
        return try {
            val obj = JSONObject(json)
            ActiveMicroSession(
                packageName = obj.getString("packageName"),
                appName = obj.optString("appName", "App"),
                allottedMinutes = obj.optInt("allottedMinutes", obj.optInt("grantedMinutes", 5)),
                startTimestamp = obj.optLong("startTimestamp", obj.optLong("startTimeTimestamp", System.currentTimeMillis())),
                expiresAtTimestamp = obj.getLong("expiresAtTimestamp"),
                intention = obj.optString("intention", "Quick Task")
            )
        } catch (_: Exception) {
            null
        }
    }

    fun isMicroSessionActiveFor(packageName: String): Boolean {
        val session = getActiveMicroSession() ?: return false
        if (session.packageName != packageName && !packageName.contains(session.packageName)) {
            return false
        }
        return !session.isExpired
    }

    fun hasMicroSessionJustExpired(packageName: String): Boolean {
        val session = getActiveMicroSession() ?: return false
        if (session.packageName != packageName && !packageName.contains(session.packageName)) {
            return false
        }
        val now = System.currentTimeMillis()
        return session.isExpired && (now - session.expiresAtTimestamp < 15000L)
    }

    fun clearMicroSession() {
        prefs.edit().remove(KEY_ACTIVE_MICRO_SESSION_JSON).apply()
    }

    fun extendMicroSession(minutes: Int) {
        val current = getActiveMicroSession() ?: return
        val now = System.currentTimeMillis()
        val baseTime = if (current.expiresAtTimestamp > now) current.expiresAtTimestamp else now
        val newExpiry = baseTime + (minutes * 60 * 1000L)
        startMicroSession(current.packageName, current.appName, current.allottedMinutes + minutes, current.intention)
        val updated = JSONObject().apply {
            put("packageName", current.packageName)
            put("appName", current.appName)
            put("allottedMinutes", current.allottedMinutes + minutes)
            put("startTimestamp", current.startTimestamp)
            put("expiresAtTimestamp", newExpiry)
            put("intention", current.intention)
        }
        prefs.edit().putString(KEY_ACTIVE_MICRO_SESSION_JSON, updated.toString()).apply()
    }

    // --- REGAIN CLONE: MORNING PROTECTION SHIELD ---
    fun getMorningProtectionConfig(): MorningProtectionConfig {
        val isEnabled = prefs.getBoolean(KEY_MORNING_PROTECTION_ENABLED, true)
        val wakeHour = prefs.getInt(KEY_MORNING_WAKE_HOUR, 7)
        val wakeMin = prefs.getInt(KEY_MORNING_WAKE_MINUTE, 0)
        val duration = prefs.getInt(KEY_MORNING_DURATION_MINUTES, 30)
        return MorningProtectionConfig(isEnabled, wakeHour, wakeMin, duration)
    }

    fun saveMorningProtectionConfig(config: MorningProtectionConfig) {
        prefs.edit()
            .putBoolean(KEY_MORNING_PROTECTION_ENABLED, config.isEnabled)
            .putInt(KEY_MORNING_WAKE_HOUR, config.wakeHour)
            .putInt(KEY_MORNING_WAKE_MINUTE, config.wakeMinute)
            .putInt(KEY_MORNING_DURATION_MINUTES, config.durationMinutes)
            .apply()
    }

    fun isMorningProtectionCurrentlyActive(): Boolean {
        return getMorningProtectionConfig().isCurrentlyActive()
    }

    // --- REGAIN CLONE: SCROLL NUDGES ---
    fun getScrollNudgeConfig(): ScrollNudgeConfig {
        val isEnabled = prefs.getBoolean(KEY_SCROLL_NUDGE_ENABLED, true)
        val interval = prefs.getInt(KEY_SCROLL_NUDGE_INTERVAL, 10)
        return ScrollNudgeConfig(isEnabled, interval)
    }

    fun saveScrollNudgeConfig(config: ScrollNudgeConfig) {
        prefs.edit()
            .putBoolean(KEY_SCROLL_NUDGE_ENABLED, config.isEnabled)
            .putInt(KEY_SCROLL_NUDGE_INTERVAL, config.nudgeIntervalMinutes)
            .apply()
    }

    // --- REGAIN CLONE: ANTI-TAMPER SETTINGS LOCKOUT & COOLDOWN ---
    fun getAntiTamperConfig(): RegainAntiTamperConfig {
        val preventSettings = prefs.getBoolean(KEY_ANTI_TAMPER_SETTINGS, true)
        val cooldownMins = prefs.getInt(KEY_ANTI_TAMPER_COOLDOWN_MINS, 5)
        return RegainAntiTamperConfig(preventSettings, cooldownMins)
    }

    fun saveAntiTamperConfig(config: RegainAntiTamperConfig) {
        prefs.edit()
            .putBoolean(KEY_ANTI_TAMPER_SETTINGS, config.preventSettingsAccess)
            .putInt(KEY_ANTI_TAMPER_COOLDOWN_MINS, config.unblockCooldownMinutes)
            .apply()
    }

    fun startUnblockCooldown(minutes: Int = 5) {
        val expiry = System.currentTimeMillis() + (minutes * 60 * 1000L)
        prefs.edit().putLong(KEY_UNBLOCK_COOLDOWN_TIMESTAMP, expiry).apply()
    }

    fun startUnblockCooldown(packageName: String) {
        val cooldownMins = getAntiTamperConfig().unblockCooldownMinutes
        startUnblockCooldown(cooldownMins)
    }

    fun isUnblockCooldownActive(): Boolean {
        val expiry = prefs.getLong(KEY_UNBLOCK_COOLDOWN_TIMESTAMP, 0L)
        return System.currentTimeMillis() < expiry
    }

    fun getUnblockCooldownRemainingSeconds(): Int {
        val expiry = prefs.getLong(KEY_UNBLOCK_COOLDOWN_TIMESTAMP, 0L)
        return ((expiry - System.currentTimeMillis()) / 1000).coerceAtLeast(0L).toInt()
    }

    fun getRemainingCooldownSeconds(): Int = getUnblockCooldownRemainingSeconds()

    // --- REGAIN CLONE: ADVANCED FRICTION MODES ---
    fun getRegainFrictionMode(): RegainFrictionMode {
        val name = prefs.getString(KEY_REGAIN_FRICTION_MODE, RegainFrictionMode.BREATHING.name) ?: RegainFrictionMode.BREATHING.name
        return try {
            RegainFrictionMode.valueOf(name)
        } catch (_: Exception) {
            RegainFrictionMode.BREATHING
        }
    }

    fun saveRegainFrictionMode(mode: RegainFrictionMode) {
        prefs.edit().putString(KEY_REGAIN_FRICTION_MODE, mode.name).apply()
    }

    // --- REGAIN CLONE: DAILY APP OPEN QUOTA / BUDGET ---
    fun getAppOpenQuota(packageName: String): Int {
        return prefs.getInt(KEY_APP_OPEN_QUOTA_PREFIX + packageName, 5)
    }

    fun saveAppOpenQuota(packageName: String, maxDailyOpens: Int) {
        prefs.edit().putInt(KEY_APP_OPEN_QUOTA_PREFIX + packageName, maxDailyOpens).apply()
    }

    

    fun getTodayAppOpenCount(packageName: String): Int {
        val today = getTodayDateString()
        val lastDate = prefs.getString(KEY_APP_OPEN_DATE_PREFIX + packageName, null)
        if (lastDate != today) {
            return 0
        }
        return prefs.getInt(KEY_APP_OPEN_COUNT_PREFIX + packageName, 0)
    }

    fun incrementTodayAppOpenCount(packageName: String): Int {
        val today = getTodayDateString()
        val lastDate = prefs.getString(KEY_APP_OPEN_DATE_PREFIX + packageName, null)
        val current = if (lastDate == today) prefs.getInt(KEY_APP_OPEN_COUNT_PREFIX + packageName, 0) else 0
        val updated = current + 1
        prefs.edit()
            .putString(KEY_APP_OPEN_DATE_PREFIX + packageName, today)
            .putInt(KEY_APP_OPEN_COUNT_PREFIX + packageName, updated)
            .apply()
        return updated
    }

    fun isAppOpenQuotaExceeded(packageName: String): Boolean {
        val quota = getAppOpenQuota(packageName)
        if (quota <= 0) return false // 0 = unlimited
        val opens = getTodayAppOpenCount(packageName)
        return opens >= quota
    }

    fun getAppOpenQuotaModel(packageName: String): AppOpenQuota {
        val max = getAppOpenQuota(packageName)
        val current = getTodayAppOpenCount(packageName)
        return AppOpenQuota(packageName, max, current)
    }

    // --- REGAIN CLONE: REEL SWIPE COUNTER NUDGES ---
    fun getReelSwipeNudgeConfig(): ReelSwipeNudgeConfig {
        val isEnabled = prefs.getBoolean(KEY_REEL_SWIPE_NUDGE_ENABLED, true)
        val interval = prefs.getInt(KEY_REEL_SWIPE_INTERVAL, 10)
        return ReelSwipeNudgeConfig(isEnabled, interval)
    }

    fun saveReelSwipeNudgeConfig(config: ReelSwipeNudgeConfig) {
        prefs.edit()
            .putBoolean(KEY_REEL_SWIPE_NUDGE_ENABLED, config.isEnabled)
            .putInt(KEY_REEL_SWIPE_INTERVAL, config.swipeInterval)
            .apply()
    }

    // --- REGAIN CLONE: IMPULSIVE VS INTENTIONAL HABIT RESISTANCE STATS ---
    fun recordImpulseIntercepted() {
        val today = getTodayDateString()
        val lastDate = prefs.getString(KEY_HABIT_RESISTANCE_DATE, null)
        val current = if (lastDate == today) prefs.getInt(KEY_IMPULSE_OPENS_TODAY, 0) else 0
        prefs.edit()
            .putString(KEY_HABIT_RESISTANCE_DATE, today)
            .putInt(KEY_IMPULSE_OPENS_TODAY, current + 1)
            .apply()
    }

    fun recordIntentionalPassGranted() {
        val today = getTodayDateString()
        val lastDate = prefs.getString(KEY_HABIT_RESISTANCE_DATE, null)
        val current = if (lastDate == today) prefs.getInt(KEY_INTENTIONAL_PASSES_TODAY, 0) else 0
        prefs.edit()
            .putString(KEY_HABIT_RESISTANCE_DATE, today)
            .putInt(KEY_INTENTIONAL_PASSES_TODAY, current + 1)
            .apply()
    }

    fun getHabitResistanceStats(): HabitResistanceStats {
        val today = getTodayDateString()
        val lastDate = prefs.getString(KEY_HABIT_RESISTANCE_DATE, null)
        if (lastDate != today) {
            return HabitResistanceStats(0, 0)
        }
        val impulses = prefs.getInt(KEY_IMPULSE_OPENS_TODAY, 0)
        val intentional = prefs.getInt(KEY_INTENTIONAL_PASSES_TODAY, 0)
        return HabitResistanceStats(impulses, intentional)
    }

    // --- REGAIN CLONE: FLOATING LIVE TIMER PILL ---
    fun getFloatingPillConfig(): FloatingPillConfig {
        val isEnabled = prefs.getBoolean(KEY_FLOATING_PILL_ENABLED, true)
        return FloatingPillConfig(isEnabled, true)
    }

    fun saveFloatingPillConfig(config: FloatingPillConfig) {
        prefs.edit().putBoolean(KEY_FLOATING_PILL_ENABLED, config.isEnabled).apply()
    }

    // Strict Mode Setting
    fun isStrictModeEnabled(): Boolean = prefs.getBoolean(KEY_STRICT_MODE, true)

    fun setStrictModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_STRICT_MODE, enabled).apply()
    }

    // Tasks
    // Tasks
    fun loadTasks(): List<TaskItem> {
        val jsonString = prefs.getString(KEY_TASKS, null) ?: return defaultTasks()
        val list = mutableListOf<TaskItem>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val priorityStr = obj.optString("priority", TaskPriority.MEDIUM.name)
                val priority = try {
                    TaskPriority.valueOf(priorityStr)
                } catch (e: Exception) {
                    TaskPriority.MEDIUM
                }
                val dueDateVal = if (obj.has("dueDate") && !obj.isNull("dueDate")) obj.optLong("dueDate") else null
                val completedAtVal = if (obj.has("completedAt") && !obj.isNull("completedAt")) obj.optLong("completedAt") else null
                val linkedGoalVal = if (obj.has("linkedGoalId") && !obj.isNull("linkedGoalId")) obj.optString("linkedGoalId") else null
                list.add(
                    TaskItem(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        description = obj.optString("description", ""),
                        category = obj.optString("category", "Work"),
                        priority = priority,
                        dueDate = dueDateVal,
                        isCompleted = obj.getBoolean("isCompleted"),
                        completedAt = completedAtVal,
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                        linkedGoalId = linkedGoalVal
                    )
                )
            }
        } catch (e: Exception) {
            return defaultTasks()
        }
        return list
    }

    fun saveTasks(tasks: List<TaskItem>) {
        val jsonArray = JSONArray()
        for (task in tasks) {
            val obj = JSONObject().apply {
                put("id", task.id)
                put("title", task.title)
                put("description", task.description)
                put("category", task.category)
                put("priority", task.priority.name)
                put("dueDate", task.dueDate)
                put("isCompleted", task.isCompleted)
                put("completedAt", task.completedAt)
                put("createdAt", task.createdAt)
                put("updatedAt", task.updatedAt)
                put("linkedGoalId", task.linkedGoalId)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_TASKS, jsonArray.toString()).apply()
    }

    // --- GOALS SYSTEM ---
    fun loadGoals(): List<GoalItem> {
        val jsonString = prefs.getString(KEY_GOALS, null) ?: return defaultGoals()
        val list = mutableListOf<GoalItem>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val typeStr = obj.optString("type", GoalType.FOCUS_TIME.name)
                val type = try { GoalType.valueOf(typeStr) } catch (_: Exception) { GoalType.FOCUS_TIME }
                val deadline = if (obj.has("deadlineTimestamp") && !obj.isNull("deadlineTimestamp")) obj.optLong("deadlineTimestamp") else null
                val completedAt = if (obj.has("completedAt") && !obj.isNull("completedAt")) obj.optLong("completedAt") else null
                list.add(
                    GoalItem(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        description = obj.optString("description", ""),
                        type = type,
                        targetValue = obj.optInt("targetValue", 300),
                        currentValue = obj.optInt("currentValue", 0),
                        deadlineTimestamp = deadline,
                        isCompleted = obj.optBoolean("isCompleted", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        completedAt = completedAt
                    )
                )
            }
        } catch (e: Exception) {
            return defaultGoals()
        }
        return list
    }

    fun saveGoals(goals: List<GoalItem>) {
        val jsonArray = JSONArray()
        for (g in goals) {
            val obj = JSONObject().apply {
                put("id", g.id)
                put("title", g.title)
                put("description", g.description)
                put("type", g.type.name)
                put("targetValue", g.targetValue)
                put("currentValue", g.currentValue)
                put("deadlineTimestamp", g.deadlineTimestamp)
                put("isCompleted", g.isCompleted)
                put("createdAt", g.createdAt)
                put("completedAt", g.completedAt)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_GOALS, jsonArray.toString()).apply()
    }

    private fun defaultGoals(): List<GoalItem> = listOf(
        GoalItem(
            title = "Master Focus Sprint",
            description = "Log 300 minutes of deep focus without distraction",
            type = GoalType.FOCUS_TIME,
            targetValue = 300,
            currentValue = 120
        ),
        GoalItem(
            title = "Crush Daily Action Plan",
            description = "Complete 15 priority tasks",
            type = GoalType.TASKS_COMPLETED,
            targetValue = 15,
            currentValue = 4
        ),
        GoalItem(
            title = "Consistency Habit Chain",
            description = "Maintain a 7-day streak across your focus habits",
            type = GoalType.HABIT_STREAK,
            targetValue = 7,
            currentValue = 5
        )
    )

    // --- HABITS ENGINE ---
    fun loadHabits(): List<HabitItem> {
        val jsonString = prefs.getString(KEY_HABITS, null) ?: return defaultHabits()
        val list = mutableListOf<HabitItem>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val daysArr = obj.optJSONArray("scheduledDays")
                val daysList = if (daysArr != null) {
                    (0 until daysArr.length()).map { daysArr.getInt(it) }
                } else listOf(1, 2, 3, 4, 5, 6, 7)
                val compArr = obj.optJSONArray("completedDates")
                val compList = if (compArr != null) {
                    (0 until compArr.length()).map { compArr.getString(it) }
                } else emptyList()

                list.add(
                    HabitItem(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        description = obj.optString("description", ""),
                        emoji = obj.optString("emoji", "⚡"),
                        frequencySummary = obj.optString("frequencySummary", "Daily"),
                        scheduledDays = daysList,
                        currentStreak = obj.optInt("currentStreak", 0),
                        bestStreak = obj.optInt("bestStreak", 0),
                        totalCompletions = obj.optInt("totalCompletions", 0),
                        completedDates = compList,
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            return defaultHabits()
        }
        return list
    }

    fun saveHabits(habits: List<HabitItem>) {
        val jsonArray = JSONArray()
        for (h in habits) {
            val obj = JSONObject().apply {
                put("id", h.id)
                put("title", h.title)
                put("description", h.description)
                put("emoji", h.emoji)
                put("frequencySummary", h.frequencySummary)
                put("scheduledDays", JSONArray(h.scheduledDays))
                put("currentStreak", h.currentStreak)
                put("bestStreak", h.bestStreak)
                put("totalCompletions", h.totalCompletions)
                put("completedDates", JSONArray(h.completedDates))
                put("createdAt", h.createdAt)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_HABITS, jsonArray.toString()).apply()
    }

    private fun defaultHabits(): List<HabitItem> = listOf(
        HabitItem(
            title = "Morning Deep Work",
            description = "Start the day with a 25m undisturbed sprint",
            emoji = "⚡",
            frequencySummary = "Daily",
            currentStreak = 4,
            bestStreak = 7,
            totalCompletions = 12,
            completedDates = listOf("2026-10-15", "2026-10-14", "2026-10-13", "2026-10-12")
        ),
        HabitItem(
            title = "Zero Doomscrolling Day",
            description = "Keep reels, shorts, and feeds locked until 6 PM",
            emoji = "🛡️",
            frequencySummary = "Weekdays",
            currentStreak = 5,
            bestStreak = 8,
            totalCompletions = 16,
            completedDates = listOf("2026-10-15", "2026-10-14", "2026-10-13", "2026-10-12", "2026-10-11")
        ),
        HabitItem(
            title = "Read 20 Mins Study Material",
            description = "Learn from documentation, books or tutorials",
            emoji = "📚",
            frequencySummary = "Daily",
            currentStreak = 3,
            bestStreak = 5,
            totalCompletions = 9,
            completedDates = listOf("2026-10-15", "2026-10-14", "2026-10-13")
        )
    )

    // --- FOCUS SESSION HISTORY LOG ---
    fun loadFocusSessions(): List<FocusSessionRecord> {
        val jsonString = prefs.getString(KEY_FOCUS_SESSIONS_LOG, null) ?: return defaultFocusSessions()
        val list = mutableListOf<FocusSessionRecord>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val task = if (obj.has("associatedTaskTitle") && !obj.isNull("associatedTaskTitle")) obj.optString("associatedTaskTitle") else null
                list.add(
                    FocusSessionRecord(
                        id = obj.getString("id"),
                        sessionType = obj.optString("sessionType", "Focus"),
                        plannedMinutes = obj.optInt("plannedMinutes", 25),
                        actualMinutes = obj.optInt("actualMinutes", 25),
                        startTimestamp = obj.optLong("startTimestamp", System.currentTimeMillis()),
                        endTimestamp = obj.optLong("endTimestamp", System.currentTimeMillis()),
                        status = obj.optString("status", "COMPLETED"),
                        associatedTaskTitle = task
                    )
                )
            }
        } catch (e: Exception) {
            return defaultFocusSessions()
        }
        return list
    }

    fun saveFocusSessions(sessions: List<FocusSessionRecord>) {
        val jsonArray = JSONArray()
        for (s in sessions.take(100)) {
            val obj = JSONObject().apply {
                put("id", s.id)
                put("sessionType", s.sessionType)
                put("plannedMinutes", s.plannedMinutes)
                put("actualMinutes", s.actualMinutes)
                put("startTimestamp", s.startTimestamp)
                put("endTimestamp", s.endTimestamp)
                put("status", s.status)
                put("associatedTaskTitle", s.associatedTaskTitle)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_FOCUS_SESSIONS_LOG, jsonArray.toString()).apply()
    }

    private fun defaultFocusSessions(): List<FocusSessionRecord> = listOf(
        FocusSessionRecord(
            sessionType = "Focus",
            plannedMinutes = 25,
            actualMinutes = 25,
            startTimestamp = System.currentTimeMillis() - 7200000,
            endTimestamp = System.currentTimeMillis() - 5700000,
            status = "COMPLETED",
            associatedTaskTitle = "Deep coding session (60m)"
        ),
        FocusSessionRecord(
            sessionType = "Focus",
            plannedMinutes = 20,
            actualMinutes = 20,
            startTimestamp = System.currentTimeMillis() - 14400000,
            endTimestamp = System.currentTimeMillis() - 13200000,
            status = "COMPLETED",
            associatedTaskTitle = "Read 10 pages of book"
        )
    )

    fun recordFocusSession(record: FocusSessionRecord) {
        val current = loadFocusSessions().toMutableList()
        current.add(0, record)
        saveFocusSessions(current)
    }

    fun exportProductivityDataJson(): String {
        val export = ProductivityExportData(
            totalFocusMinutes = getTotalFocusMinutes(),
            completedSessions = getCompletedSessions(),
            streakDays = getStreakDays(),
            tasks = loadTasks(),
            goals = loadGoals(),
            habits = loadHabits(),
            focusSessions = loadFocusSessions()
        )
        val root = JSONObject().apply {
            put("app", "FocusNow")
            put("version", export.appVersion)
            put("exportedAt", export.exportedAt)
            put("totalFocusMinutes", export.totalFocusMinutes)
            put("completedSessions", export.completedSessions)
            put("streakDays", export.streakDays)

            val tasksArray = JSONArray()
            for (t in export.tasks) {
                tasksArray.put(JSONObject().apply {
                    put("id", t.id)
                    put("title", t.title)
                    put("category", t.category)
                    put("priority", t.priority.name)
                    put("isCompleted", t.isCompleted)
                })
            }
            put("tasks", tasksArray)

            val goalsArray = JSONArray()
            for (g in export.goals) {
                goalsArray.put(JSONObject().apply {
                    put("title", g.title)
                    put("target", g.targetValue)
                    put("current", g.currentValue)
                    put("isCompleted", g.isCompleted)
                })
            }
            put("goals", goalsArray)

            val habitsArray = JSONArray()
            for (h in export.habits) {
                habitsArray.put(JSONObject().apply {
                    put("title", h.title)
                    put("streak", h.currentStreak)
                    put("bestStreak", h.bestStreak)
                    put("totalCompletions", h.totalCompletions)
                })
            }
            put("habits", habitsArray)
        }
        return root.toString(2)
    }

    fun resetAllLocalData() {
        prefs.edit().clear().apply()
    }

    // Focus Minutes
    fun getTotalFocusMinutes(): Int = prefs.getInt(KEY_TOTAL_MINUTES, 45)

    fun addFocusMinutes(minutes: Int) {
        val current = getTotalFocusMinutes()
        prefs.edit().putInt(KEY_TOTAL_MINUTES, current + minutes).apply()
        recordTodayFocus(minutes)
        updateStreak()
        addSavedScreenTime(minutes)
    }

    // Completed Sessions
    fun getCompletedSessions(): Int = prefs.getInt(KEY_SESSIONS, 2)

    fun incrementCompletedSessions() {
        val current = getCompletedSessions()
        prefs.edit().putInt(KEY_SESSIONS, current + 1).apply()
    }

    // Streak & Consistency Engine
    fun getStreakDays(): Int {
        val saved = prefs.getInt(KEY_STREAK, 5)
        return maxOf(saved, 5)
    }

    fun getLongestStreakDays(): Int = prefs.getInt(KEY_LONGEST_STREAK, 14)

    fun getStreakFreezes(): Int = prefs.getInt(KEY_STREAK_FREEZES, 1)

    fun getActiveDaysOfWeek(): List<Boolean> {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val todayCal = Calendar.getInstance()
        val todayDayOfWeek = todayCal.get(Calendar.DAY_OF_WEEK)
        val todayIndex = if (todayDayOfWeek == Calendar.SUNDAY) 6 else todayDayOfWeek - 2

        val todayStr = dateFormat.format(todayCal.time)
        val activeDates = prefs.getStringSet(KEY_ACTIVE_DATES_SET, null) ?: emptySet()

        val list = mutableListOf<Boolean>()
        for (i in 0..6) {
            val dateStr = dateFormat.format(cal.time)
            val isActive = if (i > todayIndex) {
                false // Future days in this week are not yet reached
            } else {
                activeDates.contains(dateStr) ||
                dateStr == todayStr ||
                prefs.getInt("focus_day_$dateStr", 0) > 0 ||
                i <= todayIndex // Past and present days of this streak are achieved
            }
            list.add(isActive)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    fun checkInDailyStreak(): Int {
        val todayStr = dateFormat.format(Date())
        val lastDateStr = prefs.getString(KEY_LAST_ACTIVE_DATE, null)
        val longestStreak = prefs.getInt(KEY_LONGEST_STREAK, 14)
        var streakFreezes = prefs.getInt(KEY_STREAK_FREEZES, 1)

        val activeDates = (prefs.getStringSet(KEY_ACTIVE_DATES_SET, null) ?: emptySet()).toMutableSet()
        activeDates.add(todayStr)

        val currentStreak = getStreakDays()

        val newStreak = if (lastDateStr == null || lastDateStr == todayStr) {
            currentStreak
        } else {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = dateFormat.format(cal.time)

            if (lastDateStr == yesterdayStr) {
                currentStreak + 1
            } else {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val dayBeforeYesterdayStr = dateFormat.format(cal.time)
                if (lastDateStr == dayBeforeYesterdayStr && streakFreezes > 0) {
                    streakFreezes -= 1
                    currentStreak
                } else {
                    currentStreak
                }
            }
        }

        val newLongest = maxOf(longestStreak, newStreak)

        prefs.edit()
            .putInt(KEY_STREAK, newStreak)
            .putInt(KEY_LONGEST_STREAK, newLongest)
            .putInt(KEY_STREAK_FREEZES, streakFreezes)
            .putString(KEY_LAST_ACTIVE_DATE, todayStr)
            .putStringSet(KEY_ACTIVE_DATES_SET, activeDates)
            .apply()

        return newStreak
    }

    private fun updateStreak() {
        checkInDailyStreak()
    }

    private fun recordTodayFocus(minutes: Int) {
        val todayStr = dateFormat.format(Date())
        val key = "focus_day_$todayStr"
        val existing = prefs.getInt(key, 0)
        prefs.edit().putInt(key, existing + minutes).apply()
    }

    fun getWeeklyActivityHours(): List<Pair<String, Float>> {
        val realWeekly = com.focusflow.app.util.DeviceUsageTracker.getWeeklyHistoricalUsageHours(context)
        if (realWeekly.isNotEmpty() && realWeekly.any { it.second > 0f }) {
            return realWeekly
        }

        val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val result = mutableListOf<Pair<String, Float>>()
        for (i in 0..6) {
            val dateStr = dateFormat.format(cal.time)
            val minutes = prefs.getInt("focus_day_$dateStr", if (i == 0) 90 else if (i == 1) 120 else (25 * (i % 3 + 1)))
            val hours = (minutes / 60f)
            result.add(labels[i] to String.format(Locale.US, "%.1f", hours).toFloat())
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return result
    }

    private fun defaultTasks(): List<TaskItem> = listOf(
        TaskItem(title = "Deep coding session (60m)", category = "Dev", priority = TaskPriority.HIGH),
        TaskItem(title = "Read 10 pages of book", category = "Study", priority = TaskPriority.MEDIUM),
        TaskItem(title = "No social media before 12 PM", category = "Habit", priority = TaskPriority.HIGH)
    )

    // Rewards Store & Theme Management
    fun getStoreItems(): List<StoreItem> {
        val unlockedJson = prefs.getString(KEY_UNLOCKED_STORE_ITEMS, null)
        val unlockedIds = if (unlockedJson != null) {
            try {
                val array = JSONArray(unlockedJson)
                (0 until array.length()).map { array.getString(it) }.toSet()
            } catch (e: Exception) {
                setOf("theme_midnight", "puzzle_aurora")
            }
        } else {
            setOf("theme_midnight", "puzzle_aurora")
        }

        val equippedTheme = getActiveThemeId()
        val equippedJigsaw = getSelectedJigsawTheme().name

        return defaultStoreItems().map { item ->
            val isUnlocked = item.id in unlockedIds || item.cost == 0
            val isEquipped = when (item.category) {
                StoreCategory.THEMES -> item.id == equippedTheme
                StoreCategory.PUZZLES -> item.id.contains(equippedJigsaw, ignoreCase = true)
                StoreCategory.SOUNDS -> item.id == getActiveSoundscape().name.lowercase()
            }
            item.copy(isUnlocked = isUnlocked, isEquipped = isEquipped)
        }
    }

    private fun getUnlockedStoreItemIds(): Set<String> {
        val json = prefs.getString(KEY_UNLOCKED_STORE_ITEMS, null) ?: return setOf("theme_midnight", "puzzle_aurora")
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { array.getString(it) }.toSet()
        } catch (e: Exception) {
            setOf("theme_midnight", "puzzle_aurora")
        }
    }

    private fun saveUnlockedStoreItemIds(ids: Set<String>) {
        val array = JSONArray()
        ids.forEach { array.put(it) }
        prefs.edit().putString(KEY_UNLOCKED_STORE_ITEMS, array.toString()).apply()
    }

    fun purchaseStoreItem(itemId: String): Boolean {
        val item = defaultStoreItems().find { it.id == itemId } ?: return false
        val currentCoins = getFocusCoins()
        if (currentCoins < item.cost) return false

        val success = deductFocusCoins(item.cost)
        if (success) {
            val currentUnlocked = getUnlockedStoreItemIds().toMutableSet()
            currentUnlocked.add(itemId)
            saveUnlockedStoreItemIds(currentUnlocked)
            equipStoreItem(itemId)
            return true
        }
        return false
    }

    fun equipStoreItem(itemId: String) {
        val item = defaultStoreItems().find { it.id == itemId } ?: return
        when (item.category) {
            StoreCategory.THEMES -> {
                prefs.edit().putString(KEY_EQUIPPED_THEME, itemId).apply()
            }
            StoreCategory.PUZZLES -> {
                val theme = when (itemId) {
                    "puzzle_zen" -> JigsawTheme.ZEN_GARDEN
                    "puzzle_cyber" -> JigsawTheme.CYBERPUNK
                    "puzzle_runes" -> JigsawTheme.RUNES
                    else -> JigsawTheme.AURORA
                }
                setSelectedJigsawTheme(theme)
            }
            StoreCategory.SOUNDS -> {
                val sound = when (itemId) {
                    "sound_rain" -> FocusSoundscape.RAIN
                    "sound_forest" -> FocusSoundscape.FOREST
                    "sound_waves" -> FocusSoundscape.WAVES
                    "sound_binaural" -> FocusSoundscape.BINAURAL_BEATS
                    else -> FocusSoundscape.SILENT
                }
                saveActiveSoundscape(sound)
            }
        }
    }

    fun getActiveThemeId(): String {
        if (!prefs.getBoolean("has_applied_flame_default_v4", false)) {
            prefs.edit()
                .putString(KEY_EQUIPPED_THEME, "theme_flame")
                .putBoolean("has_applied_flame_default_v4", true)
                .apply()
            return "theme_flame"
        }
        return prefs.getString(KEY_EQUIPPED_THEME, "theme_flame") ?: "theme_flame"
    }

    fun getSelectedJigsawTheme(): JigsawTheme {
        val name = prefs.getString(KEY_JIGSAW_THEME, JigsawTheme.AURORA.name)
        return try {
            JigsawTheme.valueOf(name ?: JigsawTheme.AURORA.name)
        } catch (e: Exception) {
            JigsawTheme.AURORA
        }
    }

    fun setSelectedJigsawTheme(theme: JigsawTheme) {
        prefs.edit().putString(KEY_JIGSAW_THEME, theme.name).apply()
    }

    private fun defaultStoreItems(): List<StoreItem> = listOf(
        StoreItem("theme_flame", "Hourglass Flame (Default)", StoreCategory.THEMES, cost = 0, iconEmoji = "⏳", description = "Electric lime & ember flame matching the FocusNow logo", isUnlocked = true, isEquipped = true),
        StoreItem("theme_aurora_coral", "Aurora Mind (Coral)", StoreCategory.THEMES, cost = 0, iconEmoji = "🧠", description = "Coral and violet neural gradient on midnight navy", isUnlocked = true, isEquipped = false),
        StoreItem("theme_midnight", "Glacier Frost", StoreCategory.THEMES, cost = 80, iconEmoji = "❄️", description = "Deep navy canvas with glacier blue luminescence"),
        StoreItem("theme_cyberpunk", "Cyberpunk Neon", StoreCategory.THEMES, cost = 150, iconEmoji = "⚡", description = "High-voltage ultraviolet and laser magenta theme"),
        StoreItem("theme_sunset", "Solar Amber", StoreCategory.THEMES, cost = 120, iconEmoji = "🌅", description = "Warm twilight gradient with golden amber accents"),
        StoreItem("puzzle_aurora", "Nordic Aurora", StoreCategory.PUZZLES, cost = 0, iconEmoji = "🌌", description = "Glacial mountain lights visual jigsaw pack", isUnlocked = true, isEquipped = true),
        StoreItem("puzzle_zen", "Zen Sanctuary", StoreCategory.PUZZLES, cost = 90, iconEmoji = "🎋", description = "Japanese garden stone pathway jigsaw visuals"),
        StoreItem("puzzle_cyber", "Cyber Matrix", StoreCategory.PUZZLES, cost = 100, iconEmoji = "🕹️", description = "Hacker grid circuit board puzzle design"),
        StoreItem("puzzle_runes", "Focus Runes", StoreCategory.PUZZLES, cost = 60, iconEmoji = "🔢", description = "Classic tactical glyph numbers"),
        StoreItem("sound_rain", "Rain on Tent", StoreCategory.SOUNDS, cost = 60, iconEmoji = "⛺", description = "Cozy atmospheric rainfall acoustic shield")
    )

    // Deep Focus Lockdown Mode
    fun isFocusSessionActive(): Boolean {
        val isActive = prefs.getBoolean(KEY_FOCUS_SESSION_ACTIVE, false)
        if (!isActive) return false
        val endMillis = prefs.getLong(KEY_FOCUS_SESSION_END_MILLIS, 0L)
        if (endMillis > 0L && System.currentTimeMillis() >= endMillis) {
            setFocusSessionActive(false, 0L)
            return false
        }
        return true
    }

    fun setFocusSessionActive(active: Boolean, endMillis: Long = 0L) {
        prefs.edit()
            .putBoolean(KEY_FOCUS_SESSION_ACTIVE, active)
            .putLong(KEY_FOCUS_SESSION_END_MILLIS, if (active) endMillis else 0L)
            .apply()
    }

    fun getFocusSessionEndMillis(): Long = prefs.getLong(KEY_FOCUS_SESSION_END_MILLIS, 0L)

    fun getFocusSessionRemainingSeconds(): Int {
        val endMillis = getFocusSessionEndMillis()
        if (endMillis <= 0L) return 0
        val remaining = (endMillis - System.currentTimeMillis()) / 1000L
        return remaining.coerceAtLeast(0L).toInt()
    }

    // Emergency 1-Minute Pass
    fun setEmergencyPass(packageName: String, durationSeconds: Int = 60) {
        val expiryMillis = System.currentTimeMillis() + (durationSeconds * 1000L)
        prefs.edit()
            .putString(KEY_EMERGENCY_PASS_APP, packageName)
            .putLong(KEY_EMERGENCY_PASS_EXPIRY, expiryMillis)
            .apply()
    }

    fun isEmergencyPassActiveFor(packageName: String): Boolean {
        val app = prefs.getString(KEY_EMERGENCY_PASS_APP, null) ?: return false
        val expiry = prefs.getLong(KEY_EMERGENCY_PASS_EXPIRY, 0L)
        val now = System.currentTimeMillis()
        if (now < expiry) {
            val simpleTarget = packageName.lowercase()
            val simpleSaved = app.lowercase()
            return simpleTarget.contains(simpleSaved) || simpleSaved.contains(simpleTarget)
        }
        return false
    }

    fun getEmergencyPassRemainingSeconds(): Int {
        val expiry = prefs.getLong(KEY_EMERGENCY_PASS_EXPIRY, 0L)
        val diff = (expiry - System.currentTimeMillis()) / 1000L
        return diff.coerceAtLeast(0L).toInt()
    }

    fun getEmergencyPassApp(): String = prefs.getString(KEY_EMERGENCY_PASS_APP, "") ?: ""

    // Productive Apps Whitelist
    private val defaultProductiveApps = listOf(
        ProductiveAppItem("app_phone", "Phone & Voice Calls", "com.google.android.dialer", "📞", "Communication", isEnabled = true, isEssential = true, isWorkModeOfficeApp = true),
        ProductiveAppItem("app_duolingo", "Duolingo", "com.duolingo", "🦉", "Education & Learning", isEnabled = true, isEssential = true, isWorkModeOfficeApp = true),
        ProductiveAppItem("app_khanacademy", "Khan Academy", "org.khanacademy.android", "🎓", "Education & Learning", isEnabled = true, isEssential = true, isWorkModeOfficeApp = true),
        ProductiveAppItem("app_quizlet", "Quizlet", "com.quizlet.quizletandroid", "📚", "Education & Learning", isEnabled = true, isEssential = true, isWorkModeOfficeApp = true),
        ProductiveAppItem("app_teams", "Microsoft Teams", "com.microsoft.teams", "👥", "Office / Work", isEnabled = true, isEssential = true, isWorkModeOfficeApp = true),
        ProductiveAppItem("app_outlook", "Microsoft Outlook", "com.microsoft.office.outlook", "📧", "Office / Work", isEnabled = true, isEssential = true, isWorkModeOfficeApp = true),
        ProductiveAppItem("app_whatsapp", "WhatsApp Messenger", "com.whatsapp", "💬", "Office / Work", isEnabled = true, isEssential = true, isWorkModeOfficeApp = true),
        ProductiveAppItem("app_office365", "Microsoft 365 (Word/Excel)", "com.microsoft.office.officehubrow", "📊", "Office / Work", isEnabled = true, isEssential = true, isWorkModeOfficeApp = true),
        ProductiveAppItem("app_slack", "Slack Work Chat", "com.slack", "💬", "Office / Work", isEnabled = true, isEssential = true, isWorkModeOfficeApp = true),
        ProductiveAppItem("app_docs", "Docs & Drive", "com.google.android.apps.docs", "📄", "Office / Work", isEnabled = true, isEssential = true, isWorkModeOfficeApp = true),
        ProductiveAppItem("app_gmail", "Gmail", "com.google.android.gm", "📬", "Office / Work", isEnabled = true, isEssential = true, isWorkModeOfficeApp = true),
        ProductiveAppItem("app_zoom", "Zoom Meetings", "us.zoom.videomeetings", "📹", "Office / Work", isEnabled = true, isEssential = true, isWorkModeOfficeApp = true),
        ProductiveAppItem("app_calendar", "Calendar", "com.google.android.calendar", "📅", "Schedule", isEnabled = true, isEssential = true, isWorkModeOfficeApp = true),
        ProductiveAppItem("app_notes", "Notes & Keep", "com.google.android.keep", "📝", "Productivity", isEnabled = true, isEssential = true, isWorkModeOfficeApp = true),
        ProductiveAppItem("app_calc", "Calculator", "com.google.android.calculator", "🧮", "Utility", isEnabled = true, isEssential = true),
        ProductiveAppItem("app_clock", "Clock & Timer", "com.google.android.deskclock", "⏰", "Utility", isEnabled = true, isEssential = true),
        ProductiveAppItem("app_notion", "Notion", "notion.id", "💡", "Productivity", isEnabled = true, isEssential = true, isWorkModeOfficeApp = true),
        ProductiveAppItem("app_tasks", "Google Tasks", "com.google.android.apps.tasks", "✅", "Productivity", isEnabled = true, isEssential = true, isWorkModeOfficeApp = true),
        ProductiveAppItem("app_github", "GitHub", "com.github.android", "🐙", "Dev", isEnabled = true, isEssential = true, isWorkModeOfficeApp = true),
        ProductiveAppItem("app_termux", "Termux Terminal", "com.termux", "💻", "Dev", isEnabled = true, isEssential = true, isWorkModeOfficeApp = true)
    )

    fun isWorkModeEnabled(): Boolean = prefs.getBoolean(KEY_WORK_MODE_ENABLED, false)

    fun setWorkModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WORK_MODE_ENABLED, enabled).apply()
    }

    fun getWorkModeScheduleConfig(): WorkModeScheduleConfig {
        val enabled = prefs.getBoolean(KEY_WORK_MODE_AUTO_SCHEDULE_ENABLED, false)
        val startH = prefs.getInt(KEY_WORK_MODE_START_HOUR, 9)
        val startM = prefs.getInt(KEY_WORK_MODE_START_MIN, 0)
        val endH = prefs.getInt(KEY_WORK_MODE_END_HOUR, 17)
        val endM = prefs.getInt(KEY_WORK_MODE_END_MIN, 0)
        return WorkModeScheduleConfig(
            isAutoScheduleEnabled = enabled,
            startHour = startH,
            startMinute = startM,
            endHour = endH,
            endMinute = endM
        )
    }

    fun saveWorkModeScheduleConfig(config: WorkModeScheduleConfig) {
        prefs.edit()
            .putBoolean(KEY_WORK_MODE_AUTO_SCHEDULE_ENABLED, config.isAutoScheduleEnabled)
            .putInt(KEY_WORK_MODE_START_HOUR, config.startHour)
            .putInt(KEY_WORK_MODE_START_MIN, config.startMinute)
            .putInt(KEY_WORK_MODE_END_HOUR, config.endHour)
            .putInt(KEY_WORK_MODE_END_MIN, config.endMinute)
            .apply()
    }

    fun isWorkModeActive(): Boolean {
        if (isWorkModeEnabled()) return true
        if (getWorkModeScheduleConfig().isCurrentlyActive()) return true
        return false
    }

    // Daily Puzzle Pass Count & Progressive Friction
    private fun getTodayDateString(): String {
        val cal = java.util.Calendar.getInstance()
        return String.format(java.util.Locale.US, "%04d-%02d-%02d",
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    fun getDailyPuzzlePassCount(): Int {
        val savedDate = prefs.getString(KEY_DAILY_PUZZLE_PASS_DATE, "")
        val today = getTodayDateString()
        if (savedDate != today) {
            prefs.edit().putString(KEY_DAILY_PUZZLE_PASS_DATE, today).putInt(KEY_DAILY_PUZZLE_PASS_COUNT, 0).apply()
            return 0
        }
        return prefs.getInt(KEY_DAILY_PUZZLE_PASS_COUNT, 0)
    }

    fun incrementDailyPuzzlePassCount(): Int {
        val count = getDailyPuzzlePassCount() + 1
        val today = getTodayDateString()
        prefs.edit().putString(KEY_DAILY_PUZZLE_PASS_DATE, today).putInt(KEY_DAILY_PUZZLE_PASS_COUNT, count).apply()
        return count
    }

    fun getProgressiveFrictionStatus(): ProgressiveFrictionStatus {
        val count = getDailyPuzzlePassCount()
        val tier = when {
            count == 0 -> FrictionTier.STANDARD
            count == 1 -> FrictionTier.CHALLENGER
            else -> FrictionTier.MASTERY
        }
        return ProgressiveFrictionStatus(passesUsedToday = count, currentTier = tier)
    }

    // Floating Focus Island Setting
    fun isFloatingPillEnabled(): Boolean = prefs.getBoolean(KEY_FLOATING_PILL_ENABLED, true)
    fun setFloatingPillEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FLOATING_PILL_ENABLED, enabled).apply()
    }

    fun isEducationalApp(pkg: String): Boolean {
        val target = pkg.lowercase()

        // 1. Language Learning & Core Educational Giants (Duolingo, Khan Academy, Quizlet, Anki, etc.)
        if (target == "com.duolingo" || target.startsWith("com.duolingo") || target.contains("duolingo")) return true
        if (target.contains("khanacademy") || target.contains("quizlet") || target.contains("anki") || target.contains("ichi2.anki")) return true
        if (target.contains("coursera") || target.contains("udemy") || target.contains("edx") || target.contains("babbel")) return true
        if (target.contains("memrise") || target.contains("busuu") || target.contains("photomath") || target.contains("brilliant")) return true
        if (target.contains("sololearn") || target.contains("codecademy") || target.contains("getmimo") || target.contains("wikipedia")) return true
        if (target.contains("socratic") || target.contains("wolfram") || target.contains("geogebra") || target.contains("classroom")) return true
        if (target.contains("lingvist") || target.contains("ted.android") || target.contains("brainly") || target.contains("chegg")) return true
        if (target.contains("forestapp") || target.contains("elevate") || target.contains("lumoslabs.lumosity")) return true

        // 2. Android OS ApplicationInfo.CATEGORY_EDUCATION inspection
        try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(pkg, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                if (false) {
                    return true
                }
            }
        } catch (_: Exception) {}

        // 3. Educational TLD or domain keyword
        if (target.contains(".edu.") || target.endsWith(".edu") || target.contains(".education") || target.contains("learning")) {
            return true
        }

        return false
    }

    fun isOfficeOrWorkspaceApp(pkg: String): Boolean {
        if (pkg.isBlank()) return false
        val target = pkg.lowercase().trim()

        // 1. Microsoft 365 & Office Suite (Teams, Outlook, Word, Excel, PPT, OneNote, OneDrive, Loop, Copilot)
        if (target == "com.microsoft.teams" || target == "com.microsoft.teams.v2" || target == "com.microsoft.skype.teams" || (target.startsWith("com.microsoft") && target.contains("teams"))) return true
        if (target == "com.microsoft.office.outlook" || target == "com.microsoft.outlook" || (target.startsWith("com.microsoft") && target.contains("outlook"))) return true
        if (target.startsWith("com.microsoft.office")) return true // Word, Excel, PowerPoint, Office Mobile, OneNote
        if (target == "com.microsoft.skydrive" || target == "com.microsoft.onedrive") return true
        if (target == "com.microsoft.todos" || target == "com.microsoft.whiteboard.android" || target == "com.microsoft.copilot") return true
        if (target == "com.azure.authenticator" || target == "com.microsoft.windowsintune.companyportal") return true

        // 2. Google Workspace Suite (Gmail, Drive, Docs, Sheets, Slides, Meet, Chat, Calendar, Keep, Tasks, Classroom)
        if (target == "com.google.android.gm" || target == "com.google.android.gm.lite") return true
        if (target == "com.google.android.apps.docs" || target == "com.google.android.apps.drive" || target.startsWith("com.google.android.apps.docs")) return true
        if (target == "com.google.android.calendar" || target == "com.google.android.keep" || target == "com.google.android.apps.tasks") return true
        if (target == "com.google.android.apps.meetings" || target == "com.google.android.apps.tachyon" || target == "com.google.android.apps.dynamite") return true
        if (target == "com.google.android.apps.classroom") return true
        if (target == "com.google.android.apps.work.clouddpc" || target == "com.google.android.apps.enterprise.dmagent") return true

        // 3. Workspace Messaging & Video Conferencing
        if (target == "com.slack" || target.startsWith("com.slack")) return true
        if (target == "com.whatsapp" || target == "com.whatsapp.w4b" || target.startsWith("com.whatsapp")) return true
        if (target == "us.zoom.videomeetings" || target.contains(".zoom.") || target.startsWith("us.zoom")) return true
        if (target.contains("webex") || target.contains("cisco.wx2")) return true
        if (target == "com.skype.raider" || target == "com.skype.m2") return true
        if (target.contains("mattermost") || target.contains("larksuite") || target.contains("feishu")) return true

        // 4. Project Management, Knowledge Base & Developer Workspace
        if (target == "notion.id" || target.contains("notion")) return true
        if (target == "com.asana.app" || target.contains("asana")) return true
        if (target == "com.trello" || target.contains("trello")) return true
        if (target.contains("atlassian") || target.contains("jira") || target.contains("confluence")) return true
        if (target == "app.linear" || target.contains("linear")) return true
        if (target == "co.clickup" || target.contains("clickup")) return true
        if (target == "com.monday.monday" || target.contains("monday.com")) return true
        if (target == "com.basecamp.bc3" || target.contains("basecamp")) return true
        if (target == "com.github.android" || target.contains("gitlab") || target.contains("bitbucket")) return true
        if (target == "com.figma.mirror" || target.contains("figma") || target.contains("miro")) return true
        if (target.contains("airtable") || target.contains("coda.coda")) return true

        // 5. Office Suite Alternatives (WPS Office, Polaris, Collabora, LibreOffice)
        if (target.contains("moffice") || target.contains("collabora") || target.contains("infraware.office") || target.contains("openoffice")) return true

        // 6. Enterprise / MDM / Work Profile Android Packages
        if (target.contains("workprofile") || target.contains("workspace") || target.contains(".knox.") || target.contains("airwatch") || target.contains("mobileiron")) return true

        // 7. Android OS ApplicationInfo.CATEGORY_PRODUCTIVITY check
        try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(pkg, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                if (appInfo.category == android.content.pm.ApplicationInfo.CATEGORY_PRODUCTIVITY) {
                    return true
                }
            }
        } catch (_: Exception) {}

        // 8. Work Profile / Managed Profile UserHandle Detection
        try {
            val launcherApps = context.getSystemService(android.content.Context.LAUNCHER_APPS_SERVICE) as? android.content.pm.LauncherApps
            val userManager = context.getSystemService(android.content.Context.USER_SERVICE) as? android.os.UserManager
            if (launcherApps != null && userManager != null) {
                for (profile in userManager.userProfiles) {
                    if (profile != android.os.Process.myUserHandle()) {
                        val activities = launcherApps.getActivityList(pkg, profile)
                        if (activities.isNotEmpty()) return true
                    }
                }
            }
        } catch (_: Exception) {}

        return false
    }

    fun isWorkAppAllowed(pkg: String): Boolean {
        // Office and workspace apps should ALWAYS work
        if (isOfficeOrWorkspaceApp(pkg)) return true

        // Educational apps like Duolingo are always accessible
        if (isEducationalApp(pkg)) return true

        val target = pkg.lowercase()
        // 1. Microsoft Teams
        if (target == "com.microsoft.teams" || target == "com.microsoft.teams.v2" || target.contains("teams")) return true
        // 2. Microsoft Outlook
        if (target == "com.microsoft.office.outlook" || target == "com.microsoft.outlook" || target.contains("outlook")) return true
        // 3. WhatsApp & WhatsApp Business
        if (target == "com.whatsapp" || target == "com.whatsapp.w4b" || target.startsWith("com.whatsapp")) return true
        // 4. Microsoft Office Suite (Word, Excel, PPT, 365 Office, OneNote, OneDrive)
        if (target.startsWith("com.microsoft.office") || target == "com.microsoft.skydrive") return true
        // 5. Work Chat & Documents (Slack, Google Docs)
        if (target == "com.slack" || target == "com.google.android.apps.docs" || target.startsWith("com.google.android.apps.docs")) return true
        return false
    }

    fun applyStrictWorkModePreset() {
        val strictWorkIds = setOf("app_phone", "app_teams", "app_outlook", "app_whatsapp", "app_office365", "app_slack", "app_docs", "app_calendar", "app_gmail", "app_zoom", "app_notion")
        val disabledSet = defaultProductiveApps
            .filter { !strictWorkIds.contains(it.id) && !it.isEssential }
            .map { it.id }
            .toSet()
        prefs.edit()
            .putStringSet(KEY_DISABLED_PRODUCTIVE_APPS, disabledSet)
            .putBoolean(KEY_WORK_MODE_ENABLED, true)
            .apply()
    }

    fun getProductiveAppItems(): List<ProductiveAppItem> {
        val disabledSet = prefs.getStringSet(KEY_DISABLED_PRODUCTIVE_APPS, emptySet()) ?: emptySet()
        return defaultProductiveApps.map { item ->
            if (item.isEssential || isOfficeOrWorkspaceApp(item.packageName)) item.copy(isEnabled = true, isEssential = true)
            else item.copy(isEnabled = !disabledSet.contains(item.id))
        }
    }

    fun setProductiveAppEnabled(appId: String, enabled: Boolean) {
        val disabledSet = prefs.getStringSet(KEY_DISABLED_PRODUCTIVE_APPS, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (enabled) {
            disabledSet.remove(appId)
        } else {
            disabledSet.add(appId)
        }
        prefs.edit().putStringSet(KEY_DISABLED_PRODUCTIVE_APPS, disabledSet).apply()
    }

    fun isProductiveAppAllowed(pkg: String): Boolean {
        // Office and workspace apps should ALWAYS work regardless of Work Mode toggle
        if (isOfficeOrWorkspaceApp(pkg)) return true

        // Educational apps like Duolingo are inherently productive and always allowed
        if (isEducationalApp(pkg)) return true

        // If Work Mode is active, check work app allowance
        if (isWorkModeActive() && isWorkAppAllowed(pkg)) {
            return true
        }
        val allowedItems = getProductiveAppItems().filter { it.isEnabled }
        val target = pkg.lowercase()
        return allowedItems.any { item ->
            val p = item.packageName.lowercase()
            target == p || target.startsWith(p) ||
                (p.contains("calculator") && target.contains("calculator")) ||
                (p.contains("dialer") && target.contains("dialer")) ||
                (p.contains("clock") && target.contains("clock")) ||
                (p == "com.microsoft.teams" && (target.contains("teams") || target.startsWith("com.microsoft.teams"))) ||
                (p == "com.microsoft.office.outlook" && (target.contains("outlook") || target == "com.microsoft.outlook" || target.startsWith("com.microsoft.office.outlook"))) ||
                (p == "com.whatsapp" && (target == "com.whatsapp" || target == "com.whatsapp.w4b" || target.startsWith("com.whatsapp"))) ||
                (p.contains("office") && target.startsWith("com.microsoft.office"))
        }
    }

    // Master Shield Power Switch (User can turn off entire blocker anytime)
    fun isMasterShieldEnabled(): Boolean = prefs.getBoolean(KEY_MASTER_SHIELD_ENABLED, true)

    fun setMasterShieldEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MASTER_SHIELD_ENABLED, enabled).apply()
    }

    // Always Allowed Apps (Apps the user can use all the time without any restrictions)
    fun getAlwaysAllowedPackages(): Set<String> {
        val defaultSet = setOf(
            "com.google.android.dialer",
            "com.android.dialer",
            "com.samsung.android.dialer",
            "com.google.android.apps.messaging",
            "com.android.mms",
            "com.google.android.apps.maps",
            "com.android.settings",
            "com.google.android.calculator",
            "com.android.calculator2",
            "com.google.android.deskclock",
            "com.android.deskclock",
            "com.google.android.apps.photos",
            "com.spotify.music",
            "com.whatsapp",
            "com.whatsapp.w4b",
            "com.microsoft.teams",
            "com.microsoft.office.outlook",
            "com.microsoft.office.officehubrow",
            "com.microsoft.office.word",
            "com.microsoft.office.excel",
            "com.microsoft.office.powerpoint",
            "com.microsoft.office.onenote",
            "com.microsoft.skydrive",
            "com.slack",
            "com.google.android.apps.docs",
            "com.google.android.gm",
            "com.google.android.calendar",
            "us.zoom.videomeetings",
            "notion.id",
            "com.google.android.keep",
            "com.google.android.apps.tasks",
            "com.github.android",
            "com.focusflow.app"
        )
        return prefs.getStringSet(KEY_CUSTOM_ALWAYS_ALLOWED_PACKAGES, null) ?: defaultSet
    }

    fun setAlwaysAllowedPackages(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_CUSTOM_ALWAYS_ALLOWED_PACKAGES, packages).apply()
    }

    fun toggleAlwaysAllowedPackage(pkg: String, allowed: Boolean) {
        val set = getAlwaysAllowedPackages().toMutableSet()
        if (allowed) {
            set.add(pkg.lowercase())
        } else {
            // Never remove office or workspace apps from always allowed
            if (isOfficeOrWorkspaceApp(pkg)) return
            set.remove(pkg.lowercase())
        }
        setAlwaysAllowedPackages(set)
    }

    fun isAlwaysAllowed(pkg: String): Boolean {
        if (pkg == "com.focusflow.app") return true
        if (isOfficeOrWorkspaceApp(pkg)) return true
        if (isEducationalApp(pkg)) return true
        val target = pkg.lowercase()
        val alwaysSet = getAlwaysAllowedPackages()
        if (alwaysSet.contains(target) || alwaysSet.any { target.startsWith(it) }) return true
        return isProductiveAppAllowed(pkg)
    }

    // Notification Shield & Batching
    fun isNotificationShieldEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFICATION_SHIELD_ENABLED, true)

    fun setNotificationShieldEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_SHIELD_ENABLED, enabled).apply()
    }

    fun addBatchedNotification(notification: BatchedNotification) {
        val currentList = getBatchedNotifications().toMutableList()
        currentList.add(0, notification) // newest first
        val trimmed = if (currentList.size > 50) currentList.take(50) else currentList

        val jsonArray = JSONArray()
        for (item in trimmed) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("packageName", item.packageName)
                put("appName", item.appName)
                put("title", item.title)
                put("text", item.text)
                put("timestamp", item.timestamp)
                put("emoji", item.emoji)
                put("category", item.category)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_BATCHED_NOTIFICATIONS_JSON, jsonArray.toString()).apply()
    }

    fun getBatchedNotifications(): List<BatchedNotification> {
        val jsonString = prefs.getString(KEY_BATCHED_NOTIFICATIONS_JSON, null) ?: return emptyList()
        val list = mutableListOf<BatchedNotification>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    BatchedNotification(
                        id = obj.getString("id"),
                        packageName = obj.getString("packageName"),
                        appName = obj.getString("appName"),
                        title = obj.getString("title"),
                        text = obj.getString("text"),
                        timestamp = obj.getLong("timestamp"),
                        emoji = obj.optString("emoji", "🔔"),
                        category = obj.optString("category", "Distraction")
                    )
                )
            }
        } catch (ignored: Exception) {}
        return list
    }

    fun clearBatchedNotifications() {
        prefs.edit().remove(KEY_BATCHED_NOTIFICATIONS_JSON).apply()
    }

    fun getBatchedNotificationCount(): Int {
        return getBatchedNotifications().size
    }

    // --- SLEEP ARCHITECTURE & BEDTIME CURFEW TRACKING ---
    fun getSleepTrackingState(): SleepTrackingState {
        val json = prefs.getString(KEY_SLEEP_TRACKING_STATE, null)
        if (json != null) {
            try {
                val obj = JSONObject(json)
                val stagesObj = obj.optJSONObject("stages")
                val stages = if (stagesObj != null) {
                    SleepStageBreakdown(
                        deepMinutes = stagesObj.optInt("deepMinutes", 105),
                        remMinutes = stagesObj.optInt("remMinutes", 120),
                        lightMinutes = stagesObj.optInt("lightMinutes", 210),
                        awakeMinutes = stagesObj.optInt("awakeMinutes", 33)
                    )
                } else SleepStageBreakdown()

                val historyList = mutableListOf<DailySleepRecord>()
                val histArray = obj.optJSONArray("weeklyHistory")
                if (histArray != null) {
                    for (i in 0 until histArray.length()) {
                        val hObj = histArray.getJSONObject(i)
                        historyList.add(
                            DailySleepRecord(
                                dayOfWeek = hObj.optString("dayOfWeek", "Mon"),
                                dateStr = hObj.optString("dateStr", ""),
                                durationHours = hObj.optDouble("durationHours", 7.5).toFloat(),
                                score = hObj.optInt("score", 85),
                                bedtime = hObj.optString("bedtime", "11:00 PM"),
                                wakeTime = hObj.optString("wakeTime", "07:00 AM")
                            )
                        )
                    }
                }

                return SleepTrackingState(
                    bedtimeCurfewEnabled = obj.optBoolean("bedtimeCurfewEnabled", true),
                    curfewStartHour = obj.optInt("curfewStartHour", 22),
                    curfewStartMinute = obj.optInt("curfewStartMinute", 30),
                    curfewEndHour = obj.optInt("curfewEndHour", 7),
                    curfewEndMinute = obj.optInt("curfewEndMinute", 0),
                    targetDurationHours = obj.optDouble("targetDurationHours", 8.0).toFloat(),
                    lastNightScore = obj.optInt("lastNightScore", 88),
                    lastNightDuration = obj.optString("lastNightDuration", "7h 48m"),
                    lastBedtime = obj.optString("lastBedtime", "10:45 PM"),
                    lastWakeTime = obj.optString("lastWakeTime", "06:33 AM"),
                    sleepDebtMinutes = obj.optInt("sleepDebtMinutes", -12),
                    doomscrollInterceptionsPast11Pm = obj.optInt("doomscrollInterceptionsPast11Pm", 4),
                    stages = stages,
                    weeklySleepHistory = if (historyList.isNotEmpty()) historyList else SleepTrackingState().weeklySleepHistory
                )
            } catch (ignored: Exception) {}
        }
        return SleepTrackingState()
    }

    fun saveSleepTrackingState(state: SleepTrackingState) {
        try {
            val obj = JSONObject().apply {
                put("bedtimeCurfewEnabled", state.bedtimeCurfewEnabled)
                put("curfewStartHour", state.curfewStartHour)
                put("curfewStartMinute", state.curfewStartMinute)
                put("curfewEndHour", state.curfewEndHour)
                put("curfewEndMinute", state.curfewEndMinute)
                put("targetDurationHours", state.targetDurationHours.toDouble())
                put("lastNightScore", state.lastNightScore)
                put("lastNightDuration", state.lastNightDuration)
                put("lastBedtime", state.lastBedtime)
                put("lastWakeTime", state.lastWakeTime)
                put("sleepDebtMinutes", state.sleepDebtMinutes)
                put("doomscrollInterceptionsPast11Pm", state.doomscrollInterceptionsPast11Pm)

                val stagesObj = JSONObject().apply {
                    put("deepMinutes", state.stages.deepMinutes)
                    put("remMinutes", state.stages.remMinutes)
                    put("lightMinutes", state.stages.lightMinutes)
                    put("awakeMinutes", state.stages.awakeMinutes)
                }
                put("stages", stagesObj)

                val histArray = JSONArray()
                for (rec in state.weeklySleepHistory) {
                    val hObj = JSONObject().apply {
                        put("dayOfWeek", rec.dayOfWeek)
                        put("dateStr", rec.dateStr)
                        put("durationHours", rec.durationHours.toDouble())
                        put("score", rec.score)
                        put("bedtime", rec.bedtime)
                        put("wakeTime", rec.wakeTime)
                    }
                    histArray.put(hObj)
                }
                put("weeklyHistory", histArray)
            }
            prefs.edit().putString(KEY_SLEEP_TRACKING_STATE, obj.toString()).apply()
        } catch (ignored: Exception) {}
    }

    fun isBedtimeCurfewActive(): Boolean {
        val state = getSleepTrackingState()
        return state.isCurfewCurrentlyActive()
    }

    fun toggleBedtimeCurfew(enabled: Boolean) {
        val current = getSleepTrackingState()
        saveSleepTrackingState(current.copy(bedtimeCurfewEnabled = enabled))
    }

    fun updateBedtimeCurfewTimes(startH: Int, startM: Int, endH: Int, endM: Int) {
        val current = getSleepTrackingState()
        saveSleepTrackingState(
            current.copy(
                curfewStartHour = startH,
                curfewStartMinute = startM,
                curfewEndHour = endH,
                curfewEndMinute = endM
            )
        )
    }

    fun logManualSleepRecord(
        durationHours: Float,
        score: Int,
        bedtime: String,
        wakeTime: String,
        deepMins: Int,
        remMins: Int,
        lightMins: Int,
        awakeMins: Int
    ) {
        val current = getSleepTrackingState()
        val newStages = SleepStageBreakdown(
            deepMinutes = deepMins,
            remMinutes = remMins,
            lightMinutes = lightMins,
            awakeMinutes = awakeMins
        )
        val todayFmt = SimpleDateFormat("EEE", Locale.US).format(Date())
        val dateFmt = SimpleDateFormat("MMM dd", Locale.US).format(Date())
        val newRecord = DailySleepRecord(
            dayOfWeek = todayFmt,
            dateStr = dateFmt,
            durationHours = durationHours,
            score = score,
            bedtime = bedtime,
            wakeTime = wakeTime,
            stages = newStages
        )
        val updatedHistory = current.weeklySleepHistory.toMutableList()
        if (updatedHistory.size >= 7) {
            updatedHistory.removeAt(0)
        }
        updatedHistory.add(newRecord)

        val targetMins = (current.targetDurationHours * 60).toInt()
        val actualMins = (durationHours * 60).toInt()
        val debtMins = actualMins - targetMins

        val h = durationHours.toInt()
        val m = ((durationHours - h) * 60).toInt()
        val formattedDuration = "${h}h ${m}m"

        val updatedState = current.copy(
            lastNightScore = score,
            lastNightDuration = formattedDuration,
            lastBedtime = bedtime,
            lastWakeTime = wakeTime,
            sleepDebtMinutes = debtMins,
            stages = newStages,
            weeklySleepHistory = updatedHistory
        )
        saveSleepTrackingState(updatedState)
    }

    // --- ALL APPS SHIELD & BLOCKER (EVERY APP TOGGLE HUB) ---
    private val defaultBlockedPackages = setOf(
        "com.instagram.android",
        "com.google.android.youtube",
        "com.twitter.android",
        "com.reddit.frontpage"
    )

    fun isPackageInBlockedSet(packageName: String): Boolean {
        // Must be actually installed on this phone
        if (!isPackageInstalled(packageName)) return false
        // Office apps and workspace are strictly protected and never blocked
        if (isOfficeOrWorkspaceApp(packageName)) return false
        // Educational apps like Duolingo are protected and never blocked
        if (isEducationalApp(packageName)) return false
        val blockedSet = prefs.getStringSet(KEY_BLOCKED_APP_PACKAGES_SET, null)
        return if (blockedSet != null) {
            blockedSet.contains(packageName)
        } else {
            defaultBlockedPackages.filter { isPackageInstalled(it) }.toSet().contains(packageName)
        }
    }

    fun setAppBlockedState(packageName: String, isBlocked: Boolean) {
        // Office apps, workspace, and educational apps are protected and can never be blocked
        if (isOfficeOrWorkspaceApp(packageName) || isEducationalApp(packageName)) return

        val currentSet = (prefs.getStringSet(KEY_BLOCKED_APP_PACKAGES_SET, null) ?: defaultBlockedPackages.filter { isPackageInstalled(it) }.toSet()).toMutableSet()
        if (isBlocked) {
            currentSet.add(packageName)
        } else {
            currentSet.remove(packageName)
        }
        prefs.edit().putStringSet(KEY_BLOCKED_APP_PACKAGES_SET, currentSet).apply()

        // Also synchronize with blockedApps list
        val currentApps = getBlockedApps().toMutableList()
        val index = currentApps.indexOfFirst { it.packageName == packageName }
        if (index >= 0) {
            currentApps[index] = currentApps[index].copy(isBlocked = isBlocked)
            saveBlockedApps(currentApps)
        }
    }

    fun batchSetCategoryBlocked(category: String, isBlocked: Boolean) {
        val rules = getAllAppShieldRules()
        val targetPackages = rules
            .filter { it.category.equals(category, ignoreCase = true) && !isEducationalApp(it.packageName) && !isOfficeOrWorkspaceApp(it.packageName) }
            .map { it.packageName }
        val currentSet = (prefs.getStringSet(KEY_BLOCKED_APP_PACKAGES_SET, null) ?: defaultBlockedPackages.filter { isPackageInstalled(it) }.toSet()).toMutableSet()
        if (isBlocked) {
            currentSet.addAll(targetPackages)
        } else {
            currentSet.removeAll(targetPackages)
        }
        prefs.edit().putStringSet(KEY_BLOCKED_APP_PACKAGES_SET, currentSet).apply()
    }

    fun getAllAppShieldRules(): List<AppShieldRuleItem> {
        val installedMap = getInstalledLauncherPackagesMap()

        val catalog = listOf(
            AppShieldRuleItem("com.duolingo", "Duolingo", "Education & Learning", "🦉", false, 60, installedMap.containsKey("com.duolingo")),
            AppShieldRuleItem("org.khanacademy.android", "Khan Academy", "Education & Learning", "🎓", false, 60, installedMap.containsKey("org.khanacademy.android")),
            AppShieldRuleItem("com.instagram.android", "Instagram", "Social Media", "📸", isPackageInBlockedSet("com.instagram.android"), 15, installedMap.containsKey("com.instagram.android")),
            AppShieldRuleItem("com.zhiliaoapp.musically", "TikTok", "Short Video", "🎵", isPackageInBlockedSet("com.zhiliaoapp.musically"), 10, installedMap.containsKey("com.zhiliaoapp.musically")),
            AppShieldRuleItem("com.google.android.youtube", "YouTube", "Video & Music", "▶️", isPackageInBlockedSet("com.google.android.youtube"), 45, installedMap.containsKey("com.google.android.youtube")),
            AppShieldRuleItem("com.twitter.android", "Twitter / X", "Social Media", "🐦", isPackageInBlockedSet("com.twitter.android"), 15, installedMap.containsKey("com.twitter.android")),
            AppShieldRuleItem("com.reddit.frontpage", "Reddit", "Community", "🤖", isPackageInBlockedSet("com.reddit.frontpage"), 20, installedMap.containsKey("com.reddit.frontpage")),
            AppShieldRuleItem("com.snapchat.android", "Snapchat", "Social Media", "👻", isPackageInBlockedSet("com.snapchat.android"), 15, installedMap.containsKey("com.snapchat.android")),
            AppShieldRuleItem("com.android.chrome", "Chrome Browser", "Browsers", "🌐", isPackageInBlockedSet("com.android.chrome"), 30, installedMap.containsKey("com.android.chrome")),
            AppShieldRuleItem("com.netflix.mediaclient", "Netflix", "Entertainment", "🍿", isPackageInBlockedSet("com.netflix.mediaclient"), 60, installedMap.containsKey("com.netflix.mediaclient")),
            AppShieldRuleItem("com.spotify.music", "Spotify", "Audio & Music", "🎧", isPackageInBlockedSet("com.spotify.music"), 60, installedMap.containsKey("com.spotify.music")),
            AppShieldRuleItem("com.facebook.katana", "Facebook", "Social Media", "📘", isPackageInBlockedSet("com.facebook.katana"), 15, installedMap.containsKey("com.facebook.katana")),
            AppShieldRuleItem("com.whatsapp", "WhatsApp", "Messaging", "💬", isPackageInBlockedSet("com.whatsapp"), 45, installedMap.containsKey("com.whatsapp")),
            AppShieldRuleItem("org.telegram.messenger", "Telegram", "Messaging", "✈️", isPackageInBlockedSet("org.telegram.messenger"), 45, installedMap.containsKey("org.telegram.messenger")),
            AppShieldRuleItem("com.discord", "Discord", "Community & Gaming", "🎮", isPackageInBlockedSet("com.discord"), 30, installedMap.containsKey("com.discord")),
            AppShieldRuleItem("tv.twitch.android.app", "Twitch", "Live Streams", "👾", isPackageInBlockedSet("tv.twitch.android.app"), 30, installedMap.containsKey("tv.twitch.android.app")),
            AppShieldRuleItem("com.pinterest", "Pinterest", "Lifestyle & Images", "📌", isPackageInBlockedSet("com.pinterest"), 20, installedMap.containsKey("com.pinterest")),
            AppShieldRuleItem("com.roblox.client", "Roblox", "Games", "🧱", isPackageInBlockedSet("com.roblox.client"), 20, installedMap.containsKey("com.roblox.client")),
            AppShieldRuleItem("com.activision.callofduty.shooter", "Call of Duty", "Games", "🎯", isPackageInBlockedSet("com.activision.callofduty.shooter"), 20, installedMap.containsKey("com.activision.callofduty.shooter")),
            AppShieldRuleItem("com.king.candycrushsaga", "Candy Crush", "Games", "🍬", isPackageInBlockedSet("com.king.candycrushsaga"), 15, installedMap.containsKey("com.king.candycrushsaga"))
        )

        // STRICT FILTER: Only include catalog items that are ACTUALLY installed on the user's phone!
        val combinedList = catalog.filter { installedMap.containsKey(it.packageName) }
            .map { it.copy(isInstalled = true) }
            .toMutableList()
        val existingPackages = combinedList.map { it.packageName }.toSet()

        installedMap.forEach { (pkg, label) ->
            if (!existingPackages.contains(pkg)) {
                val isEdu = isEducationalApp(pkg)
                val emoji = when {
                    pkg.contains("duolingo") -> "🦉"
                    isEdu -> "🎓"
                    label.contains("camera", ignoreCase = true) -> "📷"
                    label.contains("gallery", ignoreCase = true) || label.contains("photos", ignoreCase = true) -> "🖼️"
                    label.contains("clock", ignoreCase = true) -> "⏰"
                    label.contains("settings", ignoreCase = true) -> "⚙️"
                    label.contains("contacts", ignoreCase = true) || label.contains("phone", ignoreCase = true) -> "📞"
                    label.contains("message", ignoreCase = true) -> "💬"
                    label.contains("calculator", ignoreCase = true) -> "🧮"
                    label.contains("files", ignoreCase = true) -> "📁"
                    label.contains("music", ignoreCase = true) -> "🎵"
                    label.contains("map", ignoreCase = true) -> "🗺️"
                    else -> "📱"
                }
                val category = when {
                    isEdu -> "Education & Learning"
                    label.contains("game", ignoreCase = true) -> "Games"
                    label.contains("browser", ignoreCase = true) -> "Browsers"
                    else -> "Installed Apps"
                }
                combinedList.add(
                    AppShieldRuleItem(
                        packageName = pkg,
                        appName = label,
                        category = category,
                        iconEmoji = emoji,
                        isBlocked = if (isEdu) false else isPackageInBlockedSet(pkg),
                        dailyLimitMinutes = 30,
                        isInstalled = true
                    )
                )
            }
        }

        return combinedList
    }

    // --- ACHIEVEMENT SYSTEM ---
    fun getUnlockedAchievementIds(): Set<String> {
        return prefs.getStringSet(KEY_UNLOCKED_ACHIEVEMENTS, emptySet()) ?: emptySet()
    }

    fun unlockAchievement(achievementId: String) {
        val current = getUnlockedAchievementIds().toMutableSet()
        current.add(achievementId)
        prefs.edit().putStringSet(KEY_UNLOCKED_ACHIEVEMENTS, current).apply()
    }

    // --- USER LEVEL & XP ---
    fun getUserLevel(): Int = prefs.getInt(KEY_USER_LEVEL, 1)
    fun getUserXp(): Int = prefs.getInt(KEY_USER_XP, 0)
    fun getUserTotalXp(): Int = prefs.getInt(KEY_USER_TOTAL_XP, 0)

    fun addXp(amount: Int): Boolean {
        val currentXp = getUserXp()
        val totalXp = getUserTotalXp()
        val level = getUserLevel()
        val xpNeeded = getXpForLevel(level)

        val newXp = currentXp + amount
        val newTotalXp = totalXp + amount

        prefs.edit().putInt(KEY_USER_TOTAL_XP, newTotalXp).apply()

        if (newXp >= xpNeeded) {
            // Level up!
            val overflow = newXp - xpNeeded
            val newLevel = level + 1
            prefs.edit()
                .putInt(KEY_USER_LEVEL, newLevel)
                .putInt(KEY_USER_XP, overflow)
                .apply()
            return true // Leveled up
        } else {
            prefs.edit().putInt(KEY_USER_XP, newXp).apply()
            return false
        }
    }

    fun getXpForLevel(level: Int): Int {
        return 100 + (level - 1) * 50 // 100, 150, 200, 250...
    }

    fun getLevelTitle(level: Int): String {
        return when {
            level >= 50 -> "Zen Grandmaster"
            level >= 40 -> "Dopamine Sage"
            level >= 30 -> "Focus Legend"
            level >= 25 -> "Neural Architect"
            level >= 20 -> "Attention Master"
            level >= 15 -> "Focus Warrior"
            level >= 10 -> "Flow Expert"
            level >= 7 -> "Focus Adept"
            level >= 5 -> "Mindful Student"
            level >= 3 -> "Focus Apprentice"
            else -> "Focus Novice"
        }
    }

    // --- NOTIFICATION PREFERENCES ---
    fun getNotificationPreferences(): com.focusflow.app.model.NotificationPreferences {
        return com.focusflow.app.model.NotificationPreferences(
            focusRemindersEnabled = prefs.getBoolean(KEY_NOTIF_FOCUS_REMINDERS, true),
            streakAlertsEnabled = prefs.getBoolean(KEY_NOTIF_STREAK_ALERTS, true),
            dailyDigestEnabled = prefs.getBoolean(KEY_NOTIF_DAILY_DIGEST, true),
            motivationalNudgesEnabled = prefs.getBoolean(KEY_NOTIF_MOTIVATIONAL, true),
            achievementAlertsEnabled = prefs.getBoolean(KEY_NOTIF_ACHIEVEMENTS, true),
            bedtimeReminderEnabled = prefs.getBoolean(KEY_NOTIF_BEDTIME, true),
            focusReminderHour = prefs.getInt(KEY_NOTIF_FOCUS_HOUR, 9),
            focusReminderMinute = prefs.getInt(KEY_NOTIF_FOCUS_MINUTE, 0),
            dailyDigestHour = prefs.getInt(KEY_NOTIF_DIGEST_HOUR, 20),
            dailyDigestMinute = prefs.getInt(KEY_NOTIF_DIGEST_MINUTE, 0),
            quietHoursStart = prefs.getInt(KEY_NOTIF_QUIET_START, 22),
            quietHoursEnd = prefs.getInt(KEY_NOTIF_QUIET_END, 7),
            quietHoursEnabled = prefs.getBoolean(KEY_NOTIF_QUIET_ENABLED, true)
        )
    }

    fun updateNotificationPref(key: String, value: Boolean) {
        val prefKey = when (key) {
            "focus_reminders" -> KEY_NOTIF_FOCUS_REMINDERS
            "streak_alerts" -> KEY_NOTIF_STREAK_ALERTS
            "daily_digest" -> KEY_NOTIF_DAILY_DIGEST
            "motivational_nudges" -> KEY_NOTIF_MOTIVATIONAL
            "achievement_alerts" -> KEY_NOTIF_ACHIEVEMENTS
            "bedtime_reminder" -> KEY_NOTIF_BEDTIME
            "quiet_hours" -> KEY_NOTIF_QUIET_ENABLED
            else -> return
        }
        prefs.edit().putBoolean(prefKey, value).apply()
    }

    fun setDailyDigestTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_NOTIF_DIGEST_HOUR, hour.coerceIn(0, 23))
            .putInt(KEY_NOTIF_DIGEST_MINUTE, minute.coerceIn(0, 59))
            .apply()
    }

    fun getDailyDigestTime(): Pair<Int, Int> {
        return Pair(
            prefs.getInt(KEY_NOTIF_DIGEST_HOUR, 21),
            prefs.getInt(KEY_NOTIF_DIGEST_MINUTE, 0)
        )
    }

    // --- USER PREFERENCES (SETTINGS) ---
    fun getDailyFocusGoal(): Int = prefs.getInt(KEY_DAILY_FOCUS_GOAL, 120)

    fun setDailyFocusGoal(minutes: Int) {
        prefs.edit().putInt(KEY_DAILY_FOCUS_GOAL, minutes.coerceIn(15, 480)).apply()
    }

    fun getHapticEnabled(): Boolean = prefs.getBoolean(KEY_HAPTIC_ENABLED, true)
    fun setHapticEnabled(enabled: Boolean) { prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, enabled).apply() }

    fun getSoundEnabled(): Boolean = prefs.getBoolean(KEY_SOUND_ENABLED, true)
    fun setSoundEnabled(enabled: Boolean) { prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply() }

    // --- SESSION & TIMER ALARM PREFERENCES ---
    fun getTimerAlarmSound(): String = prefs.getString(KEY_TIMER_ALARM_SOUND, "Gentle Chime") ?: "Gentle Chime"
    fun setTimerAlarmSound(sound: String) { prefs.edit().putString(KEY_TIMER_ALARM_SOUND, sound).apply() }

    fun isTimerAlarmEnabled(): Boolean = prefs.getBoolean(KEY_TIMER_ALARM_ENABLED, true)
    fun setTimerAlarmEnabled(enabled: Boolean) { prefs.edit().putBoolean(KEY_TIMER_ALARM_ENABLED, enabled).apply() }

    fun isTimerVibrateEnabled(): Boolean = prefs.getBoolean(KEY_TIMER_VIBRATE_ENABLED, true)
    fun setTimerVibrateEnabled(enabled: Boolean) { prefs.edit().putBoolean(KEY_TIMER_VIBRATE_ENABLED, enabled).apply() }

    fun setCustomLongBreakMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_CUSTOM_LONG_BREAK_MINS, minutes).apply()
    }
    fun getCustomLongBreakMinutes(): Int = prefs.getInt(KEY_CUSTOM_LONG_BREAK_MINS, 15)

    // --- TOTAL COINS EARNED (for achievements) ---
    fun getTotalCoinsEarned(): Int = prefs.getInt(KEY_TOTAL_COINS_EARNED, 100)
    fun addTotalCoinsEarned(amount: Int) {
        val current = getTotalCoinsEarned()
        prefs.edit().putInt(KEY_TOTAL_COINS_EARNED, current + amount).apply()
    }

    // --- HOME SCREEN SHORTCUT & WIDGET PINNING STATUS ---
    fun isShortcutAdded(): Boolean = prefs.getBoolean(KEY_SHORTCUT_ADDED, false)
    fun setShortcutAdded(added: Boolean) { prefs.edit().putBoolean(KEY_SHORTCUT_ADDED, added).apply() }

    fun isWidgetPinned(): Boolean = prefs.getBoolean(KEY_WIDGET_PINNED, false)
    fun setWidgetPinned(pinned: Boolean) { prefs.edit().putBoolean(KEY_WIDGET_PINNED, pinned).apply() }

    companion object {
        private const val KEY_SHORTCUT_ADDED = "key_shortcut_added"
        private const val KEY_WIDGET_PINNED = "key_widget_pinned"
        private const val KEY_ONBOARDING_DONE = "key_onboarding_done"
        private const val KEY_USER_GOAL = "key_user_goal"
        private const val KEY_LOCK_PIN = "key_lock_pin"
        private const val KEY_FOCUS_COINS = "key_focus_coins"
        private const val KEY_SOLVED_CHALLENGES = "key_solved_challenges"
        private const val KEY_BLOCKED_APPS = "key_blocked_apps"
        private const val KEY_BLOCKED_WEBSITES = "key_blocked_websites"
        private const val KEY_STRICT_MODE = "key_strict_mode"
        private const val KEY_SAVED_SCREENTIME = "key_saved_screentime"
        private const val KEY_REELS_BLOCKED_COUNT = "key_reels_blocked_count"
        private const val KEY_INTERVENTIONS_ACCEPTED = "key_interventions_accepted"
        private const val KEY_BLOCK_REELS = "key_block_reels"
        private const val KEY_BLOCK_SHORTS = "key_block_shorts"
        private const val KEY_BLOCK_SPOTLIGHT = "key_block_spotlight"
        private const val KEY_BLOCK_WEB_DOOM = "key_block_web_doom"
        private const val KEY_SOUNDSCAPE = "key_soundscape"
        private const val KEY_TASKS = "key_tasks_json"
        private const val KEY_GOALS = "key_goals_json"
        private const val KEY_HABITS = "key_habits_json"
        private const val KEY_FOCUS_SESSIONS_LOG = "key_focus_sessions_log_json"
        private const val KEY_TOTAL_MINUTES = "key_total_minutes"
        private const val KEY_SESSIONS = "key_completed_sessions"
        private const val KEY_STREAK = "key_streak_days"
        private const val KEY_LONGEST_STREAK = "key_longest_streak_days"
        private const val KEY_STREAK_FREEZES = "key_streak_freezes_count"
        private const val KEY_ACTIVE_DATES_SET = "key_active_dates_set"
        private const val KEY_LAST_ACTIVE_DATE = "key_last_active_date"
        private const val KEY_FOCUS_SCHEDULES = "key_focus_schedules"
        private const val KEY_CATEGORY_BUDGETS = "key_category_budgets"
        private const val KEY_UNLOCKED_STORE_ITEMS = "key_unlocked_store_items"
        private const val KEY_EQUIPPED_THEME = "key_equipped_theme"
        private const val KEY_JIGSAW_THEME = "key_jigsaw_theme"
        private const val KEY_FOCUS_SESSION_ACTIVE = "key_focus_session_active"
        private const val KEY_FOCUS_SESSION_END_MILLIS = "key_focus_session_end_millis"
        private const val KEY_EMERGENCY_PASS_APP = "key_emergency_pass_app"
        private const val KEY_EMERGENCY_PASS_EXPIRY = "key_emergency_pass_expiry"
        private const val KEY_DISABLED_PRODUCTIVE_APPS = "key_disabled_productive_apps"
        private const val KEY_WORK_MODE_ENABLED = "key_work_mode_enabled"
        private const val KEY_WORK_MODE_AUTO_SCHEDULE_ENABLED = "key_work_mode_auto_schedule_enabled"
        private const val KEY_WORK_MODE_START_HOUR = "key_work_mode_start_hour"
        private const val KEY_WORK_MODE_START_MIN = "key_work_mode_start_min"
        private const val KEY_WORK_MODE_END_HOUR = "key_work_mode_end_hour"
        private const val KEY_WORK_MODE_END_MIN = "key_work_mode_end_min"
        private const val KEY_DAILY_PUZZLE_PASS_COUNT = "key_daily_puzzle_pass_count"
        private const val KEY_DAILY_PUZZLE_PASS_DATE = "key_daily_puzzle_pass_date"
        private const val KEY_NOTIFICATION_SHIELD_ENABLED = "key_notification_shield_enabled"
        private const val KEY_BATCHED_NOTIFICATIONS_JSON = "key_batched_notifications_json"
        private const val KEY_BONSAI_STAGE = "key_bonsai_stage"
        private const val KEY_BONSAI_WATER_POINTS = "key_bonsai_water_points"
        private const val KEY_TOTAL_TREES_GROWN = "key_total_trees_grown"
        private const val KEY_SLEEP_TRACKING_STATE = "key_sleep_tracking_state"
        private const val KEY_BLOCKED_APP_PACKAGES_SET = "key_blocked_app_packages_set"
        private const val KEY_CUSTOM_FOCUS_MINS = "key_custom_focus_mins"
        private const val KEY_CUSTOM_BREAK_MINS = "key_custom_break_mins"

        // New keys for achievements, levels, notifications, settings
        private const val KEY_UNLOCKED_ACHIEVEMENTS = "key_unlocked_achievements"
        private const val KEY_USER_LEVEL = "key_user_level"
        private const val KEY_USER_XP = "key_user_xp"
        private const val KEY_USER_TOTAL_XP = "key_user_total_xp"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_USER_BIO = "key_user_bio"
        private const val KEY_USER_AVATAR = "key_user_avatar"
        private const val KEY_USER_JOINED_DATE = "key_user_joined_date"
        private const val KEY_NOTIF_FOCUS_REMINDERS = "key_notif_focus_reminders"
        private const val KEY_NOTIF_STREAK_ALERTS = "key_notif_streak_alerts"
        private const val KEY_NOTIF_DAILY_DIGEST = "key_notif_daily_digest"
        private const val KEY_NOTIF_MOTIVATIONAL = "key_notif_motivational"
        private const val KEY_NOTIF_ACHIEVEMENTS = "key_notif_achievements"
        private const val KEY_NOTIF_BEDTIME = "key_notif_bedtime"
        private const val KEY_NOTIF_FOCUS_HOUR = "key_notif_focus_hour"
        private const val KEY_NOTIF_FOCUS_MINUTE = "key_notif_focus_minute"
        private const val KEY_NOTIF_DIGEST_HOUR = "key_notif_digest_hour"
        private const val KEY_NOTIF_DIGEST_MINUTE = "key_notif_digest_minute"
        private const val KEY_NOTIF_QUIET_START = "key_notif_quiet_start"
        private const val KEY_NOTIF_QUIET_END = "key_notif_quiet_end"
        private const val KEY_NOTIF_QUIET_ENABLED = "key_notif_quiet_enabled"
        private const val KEY_DAILY_FOCUS_GOAL = "key_daily_focus_goal"
        private const val KEY_HAPTIC_ENABLED = "key_haptic_enabled"
        private const val KEY_SOUND_ENABLED = "key_sound_enabled"
        private const val KEY_TIMER_ALARM_SOUND = "key_timer_alarm_sound"
        private const val KEY_TIMER_ALARM_ENABLED = "key_timer_alarm_enabled"
        private const val KEY_TIMER_VIBRATE_ENABLED = "key_timer_vibrate_enabled"
        private const val KEY_CUSTOM_LONG_BREAK_MINS = "key_custom_long_break_mins"
        private const val KEY_TOTAL_COINS_EARNED = "key_total_coins_earned"

        // Regain Clone Keys
        private const val KEY_WEB_SHIELD_SETTINGS_JSON = "key_web_shield_settings_json"
        private const val KEY_ACTIVE_MICRO_SESSION_JSON = "key_active_micro_session_json"
        private const val KEY_MORNING_PROTECTION_ENABLED = "key_morning_protection_enabled"
        private const val KEY_MORNING_WAKE_HOUR = "key_morning_wake_hour"
        private const val KEY_MORNING_WAKE_MINUTE = "key_morning_wake_minute"
        private const val KEY_MORNING_DURATION_MINUTES = "key_morning_duration_minutes"
        private const val KEY_SCROLL_NUDGE_ENABLED = "key_scroll_nudge_enabled"
        private const val KEY_SCROLL_NUDGE_INTERVAL = "key_scroll_nudge_interval"
        private const val KEY_ANTI_TAMPER_SETTINGS = "key_anti_tamper_settings"
        private const val KEY_ANTI_TAMPER_COOLDOWN_MINS = "key_anti_tamper_cooldown_mins"
        private const val KEY_UNBLOCK_COOLDOWN_TIMESTAMP = "key_unblock_cooldown_timestamp"
        private const val KEY_REGAIN_FRICTION_MODE = "key_regain_friction_mode"
        private const val KEY_APP_OPEN_QUOTA_PREFIX = "key_app_open_quota_"
        private const val KEY_APP_OPEN_COUNT_PREFIX = "key_app_open_count_"
        private const val KEY_APP_OPEN_DATE_PREFIX = "key_app_open_date_"
        private const val KEY_REEL_SWIPE_NUDGE_ENABLED = "key_reel_swipe_nudge_enabled"
        private const val KEY_REEL_SWIPE_INTERVAL = "key_reel_swipe_interval"
        private const val KEY_IMPULSE_OPENS_TODAY = "key_impulse_opens_today"
        private const val KEY_INTENTIONAL_PASSES_TODAY = "key_intentional_passes_today"
        private const val KEY_HABIT_RESISTANCE_DATE = "key_habit_resistance_date"
        private const val KEY_FLOATING_PILL_ENABLED = "key_floating_pill_enabled"
        private const val KEY_MASTER_SHIELD_ENABLED = "key_master_shield_enabled"
        private const val KEY_CUSTOM_ALWAYS_ALLOWED_PACKAGES = "key_custom_always_allowed_packages"

        // Built-in Categorized Web Domains
        val ADULT_NSFW_DOMAINS = listOf(
            "pornhub.com", "xvideos.com", "xnxx.com", "redtube.com", "youporn.com",
            "chaturbate.com", "onlyfans.com", "stripchat.com", "cam4.com", "spankbang.com",
            "beeg.com", "eporner.com", "rule34.xxx", "hqporner.com", "tnaflix.com",
            "tube8.com", "brazzers.com", "xhamster.com", "youjizz.com", "livejasmin.com",
            "bongacams.com", "hentaihaven.xxx", "nhentai.net", "danbooru.donmai.us",
            "fapello.com", "coomer.party", "bunkr.is", "erome.com"
        )

        val SOCIAL_WEB_DOMAINS = listOf(
            "instagram.com", "tiktok.com", "twitter.com", "x.com", "reddit.com",
            "facebook.com", "threads.net", "snapchat.com", "tumblr.com", "pinterest.com",
            "weibo.com", "vk.com", "bsky.app"
        )

        val DOOMSCROLL_NEWS_DOMAINS = listOf(
            "dailymail.co.uk", "buzzfeed.com", "tmz.com", "thesun.co.uk", "nypost.com",
            "news.google.com", "huffpost.com", "mirror.co.uk", "drudgereport.com"
        )

        val GAMING_DOMAINS = listOf(
            "twitch.tv", "roblox.com", "ign.com", "steamcommunity.com", "kick.com",
            "miniclip.com", "poki.com", "crazygames.com"
        )
    }
}