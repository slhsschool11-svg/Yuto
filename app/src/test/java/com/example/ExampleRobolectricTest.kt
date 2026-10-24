package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ProjectRepository
import com.example.model.ChatMessage
import com.example.model.MessageRole
import com.example.model.VideoFormatMode
import com.example.service.GeminiClientService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Yuto", appName)
  }

  @Test
  fun `verify video project creation and audits`() {
    val repo = ProjectRepository()
    val project = repo.currentProject.value
    assertEquals("YouTube AI Automation Blueprint", project.videoTitle)
    assertEquals(VideoFormatMode.LONG_FORM_16_9, project.formatMode)

    val audits = repo.calculateGrowthAudits(project)
    assertTrue("Audits should contain multiple YouTube algorithm checks", audits.size >= 5)
  }

  @Test
  fun `verify GeminiClientService service layer initialization and metadata generation`() = runBlocking {
    val service = GeminiClientService()
    assertNotNull(service.getApiKey())

    // Test title generator
    val titles = service.generateTitleSuggestions("Automate YouTube Channel", "Tech & AI")
    assertTrue("Should generate title suggestions", titles.isNotEmpty())
    assertTrue("Title should have predicted CTR", titles.first().predictedCtr > 0)

    // Test tag generator
    val tags = service.generateTagSuggestions("Automate YouTube Channel", "Tech & AI")
    assertTrue("Should generate tag suggestions", tags.isNotEmpty())

    // Test multi-turn chat response
    val history = listOf(ChatMessage(role = MessageRole.USER, content = "Hi Yuto"))
    val reply = service.sendChatMessage(history, "Suggest high CTR titles")
    assertTrue("Chat reply should be non-empty", reply.isNotBlank())
  }

  @Test
  fun `verify YouTubeApiService autonomous upload workflow execution`() = runBlocking {
    val repo = ProjectRepository()
    val initialProject = repo.currentProject.value
    repo.runAutoPilotWorkflow()
    val updatedProject = repo.currentProject.value

    assertEquals(com.example.model.ProjectState.PUBLISHED, updatedProject.state)
    assertTrue("Should have high SEO score", updatedProject.seoScore >= 90)
    assertTrue("Should have non-empty description", updatedProject.description.isNotBlank())
    assertTrue("Should have viral tags", updatedProject.tags.isNotEmpty())
  }

  @Test
  fun `verify GoogleAccountSession and YouTube login privacy state`() {
    val repo = ProjectRepository()
    val session = repo.googleSession.value
    assertTrue("Should be signed in by default", session.isSignedIn)
    assertTrue("Should contain upload scope", session.oauthScopesGranted.any { it.contains("upload") })

    repo.toggleGoogleSignIn(false)
    assertEquals(false, repo.googleSession.value.isSignedIn)
    assertEquals(false, repo.channelProfile.value.isConnected)

    repo.toggleGoogleSignIn(true)
    assertEquals(true, repo.googleSession.value.isSignedIn)
  }
}
