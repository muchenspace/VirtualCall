package com.muchen.virtualcall.data.repository

import android.content.Context
import android.net.Uri
import com.muchen.virtualcall.R
import com.muchen.virtualcall.data.local.PrefsDataSource
import com.muchen.virtualcall.data.local.PrefsKeys
import com.muchen.virtualcall.domain.model.PresentationMode
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * [SettingsRepositoryImpl] 单元测试。
 *
 * 验证配置读写逻辑，mock PrefsDataSource 和 Context。
 */
class SettingsRepositoryImplTest {

    private val prefsDataSource = mockk<PrefsDataSource>(relaxed = true)
    private val context = mockk<Context>()
    private val mockUri = mockk<Uri>(relaxed = true)
    private lateinit var repository: SettingsRepositoryImpl

    @Before
    fun setUp() {
        // android.net.Uri 在 JVM 单测中是存根，需 mock parse 避免 "not mocked" 异常
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockUri
        every { context.getString(R.string.default_contact_name) } returns "妈妈"
        every { context.getString(R.string.default_phone_number) } returns "13800000000"
        every { context.getString(R.string.default_carrier) } returns "中国移动"
        repository = SettingsRepositoryImpl(prefsDataSource, context)
    }

    @After
    fun tearDown() {
        unmockkStatic(Uri::class)
    }

    @Test
    fun `getCaller returns saved values when set`() {
        every { prefsDataSource.getString(PrefsKeys.KEY_CONTACT_NAME, any()) } returns "老板"
        every { prefsDataSource.getString(PrefsKeys.KEY_CONTACT_NUMBER, any()) } returns "13912345678"
        every { prefsDataSource.getString(PrefsKeys.KEY_CARRIER, any()) } returns "中国联通"
        every { prefsDataSource.getString(PrefsKeys.KEY_CUSTOM_RINGTONE_URI, null) } returns "content://media/audio/123"
        every { prefsDataSource.getString(PrefsKeys.KEY_PRESENTATION_MODE, any()) } returns PresentationMode.OVERLAY_KEY

        val caller = repository.getCaller()

        assertEquals("老板", caller.name)
        assertEquals("13912345678", caller.number)
        assertEquals("中国联通", caller.carrier)
        assertEquals(mockUri, caller.customRingtoneUri)
        assertEquals(PresentationMode.OVERLAY, caller.presentationMode)
    }

    @Test
    fun `getCaller returns defaults when prefs are null`() {
        every { prefsDataSource.getString(PrefsKeys.KEY_CONTACT_NAME, any()) } returns null
        every { prefsDataSource.getString(PrefsKeys.KEY_CONTACT_NUMBER, any()) } returns null
        every { prefsDataSource.getString(PrefsKeys.KEY_CARRIER, any()) } returns null
        every { prefsDataSource.getString(PrefsKeys.KEY_CUSTOM_RINGTONE_URI, null) } returns null
        every { prefsDataSource.getString(PrefsKeys.KEY_PRESENTATION_MODE, any()) } returns PresentationMode.FULLSCREEN_KEY

        val caller = repository.getCaller()

        assertEquals("妈妈", caller.name)
        assertEquals("13800000000", caller.number)
        assertEquals("中国移动", caller.carrier)
        assertNull(caller.customRingtoneUri)
        assertEquals(PresentationMode.FULLSCREEN, caller.presentationMode)
    }

    @Test
    fun `saveCaller with blank values uses defaults`() {
        repository.saveCaller("", "", "")

        verify { prefsDataSource.putString(PrefsKeys.KEY_CONTACT_NAME, "妈妈") }
        verify { prefsDataSource.putString(PrefsKeys.KEY_CONTACT_NUMBER, "13800000000") }
        verify { prefsDataSource.putString(PrefsKeys.KEY_CARRIER, "中国移动") }
    }

    @Test
    fun `saveCaller with non-blank values saves trimmed`() {
        repository.saveCaller("  张三  ", "13900001111", " 中国电信 ")

        verify { prefsDataSource.putString(PrefsKeys.KEY_CONTACT_NAME, "张三") }
        verify { prefsDataSource.putString(PrefsKeys.KEY_CONTACT_NUMBER, "13900001111") }
        verify { prefsDataSource.putString(PrefsKeys.KEY_CARRIER, "中国电信") }
    }

    @Test
    fun `savePresentationMode stores correct string key`() {
        repository.savePresentationMode(PresentationMode.OVERLAY)
        verify { prefsDataSource.putString(PrefsKeys.KEY_PRESENTATION_MODE, PresentationMode.OVERLAY_KEY) }

        repository.savePresentationMode(PresentationMode.FULLSCREEN)
        verify { prefsDataSource.putString(PrefsKeys.KEY_PRESENTATION_MODE, PresentationMode.FULLSCREEN_KEY) }
    }

    @Test
    fun `restoreDefaults resets all keys to defaults`() {
        repository.restoreDefaults()

        verify { prefsDataSource.putString(PrefsKeys.KEY_CONTACT_NAME, "妈妈") }
        verify { prefsDataSource.putString(PrefsKeys.KEY_CONTACT_NUMBER, "13800000000") }
        verify { prefsDataSource.putString(PrefsKeys.KEY_CARRIER, "中国移动") }
        verify { prefsDataSource.putString(PrefsKeys.KEY_PRESENTATION_MODE, PresentationMode.FULLSCREEN_KEY) }
        verify { prefsDataSource.remove(PrefsKeys.KEY_CUSTOM_RINGTONE_URI) }
    }
}
