package com.muchen.virtualcall.domain.usecase

import com.muchen.virtualcall.domain.model.PresentationMode
import com.muchen.virtualcall.domain.repository.VirtualCallRepository
import com.muchen.virtualcall.domain.repository.SettingsRepository
import com.muchen.virtualcall.domain.repository.SystemRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * [TriggerVirtualCallUseCase] 单元测试。
 *
 * 验证三个分支：
 * 1. 未武装 → NotArmed
 * 2. 已武装但已有来电 → AlreadyRinging
 * 3. 已武装且状态空闲 → Triggered(mode)
 */
class TriggerVirtualCallUseCaseTest {

    private val systemRepository = mockk<SystemRepository>()
    private val virtualCallRepository = mockk<VirtualCallRepository>()
    private val settingsRepository = mockk<SettingsRepository>()
    private lateinit var useCase: TriggerVirtualCallUseCase

    @Before
    fun setUp() {
        useCase = TriggerVirtualCallUseCase(systemRepository, virtualCallRepository, settingsRepository)
    }

    @Test
    fun `not armed returns NotArmed`() {
        every { systemRepository.isServiceArmed() } returns false

        val result = useCase()

        assertEquals(TriggerVirtualCallUseCase.Result.NotArmed, result)
        // 未武装时不应尝试状态转换
        verify(exactly = 0) { virtualCallRepository.tryMarkRinging() }
    }

    @Test
    fun `armed but already ringing returns AlreadyRinging`() {
        every { systemRepository.isServiceArmed() } returns true
        every { virtualCallRepository.tryMarkRinging() } returns false

        val result = useCase()

        assertEquals(TriggerVirtualCallUseCase.Result.AlreadyRinging, result)
        // 已有来电时不应读取展示模式
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
}
