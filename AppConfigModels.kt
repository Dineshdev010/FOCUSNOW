package com.focusflow.app.model

enum class AppStage {
    SPLASH,
    ONBOARDING,
    MAIN
}

data class BlockedApp(
    val name: String,
    val packageName: String,
    val category: String,
    val iconEmoji: String,
    val isBlocked: Boolean = true,
    val dailyLimitMinutes: Int = 15,
    val usedMinutesToday: Int = 0,
    val opensToday: Int = 0,
    val maxOpensPerDay: Int = 20,
    val isHardLock: Boolean = false
)

data class CategoryBudget(
    val category: String,
    val emoji: String,
    val dailyLimitMinutes: Int = 60,
    val isEnabled: Boolean = true
)

data class FocusSchedule(
    val id: String,
    val name: String,
    val emoji: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysSummary: String = "Mon - Fri",
    val isEnabled: Boolean = true,
    val blockedCategory: String = "All Distractions",
    val isStrictLock: Boolean = false,
    val targetAppPackages: List<String> = emptyList(),
    val isAlarmEnabled: Boolean = true,
    val alarmSound: String = "Zen Gong",
    val isVibrateEnabled: Boolean = true
) {
    fun matchesDayOfWeek(calendar: java.util.Calendar): Boolean {
        val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        val summary = daysSummary.trim()
        return when {
            summary.isEmpty() || summary.equals("Daily", ignoreCase = true) -> true
            summary.equals("Mon - Fri", ignoreCase = true) || summary.equals("Mon-Fri", ignoreCase = true) ->
                dayOfWeek in java.util.Calendar.MONDAY..java.util.Calendar.FRIDAY
            summary.equals("Mon - Sat", ignoreCase = true) || summary.equals("Mon-Sat", ignoreCase = true) ->
                dayOfWeek != java.util.Calendar.SUNDAY
            summary.equals("Weekends", ignoreCase = true) ->
                dayOfWeek == java.util.Calendar.SATURDAY || dayOfWeek == java.util.Calendar.SUNDAY
            else -> {
                val dayName = when (dayOfWeek) {
                    java.util.Calendar.SUNDAY -> "Sun"
                    java.util.Calendar.MONDAY -> "Mon"
                    java.util.Calendar.TUESDAY -> "Tue"
                    java.util.Calendar.WEDNESDAY -> "Wed"
                    java.util.Calendar.THURSDAY -> "Thu"
                    java.util.Calendar.FRIDAY -> "Fri"
                    java.util.Calendar.SATURDAY -> "Sat"
                    else -> ""
                }
                summary.contains(dayName, ignoreCase = true)
            }
        }
    }

    fun isCurrentlyActive(): Boolean {
        if (!isEnabled) return false
        val now = java.util.Calendar.getInstance()
        val currentMins = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)
        val startMins = startHour * 60 + startMinute
        val endMins = endHour * 60 + endMinute

        return if (startMins <= endMins) {
            matchesDayOfWeek(now) && currentMins in startMins..endMins
        } else {
            // Over midnight (e.g. 22:00 to 07:00)
            if (currentMins >= startMins) {
                matchesDayOfWeek(now)
            } else if (currentMins <= endMins) {
                val yesterday = (now.clone() as java.util.Calendar).apply {
                    add(java.util.Calendar.DAY_OF_YEAR, -1)
                }
                matchesDayOfWeek(yesterday)
            } else {
                false
            }
        }
    }

    fun shouldBlockPackage(pkg: String): Boolean {
        if (!isEnabled || !isCurrentlyActive()) return false
        return if (targetAppPackages.isNotEmpty()) {
            targetAppPackages.contains(pkg)
        } else {
            true // default to blocking all specified distraction apps
        }
    }

    fun formattedTimeRange(): String {
        fun formatTime(h: Int, m: Int): String {
            val amPm = if (h >= 12) "PM" else "AM"
            val hour12 = if (h == 0) 12 else if (h > 12) h - 12 else h
            return String.format(java.util.Locale.US, "%d:%02d %s", hour12, m, amPm)
        }
        return "${formatTime(startHour, startMinute)} - ${formatTime(endHour, endMinute)}"
    }
}

data class ShortFormBlockerConfig(
    val blockInstagramReels: Boolean = true,
    val blockYouTubeShorts: Boolean = true,
    val blockSnapchatSpotlight: Boolean = true,
    val blockWebDoomscrolling: Boolean = true
)

// --- REGAIN CLONE: MICRO-SESSION & FRICTION MODELS ---
enum class RegainFrictionDuration(val seconds: Int, val label: String) {
    QUICK_5S(5, "5s Quick Breather"),
    STANDARD_10S(10, "10s Mindful Reset"),
    DEEP_15S(15, "15s Deep Reset");

    companion object {
        val SECONDS_5 = QUICK_5S
        val SECONDS_10 = STANDARD_10S
        val SECONDS_15 = DEEP_15S
    }
}

data class ActiveMicroSession(
    val packageName: String,
    val appName: String,
    val allottedMinutes: Int,
    val startTimestamp: Long,
    val expiresAtTimestamp: Long,
    val intention: String = "Quick Task"
) {
    val grantedMinutes: Int get() = allottedMinutes
    val startTimeTimestamp: Long get() = startTimestamp

    val isExpired: Boolean get() = System.currentTimeMillis() >= expiresAtTimestamp
    fun isExpired(now: Long = System.currentTimeMillis()): Boolean = now >= expiresAtTimestamp

    val remainingSeconds: Int get() = ((expiresAtTimestamp - System.currentTimeMillis()) / 1000).coerceAtLeast(0L).toInt()
    fun remainingSeconds(now: Long = System.currentTimeMillis()): Int = ((expiresAtTimestamp - now).coerceAtLeast(0) / 1000).toInt()

    val formattedRemaining: String get() {
        val s = remainingSeconds
        val m = s / 60
        val remSec = s % 60
        return String.format(java.util.Locale.US, "%d:%02d", m, remSec)
    }
    fun formattedRemaining(now: Long = System.currentTimeMillis()): String = formattedRemaining
}

// --- REGAIN CLONE: ADVANCED WEBSITE BLOCKING MODELS ---
enum class WebBlockCategory(val id: String, val title: String, val emoji: String, val description: String) {
    ADULT_NSFW("adult_nsfw", "18+ Adult & NSFW Shield", "🔞", "Blocks pornography, adult cams, and NSFW imageboards"),
    SOCIAL_WEB("social_web", "Social Media Web", "📱", "Blocks Instagram, TikTok, Reddit, X/Twitter, Threads, Facebook web"),
    DOOMSCROLL_NEWS("doomscroll_news", "Outrage News & Tabloids", "📰", "Blocks clickbait, sensationalist news, and doomscrolling portals"),
    GAMING_STREAMING("gaming_streaming", "Gaming & Streams", "🎮", "Blocks Twitch, web games, and casual game portals")
}

data class WebShieldSettings(
    val blockAdultNsfw: Boolean = true,
    val blockSocialWeb: Boolean = true,
    val blockDoomscrollNews: Boolean = false,
    val blockGamingStreaming: Boolean = false,
    val blockIncognitoMode: Boolean = true,
    val customBlockedDomains: List<String> = emptyList(),
    val allowedWhitelistedDomains: List<String> = emptyList()
) {
    val blockAdultContent: Boolean get() = blockAdultNsfw
    val blockGaming: Boolean get() = blockGamingStreaming
    val whitelistedDomains: List<String> get() = allowedWhitelistedDomains
}

// --- REGAIN CLONE: MORNING PROTECTION, SCROLL NUDGES & ANTI-TAMPER ---
data class MorningProtectionConfig(
    val isEnabled: Boolean = true,
    val wakeHour: Int = 7,
    val wakeMinute: Int = 0,
    val durationMinutes: Int = 30 // Blocks social feeds for 30m after wake up
) {
    val wakeUpHour: Int get() = wakeHour
    val wakeUpMinute: Int get() = wakeMinute
    val shieldDurationMinutes: Int get() = durationMinutes

    fun isCurrentlyActive(): Boolean {
        if (!isEnabled) return false
        val now = java.util.Calendar.getInstance()
        val currentMins = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)
        val startMins = wakeHour * 60 + wakeMinute
        val endMins = startMins + durationMinutes
        return currentMins in startMins..endMins
    }

    fun isMorningShieldActive(): Boolean = isCurrentlyActive()

    fun formattedRange(): String {
        val endMinsTotal = wakeHour * 60 + wakeMinute + durationMinutes
        val endH = (endMinsTotal / 60) % 24
        val endM = endMinsTotal % 60
        fun fmt(h: Int, m: Int): String {
            val amPm = if (h >= 12) "PM" else "AM"
            val hour12 = if (h == 0) 12 else if (h > 12) h - 12 else h
            return String.format(java.util.Locale.US, "%d:%02d %s", hour12, m, amPm)
        }
        return "${fmt(wakeHour, wakeMinute)} - ${fmt(endH, endM)}"
    }

    fun formattedMorningWindow(): String = formattedRange()
}

data class ScrollNudgeConfig(
    val isEnabled: Boolean = true,
    val nudgeIntervalMinutes: Int = 10 // Pops up intervention after 10m continuous scrolling
)

data class RegainAntiTamperConfig(
    val preventSettingsAccess: Boolean = true, // Prevents disabling accessibility service in settings during focus/curfew
    val unblockCooldownMinutes: Int = 5 // 5m cool-off reflection period before allowing app/site unblocking
) {
    val cooldownOnRuleChangeMinutes: Int get() = unblockCooldownMinutes
}

// --- REGAIN CLONE: ADVANCED FRICTION MODES ---
enum class RegainFrictionMode(val id: String, val title: String, val emoji: String, val description: String) {
    BREATHING("breathing", "Mindful Breathing", "🫁", "5s unskippable 4-7-8 breathing circle"),
    PLEDGE_TYPING("pledge_typing", "Intentional Pledge", "✍️", "Type a conscious commitment sentence to unlock"),
    PHONE_FLIP("phone_flip", "Physical Phone Flip", "📱", "Place your phone face down for 5 seconds to unlock")
}

// --- REGAIN CLONE: DAILY APP OPEN QUOTA ---
data class AppOpenQuota(
    val packageName: String,
    val maxDailyOpens: Int = 5, // 0 = unlimited
    val currentOpensToday: Int = 0
) {
    val isExceeded: Boolean get() = maxDailyOpens > 0 && currentOpensToday >= maxDailyOpens
    val remainingOpens: Int get() = if (maxDailyOpens > 0) (maxDailyOpens - currentOpensToday).coerceAtLeast(0) else 999
}

// --- REGAIN CLONE: REEL SWIPE COUNTER NUDGE ---
data class ReelSwipeNudgeConfig(
    val isEnabled: Boolean = true,
    val swipeInterval: Int = 10 // Nudges after every 10 short-form video scrolls
)

// --- REGAIN CLONE: IMPULSIVE VS INTENTIONAL HABIT RESISTANCE STATS ---
data class HabitResistanceStats(
    val impulseOpensIntercepted: Int = 0,
    val intentionalPassesGranted: Int = 0
) {
    val totalAttempts: Int get() = impulseOpensIntercepted + intentionalPassesGranted
    val resistanceRate: Int get() = if (totalAttempts > 0) ((impulseOpensIntercepted * 100) / totalAttempts).coerceIn(0, 100) else 100
}

// --- REGAIN CLONE: FLOATING LIVE TIMER PILL ---
data class FloatingPillConfig(
    val isEnabled: Boolean = true,
    val showRemainingTime: Boolean = true
)


data class ProductiveAppItem(
    val id: String,
    val name: String,
    val packageName: String,
    val emoji: String,
    val category: String,
    val isEnabled: Boolean = true,
    val isEssential: Boolean = false,
    val isWorkModeOfficeApp: Boolean = false
)

data class BatchedNotification(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val timestamp: Long,
    val emoji: String = "🔔",
    val category: String = "Distraction"
)

enum class FocusSoundscape(val title: String, val emoji: String, val subtitle: String) {
    SILENT("Silence", "\uD83D\uDD07", "Pure Quiet"),
    RAIN("Rainfall", "\uD83C\uDF27\uFE0F", "Procedural Brown Noise"),
    FOREST("Zen Forest", "\uD83C\uDF32", "Deep Calm Ambience"),
    WAVES("Ocean Waves", "\uD83C\uDF0A", "Rhythmic Flow Waves"),
    BINAURAL_BEATS("40Hz Gamma", "\uD83E\uDDE0", "Binaural Focus Beat")
}

enum class StoreCategory {
    THEMES,
    SOUNDS,
    PUZZLES
}

data class StoreItem(
    val id: String,
    val title: String,
    val category: StoreCategory,
    val cost: Int,
    val iconEmoji: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val isEquipped: Boolean = false
)

enum class JigsawTheme(val title: String, val emoji: String, val description: String) {
    AURORA("Nordic Aurora", "🌌", "Glacial scenic mountain lights"),
    ZEN_GARDEN("Zen Garden", "🌿", "Calming moss stone sanctuary"),
    CYBERPUNK("Cyber Matrix", "⚡", "Futuristic neon gridlines"),
    RUNES("Focus Runes", "🔢", "Classic tactical glyph numbers"),
    COSMIC("Deep Cosmic", "🚀", "Galaxies, nebulae & stellar objects"),
    RETRO_ARCADE("8-Bit Arcade", "👾", "Vintage pixel gems & relics")
}

enum class PuzzleType(val title: String, val emoji: String, val subtitle: String) {
    PHOTO_JIGSAW("Real Jigsaw", "🧩", "Snap pieces into authentic photo board"),
    SLIDING_JIGSAW("Sliding Tiles", "🔢", "Slide tiles into numeric order"),
    FLOW_CONNECT("Zen Flow", "🌐", "Connect energy nodes without crossing"),
    SUDOKU_MINI("Focus Sudoku", "🎯", "4x4 mindful logic grid"),
    MEMORY_MATCH("Memory Flip", "🧠", "Match hidden pairs"),
    WORD_UNSCRAMBLE("Word Focus", "🔤", "Unscramble mindful words"),
    MATH_REFLEX("Math Sprint", "⚡", "Rapid mental arithmetic"),
    TYPING_TEST("Speed Focus", "⌨️", "Mindful typing affirmation")
}

data class ZenBonsaiStatus(
    val stage: Int = 3,
    val title: String = "Deep Focus Evergreen",
    val emoji: String = "🪴",
    val waterPoints: Int = 65,
    val maxWaterPoints: Int = 100,
    val totalTreesGrown: Int = 4,
    val healthPercent: Int = 98
)

data class OpportunityCostMetric(
    val domain: String,
    val emoji: String,
    val value: String,
    val unit: String,
    val description: String
)

data class MindfulIntention(
    val label: String,
    val emoji: String,
    val isHabitual: Boolean
)

data class CodingChallenge(
    val id: Int,
    val language: String,
    val difficulty: String,
    val question: String,
    val codeSnippet: String? = null,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val coinReward: Int = 25
)

object CodingChallengeCatalog {
    val sampleChallenges = listOf(
        CodingChallenge(
            id = 1,
            language = "Python",
            difficulty = "Easy",
            question = "What will be the output of this Python code?",
            codeSnippet = "numbers = [1, 2, 3]\nprint(len(numbers) * 2)",
            options = listOf("3", "6", "5", "Error"),
            correctIndex = 1,
            explanation = "len(numbers) is 3. 3 * 2 equals 6.",
            coinReward = 25
        ),
        CodingChallenge(
            id = 2,
            language = "Kotlin",
            difficulty = "Easy",
            question = "Which keyword declares a read-only immutable variable in Kotlin?",
            codeSnippet = "___ language = \"Kotlin\"",
            options = listOf("var", "val", "const", "let"),
            correctIndex = 1,
            explanation = "In Kotlin, 'val' declares a read-only immutable variable reference.",
            coinReward = 25
        ),
        CodingChallenge(
            id = 3,
            language = "JavaScript",
            difficulty = "Medium",
            question = "What does typeof [1, 2, 3] return in JavaScript?",
            codeSnippet = "typeof [1, 2, 3]",
            options = listOf("\"array\"", "\"object\"", "\"list\"", "\"undefined\""),
            correctIndex = 1,
            explanation = "In JavaScript, arrays are technically objects, so typeof returns 'object'.",
            coinReward = 30
        ),
        CodingChallenge(
            id = 4,
            language = "Algorithms",
            difficulty = "Easy",
            question = "What is the time complexity of searching a sorted array using Binary Search?",
            codeSnippet = null,
            options = listOf("O(1)", "O(n)", "O(log n)", "O(n\u00B2)"),
            correctIndex = 2,
            explanation = "Binary Search cuts the search space in half each iteration, giving O(log n) logarithmic complexity.",
            coinReward = 30
        ),
        CodingChallenge(
            id = 5,
            language = "Python",
            difficulty = "Medium",
            question = "What does the list comprehension [x**2 for x in range(3)] produce?",
            codeSnippet = "[x**2 for x in range(3)]",
            options = listOf("[0, 1, 4]", "[1, 4, 9]", "[0, 1, 2]", "[1, 2, 3]"),
            correctIndex = 0,
            explanation = "range(3) produces 0, 1, 2. Squaring them results in [0, 1, 4].",
            coinReward = 35
        ),
        CodingChallenge(
            id = 6,
            language = "SQL",
            difficulty = "Easy",
            question = "Which SQL clause is used to filter records after aggregation?",
            codeSnippet = "SELECT dept, COUNT(*) FROM emp GROUP BY dept ___ COUNT(*) > 5",
            options = listOf("WHERE", "HAVING", "FILTER", "LIMIT"),
            correctIndex = 1,
            explanation = "HAVING filters aggregated grouped results, whereas WHERE filters row-level records before grouping.",
            coinReward = 30
        )
    )
}

data class FocusFlowIndexMetric(
    val overallScore: Int = 92,
    val distractionResistance: Int = 96,
    val deepWorkIntensity: Int = 89,
    val goalExecution: Int = 100,
    val statusTitle: String = "Peak Dopamine Equilibrium",
    val statusSubtitle: String = "Your prefrontal cortex control is in the top 5% of digital creators this week."
)

data class ScreenTimeComposition(
    val productivePercent: Float = 0.68f,
    val communicationPercent: Float = 0.20f,
    val distractingPercent: Float = 0.12f,
    val productiveHours: String = "4h 15m",
    val savedHours: String = "2h 45m",
    val doomscrollReductionPercent: Int = 74
)

data class ChronotypeHourlySlot(
    val hour: Int,
    val label: String,
    val intensityRatio: Float,
    val isPeak: Boolean = false,
    val isVulnerable: Boolean = false
)

data class LifetimeImpactStats(
    val totalReclaimedHours: Float = 38.5f,
    val shortFormInterceptions: Int = 142,
    val zenTreesGrown: Int = 4,
    val puzzlesAndCodeSolved: Int = 28
)

data class AIFocusInsight(
    val id: String,
    val emoji: String,
    val category: String,
    val title: String,
    val message: String,
    val actionableTip: String
)

data class SleepStageBreakdown(
    val deepMinutes: Int = 105,
    val remMinutes: Int = 120,
    val lightMinutes: Int = 210,
    val awakeMinutes: Int = 33
) {
    val totalDurationMinutes: Int get() = deepMinutes + remMinutes + lightMinutes
    val totalInBedMinutes: Int get() = totalDurationMinutes + awakeMinutes
    val formattedDuration: String get() {
        val h = totalDurationMinutes / 60
        val m = totalDurationMinutes % 60
        return "${h}h ${m}m"
    }
}

data class DailySleepRecord(
    val dayOfWeek: String,
    val dateStr: String,
    val durationHours: Float,
    val score: Int,
    val bedtime: String,
    val wakeTime: String,
    val stages: SleepStageBreakdown = SleepStageBreakdown()
)

data class SleepTrackingState(
    val bedtimeCurfewEnabled: Boolean = true,
    val curfewStartHour: Int = 22,
    val curfewStartMinute: Int = 30,
    val curfewEndHour: Int = 7,
    val curfewEndMinute: Int = 0,
    val targetDurationHours: Float = 8.0f,
    val lastNightScore: Int = 88,
    val lastNightDuration: String = "7h 48m",
    val lastBedtime: String = "10:45 PM",
    val lastWakeTime: String = "06:33 AM",
    val sleepDebtMinutes: Int = -12,
    val doomscrollInterceptionsPast11Pm: Int = 4,
    val stages: SleepStageBreakdown = SleepStageBreakdown(),
    val weeklySleepHistory: List<DailySleepRecord> = listOf(
        DailySleepRecord("Mon", "Oct 09", 7.5f, 84, "11:10 PM", "06:40 AM"),
        DailySleepRecord("Tue", "Oct 10", 8.0f, 92, "10:45 PM", "06:45 AM"),
        DailySleepRecord("Wed", "Oct 11", 6.8f, 76, "11:55 PM", "06:45 AM"),
        DailySleepRecord("Thu", "Oct 12", 7.8f, 89, "10:50 PM", "06:40 AM"),
        DailySleepRecord("Fri", "Oct 13", 8.2f, 95, "11:00 PM", "07:15 AM"),
        DailySleepRecord("Sat", "Oct 14", 8.5f, 96, "11:30 PM", "08:00 AM"),
        DailySleepRecord("Sun", "Oct 15", 7.8f, 88, "10:45 PM", "06:33 AM")
    )
) {
    fun isCurfewCurrentlyActive(): Boolean {
        if (!bedtimeCurfewEnabled) return false
        val now = java.util.Calendar.getInstance()
        val currentMins = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)
        val startMins = curfewStartHour * 60 + curfewStartMinute
        val endMins = curfewEndHour * 60 + curfewEndMinute

        return if (startMins <= endMins) {
            currentMins in startMins..endMins
        } else {
            currentMins >= startMins || currentMins <= endMins
        }
    }

    fun formattedCurfewRange(): String {
        fun formatTime(h: Int, m: Int): String {
            val amPm = if (h >= 12) "PM" else "AM"
            val hour12 = if (h == 0) 12 else if (h > 12) h - 12 else h
            return String.format(java.util.Locale.US, "%d:%02d %s", hour12, m, amPm)
        }
        return "${formatTime(curfewStartHour, curfewStartMinute)} - ${formatTime(curfewEndHour, curfewEndMinute)}"
    }
}

data class AppShieldRuleItem(
    val packageName: String,
    val appName: String,
    val category: String,
    val iconEmoji: String,
    val isBlocked: Boolean,
    val dailyLimitMinutes: Int = 15,
    val isInstalled: Boolean = true
)

data class DailyFocusComparison(
    val day: String,
    val focusHours: Float,
    val distractionHours: Float
)

data class CategoryShare(
    val name: String,
    val percentage: Float,
    val formattedDuration: String,
    val colorHex: Long,
    val iconEmoji: String
)

data class HeatmapDay(
    val dayOfMonth: Int,
    val intensityLevel: Int, // 0 to 4
    val focusMinutes: Int,
    val isToday: Boolean = false
)

data class DoomscrollDefenseStat(
    val platform: String,
    val iconEmoji: String,
    val count: Int,
    val percent: Float,
    val savedHours: String,
    val colorHex: Long
)

data class DopamineTrendPoint(
    val label: String,
    val score: Int
)

// --- ACHIEVEMENT & GAMIFICATION SYSTEM ---
enum class AchievementTier(val title: String, val emoji: String) {
    BRONZE("Bronze", "🥉"),
    SILVER("Silver", "🥈"),
    GOLD("Gold", "🥇"),
    DIAMOND("Diamond", "💎"),
    LEGENDARY("Legendary", "👑")
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val tier: AchievementTier,
    val category: String,
    val targetValue: Int,
    val currentValue: Int = 0,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0L,
    val coinReward: Int = 50
) {
    val progressPercent: Float get() = (currentValue.toFloat() / targetValue.coerceAtLeast(1)).coerceIn(0f, 1f)
}

data class UserLevel(
    val level: Int = 1,
    val title: String = "Focus Novice",
    val currentXp: Int = 0,
    val xpToNextLevel: Int = 100,
    val totalXp: Int = 0
) {
    val progressPercent: Float get() = (currentXp.toFloat() / xpToNextLevel.coerceAtLeast(1)).coerceIn(0f, 1f)
}

data class DailyChallenge(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val targetValue: Int,
    val currentValue: Int = 0,
    val xpReward: Int = 50,
    val coinReward: Int = 25,
    val isCompleted: Boolean = false,
    val expiresAt: Long = 0L
) {
    val progressPercent: Float get() = (currentValue.toFloat() / targetValue.coerceAtLeast(1)).coerceIn(0f, 1f)
}

data class WeeklyChallenge(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val targetValue: Int,
    val currentValue: Int = 0,
    val xpReward: Int = 200,
    val coinReward: Int = 100,
    val isCompleted: Boolean = false
) {
    val progressPercent: Float get() = (currentValue.toFloat() / targetValue.coerceAtLeast(1)).coerceIn(0f, 1f)
}

// --- NOTIFICATION PREFERENCES ---
data class NotificationPreferences(
    val focusRemindersEnabled: Boolean = true,
    val streakAlertsEnabled: Boolean = true,
    val dailyDigestEnabled: Boolean = true,
    val motivationalNudgesEnabled: Boolean = true,
    val achievementAlertsEnabled: Boolean = true,
    val bedtimeReminderEnabled: Boolean = true,
    val focusReminderHour: Int = 9,
    val focusReminderMinute: Int = 0,
    val dailyDigestHour: Int = 20,
    val dailyDigestMinute: Int = 0,
    val quietHoursStart: Int = 22,
    val quietHoursEnd: Int = 7,
    val quietHoursEnabled: Boolean = true
)

// --- USER PREFERENCES (SETTINGS) ---
data class UserPreferences(
    val dailyFocusGoalMinutes: Int = 120,
    val autoStartStrict: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val showMotivationalQuotes: Boolean = true,
    val defaultFocusDuration: Int = 25,
    val defaultShortBreak: Int = 5,
    val defaultLongBreak: Int = 15,
    val pomodoroAutoSwitch: Boolean = false,
    val dataExportEnabled: Boolean = true
)

object AchievementCatalog {
    val allAchievements = listOf(
        // Focus Session Achievements
        Achievement("ach_first_focus", "First Focus", "Complete your first focus session", "🎯", AchievementTier.BRONZE, "Focus", 1, coinReward = 25),
        Achievement("ach_focus_10", "Focus Adept", "Complete 10 focus sessions", "⚡", AchievementTier.SILVER, "Focus", 10, coinReward = 50),
        Achievement("ach_focus_50", "Focus Master", "Complete 50 focus sessions", "🔥", AchievementTier.GOLD, "Focus", 50, coinReward = 100),
        Achievement("ach_focus_100", "Focus Legend", "Complete 100 focus sessions", "💎", AchievementTier.DIAMOND, "Focus", 100, coinReward = 250),
        Achievement("ach_focus_500", "Zen Architect", "Complete 500 focus sessions", "👑", AchievementTier.LEGENDARY, "Focus", 500, coinReward = 500),
        
        // Streak Achievements
        Achievement("ach_streak_3", "Streak Starter", "Maintain a 3-day focus streak", "🔗", AchievementTier.BRONZE, "Streaks", 3, coinReward = 30),
        Achievement("ach_streak_7", "Week Warrior", "Maintain a 7-day focus streak", "📅", AchievementTier.SILVER, "Streaks", 7, coinReward = 75),
        Achievement("ach_streak_30", "Monthly Machine", "Maintain a 30-day focus streak", "🗓️", AchievementTier.GOLD, "Streaks", 30, coinReward = 200),
        Achievement("ach_streak_100", "Centurion", "Maintain a 100-day focus streak", "🏛️", AchievementTier.DIAMOND, "Streaks", 100, coinReward = 500),
        
        // Distraction Defense Achievements
        Achievement("ach_block_10", "Shield Bearer", "Block 10 distracting app attempts", "🛡️", AchievementTier.BRONZE, "Defense", 10, coinReward = 25),
        Achievement("ach_block_50", "Fortress Wall", "Block 50 distracting app attempts", "🏰", AchievementTier.SILVER, "Defense", 50, coinReward = 75),
        Achievement("ach_block_200", "Iron Will", "Block 200 distracting app attempts", "⚔️", AchievementTier.GOLD, "Defense", 200, coinReward = 150),
        
        // Screen Time Saved
        Achievement("ach_save_60", "Hour Saver", "Save 1 hour of screen time", "⏰", AchievementTier.BRONZE, "Time", 60, coinReward = 30),
        Achievement("ach_save_300", "Time Alchemist", "Save 5 hours of screen time", "⌛", AchievementTier.SILVER, "Time", 300, coinReward = 100),
        Achievement("ach_save_1200", "Time Lord", "Save 20 hours of screen time", "🕰️", AchievementTier.GOLD, "Time", 1200, coinReward = 250),
        
        // Puzzles & Challenges
        Achievement("ach_puzzle_5", "Puzzle Rookie", "Solve 5 puzzles or coding challenges", "🧩", AchievementTier.BRONZE, "Puzzles", 5, coinReward = 25),
        Achievement("ach_puzzle_25", "Brain Athlete", "Solve 25 puzzles or coding challenges", "🧠", AchievementTier.SILVER, "Puzzles", 25, coinReward = 75),
        Achievement("ach_puzzle_100", "Puzzle Grandmaster", "Solve 100 puzzles or coding challenges", "🏆", AchievementTier.GOLD, "Puzzles", 100, coinReward = 200),
        
        // Bonsai Achievements
        Achievement("ach_bonsai_1", "Seed Planter", "Grow your first Zen Bonsai", "🌱", AchievementTier.BRONZE, "Bonsai", 1, coinReward = 30),
        Achievement("ach_bonsai_5", "Garden Keeper", "Grow 5 Zen Bonsai trees", "🪴", AchievementTier.SILVER, "Bonsai", 5, coinReward = 100),
        Achievement("ach_bonsai_10", "Forest Spirit", "Grow 10 Zen Bonsai trees", "🌳", AchievementTier.GOLD, "Bonsai", 10, coinReward = 250),
        
        // Coin Economy
        Achievement("ach_coins_500", "Coin Collector", "Earn 500 Focus Coins total", "🪙", AchievementTier.SILVER, "Economy", 500, coinReward = 50),
        Achievement("ach_coins_2000", "Coin Magnate", "Earn 2000 Focus Coins total", "💰", AchievementTier.GOLD, "Economy", 2000, coinReward = 150),
        
        // Walk Away Achievements
        Achievement("ach_walkaway_5", "Mindful Walker", "Walk away from 5 temptations", "🚶", AchievementTier.BRONZE, "Mindful", 5, coinReward = 25),
        Achievement("ach_walkaway_25", "Zen Master", "Walk away from 25 temptations", "🧘", AchievementTier.SILVER, "Mindful", 25, coinReward = 100),
        Achievement("ach_walkaway_100", "Dopamine Sage", "Walk away from 100 temptations", "🌀", AchievementTier.GOLD, "Mindful", 100, coinReward = 250)
    )
}

data class WorkModeScheduleConfig(
    val isAutoScheduleEnabled: Boolean = false,
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val endHour: Int = 17,
    val endMinute: Int = 0,
    val activeDays: Set<Int> = setOf(
        java.util.Calendar.MONDAY,
        java.util.Calendar.TUESDAY,
        java.util.Calendar.WEDNESDAY,
        java.util.Calendar.THURSDAY,
        java.util.Calendar.FRIDAY
    )
) {
    fun isCurrentlyActive(): Boolean {
        if (!isAutoScheduleEnabled) return false
        val now = java.util.Calendar.getInstance()
        val currentDay = now.get(java.util.Calendar.DAY_OF_WEEK)
        if (!activeDays.contains(currentDay)) return false

        val currentMins = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)
        val startMins = startHour * 60 + startMinute
        val endMins = endHour * 60 + endMinute

        return if (startMins <= endMins) {
            currentMins in startMins..endMins
        } else {
            currentMins >= startMins || currentMins <= endMins
        }
    }

    fun formattedRange(): String {
        fun formatTime(h: Int, m: Int): String {
            val amPm = if (h >= 12) "PM" else "AM"
            val h12 = if (h == 0) 12 else if (h > 12) h - 12 else h
            return String.format(java.util.Locale.US, "%d:%02d %s", h12, m, amPm)
        }
        return "${formatTime(startHour, startMinute)} – ${formatTime(endHour, endMinute)}"
    }
}

object NeuroscienceInsights {
    val quotes = listOf(
        "🧠 Prefrontal Resistance: Multitasking reduces operational IQ by 10 points. Staying in Teams & Outlook protects cognitive reserve.",
        "⚡ Attention Residue: When you switch from work to social feeds, your brain takes an average of 23 minutes to regain deep focus.",
        "🎯 The Flow Channel: Peak productivity only occurs after 15 minutes of uninterrupted immersion in a single task.",
        "🔬 Dopamine Detox: By solving a mindful puzzle instead of seeking an easy dopamine hit, you rewire your brain's reward pathway.",
        "💡 Deep Work Principle: High-value work requires zero context switching. Protect your working memory like gold.",
        "🛡️ Neural Plasticity: Resisting impulse unlocks today makes tomorrow's self-control 40% easier."
    )

    fun getRandomInsight(): String = quotes.random()
}

enum class FrictionTier(val label: String, val badge: String, val description: String) {
    STANDARD("Standard Friction", "Tier 1: Calm", "Standard 3x3 challenge"),
    CHALLENGER("Prefrontal Effort", "Tier 2: Intermediate", "+20% cognitive friction"),
    MASTERY("Mastery Resistance", "Tier 3: Advanced", "Maximum impulse deterrence")
}

data class ProgressiveFrictionStatus(
    val passesUsedToday: Int,
    val currentTier: FrictionTier
)

data class AlwaysAllowedAppItem(
    val id: String,
    val name: String,
    val packageName: String,
    val iconEmoji: String,
    val category: String,
    val isAlwaysAllowed: Boolean,
    val isEssential: Boolean = false
)