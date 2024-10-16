package org.giste.navigator.ui

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class NavigationViewModelTests {
    private val viewModel = NavigationViewModel()

    @Test
    fun setPartial_ifItIsInRange_shouldUpdateIt() {
        viewModel.setPartial("123,45")

        assertThat(viewModel.uiState.partial, equalTo(123450))
    }

    @Test(expected = IllegalArgumentException::class)
    fun setPartial_ifItIsOutOfRange_ShouldThrowException() {
        viewModel.setPartial("1000,00")
    }

    @Test
    fun increasePartial_ifPartialIsMinorThan999Dot99_shouldAddOneUnit() {
        viewModel.increasePartial()

        assertThat(viewModel.uiState.partial, equalTo(10))
    }

    @Test
    fun increasePartial_ifPartialIs999Dot99_shouldRemainTheSame() {
        viewModel.setPartial("999,99")
        viewModel.increasePartial()

        assertThat(viewModel.uiState.partial, equalTo(999990))
    }

    @Test
    fun decreasePartial_ifPartialGreaterThanZero_shouldSubtractOneUnit() {
        viewModel.setPartial("123,45")
        viewModel.decreasePartial()

        assertThat(viewModel.uiState.partial, equalTo(123440))
    }

    @Test
    fun decreasePartial_ifPartialIsZero_shouldRemainZero() {
        viewModel.decreasePartial()

        assertThat(viewModel.uiState.partial, equalTo(0))
    }

    @Test
    fun resetPartial_shouldSetPartialToZero() {
        viewModel.setPartial("123,45")
        viewModel.resetPartial()

        assertThat(viewModel.uiState.partial, equalTo(0))
    }

    @Test
    fun resetAll_shouldSetTotalAndPartialToZero() {
        viewModel.setPartial("123,45")
        viewModel.resetAll()

        assertThat(viewModel.uiState.partial, equalTo(0))
        //TODO("Check total")
        assertThat(viewModel.uiState.total, equalTo(0))
    }
}