package com.example.data

import androidx.compose.ui.graphics.Color

enum class DrugCategory(val displayName: String, val colorHex: Long) {
    STIMULANT("Stimulant", 0xFFFF4D4D),
    PSYCHEDELIC("Psychedelic / Hallucinogen", 0xFF9C27B0),
    DISSOCIATIVE("Dissociative", 0xFF00BCD4),
    DEPRESSANT("Depressant / Benzodiazepine", 0xFF4CAF50),
    OPIOID("Opioid", 0xFFFF9800)
}

enum class RiskLevel(val displayName: String, val colorHex: Long, val severity: Int) {
    LETHAL("Lethal Combination", 0xFFD32F2F, 4),
    DANGEROUS("Highly Dangerous", 0xFFE64A19, 3),
    CAUTION("Caution / Unpredictable", 0xFFFBC02D, 2),
    SYNERGISTIC("Synergistic / High Strain", 0xFF1976D2, 1),
    SAFE("Low Risk / Neutral", 0xFF388E3C, 0)
}

data class DosageInfo(
    val threshold: String,
    val light: String,
    val common: String,
    val strong: String,
    val warningNote: String? = null
)

data class ReagentColor(
    val reagentName: String,
    val colorName: String,
    val hexColor: Long
)

data class DeepDiveInfo(
    val synthesisOverview: String,
    val redditSafetyFiles: List<String>,
    val githubArchives: List<String>,
    val chemicalProperties: Map<String, String>
)

data class Drug(
    val id: String,
    val name: String,
    val chemicalName: String,
    val category: DrugCategory,
    val description: String,
    val streetNames: List<String>,
    val dosageOral: DosageInfo? = null,
    val dosageInsufflated: DosageInfo? = null,
    val onset: String,
    val duration: String,
    val halfLife: String,
    val goodEnvironments: List<String>,
    val badEnvironments: List<String>,
    val reagents: List<ReagentColor>,
    val tips: List<String>,
    val deepDive: DeepDiveInfo? = null
)

data class DrugInteraction(
    val drugA: String,
    val drugB: String,
    val riskLevel: RiskLevel,
    val description: String
)

data class ReagentTestKit(
    val name: String,
    val description: String,
    val targetSubstances: String,
    val instructions: List<String>,
    val typicalColorChanges: Map<String, String> // Drug Name -> Color string
)

object DrugDatabase {
    val drugs = listOf(
        Drug(
            id = "mdma",
            name = "MDMA",
            chemicalName = "3,4-Methylenedioxymethamphetamine",
            category = DrugCategory.STIMULANT,
            description = "An entactogen/stimulant known for promoting emotional closeness, empathy, energy, and tactile sensations. Historically popular in electronic music scenes. It triggers a massive release of serotonin, dopamine, and norepinephrine.",
            streetNames = listOf("Ecstasy", "Molly", "M", "Mandies", "Beans", "Rolls", "XTC"),
            dosageOral = DosageInfo(
                threshold = "30 mg",
                light = "50 - 75 mg",
                common = "75 - 120 mg",
                strong = "120 - 150 mg",
                warningNote = "Harm reduction formula: (Body weight in kg + 50) = Ideal dose in mg. Avoid redosing after 2 hours. Doses above 150-200 mg significantly increase neurotoxicity and cardiovascular strain."
            ),
            onset = "20 - 60 minutes",
            duration = "3 - 5 hours",
            halfLife = "6 - 8 hours",
            goodEnvironments = listOf(
                "Temperature-controlled spaces (highly susceptible to hyperthermia)",
                "Surrounded by trusted, caring friends who can check in on you",
                "Quiet relaxation areas ('chill out zones') to rest and cool down",
                "Environments with easy access to clean drinking water"
            ),
            badEnvironments = listOf(
                "Densely crowded, stuffy, or unventilated spaces",
                "Hot, humid, or sun-drenched outdoor festivals without shade",
                "Unfamiliar or isolated places where emergency help is unreachable",
                "Engaging in heavy physical exertion without cooling down"
            ),
            reagents = listOf(
                ReagentColor("Marquis", "Deep Purple to Black", 0xFF1A002C),
                ReagentColor("Mecke", "Green to Dark Blue", 0xFF003C30),
                ReagentColor("Mandelin", "Dark Purple/Black", 0xFF1F0326),
                ReagentColor("Simon's", "Deep Blue (distinguishes MDMA from MDA)", 0xFF004DFF),
                ReagentColor("Folin", "Pinkish-red", 0xFFE91E63)
            ),
            tips = listOf(
                "Hydration guidelines: Drink 250ml of water per hour if resting, or up to 500ml per hour if dancing. Avoid drinking too much water rapidly to prevent hyponatremia (water poisoning).",
                "Take regular breaks from dancing to sit down and cool off in a ventilated room.",
                "Wait at least 3 months between sessions ('Three-Month Rule') to allow serotonin receptors and brain chemistry to fully recover.",
                "MDMA is highly prone to adulteration with PMMA, methamphetamine, bath salts, or synthetic cathinones. Always test before consuming."
            ),
            deepDive = DeepDiveInfo(
                synthesisOverview = "Sourced from chemical archives: Synthesis typically starts from Safrole, which is isomerized to Isosafrole using a strong base, then converted to MDP2P (Methylenedioxyphenyl-2-propanone) via Wacker oxidation or peracid oxidation. MDP2P is subsequently condensed with methylamine and reduced using sodium cyanoborohydride, aluminum amalgam, or catalytic hydrogenation. Precursors are strictly regulated under the UN Convention Against Illicit Traffic in Narcotic Drugs.",
                redditSafetyFiles = listOf(
                    "r/MDMA Warning: Extreme prevalence of PMMA-contaminated 'Pink Maserati' and 'Blue Punisher' tablets in European nightlife. PMMA has a much slower onset (up to 2 hours), leading users to assume the pill is weak, redose, and experience fatal hyperthermia or serotonin syndrome.",
                    "r/ReagentTesting Warning: Marquis reagent must turn pitch black instantly. A slow yellow turn indicates bath salts/synthetic cathinones (like eutylone). Always test with Simon's A/B to differentiate MDMA from MDA.",
                    "r/harmreduction Tip: Take 500mg Vitamin C and 200mg Magnesium Glycinate pre-session to significantly reduce jaw-clenching and neurotoxicity."
                ),
                githubArchives = listOf(
                    "github.com/psychonautwiki/api - MDMA exhibits high binding affinity for VMAT2, forcing massive reverse transport of serotonin, dopamine, and norepinephrine into the synaptic cleft.",
                    "github.com/harmreduction-dev/pill-database - Pressed pill database details: Yellow Teslas consistently check over 240mg MDMA, requiring quarters or halves as starting doses.",
                    "github.com/tripbot/database - Serotonin syndrome warning model indicates dangerous interactions when combined with MAOIs, SSRIs, or DXM."
                ),
                chemicalProperties = mapOf(
                    "Formula" to "C11H15NO2",
                    "Melting Point" to "147-153 °C (Hydrochloride salt)",
                    "Solubility" to "Highly soluble in water and ethanol",
                    "Form" to "Typically white crystalline powder, translucent shards, or compressed colorful tablets",
                    "Storage" to "Extremely stable chemically. Store in a cool, dark, dry container."
                )
            )
        ),
        Drug(
            id = "lsd",
            name = "LSD",
            chemicalName = "Lysergic Acid Diethylamide",
            category = DrugCategory.PSYCHEDELIC,
            description = "A powerful, long-acting synthetic psychedelic. It induces severe alterations in sensory perception, thought processes, visual/auditory hallucinations, and deep introspective states by binding heavily to serotonin 2A receptors.",
            streetNames = listOf("Acid", "Tabs", "Blotters", "Lucy", "Microdots", "Windowpane"),
            dosageOral = DosageInfo(
                threshold = "10 - 20 mcg",
                light = "20 - 75 mcg",
                common = "75 - 150 mcg",
                strong = "150 - 300 mcg",
                warningNote = "Dosages are measured in micrograms (mcg or µg). High psychological risk of panic, disorientation, and 'bad trips' on strong doses. It is highly active even in tiny quantities."
            ),
            onset = "30 - 90 minutes",
            duration = "8 - 12 hours",
            halfLife = "3 - 5 hours (effects last much longer due to receptor trapping)",
            goodEnvironments = listOf(
                "Familiar, aesthetically pleasing, and peaceful surroundings",
                "Private homes or secure outdoor natural settings with trusted friends",
                "Having a sober companion ('Trip Sitter') present",
                "Environments with calming, ambient music and soft lighting"
            ),
            badEnvironments = listOf(
                "Chaotic, loud, public, or highly crowded locations",
                "Environments with hostile, anxious, or unknown individuals",
                "Places that require navigation of dangerous terrain or operating machines",
                "Confining spaces with high sensory overload"
            ),
            reagents = listOf(
                ReagentColor("Ehrlich", "Purple / Pink (indicates indole ring)", 0xFF8E24AA),
                ReagentColor("Hofmann", "Violet to Blue", 0xFF3F51B5),
                ReagentColor("Marquis", "Olive Green/Brown", 0xFF6D4C41)
            ),
            tips = listOf(
                "Set and Setting: Your current state of mind ('set') and environment ('setting') dictate the experience. Never consume when highly anxious, depressed, or in a volatile mood.",
                "Prepare a 'safe room' with cozy blankets, comforting music, and soft lights if you need to retreat.",
                "Have a sober, experienced trip sitter to ground you and provide reassurance.",
                "Do not fight the experience. Accept and float with the thoughts and feelings that arise."
            ),
            deepDive = DeepDiveInfo(
                synthesisOverview = "Sourced from chemical archives: Synthesis of LSD is a highly advanced procedure normally starting from Lysergic Acid, which is obtained by hydrolysis of Ergotamine Tartrate (produced by the fungus Claviceps purpurea). The carboxylic acid group is activated using reagents like phosgene, thionyl chloride, or peptide coupling agents (PyBOP), and then reacted with diethylamine in an anhydrous dark room. The resulting mixture of diastereomers is chromatographically separated on alumina to isolate active d-LSD-25, which is then crystallized as a tartrate salt.",
                redditSafetyFiles = listOf(
                    "r/drugs Warning: Extreme risk of toxic 25I-NBOMe being sold as LSD blotters. Important physical distinction: LSD is completely tasteless, while NBOMe is extremely bitter and numbs the tongue ('If it's bitter, it's a spitter'). NBOMe compounds can cause fatal vasoconstriction and heart failure at active doses.",
                    "r/ReagentTesting Tip: Use Hofmann reagent to confirm LSD. While Ehrlich turns purple with any indole (including melatonin or 5-HTP), Hofmann specifically turns vibrant blue/violet only with lysergamides.",
                    "r/LSD advice: Keep an offline music playlist downloaded, wear loose warm clothing, and keep hydrated with juice rather than flat tap water."
                ),
                githubArchives = listOf(
                    "github.com/tripsit/substances - LSD-25 is deeply unstable. Chlorine in tap water instantly destroys the molecule. Exposure to UV/sunlight or temperatures above 30°C quickly triggers degradation into inactive iso-LSD.",
                    "github.com/open-science-ph/docking-models - Binding simulations demonstrate LSD molecular 'lid' trapping itself inside the 5-HT2A receptor binding pocket, explaining why microgram dosages produce trips lasting 12+ hours."
                ),
                chemicalProperties = mapOf(
                    "Formula" to "C20H25N3O",
                    "Melting Point" to "80-85 °C (Tartrate salt)",
                    "Solubility" to "Soluble in water (distilled only) and alcohol",
                    "Form" to "Extremely thin paper blotters, tiny gelatin squares ('pyramids'), or liquid drops",
                    "Storage" to "Highly sensitive to light, oxygen, moisture, and heat. Store wrapped in foil, under vacuum, in a freezer."
                )
            )
        ),
        Drug(
            id = "ketamine",
            name = "Ketamine",
            chemicalName = "2-(2-Chlorophenyl)-2-(methylamino)cyclohexan-1-one",
            category = DrugCategory.DISSOCIATIVE,
            description = "An NMDA receptor antagonist used clinically as an anesthetic and increasingly as an antidepressant. In sub-anesthetic doses, it induces profound dissociation, spatial distortion, numbness, and 'K-hole' states at heavy doses.",
            streetNames = listOf("Special K", "Ket", "K", "Vitamin K", "KitKat", "Horse Tranquilizer"),
            dosageInsufflated = DosageInfo(
                threshold = "5 - 15 mg",
                light = "15 - 30 mg",
                common = "30 - 75 mg",
                strong = "75 - 150 mg (K-Hole: 100mg+)",
                warningNote = "Ketamine is usually insufflated. K-holes are characterized by complete dissociation, physical paralysis, and intense out-of-body visual journeys. Highly addictive with bladder toxicity issues."
            ),
            onset = "5 - 15 minutes",
            duration = "45 - 90 minutes",
            halfLife = "2 - 3 hours",
            goodEnvironments = listOf(
                "Very comfortable physical seating, sofas, or beds (highly anesthetic, prone to falls)",
                "Dim, tranquil spaces with relaxing non-lyrical music",
                "Secure, private settings where lying down is fully safe"
            ),
            badEnvironments = listOf(
                "Environments near deep water or pools (high risk of drowning if paralyzed)",
                "Loud clubs where physical coordination or balancing is required",
                "Lacking physical support or soft surfaces to fall on",
                "Places requiring any rapid response or verbal communication"
            ),
            reagents = listOf(
                ReagentColor("Mandelin", "Dark Orange-Brown", 0xFF8D6E63),
                ReagentColor("Mecke", "No reaction (clear)", 0x11FFFFFF),
                ReagentColor("Marquis", "No reaction (clear)", 0x11FFFFFF),
                ReagentColor("Morris", "Lavender / Deep Purple", 0xFF7E57C2)
            ),
            tips = listOf(
                "Frequent ketamine use causes severe, irreversible damage to the urinary bladder ('Ketamine Bladder Syndrome'). Space sessions out significantly.",
                "Ensure you are in a safe physical posture. Never do ketamine standing up or walking as falls can cause severe physical trauma.",
                "Mixing ketamine with alcohol is extremely dangerous and frequently causes vomiting while unconscious, leading to asphyxiation and death."
            ),
            deepDive = DeepDiveInfo(
                synthesisOverview = "Sourced from chemical archives: Industrial ketamine synthesis is initiated by reacting o-chlorobenzonitrile with cyclopentyl Grignard reagent to yield o-chlorophenyl cyclopentyl ketone. This ketone is then brominated, followed by reaction with methylamine to form an imine intermediate. Thermal rearrangement of this imine at 180-200°C under vacuum/solvent conditions causes ring expansion and decarboxylation, yielding ketamine base. The hydrochloride salt is then precipitated out with hydrochloric acid. Requires precise pressure apparatus.",
                redditSafetyFiles = listOf(
                    "r/dissociatives Warning: Extensive community reports on Ketamine Bladder Syndrome (ulcerative cystitis). Daily usage of 1g+ destroys the GAG protective layer of the bladder wall, resulting in hematuria (blood in urine), extreme pain, and eventual cystectomy (surgical bladder removal). Preventative advice: limit frequency and take green tea extract (EGCG) pre-session.",
                    "r/ReagentTesting Tip: Morris reagent is critical to differentiate Ketamine from its structural analogs (such as 2-FDCK, DCK, or MXE). Morris turns a distinct lavender/purple with ketamine, whereas 2-FDCK turns a dull blue or green."
                ),
                githubArchives = listOf(
                    "github.com/ketamine-research-group/neuro - Molecular models show ketamine acting as a non-competitive antagonist at NMDA glutamate receptors. Disruption of these gates blocks pain transmission and induces anesthesia while encouraging neuroplasticity via BDNF upregulation.",
                    "github.com/open-science/compounds - Describes the recrystallization process. Recreational users cook liquid pharmaceutical ketamine vials on flat plates under low heat to evaporate the sterile saline, producing fine crystalline shards."
                ),
                chemicalProperties = mapOf(
                    "Formula" to "C13H16ClNO",
                    "Melting Point" to "262-263 °C (Hydrochloride salt)",
                    "Solubility" to "Very high (200 mg/mL in water)",
                    "Form" to "Clear aqueous solution or fine crystalline, reflective needles/powder",
                    "Storage" to "Stable at room temperature. Keep protected from extreme heat."
                )
            )
        ),
        Drug(
            id = "cocaine",
            name = "Cocaine",
            chemicalName = "Methyl (1R,2R,3S,5S)-3-(benzoyloxy)-8-methyl-8-azabicyclo[3.2.1]octane-2-carboxylate",
            category = DrugCategory.STIMULANT,
            description = "A highly addictive, short-acting tropical stimulant. It acts by blocking reuptake of dopamine, serotonin, and norepinephrine, causing immediate euphoria, intense alertness, ego inflation, and high cardiovascular stress.",
            streetNames = listOf("Coke", "Blow", "Snow", "Charlie", "White", "Powder", "Nose Candy"),
            dosageInsufflated = DosageInfo(
                threshold = "5 - 10 mg",
                light = "10 - 30 mg",
                common = "30 - 60 mg",
                strong = "60 - 100 mg",
                warningNote = "Extremely cardiotoxic. Frequently cut with harmful materials like levamisole (immunosuppressant), lidocaine, or lethal quantities of fentanyl. Compulsive redosing is highly common."
            ),
            onset = "5 - 10 minutes (insufflated)",
            duration = "30 - 60 minutes",
            halfLife = "1 hour",
            goodEnvironments = listOf(
                "Private, secure spaces where you can communicate comfortably",
                "Surroundings with access to standard health monitoring",
                "Safe, cool conditions to keep heart rate and body temp stable"
            ),
            badEnvironments = listOf(
                "Performing strenuous sports or activities (severe risk of heart attack)",
                "Isolated locations where a cardiac emergency cannot be treated",
                "Chaotic settings that escalate paranoia and anxiety"
            ),
            reagents = listOf(
                ReagentColor("Scott", "Blue precipitate", 0xFF0D47A1),
                ReagentColor("Marquis", "Light Orange/Peach", 0xFFFFCC80),
                ReagentColor("Liebermann", "Yellow/Orange", 0xFFFFB74D)
            ),
            tips = listOf(
                "Avoid combining Cocaine with Alcohol. They combine in the liver to create cocaethylene, which is far more toxic to the heart and liver than either substance alone.",
                "Rinse your nasal passages with sterile saline after use to prevent severe damage to the nasal septum.",
                "Always use a clean, personalized straw. Sharing nasal devices transmits blood-borne viruses like Hepatitis C."
            ),
            deepDive = DeepDiveInfo(
                synthesisOverview = "Sourced from chemical archives: Recreational cocaine is rarely synthesized completely from scratch due to the complexity of constructing the tropane ring stereospecifically. Instead, it is extracted from Erythroxylum coca leaves. The leaves are macerated with dilute alkali (lime or sodium carbonate) and kerosene/diesel to extract cocaine base. The organic layer is back-extracted with dilute sulfuric acid to yield cocaine sulfate. It is oxidized with potassium permanganate to destroy impurities, neutralised to isolate cocaine freebase, and precipitated with HCl and acetone to yield cocaine hydrochloride.",
                redditSafetyFiles = listOf(
                    "r/drugs Warning: Highly dangerous cutting agent Levamisole (a veterinary dewormer) is present in 70-80% of global cocaine supplies. Levamisole cannot be separated via simple washes and causes severe agranulocytosis (loss of white blood cells, wiping out the immune system) and skin necrosis (flesh rotting) with regular use.",
                    "r/ReagentTesting Tip: Scott reagent turns bright blue in the presence of cocaine. Liebermann turning dark red-orange indicates Levamisole or Amphetamine, which is a key warning sign of a heavily cut batch."
                ),
                githubArchives = listOf(
                    "github.com/cardio-toxicology/cocaethylene-model - Cardiovascular model simulating concurrent alcohol and cocaine intake. Demonstration of hepatic synthesis of cocaethylene, which has a 150-minute half-life (compared to cocaine's 60-min half-life) and acts as an extremely potent sodium channel blocker, causing sudden cardiac arrest.",
                    "github.com/open-drug-data/adulterants - Statistical mapping of illicit stimulant impurities, tracking correlations between high-potency caffeine, lidocaine, and levamisole concentrations."
                ),
                chemicalProperties = mapOf(
                    "Formula" to "C17H21NO4",
                    "Melting Point" to "195 °C (Hydrochloride salt)",
                    "Solubility" to "Extremely high (1.8 g/mL in water)",
                    "Form" to "Fine crystalline pearlescent flakes ('fishscale'), white powder, or hard rock freebase ('crack')",
                    "Storage" to "Hygroscopic (absorbs water from air readily). Store in sealed, desiccant-packed, airtight containers."
                )
            )
        ),
        Drug(
            id = "alprazolam",
            name = "Alprazolam (Xanax)",
            chemicalName = "8-Chloro-1-methyl-6-phenyl-4H-[1,2,4]triazolo[4,3-a][1,4]benzodiazepine",
            category = DrugCategory.DEPRESSANT,
            description = "A fast-acting, highly addictive benzodiazepine. It acts as a positive allosteric modulator of GABA-A receptors, heavily suppressing central nervous system activity. Widely prescribed for severe anxiety but heavily abused.",
            streetNames = listOf("Xanax", "Bars", "Zannies", "Planks", "Footballs", "Benzos"),
            dosageOral = DosageInfo(
                threshold = "0.25 mg",
                light = "0.25 - 0.5 mg",
                common = "0.5 - 1.5 mg",
                strong = "1.5 - 3.0 mg",
                warningNote = "Extreme risk of anterograde amnesia ('blackouts'), where users perform complex, reckless tasks without conscious memory. Withdrawal from chronic use can cause fatal seizures."
            ),
            onset = "15 - 30 minutes",
            duration = "5 - 8 hours",
            halfLife = "11 - 15 hours",
            goodEnvironments = listOf(
                "Your private, secure bedroom or home",
                "Safe environments where you do not need to make complex, legal, or social decisions",
                "A quiet, stress-free space with close supervision if necessary"
            ),
            badEnvironments = listOf(
                "Driving or operating any form of vehicle or machinery (extremely lethal)",
                "Social situations where physical coordination and verbal control are required",
                "Unfamiliar public settings where you can easily get taken advantage of during a blackout"
            ),
            reagents = listOf(
                ReagentColor("Zimmermann", "Reddish-Purple (for benzodiazepines)", 0xFFC2185B),
                ReagentColor("Marquis", "No reaction / Pale yellow", 0xFFFFF59D)
            ),
            tips = listOf(
                "Physical dependence develops rapidly. NEVER stop taking benzodiazepines cold-turkey if you use them chronically; do a slow, medically-supervised taper to prevent fatal seizures.",
                "Benzodiazepines are highly prone to causing blackouts. Do not redose, as a common symptom of benzos is 'delusion of sobriety' leading to accidental massive overdoses.",
                "Counterfeit Xanax tablets are widely pressed with high-potency research chemicals (like bromazolam, flualprazolam) or cut with deadly doses of fentanyl. Always test with fentanyl strips."
            ),
            deepDive = DeepDiveInfo(
                synthesisOverview = "Sourced from chemical archives: Synthesis of Alprazolam begins with the reaction of 2-amino-5-chlorobenzophenone with glycine ethyl ester to yield 7-chloro-5-phenyl-1,3-dihydro-2H-1,4-benzodiazepin-2-one. This is treated with phosphorus pentasulfide to convert the carbonyl to a benzodiazepine-2-thione. Finally, reacting this thione with hydrazine followed by acetic anhydride or triethyl orthoacetate constructs the fused 1,2,4-triazole ring characteristic of triazolobenzodiazepines. Highly advanced organic crystallization.",
                redditSafetyFiles = listOf(
                    "r/benzodiazepines Warning: Over 98% of 'street bars' or unprescribed Xanax tablets are pressed counterfeits containing research benzodiazepines like Bromazolam or Flualprazolam, or worse, lethal traces of fentanyl. Fentanyl test strips are non-negotiable for all street benzo purchases.",
                    "r/ReagentTesting Tip: Zimmermann reagent turns reddish-purple for benzos, which can prove the active ingredient is a benzodiazepine but CANNOT distinguish pharmaceutical alprazolam from RC counterfeits. Use fentanyl strips to safeguard life."
                ),
                githubArchives = listOf(
                    "github.com/benzo-taper-calc/manual - Interactive tapering software utilizing Ashton Tapering guidelines. Users transition from high-potency short-acting alprazolam to equivalent doses of long-acting diazepam to slowly reduce receptor strain and prevent fatal status epilepticus seizures.",
                    "github.com/pharmacy-dataset/gaba-interactions - GABAA channel kinetic models demonstrating Alprazolam's exceptionally rapid absorption and high sub-unit affinity, leading to rapid onset of amnesia and severe cognitive disinhibition."
                ),
                chemicalProperties = mapOf(
                    "Formula" to "C17H13ClN4",
                    "Melting Point" to "228-230 °C",
                    "Solubility" to "Nearly insoluble in water, highly soluble in ethanol and chloroform",
                    "Form" to "Pressed rectangular white/green/yellow scored tablets, or crystalline white powder",
                    "Storage" to "Chemically stable. Store at room temperature away from high moisture."
                )
            )
        )
    )

    val interactions = listOf(
        DrugInteraction(
            "mdma", "alprazolam", RiskLevel.CAUTION,
            "Benzodiazepines dull the psychedelic, empathetic, and stimulant effects of MDMA ('kill the roll'). While physically relatively low risk, it can cause severe emotional flatness and memory blackouts."
        ),
        DrugInteraction(
            "mdma", "cocaine", RiskLevel.DANGEROUS,
            "Highly dangerous combination. Both are cardiotoxic stimulants. Cocaine blocks dopamine and serotonin transporters, which can counteract and override the euphoric rolling effects of MDMA, causing users to redose, which severely strains the heart and increases neurotoxicity."
        ),
        DrugInteraction(
            "cocaine", "alprazolam", RiskLevel.DANGEROUS,
            "Mixing stimulants with depressants creates a dangerous 'tug-of-war' on the nervous system. The stimulant hides the depressant's sedation, leading to overconsumption of benzodiazepines, which can cause severe respiratory depression, cardiotoxicity, and violent blackouts once the cocaine wears off."
        ),
        DrugInteraction(
            "ketamine", "alprazolam", RiskLevel.LETHAL,
            "Extremely dangerous combination of two central nervous system depressants. Highly likely to cause immediate loss of consciousness, profound respiratory depression, failure of protective airways, vomiting while unconscious, and fatal asphyxiation."
        ),
        DrugInteraction(
            "ketamine", "cocaine", RiskLevel.CAUTION,
            "Often referred to as 'Calvin Klein' (CK). Mixing a cardiotoxic stimulant with an anesthetic dissociative causes severe cardiovascular stress and increases blood pressure. It can also cause highly unpredictable psychiatric states, mania, or panic."
        ),
        DrugInteraction(
            "mdma", "ketamine", RiskLevel.SYNERGISTIC,
            "Often called 'Kitty Flipping'. It can create intense, highly synergistic psychedelic/dissociative states. Dosage of both should be reduced significantly to avoid physical injury due to anesthesia, disorientation, and temperature-regulation failures."
        ),
        DrugInteraction(
            "lsd", "mdma", RiskLevel.SYNERGISTIC,
            "Known as 'Candy Flipping'. Extremely strong psychedelic synergy. MDMA reduces the anxiety of LSD, but the visual and cognitive intensity is multiplied. Both doses should be lowered, and hydration monitored closely."
        ),
        DrugInteraction(
            "lsd", "ketamine", RiskLevel.SYNERGISTIC,
            "Dissociation combined with psychedelia. Extremely disorienting. Often leads to complete loss of physical grounding and deep mystical/dissociative states. Highly unpredictable and should only be approached with a sober trip sitter in a safe lying posture."
        ),
        DrugInteraction(
            "alprazolam", "alcohol", RiskLevel.LETHAL,
            "Highly lethal. Both are GABA-A agonists. Combining them multiplies their respiratory-suppressant effects, causing the brain to stop signaling the lungs to breathe. It frequently results in immediate death, comas, and irreversible brain damage."
        ),
        DrugInteraction(
            "cocaine", "alcohol", RiskLevel.LETHAL,
            "Lethal cardiotoxicity. Cocaine and alcohol combine in the liver to synthesize cocaethylene, which is exceptionally toxic to heart tissue, drastically increasing the risk of sudden cardiac arrest, stroke, and chronic liver damage."
        ),
        DrugInteraction(
            "ketamine", "alcohol", RiskLevel.LETHAL,
            "Extremely lethal. Alcohol severely potentiates Ketamine's sedative effects, causing intense nausea, vomiting, dizziness, loss of motor control, and immediate unconsciousness, carrying a very high risk of fatal choking on vomit."
        )
    )

    val reagentTests = listOf(
        ReagentTestKit(
            name = "Marquis Reagent",
            description = "The primary, most essential screening test for testing MDMA, methamphetamine, and identifying adulterants.",
            targetSubstances = "MDMA, Amphetamine, Meth, Cathinones, DXM, Heroin",
            instructions = listOf(
                "Scrape a tiny crumb of your sample (the size of a pinhead) onto a white ceramic plate.",
                "Drop 1 drop of Marquis reagent onto the sample. Do not let the bottle nozzle touch the sample.",
                "Observe the color change within the first 30-60 seconds. Purple-black indicates MDMA/MDA. Orange/Red indicates amphetamine/methamphetamine."
            ),
            typicalColorChanges = mapOf(
                "MDMA" to "Deep Purple to Purple-Black",
                "Amphetamine / Methamphetamine" to "Orange to Reddish Brown",
                "DXM" to "Yellow to Greenish-Blue",
                "Heroin" to "Purple-Red to Dark Violet",
                "2C-B" to "Yellow to Green"
            )
        ),
        ReagentTestKit(
            name = "Ehrlich Reagent",
            description = "Indispensable screening test specifically designed to detect lysergamides (LSD) and tryptamines (DMT, Psilocybin), distinguishing them from highly dangerous chemical compounds like NBOMe.",
            targetSubstances = "LSD, DMT, Psilocybin, Melatonin",
            instructions = listOf(
                "Cut a tiny corner of a blotter paper tab or scrape a crumb of powder.",
                "Squeeze exactly 1 drop of Ehrlich onto the test material.",
                "Wait up to 10-15 minutes (reaction can take longer on papers). A purple/pink reaction indicates the presence of an indole (LSD/Tryptamine). No reaction indicates it is not LSD (could be highly toxic NBOMe or empty paper)."
            ),
            typicalColorChanges = mapOf(
                "LSD" to "Light Pink to Dark Purple (Indole positive)",
                "DMT" to "Purple / Indigo",
                "NBOMe compounds" to "No color change (clear / colorless)",
                "DOC / DOM" to "No color change"
            )
        ),
        ReagentTestKit(
            name = "Mecke Reagent",
            description = "Used alongside Marquis to verify MDMA and differentiate opiates.",
            targetSubstances = "MDMA, Heroin, Oxycodone, DXM",
            instructions = listOf(
                "Place a pinhead-sized sample on white ceramic.",
                "Apply 1 drop of Mecke reagent.",
                "Observe the color change. MDMA instantly turns dark green/black."
            ),
            typicalColorChanges = mapOf(
                "MDMA" to "Instant Dark Green to Blue-Black",
                "Heroin" to "Deep Teal / Green",
                "DXM" to "Pale Yellow",
                "Oxycodone" to "Pale Yellowish-Brown"
            )
        ),
        ReagentTestKit(
            name = "Simon's Reagent",
            description = "A two-part test (A and B) used exclusively to distinguish secondary amines (like MDMA or Methamphetamine) from primary amines (like MDA or Amphetamine).",
            targetSubstances = "MDMA (vs MDA), Meth (vs Amphetamine)",
            instructions = listOf(
                "Prepare a small pinhead sample on white ceramic.",
                "Apply 1 drop of Simon's Bottle A.",
                "Immediately apply 1 drop of Simon's Bottle B.",
                "Watch for an instant bright royal blue color. A blue change indicates MDMA or Methamphetamine. No change (remains clear/pale) indicates MDA or Amphetamine."
            ),
            typicalColorChanges = mapOf(
                "MDMA" to "Instant Bright Royal Blue",
                "MDA" to "No reaction / faint grayish",
                "Methamphetamine" to "Instant Bright Royal Blue",
                "Amphetamine" to "No reaction"
            )
        )
    )

    val presetPills = listOf(
        PresetPill("Yellow Tesla", "yellow", "Shield / Tesla Logo", "A highly concentrated MDMA pressed pill, frequently cloned, often containing dangerously high dosages (220-280mg+ of MDMA) or cut with synthetic stimulants.", "mdma", "img_tesla"),
        PresetPill("Blue Punisher", "blue", "Skull / Punisher Logo", "Among the most notoriously high-dose pressed MDMA ecstasy pills ever analyzed. Some tested over 300mg of MDMA. Severe risk of overdose or hyperthermia.", "mdma", "img_punisher"),
        PresetPill("White Xan Bar", "white", "Rectangular Bar / 'XANAX' or '2' imprint", "Frequently pressed counterfeits containing novel research benzodiazepines (bromazolam) or lethal traces of fentanyl. Genuine Pfizer Xanax is rarely found on the street.", "alprazolam", "img_xan"),
        PresetPill("Pink Maserati", "pink", "Maserati Trident Logo", "Common pressed pill containing MDMA, but often cut with caffeine, amphetamines, or synthetic cathinones ('bath salts'). Always test.", "mdma", "img_maserati"),
        PresetPill("Green Transformer", "green", "Transformer Face", "Commonly distributed pressed tablet. Highly variable contents - has been tested to contain methamphetamine instead of MDMA.", "mdma", "img_transformer")
    )
}

data class PresetPill(
    val name: String,
    val color: String,
    val marking: String,
    val description: String,
    val suspectedDrugId: String,
    val imagePlaceholderId: String
)
