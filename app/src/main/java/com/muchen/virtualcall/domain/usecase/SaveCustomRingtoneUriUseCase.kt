package com.muchen.virtualcall.domain.usecase

import android.net.Uri
import com.muchen.virtualcall.domain.repository.SettingsRepository
import javax.inject.Inject

class SaveCustomRingtoneUriUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(uri: Uri?) {
        settingsRepository.saveCustomRingtoneUri(uri)
    }
}
