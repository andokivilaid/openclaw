package ai.openclaw.app

import ai.openclaw.app.ui.OpenClawTheme
import ai.openclaw.app.ui.VoiceTalkCompactModal
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Translucent floating overlay activity for Voice Talk mode.
 * Immediately activates Talk mode on start and provides compact floating controls.
 * Closing or dismissing the overlay leaves Talk mode running in the background.
 */
class VoiceTalkOverlayActivity : AppCompatActivity() {
  private val viewModel: MainViewModel by viewModels()
  private val permissionRequester: PermissionRequester
    get() = (application as NodeApp).permissionRequester
  private var initializedViewModel: MainViewModel? = null
  private var foreground = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    permissionRequester.attach(this)

    setContent {
      var activeViewModel by remember { mutableStateOf<MainViewModel?>(null) }

      LaunchedEffect(Unit) {
        withFrameNanos { }
        withContext(Dispatchers.Default) {
          (application as NodeApp).prefs
        }
        val readyViewModel = viewModel
        activateViewModel(readyViewModel)
        activeViewModel = readyViewModel
        readyViewModel.setTalkModeEnabled(true)
      }

      val currentViewModel = activeViewModel
      if (currentViewModel != null) {
        val appearanceThemeMode by currentViewModel.appearanceThemeMode.collectAsState()
        OpenClawTheme(themeMode = appearanceThemeMode) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.Black.copy(alpha = 0.5f))
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { finish() },
              )
              .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
          ) {
            Box(
              modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* prevent outside click dismiss */ },
              ),
            ) {
              VoiceTalkCompactModal(
                viewModel = currentViewModel,
                onDismiss = { finish() },
              )
            }
          }
        }
      }
    }
  }

  override fun onStart() {
    super.onStart()
    foreground = true
    initializedViewModel?.setForeground(true)
  }

  override fun onTopResumedActivityChanged(isTopResumedActivity: Boolean) {
    super.onTopResumedActivityChanged(isTopResumedActivity)
    updateTopResumedPermissionHost(
      isTopResumedActivity = isTopResumedActivity,
      activate = { permissionRequester.activate(this) },
      deactivate = { permissionRequester.deactivate(this) },
      refreshPermissionSurface = { initializedViewModel?.refreshNodePermissionSurface() },
    )
  }

  override fun onStop() {
    permissionRequester.deactivate(this)
    foreground = false
    if (shouldNotifyRuntimeBackgrounded(isChangingConfigurations)) {
      initializedViewModel?.setForeground(false)
    }
    super.onStop()
  }

  override fun onDestroy() {
    permissionRequester.detach(this)
    super.onDestroy()
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray,
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    permissionRequester.onRequestPermissionsResult(requestCode, permissions, grantResults)
    initializedViewModel?.refreshNodePermissionSurface()
  }

  private fun activateViewModel(readyViewModel: MainViewModel) {
    if (initializedViewModel != null) return
    initializedViewModel = readyViewModel
    readyViewModel.setForeground(foreground)
    readyViewModel.attachRuntimeUi(owner = this, permissionRequester = permissionRequester)
    NodeForegroundService.start(this)
  }
}
