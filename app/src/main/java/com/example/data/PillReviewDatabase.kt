package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "pill_reviews")
data class PillReview(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val drugId: String,       // ID of the drug (e.g. "mdma", "lsd", "ketamine")
    val pillName: String,     // Name of the pill/tablet (e.g., "Yellow Tesla")
    val shape: String,        // Shape / imprint (e.g., "Shield / Tesla Logo")
    val colorHex: String,     // Hex of the color (e.g., "#FFFFCC00")
    val colorName: String,    // e.g. "Yellow"
    val potency: String,      // User rating / observation of strength (e.g., "Extremely Strong", "Warning: 280mg+", "Normal", "Underdosed")
    val dangerAlerts: String, // Alerts like "Fentanyl cut warning", "Contains PMMA", "Safe profile"
    val description: String,  // Detailed review comment
    val rating: Int,          // 1 to 5 stars
    val userAlias: String,    // Name of user (e.g. "AnonSitter", "harm_reduction_crew")
    val timestamp: Long = System.currentTimeMillis(),
    val location: String = "Global", // Location of the report (e.g. "United States", "Germany")
    val upvotes: Int = 0,     // Upvotes for safety rating
    val downvotes: Int = 0    // Downvotes for dangerous profile
)

@Entity(tableName = "safety_books")
data class SafetyBook(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val category: String, // e.g., "Harm Reduction", "Trip Sitter Manual", "Overdose Prevention", "Tutorial"
    val summary: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface PillReviewDao {
    @Query("SELECT * FROM pill_reviews ORDER BY timestamp DESC")
    fun getAllReviews(): Flow<List<PillReview>>

    @Query("SELECT * FROM pill_reviews WHERE drugId = :drugId ORDER BY timestamp DESC")
    fun getReviewsForDrug(drugId: String): Flow<List<PillReview>>

    @Query("SELECT COUNT(*) FROM pill_reviews")
    suspend fun getReviewCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: PillReview)

    @Query("UPDATE pill_reviews SET upvotes = upvotes + 1 WHERE id = :id")
    suspend fun upvoteReview(id: Int)

    @Query("UPDATE pill_reviews SET downvotes = downvotes + 1 WHERE id = :id")
    suspend fun downvoteReview(id: Int)
}

@Entity(tableName = "substances")
data class SubstanceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val chemicalName: String,
    val categoryName: String,
    val description: String,
    val streetNames: String,
    val onset: String,
    val duration: String,
    val halfLife: String,
    val tips: String,
    val dosageOralThreshold: String,
    val dosageOralLight: String,
    val dosageOralCommon: String,
    val dosageOralStrong: String,
    val dosageOralWarning: String,
    val timestamp: Long = System.currentTimeMillis()
)

fun SubstanceEntity.toDrug(): Drug {
    val cat = try {
        DrugCategory.valueOf(this.categoryName.uppercase())
    } catch(e: Exception) {
        DrugCategory.STIMULANT
    }
    return Drug(
        id = this.id,
        name = this.name,
        chemicalName = this.chemicalName,
        category = cat,
        description = this.description,
        streetNames = this.streetNames.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        dosageOral = DosageInfo(
            threshold = this.dosageOralThreshold,
            light = this.dosageOralLight,
            common = this.dosageOralCommon,
            strong = this.dosageOralStrong,
            warningNote = this.dosageOralWarning.ifEmpty { null }
        ),
        onset = this.onset,
        duration = this.duration,
        halfLife = this.halfLife,
        goodEnvironments = listOf("Comfortable, quiet physical space with trusted friends", "Surroundings with access to standard safety metrics"),
        badEnvironments = listOf("Crowded, dark, unventilated, or high temperature spaces", "Hostile, volatile or unknown social settings"),
        reagents = emptyList(),
        tips = this.tips.split("\n").map { it.trim() }.filter { it.isNotEmpty() },
        deepDive = DeepDiveInfo(
            synthesisOverview = "Sourced from local chemical library: Synthesis requires strictly regulated precursor compounds and highly controlled chemical environments.",
            redditSafetyFiles = listOf(
                "r/harmreduction Warning: Always test and verify purity of active powders.",
                "r/ReagentTesting Tip: Use appropriate spot tests for qualitative screening before administration."
            ),
            githubArchives = listOf("github.com/psychonautwiki/api - Profile for " + this.name),
            chemicalProperties = mapOf(
                "Formula" to "N/A",
                "Form" to "Typically powder or crystalline material",
                "Storage" to "Store in a cool, dry, secure container"
            )
        )
    )
}

@Dao
interface SafetyBookDao {
    @Query("SELECT * FROM safety_books ORDER BY timestamp DESC")
    fun getAllBooks(): Flow<List<SafetyBook>>

    @Query("SELECT COUNT(*) FROM safety_books")
    suspend fun getBookCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: SafetyBook)

    @Query("DELETE FROM safety_books WHERE id = :id")
    suspend fun deleteBook(id: Int)
}

@Dao
interface SubstanceDao {
    @Query("SELECT * FROM substances ORDER BY timestamp DESC")
    fun getAllSubstances(): Flow<List<SubstanceEntity>>

    @Query("SELECT COUNT(*) FROM substances")
    suspend fun getSubstanceCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubstance(substance: SubstanceEntity)

    @Query("DELETE FROM substances WHERE id = :id")
    suspend fun deleteSubstanceById(id: String)
}

@Database(
    entities = [
        PillReview::class,
        SafetyBook::class,
        SubstanceEntity::class,
        SubstanceEffectEntity::class,
        SubstanceRiskEntity::class,
        HarmReductionTipEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class PillReviewDatabase : RoomDatabase() {
    abstract fun pillReviewDao(): PillReviewDao
    abstract fun safetyBookDao(): SafetyBookDao
    abstract fun substanceDao(): SubstanceDao
    abstract fun substanceEffectDao(): SubstanceEffectDao
    abstract fun substanceRiskDao(): SubstanceRiskDao
    abstract fun harmReductionTipDao(): HarmReductionTipDao

    companion object {
        @Volatile
        private var INSTANCE: PillReviewDatabase? = null

        fun getDatabase(context: Context): PillReviewDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PillReviewDatabase::class.java,
                    "pill_review_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class PillReviewRepository(
    private val reviewDao: PillReviewDao,
    private val bookDao: SafetyBookDao,
    private val substanceDao: SubstanceDao
) {
    val allReviews: Flow<List<PillReview>> = reviewDao.getAllReviews()
    
    fun getReviewsForDrug(drugId: String): Flow<List<PillReview>> = reviewDao.getReviewsForDrug(drugId)
    
    suspend fun insert(review: PillReview) = reviewDao.insertReview(review)

    suspend fun upvoteReview(id: Int) = reviewDao.upvoteReview(id)

    suspend fun downvoteReview(id: Int) = reviewDao.downvoteReview(id)

    val allBooks: Flow<List<SafetyBook>> = bookDao.getAllBooks()

    suspend fun insertBook(book: SafetyBook) = bookDao.insertBook(book)

    suspend fun deleteBook(id: Int) = bookDao.deleteBook(id)

    val allSubstances: Flow<List<SubstanceEntity>> = substanceDao.getAllSubstances()

    suspend fun insertSubstance(substance: SubstanceEntity) = substanceDao.insertSubstance(substance)

    suspend fun deleteSubstanceById(id: String) = substanceDao.deleteSubstanceById(id)
    
    suspend fun prepopulateIfEmpty() {
        if (reviewDao.getReviewCount() == 0) {
            val defaults = listOf(
                PillReview(
                    drugId = "mdma",
                    pillName = "Blue Punisher",
                    shape = "Skull / Punisher Logo",
                    colorHex = "#FF0D47A1",
                    colorName = "Blue",
                    potency = "Dangerously High (300mg+)",
                    dangerAlerts = "Warning: Extreme dosage reported across Reddit & European lab tests (e.g., Zurich drug checking). Prone to severe hyperthermia.",
                    description = "Tested with Marquis, Mecke, and Simons in mid-2025. Instantly black with Marquis. Clean MDMA but the strength is terrifying. Half is easily a heavy dose.",
                    rating = 5,
                    userAlias = "SitterSoren",
                    location = "Germany",
                    upvotes = 142,
                    downvotes = 4
                ),
                PillReview(
                    drugId = "mdma",
                    pillName = "Yellow Tesla",
                    shape = "Shield / Tesla Logo",
                    colorHex = "#FFFFB300",
                    colorName = "Yellow",
                    potency = "Very Strong (240mg+)",
                    dangerAlerts = "Warning: Heavily copied. Some copies contain caffeine & MDA, others contain dangerous synthetic cathinones. Test before rolling.",
                    description = "Marquis went straight to dark purple. Ehrlich was clear (as expected). Strong jaw-clenching and intense energy. Always split these in quarters.",
                    rating = 4,
                    userAlias = "RollMaster_99",
                    location = "United States",
                    upvotes = 98,
                    downvotes = 1
                ),
                PillReview(
                    drugId = "alprazolam",
                    pillName = "White Xanax Bar",
                    shape = "Rectangular Bar / 'XANAX' or '2' imprint",
                    colorHex = "#FFEAEAEA",
                    colorName = "White",
                    potency = "Highly Variable (Unpredictable)",
                    dangerAlerts = "Warning: 99% of street 'bars' are pressed with bromazolam or clonazolam, and frequently contaminated with Fentanyl.",
                    description = "Bought as 'pharmacy grade' but tested positive for Bromazolam via Zimmermann. Completely blanked out for 12 hours after taking just half a bar. Extremely dangerous and highly prone to compulsive redosing.",
                    rating = 1,
                    userAlias = "HarmReductionGuy",
                    location = "Canada",
                    upvotes = 205,
                    downvotes = 2
                ),
                PillReview(
                    drugId = "ketamine",
                    pillName = "Liquid Vial S-Ketamine",
                    shape = "Clear liquid / crystalline powder after drying",
                    colorHex = "#FFFFFFFF",
                    colorName = "Crystal/Clear",
                    potency = "Strong (Pure S-Isomer)",
                    dangerAlerts = "Caution: Extreme dissociative anesthetic effects. Keep away from water to prevent accidental drowning during motor paralysis.",
                    description = "Morris reagent immediately turned a beautiful bright lavender/purple. Very intense S-isomer K-hole visuals, incredibly rapid onset (under 10 minutes insufflated). Drink plenty of water and protect your bladder.",
                    rating = 5,
                    userAlias = "CosmicTraveler",
                    location = "United Kingdom",
                    upvotes = 67,
                    downvotes = 0
                ),
                PillReview(
                    drugId = "cocaine",
                    pillName = "Fishscale Powder",
                    shape = "Crystalline shiny flakes/powder",
                    colorHex = "#FFFFFFFF",
                    colorName = "Off-White",
                    potency = "Strong (High Purity)",
                    dangerAlerts = "Warning: Tested positive for Levamisole. High cardiotoxicity warning when mixed with alcohol (Cocaethylene).",
                    description = "Scott reagent turned deep blue. Liebermann turned slightly yellow-orange indicating presence of levamisole. Extreme heart racing and mild paranoia. Highly compulsive.",
                    rating = 3,
                    userAlias = "NoseRescue",
                    location = "Australia",
                    upvotes = 43,
                    downvotes = 8
                ),
                PillReview(
                    drugId = "lsd",
                    pillName = "Albert Hofmann 1943 Tab",
                    shape = "Blotter Paper Tab with Bicycle Art",
                    colorHex = "#FFFFB74D",
                    colorName = "Multi-color Art",
                    potency = "Standard (100mcg - 125mcg)",
                    dangerAlerts = "Low risk of direct chemical adulteration if Ehrlich turns purple. Do not take in chaotic settings.",
                    description = "Tested positive with Ehrlich after 12 minutes (slow violet reaction). Beautiful, clean trip. Very visual with fractal overlays and deep emotional warmth. Lasted 11 hours.",
                    rating = 5,
                    userAlias = "AcidExplorer",
                    location = "Global",
                    upvotes = 118,
                    downvotes = 1
                )
            )
            for (review in defaults) {
                reviewDao.insertReview(review)
            }
        }

        if (bookDao.getBookCount() == 0) {
            val defaultBooks = listOf(
                SafetyBook(
                    title = "The Psychedelic Sitter Guidebook",
                    author = "DanceSafe Coalition",
                    category = "Trip Sitter Manual",
                    summary = "Trauma-informed session orchestration, environment setup, active redirection, and emotional grounding workflows.",
                    content = "CHAPTER 1: THE FOUR PILLARS OF SITTING\n\n1. SETTING THE ENVIRONMENT:\nChoose a quiet, comfortable, secure space free from unannounced visitors or chaotic external noises. Subdued lighting and high-quality ambient acoustic playlists are standard parameters.\n\n2. ACTIVE NON-INTERFERENCE:\nYour primary role is safety monitoring, not guiding or directing the session. Unless the voyager enters a state of high distress, maintain a calm, supportive presence in the background.\n\n3. THERAPEUTIC REDIRECTION:\nIf the voyager experiences difficult emotional states, suggest a simple physical change: move to a different room, change the background music, adjust the lighting, or practice deep diaphragmatic breathing.\n\n4. POST-SESSION INTEGRATION:\nIn the final hours, offer non-judgmental reflective dialogue. Allow them to articulate complex insights in their own words. Avoid imposing external psychological frameworks."
                ),
                SafetyBook(
                    title = "Reagent Spot Testing Bible",
                    author = "Bunk Police Collective",
                    category = "Chemistry",
                    summary = "Step-by-step master instructions on using Marquis, Mecke, Mandelin, and Ehrlich kits to identify adulterants.",
                    content = "SECTION A: BASIC REAGENT DIRECTIVES\n\nAlways work on a clean ceramic plate or glass surface. Use a sample size roughly equivalent to a single grain of salt (1-2mg). Drop exactly ONE drop of reagent directly onto the sample.\n\nREAGENT EXPECTED REACTIONS:\n\n1. MARQUIS:\n- MDMA / MDA: Instantly turns royal purple, transitioning to a deep jet-black within 3-5 seconds.\n- Amphetamine / Methamphetamine: Deep orange transitioning to dark reddish-brown.\n- Heroin / Morphine: Deep reddish-purple.\n- DXM: Slow yellow transitioning to green-black.\n\n2. EHRLICH:\n- LSD / 1P-LSD / DMT (Indoles): Slow light violet/purple reaction (takes 5-20 minutes). If no reaction occurs after 30 minutes, it is likely a highly dangerous research chemical like 25i-NBOMe or DOx.\n\n3. MORRIS:\n- Ketamine: Turns a bright, rich lavender/purple after thorough stirring. Cocaine turns a deep sea-blue."
                ),
                SafetyBook(
                    title = "MDMA Harm Reduction 101",
                    author = "RollSafe Alliance",
                    category = "Harm Reduction",
                    summary = "Essential protocols on precise weight-based dosing, hydration limits, thermoregulation, and recovery supplements.",
                    content = "1. WEIGHT-BASED DOSING CALCULATION:\nTo calculate a safe, clinical-grade dosage threshold: Take your body weight in kilograms, add 50, and that is your maximum target dose in milligrams.\nExample: 70kg user + 50 = 120mg total dose. Avoid redosing after the 2-hour mark.\n\n2. HYDRATION METRICS:\nMDMA induces antidiuretic hormone (ADH) release, making water retention highly prevalent. Accidental water intoxication (hyponatremia) is a leading cause of severe harm.\n- Active dancing: Drink 250-500mL of mineral-rich electrolyte fluids per hour.\n- Resting/Chilling: Limit consumption to 250mL of water/electrolytes per hour.\n\n3. THERMOREGULATION:\nHyperthermia (extreme overheating) combined with crowded environments is the leading catalyst for MDMA-induced organ failure. Take dedicated cooling breaks (15 minutes of rest out of every hour of dancing) in temperature-controlled spaces.\n\n4. COGNITIVE RECOVERY:\nReplenish serotonin levels naturally. Take 5-HTP (100mg) combined with EGCG (green tea extract) daily for 3-5 days starting exactly 24 hours after the session completes. Never take 5-HTP while MDMA is active as this can precipitate fatal Serotonin Syndrome."
                ),
                SafetyBook(
                    title = "Fentanyl Safety & Overdose Rescue",
                    author = "Harm Reduction Coalition",
                    category = "Overdose Prevention",
                    summary = "Critical tutorial on preparing drug dilutions for Fentanyl test strips, and administering life-saving Naloxone.",
                    content = "1. FENTANYL TEST STRIP DILUTION PROTOCOL:\nFentanyl is extremely active at microgram scales and is rarely distributed evenly inside a pill/powder (the Chocolate Chip Cookie effect).\n- ALWAYS crush and thoroughly dissolve the entire intended dose in clean water.\n- Stimulants (Cocaine/Meth): Dilute 10mg of sample per 1mL of water to prevent false positives from active precursor chemicals.\n- Other drugs: Dilute 1mg of sample per 1mL of water.\n- Insert test strip up to the max line for 15 seconds. Two lines indicate a NEGATIVE result (safe). One line indicates a POSITIVE result (Fentanyl detected - DO NOT CONSUME).\n\n2. RECOGNIZING AN OPIOID OVERDOSE:\nLook for the classic triad of symptoms:\n- Pinpoint (extremely constricted) pupils.\n- Pale/blue/gray skin, lips, or fingernails.\n- Extremely shallow, slow breathing, or guttural snoring/gasping noises.\n\n3. NALOXONE ADMINISTRATION:\n- Call emergency services immediately.\n- Spray Narcan (4mg) directly up one nostril.\n- If breathing does not resume within 2-3 minutes, administer a second Narcan dose in the opposite nostril.\n- Perform rescue breathing and place the individual in the Recovery Position to prevent aspiration."
                ),
                SafetyBook(
                    title = "Erowid Psychoactive Vaults Master Guide",
                    author = "Erowid Center",
                    category = "Research & History",
                    summary = "Dosage thresholds, experiential indexing, chemical identification protocols, and history of psychoactives.",
                    content = "EROWID VAULTS INFORMATION ARCHIVE\n\n1. UNDERSTANDING EXPERIENCE REPORTS (TRIP REPORTS):\nSince 1995, Erowid has collected and moderated over 40,000 subjective substance experience reports. These document unexpected physical reactions, visual phenomena, dosing errors, and therapeutic breakthroughs. Experiential data is vital alongside pure toxicology.\n\n2. DOSAGE & THRESHOLD PROTOCOLS:\nEvery substance entry contains precise weight thresholds:\n- Threshold: The minimum dose required to detect any subjective alteration.\n- Light: Mild alterations, clear headedness.\n- Common: Full spectrum of standard effects.\n- Strong: Intense peak effects, heavy motor impairment.\n- Heavy: Highly overwhelming state. Prone to extreme sensory distortion.\nAlways start with a Threshold or Light dose to check for unique physiological sensitivities."
                ),
                SafetyBook(
                    title = "TripSit Peer Support Companion",
                    author = "TripSit Team",
                    category = "Trip Sitter Manual",
                    summary = "Direct instructions on managing bad trips, communicating with individuals experiencing drug-induced anxiety, and active grounding techniques.",
                    content = "1. CALMING AN ACTIVE DISTRESS STATE:\n- Eliminate sensory overload: Move to a dim, quiet room with fresh air. No flashing lights or heavy beats.\n- Change the setting: Simple changes like changing a song, drinking a sip of water, or looking at a calm visual scene can immediately break negative feedback loops.\n- Reinforce safety: State calmly, 'You are in a safe place. You took a substance. It will wear off. I am here with you.'\n\n2. VERBAL TECHNIQUES:\n- Use a soft, steady tone of voice. Avoid frantic questioning.\n- Encourage breathing: 'Breathe in for 4 seconds, hold for 4, breathe out for 4.'\n- Do not argue with hallucinations or delusional content. Validate their feelings of anxiety without validating dangerous delusions."
                ),
                SafetyBook(
                    title = "Drugs and Me Educational Toolkit",
                    author = "Drugs and Me",
                    category = "Harm Reduction",
                    summary = "Detailed overview of the biological effects of common recreationals on neurotransmitters, and interactive health tracking tips.",
                    content = "THE NEUROBIOLOGICAL MATRIX\n\n1. STIMULANTS (Cocaine, MDMA, Amphetamines):\n- Neurotransmitter trigger: Massive release of Dopamine, Norepinephrine, and Serotonin.\n- Critical risks: Cardiotoxicity, elevated blood pressure, severe jaw-clenching, hyperthermia, and post-session receptor depletion.\n- Mitigation: Pre-hydrate with magnesium to limit jaw tension. Keep a strict timeline to prevent compulsive redosing.\n\n2. DEPRESSANTS (Alcohol, Ketamine, GHB, Benzodiazepines):\n- Neurotransmitter trigger: GABA receptor agonist / NMDA receptor antagonist.\n- Critical risks: Motor paralysis, respiratory depression, vomiting while unconscious (choking risk).\n- Mitigation: Always place unconscious individuals in the Recovery Position. Never mix depressant families."
                ),
                SafetyBook(
                    title = "Saferparty Zurich Laboratory Alerts",
                    author = "Saferparty.ch",
                    category = "Chemistry",
                    summary = "Current chemical analysis warnings, extremely high dose MDMA warnings, and novel psychoactive substance (NPS) identification.",
                    content = "SWISS LAB ALERTS & ANALYSIS DIRECTIVES\n\n1. LABORATORY DRUG CHECKING:\nThrough high-performance liquid chromatography (HPLC) and mass spectrometry, mobile labs in Zurich analyze real drug content. Street drugs often vary wildly from advertised compositions.\n\n2. HIGH-DOSE MDMA ALERTS:\nMDMA tablets exceeding 200mg-300mg are extremely common in nightlife venues today. Since the therapeutic dose is around 100mg-120mg, taking an entire high-dose pill can trigger severe serotonin toxicity, heart distress, and mental blackouts.\n- ALWAYS start with a quarter (1/4) of a tablet.\n- Wait at least 2 full hours before evaluating effects.\n\n3. NOVEL PSYCHOACTIVE SUBSTANCES (NPS):\nMany white powders sold as Ketamine or Cocaine are contaminated with novel synthetic cathinones (e.g., 2-MMC, 3-MMC) or dissociatives (e.g., O-PCE). These have significantly higher cardiotoxicity and addiction potentials."
                )
            )
            for (book in defaultBooks) {
                bookDao.insertBook(book)
            }
        }
    }
}

object RepositoryProvider {
    @Volatile
    private var repository: PillReviewRepository? = null

    fun getRepository(context: Context): PillReviewRepository {
        return repository ?: synchronized(this) {
            val db = PillReviewDatabase.getDatabase(context)
            val repo = PillReviewRepository(db.pillReviewDao(), db.safetyBookDao(), db.substanceDao())
            repository = repo
            repo
        }
    }
}

