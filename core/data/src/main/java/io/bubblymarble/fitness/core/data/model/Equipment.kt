package io.bubblymarble.fitness.core.data.model

/**
 * Canonical list of equipment slugs. Exercise records use these literal values in their
 * `equipment` field, the onboarding checklist uses them to record what the user owns,
 * and the plan generator filters on them. A single source of truth keeps the slugs
 * consistent across DB, UI, and AI prompt.
 */
object Equipment {
    const val BODYWEIGHT = "bodyweight"
    const val DUMBBELL = "dumbbell"
    const val BARBELL = "barbell"
    const val EZ_BAR = "ez-bar"
    const val KETTLEBELL = "kettlebell"
    const val BENCH = "bench"
    const val PULL_UP_BAR = "pull-up-bar"
    const val DIP_BARS = "dip-bars"
    const val RESISTANCE_BANDS = "bands"
    const val JUMP_ROPE = "jump-rope"
    const val YOGA_MAT = "mat"
    const val FOAM_ROLLER = "foam-roller"
    const val MEDICINE_BALL = "medicine-ball"
    const val GYM_BALL = "gym-ball"
    const val AB_WHEEL = "ab-wheel"
    const val SUSPENSION_TRAINER = "suspension-trainer"
    const val BOX = "plyo-box"
    const val CABLE = "cable"
    const val MACHINE = "machine"
    const val TREADMILL = "treadmill"
    const val STATIONARY_BIKE = "bike"
    const val ROWER = "rower"

    /** Items shown in the onboarding home-equipment checklist. */
    val homeChecklist: List<EquipmentItem> = listOf(
        EquipmentItem(DUMBBELL, "Dumbbells"),
        EquipmentItem(BARBELL, "Barbell + plates"),
        EquipmentItem(EZ_BAR, "EZ-bar / curl bar"),
        EquipmentItem(KETTLEBELL, "Kettlebell(s)"),
        EquipmentItem(BENCH, "Bench (flat / adjustable)"),
        EquipmentItem(PULL_UP_BAR, "Pull-up bar"),
        EquipmentItem(DIP_BARS, "Dip bars / parallettes"),
        EquipmentItem(RESISTANCE_BANDS, "Resistance bands"),
        EquipmentItem(JUMP_ROPE, "Jump rope"),
        EquipmentItem(YOGA_MAT, "Yoga / exercise mat"),
        EquipmentItem(FOAM_ROLLER, "Foam roller"),
        EquipmentItem(MEDICINE_BALL, "Medicine ball"),
        EquipmentItem(GYM_BALL, "Swiss / gym ball"),
        EquipmentItem(AB_WHEEL, "Ab wheel"),
        EquipmentItem(SUSPENSION_TRAINER, "TRX / suspension trainer"),
        EquipmentItem(BOX, "Plyo box / step"),
        EquipmentItem(TREADMILL, "Treadmill"),
        EquipmentItem(STATIONARY_BIKE, "Stationary bike"),
        EquipmentItem(ROWER, "Rowing machine"),
    )

    /** Bodyweight is always implicitly available. Used by `EquipmentAccess.BODYWEIGHT_ONLY`. */
    val bodyweightOnly: Set<String> = setOf(BODYWEIGHT)

    /** Everything is available. Used by `EquipmentAccess.FULL_GYM`. */
    val fullGym: Set<String> = setOf(BODYWEIGHT) + homeChecklist.map { it.slug } + setOf(CABLE, MACHINE)

    fun resolve(access: EquipmentAccess, owned: Set<String>): Set<String> = when (access) {
        EquipmentAccess.BODYWEIGHT_ONLY -> bodyweightOnly
        EquipmentAccess.MINIMAL_HOME -> owned + BODYWEIGHT // bodyweight is always available
        EquipmentAccess.FULL_GYM -> fullGym
    }
}

data class EquipmentItem(val slug: String, val label: String)
