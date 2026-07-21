package com.flatcode.bubblebottom

import android.content.Context
import android.content.ContextWrapper
import java.util.Locale

class MyContextWrapper(base: Context) : ContextWrapper(base) {

    companion object {

        @Suppress("unused")
        fun wrap(contextParam: Context?, language: String): ContextWrapper? {
            val context = contextParam ?: return null
            val config = context.resources.configuration

            if (language.isNotEmpty()) {
                val locale = Locale.Builder().setLanguage(language).build()
                Locale.setDefault(locale)
                config.setLocale(locale)
                config.setLayoutDirection(locale)
            }

            val wrappedContext = context.createConfigurationContext(config)
            return MyContextWrapper(wrappedContext)
        }
    }
}