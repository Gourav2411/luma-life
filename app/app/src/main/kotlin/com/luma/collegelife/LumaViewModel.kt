package com.luma.collegelife

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.luma.ai.AssistantInput
import com.luma.ai.AssistantTurnRequest
import com.luma.ai.AssistantTurnResponse
import com.luma.ai.DemoAssistantGateway
import com.luma.data.PlanRepository
import com.luma.data.local.LumaDatabase
import com.luma.model.ChatMessage
import com.luma.model.EnergyLevel
import com.luma.model.LifeArea
import com.luma.model.PlanHorizon
import com.luma.model.PlanItem
import com.luma.model.PlanProposal
import com.luma.model.PlanSource
import com.luma.model.PlanStatus
import com.luma.model.Reflection
import com.luma.model.Flexibility
import com.luma.planning.DeterministicScheduler
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Application.preferences by preferencesDataStore("luma_preferences")

enum class EntryStage { SPLASH, WELCOME, AUTH, PERMISSIONS, SETUP, MAIN }
enum class MainDestination { TODAY, PLANS, GROW, LUMA }
enum class OverlayScreen { NONE, PLAN_REVIEW, FOCUS, SETTINGS, CALENDAR, MEMORY, REFLECTION }

data class SetupState(
    val name: String = "",
    val collegeStage: String = "2nd year",
    val direction: String = "",
    val selectedAreas: Set<LifeArea> = LifeArea.entries.toSet(),
)

class LumaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PlanRepository(LumaDatabase.getInstance(application).planItemDao())
    private val assistant = DemoAssistantGateway(DeterministicScheduler())

    private val _entryStage = MutableStateFlow(EntryStage.SPLASH)
    val entryStage: StateFlow<EntryStage> = _entryStage.asStateFlow()

    private val _destination = MutableStateFlow(MainDestination.TODAY)
    val destination: StateFlow<MainDestination> = _destination.asStateFlow()

    private val _overlay = MutableStateFlow(OverlayScreen.NONE)
    val overlay: StateFlow<OverlayScreen> = _overlay.asStateFlow()

    private val _items = MutableStateFlow<List<PlanItem>>(emptyList())
    val items: StateFlow<List<PlanItem>> = _items.asStateFlow()

    private val _energy = MutableStateFlow(EnergyLevel.STEADY)
    val energy: StateFlow<EnergyLevel> = _energy.asStateFlow()

    private val _setup = MutableStateFlow(SetupState())
    val setup: StateFlow<SetupState> = _setup.asStateFlow()

    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input.asStateFlow()

    private val _messages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = "welcome-message",
                isUser = false,
                text = "Tell me the whole situation—exam, energy, football, date, commute, anything. I’ll propose a feasible plan and explain every change.",
                createdAt = Instant.now().toString(),
            ),
        ),
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _lastResponse = MutableStateFlow<AssistantTurnResponse?>(null)
    val lastResponse: StateFlow<AssistantTurnResponse?> = _lastResponse.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _selectedChanges = MutableStateFlow<Set<String>>(emptySet())
    val selectedChanges: StateFlow<Set<String>> = _selectedChanges.asStateFlow()

    private val _reflections = MutableStateFlow<List<Reflection>>(emptyList())
    val reflections: StateFlow<List<Reflection>> = _reflections.asStateFlow()

    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice.asStateFlow()

    init {
        viewModelScope.launch {
            val complete = application.preferences.data.first()[booleanPreferencesKey("onboarding_complete")] == true
            delay(850)
            _entryStage.value = if (complete) EntryStage.MAIN else EntryStage.WELCOME
        }
        viewModelScope.launch {
            if (repository.isEmpty()) repository.save(DemoData.dayItems())
            repository.observeDay(DemoData.today).collect { _items.value = it }
        }
    }

    fun advanceEntry() {
        _entryStage.value = when (_entryStage.value) {
            EntryStage.SPLASH -> EntryStage.WELCOME
            EntryStage.WELCOME -> EntryStage.AUTH
            EntryStage.AUTH -> EntryStage.PERMISSIONS
            EntryStage.PERMISSIONS -> EntryStage.SETUP
            EntryStage.SETUP, EntryStage.MAIN -> EntryStage.MAIN
        }
    }

    fun useProductDemo() {
        _notice.value = "Product demo uses local Room storage. No account was created."
        _entryStage.value = EntryStage.PERMISSIONS
    }

    fun completeSetup() {
        viewModelScope.launch {
            getApplication<Application>().preferences.edit {
                it[booleanPreferencesKey("onboarding_complete")] = true
            }
            _entryStage.value = EntryStage.MAIN
        }
    }

    fun updateSetup(transform: (SetupState) -> SetupState) {
        _setup.value = transform(_setup.value)
    }

    fun selectDestination(destination: MainDestination) {
        _overlay.value = OverlayScreen.NONE
        _destination.value = destination
    }

    fun showOverlay(screen: OverlayScreen) {
        _overlay.value = screen
    }

    fun closeOverlay() {
        _overlay.value = OverlayScreen.NONE
    }

    fun setEnergy(level: EnergyLevel) {
        _energy.value = level
    }

    fun setInput(value: String) {
        _input.value = value
    }

    fun setListening(value: Boolean) {
        _isListening.value = value
    }

    fun askLuma(horizon: PlanHorizon = PlanHorizon.DAY, prompt: String? = null) {
        val text = prompt ?: _input.value
        if (text.isBlank() || _isThinking.value) return
        _input.value = ""
        _messages.value += ChatMessage(
            id = UUID.randomUUID().toString(),
            isUser = true,
            text = text,
            createdAt = Instant.now().toString(),
        )
        _isThinking.value = true
        _destination.value = MainDestination.LUMA
        _overlay.value = OverlayScreen.NONE
        viewModelScope.launch {
            val response = assistant.turn(
                AssistantTurnRequest(
                    conversationId = "local-demo",
                    input = AssistantInput("text", text),
                    horizon = horizon,
                    clientContextVersion = 1,
                    anchorDate = DemoData.today,
                    existingItems = _items.value,
                ),
            )
            _lastResponse.value = response
            _selectedChanges.value = response.proposal?.changes?.map { it.id }?.toSet().orEmpty()
            _messages.value += ChatMessage(
                id = UUID.randomUUID().toString(),
                isUser = false,
                text = response.assistantMessage,
                createdAt = Instant.now().toString(),
            )
            _isThinking.value = false
        }
    }

    fun reviewProposal() {
        if (_lastResponse.value?.proposal != null) _overlay.value = OverlayScreen.PLAN_REVIEW
    }

    fun toggleProposalChange(id: String) {
        _selectedChanges.value = _selectedChanges.value.toMutableSet().apply {
            if (!add(id)) remove(id)
        }
    }

    fun approveProposal() {
        val proposal = _lastResponse.value?.proposal ?: return
        viewModelScope.launch {
            repository.applyApprovedProposal(proposal, _selectedChanges.value)
            _notice.value = "${_selectedChanges.value.size} approved change(s) applied locally. External calendar sync is still off."
            _lastResponse.value = null
            _selectedChanges.value = emptySet()
            _overlay.value = OverlayScreen.NONE
            _destination.value = MainDestination.TODAY
        }
    }

    fun rejectProposal() {
        _notice.value = "Proposal rejected. Your plan was not changed."
        _lastResponse.value = null
        _selectedChanges.value = emptySet()
        _overlay.value = OverlayScreen.NONE
    }

    fun toggleComplete(id: String) {
        viewModelScope.launch {
            val updated = _items.value.firstOrNull { it.id == id } ?: return@launch
            repository.save(
                listOf(
                    updated.copy(
                        status = if (updated.status == PlanStatus.COMPLETED) PlanStatus.ACTIVE else PlanStatus.COMPLETED,
                    ),
                ),
            )
        }
    }

    fun quickCapture(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.save(
                listOf(
                    PlanItem(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        date = DemoData.today,
                        startMinute = null,
                        durationMinutes = 30,
                        lifeArea = LifeArea.SELF_DIRECTION,
                        flexibility = Flexibility.FLEXIBLE,
                        source = PlanSource.USER,
                        status = PlanStatus.DRAFT,
                        energy = EnergyLevel.STEADY,
                        notes = "Quick capture—schedule intentionally later.",
                    ),
                ),
            )
            _notice.value = "Captured as an unscheduled draft."
        }
    }

    fun saveReflection(workedWell: String, adjust: String) {
        _reflections.value = listOf(
            Reflection(
                id = UUID.randomUUID().toString(),
                date = DemoData.today,
                energy = _energy.value,
                workedWell = workedWell.ifBlank { "I noticed what today actually required." },
                needsAdjustment = adjust.ifBlank { "Keep tomorrow realistic." },
                evidenceIds = emptyList(),
            ),
        ) + _reflections.value
        _notice.value = "Reflection saved. It will inform tomorrow’s capacity."
        _overlay.value = OverlayScreen.NONE
    }

    fun consumeNotice() {
        _notice.value = null
    }

    fun notify(message: String) {
        _notice.value = message
    }

    fun resetProductDemo() {
        viewModelScope.launch {
            getApplication<Application>().preferences.edit { it.clear() }
            _entryStage.value = EntryStage.WELCOME
            _overlay.value = OverlayScreen.NONE
            _destination.value = MainDestination.TODAY
            _notice.value = "Local onboarding reset. Cached plan data remains available."
        }
    }

    val currentProposal: PlanProposal?
        get() = _lastResponse.value?.proposal
}
