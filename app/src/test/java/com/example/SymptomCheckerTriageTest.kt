package com.example

import com.example.ui.components.*
import org.junit.Assert.*
import org.junit.Test

class SymptomCheckerTriageTest {

    @Test
    fun testIdleStateWhenNoSignsEntered() {
        val result = evaluatePhysiologicalSigns(
            consciousness = null,
            breathing = null,
            pulse = null,
            skinTemp = null,
            pupils = null,
            motorSigns = emptySet()
        )
        assertEquals(TriageEmergencyLevel.IDLE, result.level)
        assertFalse(result.showNaloxonePrompt)
    }

    @Test
    fun testCriticalOpioidOverdoseDetection() {
        val result = evaluatePhysiologicalSigns(
            consciousness = ConsciousnessState.UNRESPONSIVE,
            breathing = BreathingSign.SLOW_SHALLOW,
            pulse = PulseSign.BRADYCARDIA,
            skinTemp = SkinTempSign.CYANOTIC_COLD,
            pupils = PupilReaction.PINPOINT,
            motorSigns = emptySet()
        )
        assertEquals(TriageEmergencyLevel.CRITICAL, result.level)
        assertTrue(result.showNaloxonePrompt)
        assertTrue(result.showRecoveryPositionPrompt)
        assertTrue(result.primarySyndrome.contains("OPIOID", ignoreCase = true))
        assertTrue(result.dispatcherScript.contains("unresponsive", ignoreCase = true))
    }

    @Test
    fun testCriticalSeizureEmergency() {
        val result = evaluatePhysiologicalSigns(
            consciousness = ConsciousnessState.CONFUSED,
            breathing = BreathingSign.NORMAL,
            pulse = PulseSign.NORMAL,
            skinTemp = SkinTempSign.NORMAL,
            pupils = PupilReaction.NORMAL,
            motorSigns = setOf(MotorSymptom.ACTIVE_SEIZURES)
        )
        assertEquals(TriageEmergencyLevel.CRITICAL, result.level)
        assertTrue(result.showSeizurePrompt)
        assertTrue(result.primarySyndrome.contains("SEIZURE", ignoreCase = true))
    }

    @Test
    fun testHyperthermiaCriticalCooling() {
        val result = evaluatePhysiologicalSigns(
            consciousness = ConsciousnessState.CONFUSED,
            breathing = BreathingSign.NORMAL,
            pulse = PulseSign.TACHYCARDIA,
            skinTemp = SkinTempSign.HYPERTHERMIC_HOT,
            pupils = PupilReaction.DILATED,
            motorSigns = setOf(MotorSymptom.MUSCLE_RIGIDITY)
        )
        assertEquals(TriageEmergencyLevel.CRITICAL, result.level)
        assertTrue(result.showCoolingPrompt)
    }

    @Test
    fun testModeratePanicAndHyperventilation() {
        val result = evaluatePhysiologicalSigns(
            consciousness = ConsciousnessState.ALERT,
            breathing = BreathingSign.RAPID_HYPERVENTILATE,
            pulse = PulseSign.TACHYCARDIA,
            skinTemp = SkinTempSign.NORMAL,
            pupils = PupilReaction.NORMAL,
            motorSigns = setOf(MotorSymptom.PANIC_TERROR)
        )
        assertEquals(TriageEmergencyLevel.MODERATE, result.level)
        assertTrue(result.showBreathingPacer)
        assertFalse(result.showNaloxonePrompt)
    }

    @Test
    fun testStablePhysiologicalState() {
        val result = evaluatePhysiologicalSigns(
            consciousness = ConsciousnessState.ALERT,
            breathing = BreathingSign.NORMAL,
            pulse = PulseSign.NORMAL,
            skinTemp = SkinTempSign.NORMAL,
            pupils = PupilReaction.NORMAL,
            motorSigns = emptySet()
        )
        assertEquals(TriageEmergencyLevel.STABLE, result.level)
        assertFalse(result.showNaloxonePrompt)
        assertFalse(result.showBreathingPacer)
    }
}
