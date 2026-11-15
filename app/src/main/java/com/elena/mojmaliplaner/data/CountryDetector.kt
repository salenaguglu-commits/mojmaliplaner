package com.elena.mojmaliplaner.data

import android.content.Context
import android.content.res.Configuration
import android.telephony.TelephonyManager
import java.util.Locale

object CountryDetector {

    fun isSerbiaUser(context: Context): Boolean {
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val simCountry = telephonyManager?.simCountryIso?.lowercase(Locale.ROOT)
            val networkCountry = telephonyManager?.networkCountryIso?.lowercase(Locale.ROOT)

            if (simCountry?.contains("rs") == true || networkCountry?.contains("rs") == true) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val deviceCountry = Locale.getDefault().country.lowercase(Locale.ROOT)
        val deviceLanguage = Locale.getDefault().language.lowercase(Locale.ROOT)

        return deviceCountry == "rs" || deviceLanguage == "sr"
    }

    fun applyAppLocale(context: Context): Context {
        val targetLocale = if (isSerbiaUser(context)) Locale("sr", "RS") else Locale.ENGLISH
        Locale.setDefault(targetLocale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(targetLocale)

        return context.createConfigurationContext(config)
    }
}
