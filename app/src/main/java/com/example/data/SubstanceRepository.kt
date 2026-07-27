package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ============================================================================
// ROOM ENTITIES FOR SUBSTANCES, EFFECTS, RISKS, AND HARM REDUCTION TIPS
// ============================================================================

/**
 * Represents a substance's individual effect (e.g. Euphoria, Jaw Clenching, Nausea).
 */
@Entity(tableName = "substance_effects")
data class SubstanceEffectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val substanceId: String,
    val effectType: String, // e.g., "PHYSICAL", "COGNITIVE", "VISUAL", "AUDITORY", "AFTER_EFFECTS"
    val title: String,
    val description: String,
    val intensity: String // e.g., "MILD", "MODERATE", "STRONG", "VARIABLE"
)

/**
 * Represents health risks, physiological hazards, or lethal drug interactions.
 */
@Entity(tableName = "substance_risks")
data class SubstanceRiskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val substanceId: String,
    val riskLevel: String, // e.g., "LETHAL", "HIGH", "MODERATE", "CAUTION"
    val title: String,
    val description: String,
    val mitigationNote: String
)

/**
 * Represents actionable harm reduction advice, set & setting guidelines, or testing protocols.
 */
@Entity(tableName = "harm_reduction_tips")
data class HarmReductionTipEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val substanceId: String,
    val category: String, // e.g., "DOSING", "SET_AND_SETTING", "REAGENT_TESTING", "RECOVERY", "HYDRATION", "GENERAL"
    val tipText: String,
    val importanceLevel: Int = 3 // Scale 1 (Low) to 5 (Critical)
)

/**
 * Room Relation data class grouping a Substance with its associated Effects, Risks, and Harm Reduction Tips.
 */
data class SubstanceWithDetails(
    @Embedded val substance: SubstanceEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "substanceId"
    )
    val effects: List<SubstanceEffectEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "substanceId"
    )
    val risks: List<SubstanceRiskEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "substanceId"
    )
    val harmReductionTips: List<HarmReductionTipEntity>
)

// ============================================================================
// DAOS (DATA ACCESS OBJECTS)
// ============================================================================

@Dao
interface SubstanceEffectDao {
    @Query("SELECT * FROM substance_effects WHERE substanceId = :substanceId")
    fun getEffectsForSubstance(substanceId: String): Flow<List<SubstanceEffectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEffect(effect: SubstanceEffectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEffects(effects: List<SubstanceEffectEntity>)

    @Query("DELETE FROM substance_effects WHERE substanceId = :substanceId")
    suspend fun deleteEffectsForSubstance(substanceId: String)
}

@Dao
interface SubstanceRiskDao {
    @Query("SELECT * FROM substance_risks WHERE substanceId = :substanceId")
    fun getRisksForSubstance(substanceId: String): Flow<List<SubstanceRiskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRisk(risk: SubstanceRiskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRisks(risks: List<SubstanceRiskEntity>)

    @Query("DELETE FROM substance_risks WHERE substanceId = :substanceId")
    suspend fun deleteRisksForSubstance(substanceId: String)
}

@Dao
interface HarmReductionTipDao {
    @Query("SELECT * FROM harm_reduction_tips WHERE substanceId = :substanceId ORDER BY importanceLevel DESC")
    fun getTipsForSubstance(substanceId: String): Flow<List<HarmReductionTipEntity>>

    @Query("SELECT * FROM harm_reduction_tips ORDER BY importanceLevel DESC")
    fun getAllTips(): Flow<List<HarmReductionTipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTip(tip: HarmReductionTipEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTips(tips: List<HarmReductionTipEntity>)

    @Query("DELETE FROM harm_reduction_tips WHERE substanceId = :substanceId")
    suspend fun deleteTipsForSubstance(substanceId: String)
}

// ============================================================================
// REPOSITORY PATTERN IMPLEMENTATION
// ============================================================================

/**
 * Repository pattern implementation providing clean data abstraction for substances,
 * their physical/cognitive effects, health risks, and harm reduction guidelines.
 */
class SubstanceRepository(
    private val substanceDao: SubstanceDao,
    private val effectDao: SubstanceEffectDao,
    private val riskDao: SubstanceRiskDao,
    private val tipDao: HarmReductionTipDao
) {
    val allSubstances: Flow<List<SubstanceEntity>> = substanceDao.getAllSubstances()

    val allHarmReductionTips: Flow<List<HarmReductionTipEntity>> = tipDao.getAllTips()

    fun getSubstanceById(id: String): Flow<SubstanceEntity?> =
        substanceDao.getAllSubstances().map { list -> list.find { it.id == id } }

    fun getEffectsForSubstance(substanceId: String): Flow<List<SubstanceEffectEntity>> =
        effectDao.getEffectsForSubstance(substanceId)

    fun getRisksForSubstance(substanceId: String): Flow<List<SubstanceRiskEntity>> =
        riskDao.getRisksForSubstance(substanceId)

    fun getTipsForSubstance(substanceId: String): Flow<List<HarmReductionTipEntity>> =
        tipDao.getTipsForSubstance(substanceId)

    suspend fun insertSubstance(substance: SubstanceEntity) =
        substanceDao.insertSubstance(substance)

    suspend fun insertEffect(effect: SubstanceEffectEntity) =
        effectDao.insertEffect(effect)

    suspend fun insertRisk(risk: SubstanceRiskEntity) =
        riskDao.insertRisk(risk)

    suspend fun insertTip(tip: HarmReductionTipEntity) =
        tipDao.insertTip(tip)

    suspend fun insertSubstanceFullProfile(
        substance: SubstanceEntity,
        effects: List<SubstanceEffectEntity>,
        risks: List<SubstanceRiskEntity>,
        tips: List<HarmReductionTipEntity>
    ) {
        substanceDao.insertSubstance(substance)
        effectDao.insertEffects(effects)
        riskDao.insertRisks(risks)
        tipDao.insertTips(tips)
    }

    suspend fun deleteSubstance(id: String) {
        substanceDao.deleteSubstanceById(id)
        effectDao.deleteEffectsForSubstance(id)
        riskDao.deleteRisksForSubstance(id)
        tipDao.deleteTipsForSubstance(id)
    }

    /**
     * Seeds initial comprehensive harm reduction knowledge base into Room if DB is clean.
     */
    suspend fun prepopulateIfEmpty() {
        if (substanceDao.getSubstanceCount() == 0) {
            // MDMA
            val mdma = SubstanceEntity(
                id = "mdma",
                name = "MDMA",
                chemicalName = "3,4-Methylenedioxymethamphetamine",
                categoryName = "STIMULANT",
                description = "An entactogen/stimulant promoting emotional closeness, empathy, energy, and tactile sensations.",
                streetNames = "Ecstasy, Molly, M, Mandies, Beans, Rolls",
                onset = "20 - 60 minutes",
                duration = "3 - 5 hours",
                halfLife = "6 - 8 hours",
                tips = "Test with Marquis reagent.\nStay hydrated (500ml/hr if active).\nTake 3-month breaks between sessions.",
                dosageOralThreshold = "30 mg",
                dosageOralLight = "50 - 75 mg",
                dosageOralCommon = "75 - 120 mg",
                dosageOralStrong = "120 - 150 mg",
                dosageOralWarning = "Doses above 150mg increase neurotoxicity and cardiovascular strain."
            )
            val mdmaEffects = listOf(
                SubstanceEffectEntity(substanceId = "mdma", effectType = "COGNITIVE", title = "Empathy & Euphoria", description = "Strong emotional openness, affection, and heightened music appreciation.", intensity = "STRONG"),
                SubstanceEffectEntity(substanceId = "mdma", effectType = "PHYSICAL", title = "Pupil Dilation & Bruxism", description = "Involuntary jaw clenching, eye wiggles (nystagmus), and increased body temperature.", intensity = "MODERATE"),
                SubstanceEffectEntity(substanceId = "mdma", effectType = "AFTER_EFFECTS", title = "Mid-Week Dip", description = "Serotonin depletion 48-72 hours post-use leading to temporary low mood.", intensity = "MODERATE")
            )
            val mdmaRisks = listOf(
                SubstanceRiskEntity(substanceId = "mdma", riskLevel = "LETHAL", title = "MAOI Interaction", description = "Combining MDMA with MAOI antidepressants can trigger fatal Serotonin Syndrome.", mitigationNote = "Never combine MDMA with MAOIs or SSRIs."),
                SubstanceRiskEntity(substanceId = "mdma", riskLevel = "HIGH", title = "Hyperthermia & Hyponatremia", description = "Overheating coupled with excessive plain water intake without electrolytes.", mitigationNote = "Sip electrolyte drinks and take regular cooling breaks.")
            )
            val mdmaTips = listOf(
                HarmReductionTipEntity(substanceId = "mdma", category = "DOSING", tipText = "Calculate ideal dosage: (Body weight in kg + 50) = Ideal dose in mg.", importanceLevel = 5),
                HarmReductionTipEntity(substanceId = "mdma", category = "REAGENT_TESTING", tipText = "Always test with Marquis, Mecke, and Simon's reagents to rule out PMMA or synthetic cathinones.", importanceLevel = 5),
                HarmReductionTipEntity(substanceId = "mdma", category = "RECOVERY", tipText = "Supplement with 5-HTP (with EGCG) 24 hours AFTER the experience to aid serotonin synthesis.", importanceLevel = 4)
            )

            // LSD
            val lsd = SubstanceEntity(
                id = "lsd",
                name = "LSD",
                chemicalName = "Lysergic Acid Diethylamide",
                categoryName = "PSYCHEDELIC",
                description = "A potent classical psychedelic altering perception, time, self-awareness, and visual cognition.",
                streetNames = "Acid, Lucy, Tabs, Blotter, Microdot",
                onset = "30 - 90 minutes",
                duration = "8 - 12 hours",
                halfLife = "3 - 5 hours",
                tips = "Test with Ehrlich reagent (must turn purple).\nPrepare a comfortable, safe setting.\nKeep trip sitter available.",
                dosageOralThreshold = "15 ug",
                dosageOralLight = "25 - 75 ug",
                dosageOralCommon = "75 - 150 ug",
                dosageOralStrong = "150 - 300 ug",
                dosageOralWarning = "High doses induce intense ego dissolution and profound sensory overload."
            )
            val lsdEffects = listOf(
                SubstanceEffectEntity(substanceId = "lsd", effectType = "VISUAL", title = "Geometric Distortions", description = "Tracer effects, color enhancement, visual geometric patterns, and breathing textures.", intensity = "STRONG"),
                SubstanceEffectEntity(substanceId = "lsd", effectType = "COGNITIVE", title = "Conceptual Thinking & Synesthesia", description = "Dissolution of mental boundaries, deep introspection, hearing colors or seeing sounds.", intensity = "STRONG")
            )
            val lsdRisks = listOf(
                SubstanceRiskEntity(substanceId = "lsd", riskLevel = "HIGH", title = "NBOMe Contamination", description = "Bitter blotters containing toxic 25I-NBOMe.", mitigationNote = "If it's bitter, it's a spitter! Pure LSD is tasteless. Test with Ehrlich reagent.")
            )
            val lsdTips = listOf(
                HarmReductionTipEntity(substanceId = "lsd", category = "REAGENT_TESTING", tipText = "Ehrlich reagent turns purple in the presence of LSD/indole alkaloids.", importanceLevel = 5),
                HarmReductionTipEntity(substanceId = "lsd", category = "SET_AND_SETTING", tipText = "Establish a calm physical environment with comfortable seating, hydration, and trusted companions.", importanceLevel = 4)
            )

            insertSubstanceFullProfile(mdma, mdmaEffects, mdmaRisks, mdmaTips)
            insertSubstanceFullProfile(lsd, lsdEffects, lsdRisks, lsdTips)
        }
    }
}

/**
 * Singleton Provider for SubstanceRepository
 */
object SubstanceRepositoryProvider {
    @Volatile
    private var repository: SubstanceRepository? = null

    fun getRepository(context: Context): SubstanceRepository {
        return repository ?: synchronized(this) {
            val db = PillReviewDatabase.getDatabase(context)
            val repo = SubstanceRepository(
                db.substanceDao(),
                db.substanceEffectDao(),
                db.substanceRiskDao(),
                db.harmReductionTipDao()
            )
            repository = repo
            repo
        }
    }
}
