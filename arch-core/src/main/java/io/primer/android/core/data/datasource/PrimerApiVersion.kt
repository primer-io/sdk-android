package io.primer.android.core.data.datasource

import io.primer.android.core.data.datasource.PrimerApiVersion.V2_3
import io.primer.android.core.data.datasource.PrimerApiVersion.V2_4
import io.primer.android.core.data.network.utils.Constants

/**
 * Enum class representing the supported versions of the Primer API.
 *
 * [V2_3]: Represents API version 2.3.
 * [V2_4]: Represents API version 2.4.
 */
enum class PrimerApiVersion(internal val version: String) {
    /** API version 2.3 */
    V2_3("2.3"),

    /** API version 2.4 */
    V2_4("2.4"),
    ;

    companion object {
        /** The latest supported API version. */
        val LATEST = V2_4
    }
}

fun PrimerApiVersion.toHeaderMap(): Map<String, String> = mapOf(toHeaderPair())

fun PrimerApiVersion.toHeaderPair(): Pair<String, String> = Constants.SDK_API_VERSION_HEADER to this.version
