package org.giste.navigator.ui

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test


class NavigationViewModelTests {
    private val viewModel = NavigationViewModel()

    @Test
    fun setPartial_ifItIsInRange_shouldUpdateIt() {
        viewModel.setPartial("123,45")

        assertEquals(123450, viewModel.uiState.partial)
    }

    @Test
    fun setPartial_ifItIsOutOfRange_ShouldThrowException() {
        assertThrows(IllegalArgumentException::class.java) {
            viewModel.setPartial("1000,00")
        }
    }

    @Test
    fun increasePartial_ifPartialIsMinorThan999Dot99_shouldAddOneUnit() {
        viewModel.increasePartial()

        assertEquals(10, viewModel.uiState.partial)
    }

    @Test
    fun increasePartial_ifPartialIs999Dot99_shouldRemainTheSame() {
        viewModel.setPartial("999,99")
        viewModel.increasePartial()

        assertEquals(999990, viewModel.uiState.partial)
    }

    @Test
    fun decreasePartial_ifPartialGreaterThanZero_shouldSubtractOneUnit() {
        viewModel.setPartial("123,45")
        viewModel.decreasePartial()

        assertEquals(123440, viewModel.uiState.partial)
    }

    @Test
    fun decreasePartial_ifPartialIsZero_shouldRemainZero() {
        viewModel.decreasePartial()

        assertEquals(0, viewModel.uiState.partial)
    }

    @Test
    fun resetPartial_shouldSetPartialToZero() {
        viewModel.setPartial("123,45")
        viewModel.resetPartial()

        assertEquals(0, viewModel.uiState.partial)
    }

    @Test
    fun resetAll_shouldSetTotalAndPartialToZero() {
        viewModel.setPartial("123,45")
        viewModel.resetAll()

        assertEquals(0, viewModel.uiState.partial)
        //TODO("Check total")
        assertEquals(0, viewModel.uiState.total)
    }
}