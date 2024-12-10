package io.primer.android.configuration.data.model

import io.primer.android.configuration.extension.sanitizedCardNumber
import kotlin.math.abs

const val CARD_PADDING: Int = 8

class CardNetwork {

    enum class Type(val displayName: String) {

        OTHER(displayName = "Other"),
        VISA(displayName = "Visa"),
        MASTERCARD(displayName = "Mastercard"),
        AMEX(displayName = "American Express"),
        DINERS_CLUB(displayName = "Diners Club"),
        DISCOVER(displayName = "Discover"),
        JCB(displayName = "JCB"),
        UNIONPAY(displayName = "UnionPay"),
        MAESTRO(displayName = "Maestro"),
        ELO(displayName = "Elo"),
        MIR(displayName = "MIR"),
        HIPER(displayName = "Hiper"),
        HIPERCARD(displayName = "Hipercard"),
        CARTES_BANCAIRES(displayName = "Cartes Bancaires"),
        DANKORT(displayName = "Dankort");

        companion object {
            fun valueOrNull(type: String?) = entries.find {
                it.name == type
            }
        }
    }

    open class Descriptor(
        val type: Type,
        val gaps: List<Int>,
        val lengths: List<Int>,
        lower: String,
        upper: String?,
        val cvvLength: Int = CVV_LEN_3
    ) : Comparable<Descriptor> {

        private val weight: Int = lower.length
        private val min = lower.padEnd(CARD_PADDING, '0')
        private val max = (upper ?: lower).padEnd(CARD_PADDING, '9')

        override fun compareTo(other: Descriptor): Int {
            return weight - other.weight
        }

        fun matches(bin: String): Boolean {
            return bin.padEnd(CARD_NUM_GAP_8, '0') in min..max
        }

        fun shortestDistanceInRange(bin: String): Long {
            val paddedBin = bin.sanitizedCardNumber().padEnd(CARD_NUM_GAP_8, '0')
            return abs(
                minOf(
                    paddedBin.toBigInteger() - min.toBigInteger(),
                    max.toBigInteger() - paddedBin.toBigInteger()
                ).toLong()
            )
        }
    }

    internal class VisaDescriptor(lower: String, upper: String? = null) :
        Descriptor(Type.VISA, CARD_GAPS_4_8_12, CARD_LENS_16_18_19, lower, upper)

    internal class MastercardDescriptor(lower: String, upper: String? = null) :
        Descriptor(Type.MASTERCARD, CARD_GAPS_4_8_12, CARD_LENS_16, lower, upper)

    internal class AmexDescriptor(lower: String, upper: String? = null) :
        Descriptor(Type.AMEX, CARD_GAPS_4_10, CARD_LENS_15, lower, upper, cvvLength = CVV_LEN_4)

    internal class DinersDescriptor(lower: String, upper: String? = null) :
        Descriptor(Type.DINERS_CLUB, CARD_GAPS_4_10, CARD_LENS_14_16_19, lower, upper)

    internal class DiscoverDescriptor(lower: String, upper: String? = null) :
        Descriptor(Type.DISCOVER, CARD_GAPS_4_8_12, CARD_LENS_16_19, lower, upper)

    internal class JcbDescriptor(lower: String, upper: String? = null) :
        Descriptor(Type.JCB, CARD_GAPS_4_8_12, CARD_LENS_16_17_18_19, lower, upper)

    internal class UnionPayDescriptor(lower: String, upper: String? = null) :
        Descriptor(Type.UNIONPAY, CARD_GAPS_4_8_12, CARD_LENS_16_17_18_19, lower, upper)

    internal class MaestroDescriptor(lower: String, upper: String? = null) :
        Descriptor(
            Type.MAESTRO,
            CARD_GAPS_4_8_12,
            CARD_LENS_12_13_14_15_16_17_18_19,
            lower,
            upper
        )

    internal class EloDescriptor(lower: String, upper: String? = null) :
        Descriptor(Type.ELO, CARD_GAPS_4_8_12, CARD_LENS_16, lower, upper)

    internal class MirDescriptor(lower: String, upper: String? = null) :
        Descriptor(Type.MIR, CARD_GAPS_4_8_12, CARD_LENS_16_17_18_19, lower, upper)

    internal class HiperDescriptor(lower: String, upper: String? = null) :
        Descriptor(Type.HIPER, CARD_GAPS_4_8_12, CARD_LENS_16, lower, upper)

    internal class HiperCardDescriptor(lower: String, upper: String? = null) :
        Descriptor(Type.HIPERCARD, CARD_GAPS_4_8_12, CARD_LENS_16, lower, upper)

    internal class DankortCardDescriptor(lower: String, upper: String? = null) :
        Descriptor(Type.DANKORT, CARD_GAPS_4_8_12, CARD_LENS_16, lower, upper)

    companion object {

        private val types = listOf(
            VisaDescriptor("4"),
            MastercardDescriptor("51", "55"),
            MastercardDescriptor("2221", "2229"),
            MastercardDescriptor("223", "229"),
            MastercardDescriptor("23", "26"),
            MastercardDescriptor("270", "271"),
            MastercardDescriptor("2720"),
            AmexDescriptor("34"),
            AmexDescriptor("37"),
            DinersDescriptor("300", "305"),
            DinersDescriptor("36"),
            DinersDescriptor("38"),
            DinersDescriptor("39"),
            DiscoverDescriptor("6011"),
            DiscoverDescriptor("644", "649"),
            DiscoverDescriptor("65"),
            JcbDescriptor("2131"),
            JcbDescriptor("1800"),
            JcbDescriptor("3528", "3589"),
            UnionPayDescriptor("620"),
            UnionPayDescriptor("624", "626"),
            UnionPayDescriptor("62100", "62182"),
            UnionPayDescriptor("62184", "62187"),
            UnionPayDescriptor("62185", "62197"),
            UnionPayDescriptor("62200", "62205"),
            UnionPayDescriptor("622010", "622999"),
            UnionPayDescriptor("622018"),
            UnionPayDescriptor("622019", "622999"),
            UnionPayDescriptor("62207", "62209"),
            UnionPayDescriptor("622126", "622925"),
            UnionPayDescriptor("623", "626"),
            UnionPayDescriptor("6270"),
            UnionPayDescriptor("6272"),
            UnionPayDescriptor("6276"),
            UnionPayDescriptor("627700", "627779"),
            UnionPayDescriptor("627781", "627799"),
            UnionPayDescriptor("6282", "6289"),
            UnionPayDescriptor("6291"),
            UnionPayDescriptor("6292"),
            UnionPayDescriptor("810"),
            UnionPayDescriptor("8110", "8131"),
            UnionPayDescriptor("8132", "8151"),
            UnionPayDescriptor("8152", "8163"),
            UnionPayDescriptor("8164", "8171"),
            MaestroDescriptor("493698"),
            MaestroDescriptor("500000", "504174"),
            MaestroDescriptor("504176", "506698"),
            MaestroDescriptor("506779", "508999"),
            MaestroDescriptor("56", "59"),
            MaestroDescriptor("63"),
            MaestroDescriptor("67"),
            MaestroDescriptor("6"),
            EloDescriptor("401178"),
            EloDescriptor("401179"),
            EloDescriptor("438935"),
            EloDescriptor("457631"),
            EloDescriptor("457632"),
            EloDescriptor("431274"),
            EloDescriptor("451416"),
            EloDescriptor("457393"),
            EloDescriptor("504175"),
            EloDescriptor("627780"),
            EloDescriptor("636297"),
            EloDescriptor("636368"),
            EloDescriptor("506699", "506778"),
            EloDescriptor("509000", "509999"),
            EloDescriptor("650031", "650033"),
            EloDescriptor("650035", "650051"),
            EloDescriptor("650405", "650439"),
            EloDescriptor("650485", "650538"),
            EloDescriptor("650541", "650598"),
            EloDescriptor("650700", "650718"),
            EloDescriptor("650720", "650727"),
            EloDescriptor("650901", "650978"),
            EloDescriptor("651652", "651679"),
            EloDescriptor("655000", "655019"),
            EloDescriptor("655021", "655058"),
            MirDescriptor("2200", "2204"),
            HiperDescriptor("637095"),
            HiperDescriptor("63737423"),
            HiperDescriptor("63743358"),
            HiperDescriptor("637568"),
            HiperDescriptor("637599"),
            HiperDescriptor("637609"),
            HiperDescriptor("637612"),
            HiperCardDescriptor("606282"),
            DankortCardDescriptor("5019")
        ).sorted()

        fun lookup(bin: String): Descriptor {
            val matching = types.filter { it.matches(bin) }

            return if (matching.isNotEmpty()) {
                matching.minBy { descriptor ->
                    descriptor.shortestDistanceInRange(bin)
                }
            } else {
                Descriptor(Type.OTHER, CARD_GAPS_4_8_12, CARD_LENS_16, "", "")
            }
        }

        fun lookupAll(bin: String): List<Descriptor> {
            val matching = types.filter { it.matches(bin) }
            return matching.ifEmpty {
                listOf(Descriptor(Type.OTHER, CARD_GAPS_4_8_12, CARD_LENS_16, "", ""))
            }
        }

        fun lookupByCardNetwork(cardNetwork: String): Descriptor {
            val matching = types.filter {
                it.type == Type.entries.firstOrNull { type -> type.name == cardNetwork }
            }

            return if (matching.isNotEmpty()) {
                matching.first()
            } else {
                Descriptor(Type.OTHER, CARD_GAPS_4_8_12, CARD_LENS_16, "", "")
            }
        }
    }
}

private const val CARD_NUM_GAP_4 = 4
private const val CARD_NUM_GAP_8 = 8
private const val CARD_NUM_GAP_10 = 10
private const val CARD_NUM_GAP_12 = 12

private val CARD_GAPS_4_10 = listOf(
    CARD_NUM_GAP_4,
    CARD_NUM_GAP_10
)
private val CARD_GAPS_4_8_12 = listOf(
    CARD_NUM_GAP_4,
    CARD_NUM_GAP_8,
    CARD_NUM_GAP_12
)

private const val CARD_NUM_LEN_12 = 12
private const val CARD_NUM_LEN_13 = 13
private const val CARD_NUM_LEN_14 = 14
private const val CARD_NUM_LEN_15 = 15
private const val CARD_NUM_LEN_16 = 16
private const val CARD_NUM_LEN_17 = 17
private const val CARD_NUM_LEN_18 = 18
private const val CARD_NUM_LEN_19 = 19

private val CARD_LENS_15 = listOf(
    CARD_NUM_LEN_15
)
private val CARD_LENS_16 = listOf(
    CARD_NUM_LEN_16
)
private val CARD_LENS_16_19 = listOf(
    CARD_NUM_LEN_16,
    CARD_NUM_LEN_19
)
private val CARD_LENS_14_16_19 = listOf(
    CARD_NUM_LEN_14,
    CARD_NUM_LEN_16,
    CARD_NUM_LEN_19
)
private val CARD_LENS_16_18_19 = listOf(
    CARD_NUM_LEN_16,
    CARD_NUM_LEN_18,
    CARD_NUM_LEN_19
)
private val CARD_LENS_16_17_18_19 = listOf(
    CARD_NUM_LEN_16,
    CARD_NUM_LEN_17,
    CARD_NUM_LEN_18,
    CARD_NUM_LEN_19
)
private val CARD_LENS_12_13_14_15_16_17_18_19 = listOf(
    CARD_NUM_LEN_12,
    CARD_NUM_LEN_13,
    CARD_NUM_LEN_14,
    CARD_NUM_LEN_15,
    CARD_NUM_LEN_16,
    CARD_NUM_LEN_17,
    CARD_NUM_LEN_18,
    CARD_NUM_LEN_19
)

private const val CVV_LEN_3 = 3
private const val CVV_LEN_4 = 4
