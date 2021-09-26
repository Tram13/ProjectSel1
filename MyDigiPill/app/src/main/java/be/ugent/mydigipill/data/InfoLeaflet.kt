package be.ugent.mydigipill.data

/**
 * This data class is used to store everything we want to keep from the api call
 * It is also used to know what to display to the user according to what we got
 * and what is a null pointer.
 */
data class InfoLeaflet(
    val disclaimer: String,
    val description: String? = null,
    val route: String? = null, //route
    val brand: String? = null, //brand_name
    val warnings: String? = null, //warnings_and_cautions
    val warningsSmall: String? = null, //boxed_warning
    val pediatricUse: String? = null, //pediatric_use
    val dosage: String? = null, // dosage_and_administration
    val reactions: String? = null, // adverse_reactions
    val pregnancy: String? = null, // pregnancy
    val overdosage: String? = null // overdosage
) {

    constructor(map: Map<String, String?>) : this(
        disclaimer = map.getOrDefault("disclaimer", "") ?: "",
        description = map["description"],
        route = map["route"],
        brand = map["brand"],
        warnings = map["warnings"],
        warningsSmall = map["warningsSmall"],
        pediatricUse = map["pediatricUse"],
        dosage = map["dosage"],
        reactions = map["reactions"],
        pregnancy = map["pregnancy"],
        overdosage = map["overdosage"]
    )

}