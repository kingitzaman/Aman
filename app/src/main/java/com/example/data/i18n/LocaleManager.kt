package com.example.data.i18n

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val code: String, val displayName: String) {
    EN("en", "English"),
    HI("hi", "हिंदी"),
    UR("ur", "اردو")
}

object LocaleManager {
    private val _currentLanguage = MutableStateFlow(AppLanguage.EN)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
    }

    private val translations: Map<String, Map<AppLanguage, String>> = mapOf(
        "app_tagline" to mapOf(
            AppLanguage.EN to "Think • Search • Create • Grow",
            AppLanguage.HI to "सोचें • खोजें • बनाएं • आगे बढ़ें",
            AppLanguage.UR to "سوچیں • تلاش کریں • بنائیں • آگے بڑھیں"
        ),
        "greeting_prefix" to mapOf(
            AppLanguage.EN to "How can I help you today,",
            AppLanguage.HI to "आज मैं आपकी क्या सहायता कर सकता हूँ,",
            AppLanguage.UR to "آج میں آپ کی کیا مدد کر سکتا ہوں،"
        ),
        "chat_placeholder" to mapOf(
            AppLanguage.EN to "Ask Amanix anything, or request code, analysis, or ideas...",
            AppLanguage.HI to "अमानिक्स से कुछ भी पूछें, या कोड, विश्लेषण, विचार मांगें...",
            AppLanguage.UR to "امانکس سے کچھ بھی پوچھیں، کوڈ، تجزیہ یا خیالات حاصل کریں..."
        ),
        "send" to mapOf(
            AppLanguage.EN to "Send",
            AppLanguage.HI to "भेजें",
            AppLanguage.UR to "ارسال کریں"
        ),
        "stop_generation" to mapOf(
            AppLanguage.EN to "Stop Generation",
            AppLanguage.HI to "उत्पादन रोकें",
            AppLanguage.UR to "روک دیں"
        ),
        "web_search_title" to mapOf(
            AppLanguage.EN to "Web Search",
            AppLanguage.HI to "वेब खोज",
            AppLanguage.UR to "ویب تلاش"
        ),
        "coding_title" to mapOf(
            AppLanguage.EN to "Coding & Debugging",
            AppLanguage.HI to "कोडिंग और डिबगिंग",
            AppLanguage.UR to "کوڈنگ اور ڈیبگنگ"
        ),
        "learning_title" to mapOf(
            AppLanguage.EN to "Learning & Explain",
            AppLanguage.HI to "सीखें और समझें",
            AppLanguage.UR to "سیکھیں اور سمجھیں"
        ),
        "writing_title" to mapOf(
            AppLanguage.EN to "Writing & Editing",
            AppLanguage.HI to "लेखन और संपादन",
            AppLanguage.UR to "تحریر اور ترمیم"
        ),
        "brainstorm_title" to mapOf(
            AppLanguage.EN to "Brainstorm & Solve",
            AppLanguage.HI to "मंथन और समस्या निवारण",
            AppLanguage.UR to "غور و فکر اور حل"
        ),
        "settings_title" to mapOf(
            AppLanguage.EN to "Settings & Profile",
            AppLanguage.HI to "सेटिंग्स और प्रोफ़ाइल",
            AppLanguage.UR to "ترتیبات اور پروفائل"
        ),
        "no_conversations" to mapOf(
            AppLanguage.EN to "No conversations yet.",
            AppLanguage.HI to "अभी कोई बातचीत नहीं है।",
            AppLanguage.UR to "ابھی کوئی گفتگو موجود نہیں ہے۔"
        ),
        "new_chat" to mapOf(
            AppLanguage.EN to "New Chat",
            AppLanguage.HI to "नई चैट",
            AppLanguage.UR to "نئی بات چیت"
        ),
        "not_configured" to mapOf(
            AppLanguage.EN to "NOT CONFIGURED",
            AppLanguage.HI to "कॉन्फ़िगर नहीं किया गया",
            AppLanguage.UR to "ترتیب نہیں دی گئی"
        )
    )

    fun tr(key: String): String {
        val lang = _currentLanguage.value
        return translations[key]?.get(lang) ?: translations[key]?.get(AppLanguage.EN) ?: key
    }
}
