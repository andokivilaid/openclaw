package ai.openclaw.app.ui

import ai.openclaw.app.MainViewModel
import ai.openclaw.app.i18n.nativeString
import ai.openclaw.app.ui.chat.rememberChatRealtimeTalkLauncher
import ai.openclaw.app.ui.design.ClawPlainIconButton
import ai.openclaw.app.ui.design.ClawPrimaryButton
import ai.openclaw.app.ui.design.ClawSecondaryButton
import ai.openclaw.app.ui.design.ClawStatus
import ai.openclaw.app.ui.design.ClawStatusPill
import ai.openclaw.app.ui.design.ClawTheme
import ai.openclaw.app.ui.design.TalkWaveform
import ai.openclaw.app.ui.design.TalkWaveformPhase
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Resolves the waveform animation phase based on realtime Talk Mode state.
 */
internal fun resolveVoiceTalkWaveformPhase(
  talkActive: Boolean,
  listening: Boolean,
  speaking: Boolean,
  awaitingAgent: Boolean,
): TalkWaveformPhase =
  when {
    !talkActive -> TalkWaveformPhase.Idle
    speaking -> TalkWaveformPhase.Speaking(level = null)
    awaitingAgent -> TalkWaveformPhase.Thinking
    listening -> TalkWaveformPhase.Listening(level = 0.5f, speechActive = true)
    else -> TalkWaveformPhase.Thinking
  }

/**
 * Dashboard action card widget for Voice Talk mode.
 * Displays current status, quick waveform preview, and triggers the compact Talk modal.
 */
@Composable
internal fun VoiceTalkDashboardWidget(
  viewModel: MainViewModel,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val talkActive by viewModel.talkModeEnabled.collectAsState()
  val talkStatusText by viewModel.talkModeStatusText.collectAsState()
  val listening by viewModel.talkModeListening.collectAsState()
  val speaking by viewModel.talkModeSpeaking.collectAsState()
  val awaitingAgent by viewModel.talkAwaitingAgent.collectAsState()

  val phase = resolveVoiceTalkWaveformPhase(
    talkActive = talkActive,
    listening = listening,
    speaking = speaking,
    awaitingAgent = awaitingAgent,
  )

  Surface(
    onClick = onClick,
    modifier = modifier
      .fillMaxWidth()
      .semantics {
        contentDescription = if (talkActive) {
          nativeString("Voice Talk Active, click to open controls")
        } else {
          nativeString("Voice Talk, click to open controls")
        }
      },
    shape = RoundedCornerShape(ClawTheme.radii.panel),
    color = ClawTheme.colors.surface,
    contentColor = ClawTheme.colors.text,
    border = BorderStroke(1.dp, ClawTheme.colors.border),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Surface(
          modifier = Modifier.size(42.dp),
          shape = CircleShape,
          color = if (talkActive) ClawTheme.colors.accent else Color(0xFF1976D2),
          tonalElevation = 2.dp,
          shadowElevation = 4.dp,
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.GraphicEq,
              contentDescription = null,
              modifier = Modifier.size(24.dp),
              tint = Color.White,
            )
          }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Text(
              text = nativeString("Voice Talk"),
              style = ClawTheme.type.title.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
              color = ClawTheme.colors.text,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            ClawStatusPill(
              text = if (talkActive) talkStatusText else nativeString("Tap to talk"),
              status = if (talkActive) ClawStatus.Success else ClawStatus.Neutral,
              modifier = Modifier,
            )
          }
          Text(
            text = if (talkActive) {
              nativeString("Realtime voice session in progress")
            } else {
              nativeString("Hands-free conversational assistant")
            },
            style = ClawTheme.type.caption,
            color = ClawTheme.colors.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      // Compact waveform visualizer embedded in the widget card
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .height(36.dp),
        shape = RoundedCornerShape(ClawTheme.radii.control),
        color = ClawTheme.colors.canvas.copy(alpha = 0.6f),
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
          contentAlignment = Alignment.Center,
        ) {
          TalkWaveform(
            phase = phase,
            modifier = Modifier
              .fillMaxWidth()
              .height(30.dp),
          )
        }
      }
    }
  }
}

/**
 * Compact modal dialog for Voice Talk mode with active audio visualization
 * and essential session controls (mute/unmute mic, speaker toggle, start/end talk).
 */
@Composable
internal fun VoiceTalkCompactModal(
  viewModel: MainViewModel,
  onDismiss: () -> Unit,
) {
  val talkActive by viewModel.talkModeEnabled.collectAsState()
  val talkStatusText by viewModel.talkModeStatusText.collectAsState()
  val listening by viewModel.talkModeListening.collectAsState()
  val speaking by viewModel.talkModeSpeaking.collectAsState()
  val awaitingAgent by viewModel.talkAwaitingAgent.collectAsState()
  val speakerEnabled by viewModel.speakerEnabled.collectAsState()
  val startTalkLauncher = rememberChatRealtimeTalkLauncher(viewModel)

  val phase = resolveVoiceTalkWaveformPhase(
    talkActive = talkActive,
    listening = listening,
    speaking = speaking,
    awaitingAgent = awaitingAgent,
  )

  FoldAwareDialog(
    onDismissRequest = onDismiss,
    title = nativeString("Voice Talk Mode"),
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp),
      shape = RoundedCornerShape(ClawTheme.radii.sheet),
      color = ClawTheme.colors.surfaceRaised,
      contentColor = ClawTheme.colors.text,
      tonalElevation = 6.dp,
      shadowElevation = 12.dp,
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Modal Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .background(
                  color = if (talkActive) ClawTheme.colors.success else ClawTheme.colors.textMuted,
                  shape = CircleShape,
                ),
            )
            Text(
              text = nativeString("Voice Talk Mode"),
              style = ClawTheme.type.title.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
              color = ClawTheme.colors.text,
            )
          }
          ClawPlainIconButton(
            icon = Icons.Default.Close,
            contentDescription = nativeString("Close"),
            onClick = onDismiss,
          )
        }

        // Waveform container
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
          shape = RoundedCornerShape(ClawTheme.radii.button),
          color = ClawTheme.colors.canvas.copy(alpha = 0.8f),
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            contentAlignment = Alignment.Center,
          ) {
            TalkWaveform(
              phase = phase,
              modifier = Modifier
                .fillMaxWidth()
                .height(76.dp),
            )
          }
        }

        // Status description
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = if (talkActive) talkStatusText else nativeString("Talk Mode Inactive"),
            style = ClawTheme.type.body.copy(fontWeight = FontWeight.SemiBold),
            color = if (talkActive) ClawTheme.colors.accent else ClawTheme.colors.textMuted,
          )
          Text(
            text = when {
              !talkActive -> nativeString("Start talk to begin realtime audio conversation.")
              speaking -> nativeString("Assistant is speaking…")
              listening -> nativeString("Listening to your voice…")
              awaitingAgent -> nativeString("Waiting for response…")
              else -> nativeString("Ready")
            },
            style = ClawTheme.type.caption,
            color = ClawTheme.colors.textSubtle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        // Controls: Talk Toggle, Speaker Toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          // Mic / Talk Mode Toggle Button
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Surface(
              onClick = {
                if (talkActive) {
                  viewModel.setTalkModeEnabled(false)
                } else {
                  startTalkLauncher()
                }
              },
              modifier = Modifier.size(ClawTheme.spacing.touchTarget),
              shape = CircleShape,
              color = if (!talkActive) ClawTheme.colors.dangerSoft else ClawTheme.colors.surfacePressed,
              contentColor = if (!talkActive) ClawTheme.colors.danger else ClawTheme.colors.text,
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = if (talkActive) Icons.Default.Mic else Icons.Default.MicOff,
                  contentDescription = if (talkActive) nativeString("Turn off Talk Mode") else nativeString("Turn on Talk Mode"),
                  modifier = Modifier.size(20.dp),
                )
              }
            }
            Text(
              text = if (talkActive) nativeString("Mic Active") else nativeString("Mic Off"),
              style = ClawTheme.type.caption.copy(fontSize = 11.sp),
              color = ClawTheme.colors.textMuted,
            )
          }

          // Speaker On / Mute Button
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Surface(
              onClick = {
                viewModel.setSpeakerEnabled(!speakerEnabled)
              },
              modifier = Modifier.size(ClawTheme.spacing.touchTarget),
              shape = CircleShape,
              color = if (!speakerEnabled) ClawTheme.colors.warningSoft else ClawTheme.colors.surfacePressed,
              contentColor = if (!speakerEnabled) ClawTheme.colors.warning else ClawTheme.colors.text,
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = if (speakerEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                  contentDescription = if (speakerEnabled) nativeString("Mute speaker") else nativeString("Enable speaker"),
                  modifier = Modifier.size(20.dp),
                )
              }
            }
            Text(
              text = if (speakerEnabled) nativeString("Speaker") else nativeString("Muted"),
              style = ClawTheme.type.caption.copy(fontSize = 11.sp),
              color = ClawTheme.colors.textMuted,
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Primary Action: Start / End Call
        if (talkActive) {
          ClawSecondaryButton(
            text = nativeString("End Talk"),
            onClick = {
              viewModel.setTalkModeEnabled(false)
            },
            modifier = Modifier.fillMaxWidth(),
          )
        } else {
          ClawPrimaryButton(
            text = nativeString("Start Talk"),
            onClick = {
              startTalkLauncher()
            },
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    }
  }
}
