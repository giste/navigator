package org.giste.navigator.ui

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class NavigationViewModelTests {
    private val viewModel = NavigationViewModel()

    @Test
    fun setPartial_shouldUpdateIt() {
        viewModel.setPartial("123,45")

        assertThat(viewModel.partial, equalTo("123,45"))
    }

    @Test
    fun increasePartial_ifPartialIsMinorThan999Dot99_shouldAddOneUnit() {
        viewModel.increasePartial()

        assertThat(viewModel.partial, equalTo("0,01"))
    }

    @Test
    fun increasePartial_ifPartialIs999Dot99_shouldRemainTheSame() {
        viewModel.setPartial("999,99")
        viewModel.increasePartial()

        assertThat(viewModel.partial, equalTo("999,99"))
    }

    @Test
    fun decreasePartial_ifPartialGreaterThanZero_shouldSubtractOneUnit() {
        viewModel.setPartial("123,45")
        viewModel.decreasePartial()

        assertThat(viewModel.partial, equalTo("123,44"))
    }

    @Test
    fun decreasePartial_ifPartialIsZero_shouldRemainZero() {
        viewModel.decreasePartial()

        assertThat(viewModel.partial, equalTo("0,00"))
    }

    @Test
    fun resetPartial_shouldSetPartialToZero() {
        viewModel.setPartial("123,45")
        viewModel.resetPartial()

        assertThat(viewModel.partial, equalTo("0,00"))
    }

    @Test
    fun resetAll_shouldSetTotalAndPartialToZero() {
        viewModel.setPartial("123,45")
        viewModel.resetAll()

        assertThat(viewModel.partial, equalTo("0,00"))
        //TODO("Check total")
        assertThat(viewModel.total, equalTo("0,00"))
    }
}