package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Class representing interactive spore particles
data class SporeParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Color,
    var alpha: Float,
    var size: Float,
    var life: Float,      // 1.0 down to 0.0
    var decay: Float
)

// Class representing Acid Fountain droplets
data class AcidDroplet(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float,
    var size: Float,
    var color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JungleLandingScreen(
    userEmail: String,
    onEnterApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    // Core animation frames
    val infiniteTransition = rememberInfiniteTransition(label = "jungle_animations")
    
    // Pulse scale of the entry card
    val cardPulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_pulse"
    )

    // Glow alpha of the forest background
    val forestGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "forest_glow"
    )

    // DMT molecule continuous rotation angle
    val dmtRotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dmt_rotation"
    )

    // Spore and Acid particles state
    val spores = remember { mutableStateListOf<SporeParticle>() }
    val acidDroplets = remember { mutableStateListOf<AcidDroplet>() }

    // Spawn & physics simulator
    LaunchedEffect(Unit) {
        var frameCount = 0
        while (isActive) {
            frameCount++
            
            // 1. UPDATE SPORES
            val iterator = spores.iterator()
            while (iterator.hasNext()) {
                val spore = iterator.next()
                spore.x += spore.vx
                spore.y += spore.vy
                spore.vy += 0.05f // slight gravity drift
                spore.vx += (Random.nextFloat() - 0.5f) * 0.2f // wind wobble
                spore.life -= spore.decay
                spore.alpha = spore.life
                if (spore.life <= 0f) {
                    iterator.remove()
                }
            }

            // 2. SPAWN ACID DROPLETS FROM FOUNTAINS
            // Fountain 1: Left (x = screenWidth * 0.15, bottom)
            // Fountain 2: Right (x = screenWidth * 0.85, bottom)
            if (frameCount % 2 == 0) {
                // Left fountain: shooting rightwards and up
                acidDroplets.add(
                    AcidDroplet(
                        x = 150f,
                        y = 1200f,
                        vx = (Random.nextFloat() * 4f) + 3f,
                        vy = -(Random.nextFloat() * 12f) - 14f,
                        alpha = 1.0f,
                        size = (Random.nextFloat() * 6f) + 4f,
                        color = Color(0xFF00FFCC) // Neon Cyan
                    )
                )
                // Right fountain: shooting leftwards and up
                acidDroplets.add(
                    AcidDroplet(
                        x = 900f,
                        y = 1200f,
                        vx = -((Random.nextFloat() * 4f) + 3f),
                        vy = -(Random.nextFloat() * 12f) - 14f,
                        alpha = 1.0f,
                        size = (Random.nextFloat() * 6f) + 4f,
                        color = Color(0xFFFF007F) // Neon Pink
                    )
                )
                // Center fountain (pure acid yellow)
                acidDroplets.add(
                    AcidDroplet(
                        x = 540f,
                        y = 1350f,
                        vx = (Random.nextFloat() - 0.5f) * 3f,
                        vy = -(Random.nextFloat() * 8f) - 18f,
                        alpha = 1.0f,
                        size = (Random.nextFloat() * 7f) + 5f,
                        color = Color(0xFFCCFF00) // Neon Yellow/Lime
                    )
                )
            }

            // 3. UPDATE ACID DROPLETS
            val acidIterator = acidDroplets.iterator()
            while (acidIterator.hasNext()) {
                val drop = acidIterator.next()
                drop.x += drop.vx
                drop.y += drop.vy
                drop.vy += 0.6f // strong gravity for liquid droplets
                drop.alpha -= 0.015f // evaporate as they fall
                if (drop.alpha <= 0f || drop.y > 1600f) {
                    acidIterator.remove()
                }
            }

            delay(16) // ~60 FPS frame ticks
        }
    }

    // Interactive tap triggers a burst of spores from the mushrooms
    fun triggerSporeBurst(x: Float, y: Float) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        val colors = listOf(Color(0xFF00FFCC), Color(0xFFFF007F), Color(0xFFCCFF00), Color(0xFFB026FF), Color.White)
        repeat(25) {
            spores.add(
                SporeParticle(
                    x = x,
                    y = y,
                    vx = (Random.nextFloat() - 0.5f) * 8f,
                    vy = (Random.nextFloat() - 0.7f) * 6f - 3f,
                    color = colors.random(),
                    alpha = 1.0f,
                    size = (Random.nextFloat() * 8f) + 5f,
                    life = 1.0f,
                    decay = (Random.nextFloat() * 0.02f) + 0.01f
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF050811)) // Deep cosmic dark green-blue
    ) {
        // Starry night / nebulous background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0D1B2A).copy(alpha = 0.8f),
                            Color(0xFF081C15).copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
        )

        // The master custom drawing canvas: sways, vines, fountains, DMT, mushrooms, spores
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // Detect click proximity to mushrooms to trigger spore bursts
                        triggerSporeBurst(offset.x, offset.y)
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. DRAW GLOWING BACKDROP JUNGLE VINES (using Sine curve wave lines)
            val vineCount = 6
            for (i in 0 until vineCount) {
                val path = Path()
                val startX = width * (i + 1) / (vineCount + 1)
                path.moveTo(startX, 0f)
                
                // Draw a swaying vine path down to 70% of screen height
                val segments = 20
                for (j in 1..segments) {
                    val progress = j.toFloat() / segments
                    val y = height * 0.7f * progress
                    // Sway factor combines forestGlow for gentle animation
                    val sway = sin((progress * 4 * Math.PI) + (forestGlow * 2 * Math.PI)).toFloat() * 15f
                    val x = startX + sway
                    path.lineTo(x, y)
                }

                // Draw the vine
                drawPath(
                    path = path,
                    color = Color(0xFF00FFCC).copy(alpha = 0.12f + (i * 0.02f)),
                    style = Stroke(width = 4f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 15f), 0f))
                )

                // Draw tiny glowing leaf nodes along the vine
                for (j in 1..segments step 4) {
                    val progress = j.toFloat() / segments
                    val y = height * 0.7f * progress
                    val sway = sin((progress * 4 * Math.PI) + (forestGlow * 2 * Math.PI)).toFloat() * 15f
                    val x = startX + sway
                    drawCircle(
                        color = Color(0xFFFF007F).copy(alpha = forestGlow * 0.4f),
                        radius = 8f,
                        center = Offset(x, y)
                    )
                }
            }

            // 2. DRAW THE ROTATING DMT MOLECULE (Tryptamine core)
            // Centered in upper half: (width * 0.5, height * 0.28)
            val dmtCenter = Offset(width * 0.5f, height * 0.28f)
            val dmtScale = 55f

            rotate(degrees = dmtRotationAngle, pivot = dmtCenter) {
                // Indigo/Cyan glow behind molecule
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFB026FF).copy(alpha = 0.15f), Color.Transparent),
                        center = dmtCenter,
                        radius = dmtScale * 3.5f
                    ),
                    center = dmtCenter,
                    radius = dmtScale * 3.5f
                )

                // Define 2D projected coordinates for DMT (Benzene ring + Pyrrole ring + side chain)
                // Benzene ring (6 vertices)
                val b0 = Offset(0f, -1f)
                val b1 = Offset(0.86f, -0.5f)
                val b2 = Offset(0.86f, 0.5f)
                val b3 = Offset(0f, 1f)
                val b4 = Offset(-0.86f, 0.5f)
                val b5 = Offset(-0.86f, -0.5f)

                // Pyrrole ring (shares b0 and b1, adds 3 vertices to the right)
                val p0 = Offset(1.5f, -1.0f) // Nitrogen atom in pyrrole
                val p1 = Offset(2.0f, -0.2f)
                val p2 = Offset(1.5f, 0.6f)

                // Ethylamine chain extending from benzene b4 (bottom left)
                val c0 = Offset(-1.6f, 1.0f)
                val c1 = Offset(-2.3f, 0.6f)
                val n0 = Offset(-3.0f, 1.2f) // Nitrogen atom of chain
                val m0 = Offset(-3.6f, 0.8f) // Methyl 1
                val m1 = Offset(-3.1f, 2.1f) // Methyl 2

                val allNodes = listOf(
                    b0, b1, b2, b3, b4, b5, p0, p1, p2, c0, c1, n0, m0, m1
                ).map { node ->
                    Offset(dmtCenter.x + node.x * dmtScale, dmtCenter.y + node.y * dmtScale)
                }

                // Draw molecular bonds (connections)
                val connections = listOf(
                    Pair(0, 1), Pair(1, 2), Pair(2, 3), Pair(3, 4), Pair(4, 5), Pair(5, 0), // Benzene Ring
                    Pair(1, 6), Pair(6, 7), Pair(7, 8), Pair(8, 2), // Pyrrole Ring
                    Pair(4, 9), Pair(9, 10), Pair(10, 11), Pair(11, 12), Pair(11, 13) // Side chain & methyls
                )

                connections.forEach { (startIdx, endIdx) ->
                    drawLine(
                        color = Color(0xFF00FFCC).copy(alpha = 0.85f),
                        start = allNodes[startIdx],
                        end = allNodes[endIdx],
                        strokeWidth = 5f
                    )
                }

                // Draw atoms as glowing nodes (Nitrogens in magenta, Carbons in white/cyan)
                allNodes.forEachIndexed { idx, offset ->
                    val isNitrogen = idx == 6 || idx == 11
                    val nodeColor = if (isNitrogen) Color(0xFFFF007F) else Color.White
                    val nodeRadius = if (isNitrogen) 12f else 8f

                    // Atom outer glow
                    drawCircle(
                        color = nodeColor.copy(alpha = 0.4f),
                        radius = nodeRadius * 2f,
                        center = offset
                    )
                    // Atom solid center
                    drawCircle(
                        color = nodeColor,
                        radius = nodeRadius,
                        center = offset
                    )
                }
            }

            // 3. DRAW GLOWING INTERACTIVE JUNGLE SHROOMS (along bottom edges and rocks)
            // Shroom 1 (Left): Center on (width * 0.25f, height * 0.82f)
            // Shroom 2 (Right): Center on (width * 0.75f, height * 0.84f)
            // Shroom 3 (Mini): Center on (width * 0.45f, height * 0.85f)
            val mushrooms = listOf(
                Triple(width * 0.22f, height * 0.83f, 75f),
                Triple(width * 0.78f, height * 0.85f, 65f),
                Triple(width * 0.48f, height * 0.87f, 40f)
            )

            mushrooms.forEach { (shroomX, shroomY, r) ->
                // Mushroom Cap Glow (pulsing with forestGlow)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFF007F).copy(alpha = forestGlow * 0.3f), Color.Transparent),
                        center = Offset(shroomX, shroomY - r * 0.3f),
                        radius = r * 2f
                    ),
                    center = Offset(shroomX, shroomY - r * 0.3f),
                    radius = r * 2f
                )

                // Stem Path
                val stemPath = Path()
                stemPath.moveTo(shroomX - r * 0.2f, shroomY)
                stemPath.quadraticTo(shroomX - r * 0.1f, shroomY - r * 0.6f, shroomX - r * 0.15f, shroomY - r * 0.8f)
                stemPath.lineTo(shroomX + r * 0.15f, shroomY - r * 0.8f)
                stemPath.quadraticTo(shroomX + r * 0.1f, shroomY - r * 0.6f, shroomX + r * 0.2f, shroomY)
                stemPath.close()

                drawPath(
                    path = stemPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF00FFCC).copy(alpha = 0.8f), Color(0xFF023E3A).copy(alpha = 0.5f)),
                        startY = shroomY - r * 0.8f,
                        endY = shroomY
                    )
                )

                // Cap (Semicircle / Arc cap)
                val capPath = Path()
                capPath.moveTo(shroomX - r * 0.8f, shroomY - r * 0.8f)
                // Curve top cap
                capPath.cubicTo(
                    shroomX - r * 0.7f, shroomY - r * 1.6f,
                    shroomX + r * 0.7f, shroomY - r * 1.6f,
                    shroomX + r * 0.8f, shroomY - r * 0.8f
                )
                // Bottom curve lip
                capPath.quadraticTo(shroomX, shroomY - r * 0.75f, shroomX - r * 0.8f, shroomY - r * 0.8f)
                capPath.close()

                drawPath(
                    path = capPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFF007F), Color(0xFF88004F)),
                        startY = shroomY - r * 1.5f,
                        endY = shroomY - r * 0.8f
                    )
                )

                // Glowing spots on the cap
                val spots = listOf(
                    Offset(shroomX, shroomY - r * 1.2f),
                    Offset(shroomX - r * 0.3f, shroomY - r * 1.0f),
                    Offset(shroomX + r * 0.3f, shroomY - r * 1.0f),
                    Offset(shroomX - r * 0.15f, shroomY - r * 1.35f),
                    Offset(shroomX + r * 0.15f, shroomY - r * 1.35f)
                )
                spots.forEach { spotOffset ->
                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f),
                        radius = r * 0.08f,
                        center = spotOffset
                    )
                }
            }

            // 4. DRAW STREAMING FOUNTAINS OF ACID
            // We draw the liquid reservoir line at the bottom
            val waterLevelY = height * 0.92f
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00FFCC).copy(alpha = 0.25f), Color(0xFFFF007F).copy(alpha = 0.1f)),
                    startY = waterLevelY,
                    endY = height
                ),
                topLeft = Offset(0f, waterLevelY),
                size = Size(width, height - waterLevelY)
            )

            // Draw current active acid droplets
            acidDroplets.forEach { drop ->
                drawCircle(
                    color = drop.color.copy(alpha = drop.alpha),
                    radius = drop.size,
                    center = Offset(drop.x, drop.y)
                )
                // Draw little tail/streak for falling speed
                drawLine(
                    color = drop.color.copy(alpha = drop.alpha * 0.4f),
                    start = Offset(drop.x, drop.y),
                    end = Offset(drop.x - drop.vx * 0.8f, drop.y - drop.vy * 0.8f),
                    strokeWidth = drop.size * 0.5f
                )
            }

            // 5. DRAW ACTIVE PULSATING SPORES
            spores.forEach { spore ->
                drawCircle(
                    color = spore.color.copy(alpha = spore.alpha),
                    radius = spore.size * spore.life,
                    center = Offset(spore.x, spore.y)
                )
            }
        }

        // Tap instructions overlay (Floating gently)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.55f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = Color(0xFFFF007F),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TAP SCREEN TO RELEASE PSYCHEDELIC SPORES",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.85f),
                    letterSpacing = 1.sp
                )
            }
        }

        // BOTTOM OVERLAY PORTAL CARD (TurnApp styling layout)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(
                        scaleX = cardPulse,
                        scaleY = cardPulse
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0A0E1A).copy(alpha = 0.92f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF00FFCC), Color(0xFFFF007F))
                    )
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFF007F).copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = null,
                                tint = Color(0xFFFF007F),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AUTHENTICATED: $userEmail",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "The Psychedelic Oasis",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "You are entering the ultimate harm reduction sanctuary. Explore spot-testing simulators, safety manuals, drug interactions, and community checking databases.",
                        fontSize = 12.sp,
                        color = Color.LightGray.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onEnterApp()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00FFCC),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("enter_jungle_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "ENTER HARM REDUCTION OASIS",
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
