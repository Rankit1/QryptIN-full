package com.qryptin.calls.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qryptin.calls.data.repository.RoomCallRepository
import com.qryptin.calls.model.*
import com.qryptin.calls.repository.CallRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class CallsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CallRepository = RoomCallRepository(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow(CallsUiState(isLoading = true))
    val uiState: StateFlow<CallsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(200)
                .flatMapLatest { query ->
                    if (query.isBlank()) repository.observeAll()
                    else repository.search(query)
                }
                .collectLatest { calls ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            allCalls  = calls,
                            groups    = groupCalls(calls),
                        )
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun deleteCall(callId: String) {
        viewModelScope.launch { repository.delete(callId) }
    }

    // ── Grouping logic ────────────────────────────────────────

    private fun groupCalls(calls: List<CallModel>): List<CallGroup> {
        if (calls.isEmpty()) return emptyList()

        val now        = System.currentTimeMillis()
        val todayStart = dayStart(now)
        val yestStart  = todayStart - TimeUnit.DAYS.toMillis(1)

        val recent    = calls.filter { it.timestamp >= todayStart }
        val yesterday = calls.filter { it.timestamp in yestStart until todayStart }
        val earlier   = calls.filter { it.timestamp < yestStart }

        return buildList {
            if (recent.isNotEmpty())    add(CallGroup("Recent",    recent))
            if (yesterday.isNotEmpty()) add(CallGroup("Yesterday", yesterday))
            if (earlier.isNotEmpty())   add(CallGroup("Earlier",   earlier))
        }
    }

    private fun dayStart(epochMillis: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
