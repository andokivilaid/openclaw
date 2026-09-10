package ai.openclaw.app.ui

import ai.openclaw.app.ui.design.TalkWaveformPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VoiceTalkDashboardWidgetTest {
  @Test
  fun resolvePhaseWhenTalkInactiveReturnsIdle() {
    val phase =
      resolveVoiceTalkWaveformPhase(
        talkActive = false,
        listening = false,
        speaking = false,
        awaitingAgent = false,
      )
    assertEquals(TalkWaveformPhase.Idle, phase)
  }

  @Test
  fun resolvePhaseWhenSpeakingReturnsSpeakingPhase() {
    val phase =
      resolveVoiceTalkWaveformPhase(
        talkActive = true,
        listening = false,
        speaking = true,
        awaitingAgent = false,
      )
    assertTrue(phase is TalkWaveformPhase.Speaking)
  }

  @Test
  fun resolvePhaseWhenAwaitingAgentReturnsThinkingPhase() {
    val phase =
      resolveVoiceTalkWaveformPhase(
        talkActive = true,
        listening = false,
        speaking = false,
        awaitingAgent = true,
      )
    assertEquals(TalkWaveformPhase.Thinking, phase)
  }

  @Test
  fun resolvePhaseWhenListeningReturnsListeningPhase() {
    val phase =
      resolveVoiceTalkWaveformPhase(
        talkActive = true,
        listening = true,
        speaking = false,
        awaitingAgent = false,
      )
    assertTrue(phase is TalkWaveformPhase.Listening)
    val listeningPhase = phase as TalkWaveformPhase.Listening
    assertTrue(listeningPhase.speechActive)
    assertTrue(listeningPhase.level > 0f)
  }

  @Test
  fun resolvePhaseWhenActiveButIdleReturnsThinkingSwell() {
    val phase =
      resolveVoiceTalkWaveformPhase(
        talkActive = true,
        listening = false,
        speaking = false,
        awaitingAgent = false,
      )
    assertEquals(TalkWaveformPhase.Thinking, phase)
  }
}
