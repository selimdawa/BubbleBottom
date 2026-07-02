package com.flatcode.bubblebottom

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.os.ConfigurationCompat
import java.util.Locale

@Suppress("unused")
class MyContextWrapper(base: Context) : ContextWrapper(base) {

    companion object {

        fun wrap(contextParam: Context?, language: String): ContextWrapper? {
            var context = contextParam ?: return null
            val config = context.resources.configuration

            if (language.isNotEmpty()) {
                val locale = Locale.Builder().setLanguage(language).build()
                Locale.setDefault(locale)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setSystemLocale(config, locale)
                } else {
                    setSystemLocaleLegacy(config, locale)
                }
                config.setLayoutDirection(locale)
            }

            context = context.createConfigurationContext(config)
            return MyContextWrapper(context)
        }

        private fun getSystemLocaleLegacy(config: Configuration): Locale {
            val locales = ConfigurationCompat.getLocales(config)
            return locales.get(0) ?: Locale.getDefault()
        }

        @RequiresApi(Build.VERSION_CODES.N)
        private fun getSystemLocale(config: Configuration): Locale {
            return config.locales.get(0)
        }

        private fun setSystemLocaleLegacy(config: Configuration, locale: Locale) {
            config.setLocale(locale)
        }

        @RequiresApi(Build.VERSION_CODES.N)
        private fun setSystemLocale(config: Configuration, locale: Locale) {
            config.setLocale(locale)
        }
    }
}