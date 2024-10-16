package org.giste.navigator.ui

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Tests for NavigationViewModel")
class NavigationViewModelTests {
    private val viewModel = NavigationViewModel()

    @DisplayName("Given minimum partial (0)")
    @Nested
    inner class PartialIsMin {
        @Test
        fun `increase() should add 10 meters`() {
            viewModel.increasePartial()

            assertEquals(10, viewModel.uiState.partial)
        }

        @Test
        fun `decrease() should not change partial`(){
            viewModel.decreasePartial()

            assertEquals(0, viewModel.uiState.partial)
        }
    }

    @DisplayName("Given maximum partial (>=999990)")
    @Nested
    inner class PartialIsMax {
        @BeforeEach
        fun setup() {
            viewModel.setPartial("999,99")
        }

        @Test
        fun `increase() should not change partial`() {
            viewModel.increasePartial()

            assertEquals(999990, viewModel.uiState.partial)
        }

        @Test
        fun `decrease() should subtract 10 meters`() {
            viewModel.decreasePartial()

            assertEquals(999980, viewModel.uiState.partial)
        }
    }

    @DisplayName("setPartial()")
    @Nested
    inner class SetPartialTests {
        @Test
        fun `when it's in 0-999 should update partial`() {
            viewModel.setPartial("123,45")

            assertEquals(123450, viewModel.uiState.partial)
        }

        @Test
        fun `when it's out of range should throw IllegalArgumentException`(){
            assertThrows(IllegalArgumentException::class.java) {
                viewModel.setPartial("1000,00")
            }
        }
    }

    @DisplayName("Given partial and total > 0")
    @Nested
    inner class ResetTests {
        @BeforeEach
        fun setup() {
            viewModel.setPartial("123,45")
            //TODO("Can I set total value?")
        }

        @Test
        fun `resetPartial() should set partial to 0`() {
            viewModel.resetPartial()

            assertEquals(0, viewModel.uiState.partial)
        }

        @Test
        fun `resetAll() should set partial and total to 0`() {
            viewModel.resetAll()

            assertEquals(0, viewModel.uiState.partial)
            assertEquals(0, viewModel.uiState.total)
        }
    }

}