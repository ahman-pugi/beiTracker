package com.ahmanpg.beitracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmanpg.beitracker.data.model.TrackedItem
import com.ahmanpg.beitracker.data.repository.PriceData
import com.ahmanpg.beitracker.data.repository.PriceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PriceViewModel @Inject constructor(
    private val repository: PriceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PriceUiState())
    val uiState: StateFlow<PriceUiState> = _uiState.asStateFlow()

    fun searchProducts(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val results = repository.searchProducts(query)
                _uiState.update { it.copy(isLoading = false, searchResults = results) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun trackProduct(url: String, desiredPrice: Double? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = repository.fetchAndSaveCurrentPrice(url)
            result.onSuccess { data ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentPrice = data,
                        error = null,
                        desiredPrice = desiredPrice
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun loadHistory(url: String) {
        viewModelScope.launch {
            repository.getPriceHistory(url).collect { history ->
                _uiState.update { it.copy(history = history) }
            }
        }
    }
}

// UI state – single source for screen
data class PriceUiState(
    val isLoading: Boolean = false,
    val searchResults: List<TrackedItem> = emptyList(),
    val currentPrice: PriceData? = null,
    val history: List<PriceData> = emptyList(),
    val desiredPrice: Double? = null,
    val error: String? = null
)
