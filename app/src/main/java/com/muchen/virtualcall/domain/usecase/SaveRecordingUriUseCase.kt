package com.muchen.virtualcall.domain.usecase

import android.net.Uri
import com.muchen.virtualcall.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * 保存接通后录音 URI。传入 null 清除录音。
 */
class SaveRecordingUriUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(uri: Uri?) {
        settingsRepository.saveRecordingUri(uri)
    }
}
