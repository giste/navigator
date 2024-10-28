package org.giste.navigator.ui

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

//@InstallIn(SingletonComponent::class)
//@Module
class UiModule {
//    @Provides
    fun providePdfBitmapConverter(
        @ApplicationContext appContext: Context,
    ): PdfBitmapConverter {
        return PdfBitmapConverter(appContext)
    }
}