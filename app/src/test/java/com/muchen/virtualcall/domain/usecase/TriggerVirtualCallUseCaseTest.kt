package com.muchen.virtualcall.domain.usecase

import com.muchen.virtualcall.domain.model.PresentationMode
import com.muchen.virtualcall.domain.repository.SettingsRepository
import com.muchen.virtualcall.domain.repository.SystemRepository
import com.muchen.virtualcall.domain.repository.VirtualCallRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TriggerVirtualCallUseCaseTest {

    private val systemRepository = mockk<SystemRepository>()
    private val virtualCallRepository = mockk<VirtualCallRepository>()
    private val settingsRepository = mockk<SettingsRepository>()
    private lateinit var useCase: TriggerVirtualCallUseCase

    @Before
    fun setUp() {
        useCase = TriggerVirtualCallUseCase(systemRepository, virtualCallRepository, settingsRepository)
        every { systemRepository.isUserDisarmed() } returns false
        every { systemRepository.isBackgroundPopupGranted() } returns true
    }

    @Test
    fun `not armed returns NotArmed`() {
        every { systemRepository.isServiceArmed() } returns false

        val result = useCase()

        assertEquals(TriggerVirtualCallUseCase.Result.NotArmed, result)
        verify(exactly = 0) { virtualCallRepository.tryMarkRinging() }
    }

    @Test
    fun `user disarmed returns NotArmed even when armed flag is set`() {
        every { systemRepository.isServiceArmed() } returns true
        every { systemRepository.isUserDisarmed() } returns true

        val result = useCase()

        assertEquals(TriggerVirtualCallUseCase.Result.NotArmed, result)
        verify(exactly = 0) { virtualCallRepository.tryMarkRinging() }
    }

    @Test
    fun `bypassArmed triggers even when not armed`() {
        every { systemRepository.isServiceArmed() } returns false
        every { virtualCallRepository.tryMarkRinging() } returns true
        every { settingsRepository.getPresentationMode() } returns PresentationMode.FULLSCREEN

        val result = useCase(bypassArmed = true)

        assertTrue(result is TriggerVirtualCallUseCase.Result.Triggered)
    }

    @Test
    fun `armed but already ringing returns AlreadyRinging`() {
        every { systemRepository.isServiceArmed() } returns true
        every { virtualCallRepository.tryMarkRinging() } returns false

        val result = useCase()

        assertEquals(TriggerVirtualCallUseCase.Result.AlreadyRinging, result)
        verify(exactly = 0) { settingsRepository.getPresentationMode() }
    }

    @Test
    fun `armed and idle returns Triggered with fullscreen mode`() {
        every { systemRepository.isServiceArmed() } returns true
        every { virtualCallRepository.tryMarkRinging() } returns true
        every { settingsRepository.getPresentationMode() } returns PresentationMode.FULLSCREEN

        val result = useCase()

        assertTrue(result is TriggerVirtualCallUseCase.Result.Triggered)
        assertEquals(PresentationMode.FULLSCREEN, (result as TriggerVirtualCallUseCase.Result.Triggered).mode)
    }

    @Test
    fun `armed and idle returns Triggered with overlay mode`() {
        every { systemRepository.isServiceArmed() } returns true
        every { virtualCallRepository.tryMarkRinging() } returns true
        every { settingsRepository.getPresentationMode() } returns PresentationMode.OVERLAY

        val result = useCase()

        assertTrue(result is TriggerVirtualCallUseCase.Result.Triggered)
        assertEquals(PresentationMode.OVERLAY, (result as TriggerVirtualCallUseCase.Result.Triggered).mode)
    }

    @Test
    fun `tryMarkRinging called exactly once when armed`() {
        every { systemRepository.isServiceArmed() } returns true
        every { virtualCallRepository.tryMarkRinging() } returns true
        every { settingsRepository.getPresentationMode() } returns PresentationMode.FULLSCREEN

        useCase()

        verify(exactly = 1) { virtualCallRepository.tryMarkRinging() }
    }

    @Test
    fun `background popup not granted returns BackgroundPopupNotGranted`() {
        every { systemRepository.isBackgroundPopupGranted() } returns false

        val result = useCase()

        assertEquals(TriggerVirtualCallUseCase.Result.BackgroundPopupNotGranted, result)
        verify(exactly = 0) { virtualCallRepository.tryMarkRinging() }
    }

    @Test
    fun `background popup not granted blocks bypassArmed trigger too`() {
        every { systemRepository.isBackgroundPopupGranted() } returns false

        val result = useCase(bypassArmed = true)

        assertEquals(TriggerVirtualCallUseCase.Result.BackgroundPopupNotGranted, result)
        verify(exactly = 0) { virtualCallRepository.tryMarkRinging() }
    }
}
