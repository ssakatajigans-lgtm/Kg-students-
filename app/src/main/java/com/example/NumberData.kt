package com.example

data class NumberDetail(
    val value: Int,
    val englishSymbol: String,
    val englishWord: String,
    val hindiSymbol: String,
    val hindiWord: String,
    val hindiTransliteration: String,
    val gujaratiSymbol: String,
    val gujaratiWord: String,
    val gujaratiTransliteration: String
)

object NumberData {
    val HINDI_DIGITS = charArrayOf('०', '१', '२', '३', '४', '५', '६', '७', '८', '९')
    val GUJARATI_DIGITS = charArrayOf('૦', '૧', '૨', '૩', '૪', '૫', '૬', '૭', '૮', '૯')

    fun Int.toHindiDigitsString(): String {
        return this.toString().map { HINDI_DIGITS[it - '0'] }.joinToString("")
    }

    fun Int.toGujaratiDigitsString(): String {
        return this.toString().map { GUJARATI_DIGITS[it - '0'] }.joinToString("")
    }

    private val englishOnes = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    )
    private val englishTens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )

    fun getEnglishWordRepresentation(number: Int): String {
        return when {
            number < 20 -> englishOnes[number]
            number < 100 -> {
                val ten = englishTens[number / 10]
                val one = englishOnes[number % 10]
                if (one.isEmpty()) ten else "$ten-$one"
            }
            number == 100 -> "One Hundred"
            else -> ""
        }
    }

    private val hindiWords = arrayOf(
        // 1 to 100
        "एक", "दो", "तीन", "चार", "पाँच", "छह", "सात", "आठ", "नौ", "दस",
        "ग्यारह", "बारह", "तेरह", "चौदह", "पन्द्रह", "सोलह", "सत्रह", "अठारह", "उन्नीस", "बीस",
        "इक्कीस", "बाईस", "तेईस", "चौबीस", "पच्चीस", "छब्बीस", "सत्ताईस", "अट्ठाईस", "उनतीस", "तीस",
        "इकतीस", "बत्तीस", "तैंत्तीस", "चौंतीस", "पैंतीस", "छत्तीस", "सैंतीस", "अड़तीस", "उनतालीस", "चालीस",
        "इकतालीस", "बयालीस", "तैंतालीस", "चवालीस", "पैंतालीस", "छियालीस", "सैंतालीस", "अड़तालीस", "उनचास", "पचास",
        "इक्कावन", "बावन", "तिरपन", "चौवन", "पचपन", "छप्पन", "सत्तावन", "अट्ठावन", "उनसठ", "साठ",
        "इकसठ", "बासठ", "तिरसठ", "चौंसठ", "पैंसठ", "छियासठ", "सतसठ", "अड़सठ", "उनहत्तर", "सत्तर",
        "इकहत्तर", "बहत्तर", "तिहत्तर", "चौहत्तर", "पचहत्तर", "छहत्तर", "सतहत्तर", "अठहत्तर", "उनासी", "अस्सी",
        "इक्यासी", "बयासी", "तिरासी", "चौरासी", "पचासी", "छियासी", "सतासी", "अठासी", "नवासी", "नब्बे",
        "इक्यानवे", "बानवे", "तिरानवे", "चौरानवे", "पचानवे", "छियानवे", "सत्तानवे", "अट्ठानवे", "निन्यानवे", "सौ"
    )

    private val hindiTransliterations = arrayOf(
        "Ek", "Do", "Teen", "Char", "Paanch", "Chhah", "Saat", "Aath", "Nau", "Das",
        "Gyaarah", "Baarah", "Teerah", "Chaudah", "Pandrah", "Solah", "Satrah", "Atthaarah", "Unnees", "Bees",
        "Ikkees", "Baarees", "Tayees", "Chaubees", "Pachchees", "Chhabbees", "Sattaayees", "Atthaayees", "Untees", "Tees",
        "Iktees", "Battees", "Taintees", "Chauntees", "Paintees", "Chhattees", "Saintees", "Adtees", "Untaalees", "Chaalees",
        "Iktaalees", "Bayaalees", "Taintalees", "Chawaalees", "Paintaalees", "Chhiyaalees", "Saintaalees", "Artaalees", "Unchaas", "Pachaas",
        "Ikkaawan", "Baawan", "Tirpan", "Chauwan", "Pachpan", "Chhappan", "Sattaawan", "Atthaawan", "Unsath", "Saath",
        "Iksath", "Baasath", "Tirsath", "Chaunsath", "Painsath", "Chhiyaasath", "Satsath", "Arsath", "Unhattar", "Sattar",
        "Ikhattar", "Bahattar", "Tihattar", "Chauhattar", "Pachhattar", "Chhahattar", "Satahattar", "Athahtar", "Unaassee", "Assee",
        "Ikyaasee", "Bayaasee", "Tiraasee", "Chauraasee", "Pachaasee", "Chhiyaasee", "Sataasee", "Athaasee", "Nawaasee", "Nabbee",
        "Ikyaanwe", "Baanwe", "Tiraanwe", "Chauraanwe", "Pachaanwe", "Chhiyaanwe", "Sattaanwe", "Atthaanwe", "Ninyaanwe", "Sau"
    )

    private val gujaratiWords = arrayOf(
        "એક", "બે", "ત્રણ", "ચાર", "પાંચ", "છ", "સાત", "આઠ", "નવ", "દસ",
        "અગિયાર", "બાર", "તેર", "ચૌદ", "પંદર", "સોળ", "સત્તર", "અઢાર", "ઓગણીસ", "વીસ",
        "એકવીસ", "બાવીસ", "તેવીસ", "ચોવીસ", "પંચીસ", "છવીસ", "સત્તાવીસ", "અઠ્ઠાવીસ", "ઓગણત્રીસ", "ત્રીસ",
        "એકત્રીસ", "બત્રીસ", "તેત્રીસ", "ચોત્રીસ", "પાંત્રીસ", "છત્રીસ", "સાડત્રીસ", "આડત્રીસ", "ઓગણચાળીસ", "ચાળીસ",
        "એકતાલીસ", "બેતાલીસ", "તેતાલીસ", "ચોતાલીસ", "પિસ્તાલીસ", "છેતાલીસ", "સુડતાલીસ", "અડતાલીસ", "ઓગણપચાસ", "પચાસ",
        "એકાવન", "બાવન", "ત્રેપન", "ચોવન", "પંચાવન", "છપ્પન", "સતાવન", "અઠાવન", "ઓગણસાઈઠ", "સાઈઠ",
        "એકસઠ", "બાસઠ", "ત્રેસઠ", "ચોસઠ", "પાંસઠ", "છાસઠ", "સડસઠ", "આડસઠ", "ઓગણસિત્તેર", "સિત્તેર",
        "એકોતેર", "બોતેર", "તોતેર", "ચોતેર", "પંચોતેર", "છોતેર", "સિત્યોતેર", "ઇઠ્યોતેર", "ઓગણએસી", "એસી",
        "એક્યાસી", "બ્યાસી", "ત્યાસી", "ચોર્યાસી", "પંચાસી", "છ્યાસી", "સત્યાસી", "અઠ્યાસી", "ઓગણતેવું", "નેવું",
        "એકાણું", "બાણું", "ત્રાણું", "ચોરાણું", "પંચાણું", "છન્નું", "સત્તાણું", "અઠ્ઠાણું", "નવાણું", "સો"
    )

    private val gujaratiTransliterations = arrayOf(
        "Ek", "Be", "Tran", "Chaar", "Paanch", "Chha", "Saat", "Aath", "Nav", "Das",
        "Agiyar", "Baar", "Ter", "Chaud", "Pandar", "Sol", "Sattar", "Adhaar", "Ognis", "Vees",
        "Ekvees", "Bavees", "Tevees", "Chovees", "Panchees", "Chhavees", "Sattavees", "Atthavees", "Ogantrees", "Trees",
        "Ektrees", "Batrees", "Tetrees", "Chotrees", "Pantrees", "Chhatrees", "Sadtrees", "Adtrees", "Oganchalees", "Chalees",
        "Ektalees", "Betalees", "Tetalees", "Chotalees", "Pistalees", "Chetalees", "Sudtalees", "Adtalees", "Oganpachas", "Pachas",
        "Ekavan", "Bavan", "Trepan", "Chovan", "Panchavan", "Chhappan", "Satavan", "Athavan", "Ogansaaith", "Saaith",
        "Eksath", "Baasath", "Tresath", "Chosath", "Paansath", "Chhasath", "Sadsath", "Aadsath", "Ogansitter", "Sitter",
        "Ekoter", "Boter", "Toter", "Choter", "Panchoter", "Chhoter", "Sityoter", "Ithyoter", "Oganasi", "Asi",
        "Ekyasi", "Byasi", "Tyasi", "Choryasi", "Panchasi", "Chhyasi", "Satyasi", "Athyasi", "Ogantevu", "Nevu",
        "Ekaanum", "Baanum", "Traanum", "Choraanum", "Panchaanum", "Chhannum", "Sattaanum", "Aththaanum", "Navaanum", "So"
    )

    val list: List<NumberDetail> by lazy {
        (1..100).map { i ->
            NumberDetail(
                value = i,
                englishSymbol = i.toString(),
                englishWord = getEnglishWordRepresentation(i),
                hindiSymbol = i.toHindiDigitsString(),
                hindiWord = hindiWords[i - 1],
                hindiTransliteration = hindiTransliterations[i - 1],
                gujaratiSymbol = i.toGujaratiDigitsString(),
                gujaratiWord = gujaratiWords[i - 1],
                gujaratiTransliteration = gujaratiTransliterations[i - 1]
            )
        }
    }
}
