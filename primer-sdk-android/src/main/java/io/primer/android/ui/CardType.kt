package io.primer.android.ui

// FIXME why are these properties unused?
internal class CardType(type: Type, val gaps: List<Int>) {
    enum class Type {
        UNKOWN,
        VISA,
        MASTERCARD,
        AMEX,
        DINERS,
        DISCOVER,
        JCB,
        UNIONPAY,
        MAESTRO,
        ELO,
        MIR,
        HIPER,
        HIPERCARD
    }

    open class Descriptor(
        val type: Type,
        val gaps: List<Int>,
        val lengths: List<Int>,
        lower: String,
        upper: String?,
        val cvvLength: Int = 3,
    ) : Comparable<Descriptor> {

        private val weight: Int = lower.length
        private val min = lower.padEnd(8, '0')
        private val max = (upper ?: lower).padEnd(8, '9')

        override fun compareTo(other: Descriptor): Int {
            return weight - other.weight
        }

        fun matches(bin: String): Boolean {
            return bin.padEnd(8, '0') in min..max
        }
    }

    class VisaDescriptor(lower: String, upper: String? = null) :
        Descriptor(Type.VISA, listOf(4, 8, 12), listOf(16, 18, 19), lower, upper)

    class MastercardDescriptor(lower: String, upper: String? = null) :
        Descriptor(Type.MASTERCARD, listOf(4, 8, 12), listOf(16), lower, upper)

    class AmexDescriptor(lower: String, upper: String? = null) :
        Descriptor(Type.AMEX, listOf(4, 10), listOf(15), lower, upper, cvvLength = 4)

    class DinersDescriptor(lower: String, upper: String? = null) :
        Descriptor(Type.DINERS, listOf(4, 10), listOf(14, 16, 19), lower, upper)

    class DiscoverDescriptor(lower: String, upper: String? = null) :
        Descriptor(Type.DISCOVER, listOf(4, 8, 12), listOf(16, 19), lower, upper)

    class JcbDescriptor(lower: String, upper: String? = null) :
        Descriptor(Type.JCB, listOf(4, 8, 12), listOf(16, 17, 18, 19), lower, upper)

    class UnionpayDescriptor(lower: String, upper: String? = null) :
        Descriptor(Type.UNIONPAY, listOf(4, 8, 12), listOf(14, 15, 16, 17, 18, 19), lower, upper)

    class MaestroDescriptor(lower: String, upper: String? = null) :
        Descriptor(
            Type.MAESTRO,
            listOf(4, 8, 12),
            listOf(12, 13, 14, 15, 16, 17, 18, 19),
            lower,
            upper
        )

    class EloDescriptor(lower: String, upper: String? = null) :
        Descriptor(Type.ELO, listOf(4, 8, 12), listOf(16), lower, upper)

    class MirDescriptor(lower: String, upper: String? = null) :
        Descriptor(Type.MIR, listOf(4, 8, 12), listOf(16, 17, 18, 19), lower, upper)

    class HiperDescriptor(lower: String, upper: String? = null) :
        Descriptor(Type.HIPER, listOf(4, 8, 12), listOf(16), lower, upper)

    class HiperCardDescriptor(lower: String, upper: String? = null) :
        Descriptor(Type.HIPERCARD, listOf(4, 8, 12), listOf(16), lower, upper)

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
            UnionpayDescriptor("620"),
            UnionpayDescriptor("624", "626"),
            UnionpayDescriptor("62100", "62182"),
            UnionpayDescriptor("62184", "62187"),
            UnionpayDescriptor("62185", "62197"),
            UnionpayDescriptor("62200", "62205"),
            UnionpayDescriptor("622010", "622999"),
            UnionpayDescriptor("622018"),
            UnionpayDescriptor("622019", "622999"),
            UnionpayDescriptor("62207", "62209"),
            UnionpayDescriptor("622126", "622925"),
            UnionpayDescriptor("623", "626"),
            UnionpayDescriptor("6270"),
            UnionpayDescriptor("6272"),
            UnionpayDescriptor("6276"),
            UnionpayDescriptor("627700", "627779"),
            UnionpayDescriptor("627781", "627799"),
            UnionpayDescriptor("6282", "6289"),
            UnionpayDescriptor("6291"),
            UnionpayDescriptor("6292"),
            UnionpayDescriptor("810"),
            UnionpayDescriptor("8110", "8131"),
            UnionpayDescriptor("8132", "8151"),
            UnionpayDescriptor("8152", "8163"),
            UnionpayDescriptor("8164", "8171"),
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
            HiperCardDescriptor("606282")
        ).sorted()

        fun lookup(bin: String): Descriptor {
            val matching = types.filter { it.matches(bin) }

            return if (matching.isNotEmpty()) {
                matching[0]
            } else {
                Descriptor(Type.UNKOWN, listOf(4, 8, 12), listOf(16), "", "")
            }
        }
    }
}
