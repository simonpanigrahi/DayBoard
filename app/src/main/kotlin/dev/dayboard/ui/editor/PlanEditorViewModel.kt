package dev.dayboard.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.dayboard.AppContainer
import dev.dayboard.data.repo.PlanRepository
import dev.dayboard.data.repo.PlannedBlock
import dev.dayboard.data.repo.SettingsRepository
import dev.dayboard.engine.ResolvedPlan
import dev.dayboard.engine.model.BlockKind
import dev.dayboard.engine.model.ChecklistItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class EditorUiState(
    val loaded: Boolean = false,
    val blocks: List<BlockDraft> = emptyList(),
    val dayStart: LocalTime = SettingsRepository.DEFAULT_DAY_START
) {
    /** The sweep run over the draft, which is what Review renders. */
    val preview: ResolvedPlan
        get() = ResolvedPlan.from(
            date = LocalDate.now(),
            dayStart = dayStart,
            blocks = blocks.mapIndexed { index, draft -> draft.toBlock(index) }
        )
}

class PlanEditorViewModel(
    private val plans: PlanRepository,
    private val settings: SettingsRepository,
    private val zone: ZoneId = ZoneId.systemDefault()
) : ViewModel() {

    private val date = LocalDate.now(zone)
    private val _state = MutableStateFlow(EditorUiState())
    val state: StateFlow<EditorUiState> = _state.asStateFlow()

    private var existingDayId = 0L

    init {
        viewModelScope.launch {
            val dayStart = settings.dayStart.first()
            val existing = plans.load(date)
            existingDayId = existing?.day?.id ?: 0L
            _state.value = EditorUiState(
                loaded = true,
                dayStart = dayStart,
                blocks = existing?.let { plan ->
                    plan.blocks.map { block -> BlockDraft.of(block, plan.checklists[block.id].orEmpty()) }
                } ?: listOf(BlockDraft.new())
            )
        }
    }

    /** Flow blocks chain from here, so a plan built in the evening needs this movable. */
    fun shiftDayStart(minutes: Long) {
        val moved = _state.value.dayStart.plusMinutes(minutes)
        _state.update { it.copy(dayStart = moved) }
        viewModelScope.launch { settings.setDayStart(moved) }
    }

    fun startDayNow() {
        val now = LocalTime.now(zone).withSecond(0).withNano(0)
        _state.update { it.copy(dayStart = now) }
        viewModelScope.launch { settings.setDayStart(now) }
    }

    fun addBlock() = edit { it + BlockDraft.new() }

    fun removeBlock(key: Long) = edit { blocks -> blocks.filterNot { it.key == key } }

    fun move(from: Int, to: Int) = edit { blocks ->
        if (from !in blocks.indices || to !in blocks.indices) blocks
        else blocks.toMutableList().apply { add(to, removeAt(from)) }
    }

    fun setTitle(key: Long, title: String) = editBlock(key) { it.copy(title = title) }

    fun changeMinutes(key: Long, delta: Int) =
        editBlock(key) { it.copy(minutes = (it.minutes + delta).coerceIn(5, 8 * 60)) }

    fun setKind(key: Long, kind: BlockKind) = editBlock(key) { it.copy(kind = kind) }

    fun toggleFixed(key: Long) = editBlock(key) { draft ->
        draft.copy(
            fixed = !draft.fixed,
            startLocal = if (!draft.fixed) draft.startLocal ?: _state.value.dayStart else draft.startLocal
        )
    }

    fun shiftStart(key: Long, minutes: Long) = editBlock(key) { draft ->
        draft.copy(startLocal = (draft.startLocal ?: _state.value.dayStart).plusMinutes(minutes))
    }

    fun addItem(key: Long) = editBlock(key) { it.copy(items = it.items + ItemDraft.new()) }

    fun setItemText(key: Long, itemKey: Long, text: String) = editBlock(key) { draft ->
        draft.copy(items = draft.items.map { if (it.key == itemKey) it.copy(text = text) else it })
    }

    fun removeItem(key: Long, itemKey: Long) = editBlock(key) { draft ->
        draft.copy(items = draft.items.filterNot { it.key == itemKey })
    }

    /** The one write path: everything above only shapes the draft. */
    fun commit(onCommitted: () -> Unit) {
        val drafts = _state.value.blocks.filter { it.title.isNotBlank() }
        viewModelScope.launch {
            plans.commit(
                date = date,
                zone = zone,
                blocks = drafts.mapIndexed { index, draft ->
                    PlannedBlock(
                        block = draft.toBlock(index),
                        items = draft.items.filter { it.text.isNotBlank() }.mapIndexed { itemIndex, item ->
                            ChecklistItem(id = item.id, blockId = draft.id, text = item.text, orderIndex = itemIndex)
                        }
                    )
                },
                existingDayId = existingDayId
            )
            onCommitted()
        }
    }

    private fun edit(transform: (List<BlockDraft>) -> List<BlockDraft>) {
        _state.update { it.copy(blocks = transform(it.blocks)) }
    }

    private fun editBlock(key: Long, transform: (BlockDraft) -> BlockDraft) =
        edit { blocks -> blocks.map { if (it.key == key) transform(it) else it } }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { PlanEditorViewModel(container.plans, container.settings) }
        }
    }
}
