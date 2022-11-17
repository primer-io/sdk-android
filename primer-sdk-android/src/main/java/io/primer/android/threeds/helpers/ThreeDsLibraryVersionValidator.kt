package io.primer.android.threeds.helpers

import io.primer.android.data.configuration.models.Environment
import io.primer.android.domain.session.repository.ConfigurationRepository
import io.primer.android.threeds.BuildConfig

internal class ThreeDsLibraryVersionValidator(
    private val configurationRepository: ConfigurationRepository
) {

    fun isValidVersion() = configurationRepository.getConfiguration().environment ==
        Environment.PRODUCTION || BuildConfig.SDK_VERSION_STRING == getThreeDsIncludedVersion()

    private fun getThreeDsIncludedVersion(): String? {
        return try {
            val clazz = Class.forName(THREE_DS_BUILD_CONFIG_PATH)
            clazz.getField(THREE_DS_SDK_VERSION_FIELD).get(clazz) as String
        } catch (ignored: ClassNotFoundException) {
            null
        } catch (ignored: NoSuchFieldException) {
            null
        }
    }

    private companion object {

        const val THREE_DS_BUILD_CONFIG_PATH = "io.primer.android.threeds.BuildConfig"
        const val THREE_DS_SDK_VERSION_FIELD = "SDK_VERSION_STRING"
    }
}
