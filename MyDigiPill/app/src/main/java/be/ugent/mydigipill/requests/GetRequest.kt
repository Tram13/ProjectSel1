package be.ugent.mydigipill.requests

import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONObject

/**
 * This class is used to send requests to the Food and Drug Administration api of the United States.
 * It makes use of the Android Networking library to send the actual request.
 * @author Arthur Deruytter
 */
object GetRequest {

    private const val TAG: String = "GETREQUEST"//this.javaClass.simpleName

    /**
     * Each request expects
     * @param uri this is a string that denotes a uri to which the request will be sent.
     * @param type this is the type of search request we will be doing.
     * @param priority this is the priority of the request, the default is
     * @see Priority.HIGH
     * @param value This is the value that we will be searching for,
     * we ONLY take this if the type does not have a value set already.
     */
    fun makeGetRequestToURI(
        uri: String = "https://api.fda.gov/drug/label.json",
        type: SearchTypes,
        priority: Priority = Priority.HIGH,
        value: String,
        skip: Int = 0,
        onComplete: ((res: JSONObject?) -> Unit),
        onError: ((error: ANError) -> Unit)
    ) {
        if (type.value.isNullOrEmpty()) type.value = value.replace(' ', '+')
        AndroidNetworking.get(uri)
            .addQueryParameter("search", makeName(type))
            .addQueryParameter("limit", type.limit.toString())
            .addQueryParameter("skip", skip.toString())
            .setTag(type.tag)
            .setPriority(priority)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    /*
                    val string = response!!.getJSONArray("results").getJSONObject(0).toString()
                    val maxLogSize = 1000
                    for (i in 0..(string.length / maxLogSize)) {
                        val start = i * maxLogSize
                        var end = (i + 1) * maxLogSize
                        end = if (end > string.length) string.length else end
                        Log.v("INFOLEAFLET", string.substring(start, end))
                    }
                    */
                    onComplete(response)
                }

                override fun onError(anError: ANError) {
                    onError(anError)
                }
            }
            )
    }

    /**
     * This is a small help function to make the full query string.
     */
    private fun makeName(type: SearchTypes): String {
        return "${type.fullName}:\"${type.value}\""
    }

    /**
     * This enum class holds all of the different search types we support.
     */
    enum class SearchTypes {
        /**
         * This type searches all the generic names of Drugs for a match.
         */
        GENERIC {
            override val fullName: String = "openfda.generic_name"
            override val tag: String = "GENERIC TAG"
            override var value: String? = null
        },

        /**
         * This type searches all the names of the manufacturers of Drugs for a match.
         */
        MANUFACTURER {
            override val fullName: String = "openfda.manufacturer_name"
            override val tag: String = "MANUFACTURER TAG"
            override var value: String? = null
            override var limit: Int = 10
            //We do this because if you search by manufacturer the first one will probably not be the one you want
        },

        /**
         * This type searches all the substance names of Drugs for a match.
         */
        SUBSTANCE {
            override val fullName: String = "openfda.substance_name"
            override val tag: String = "SUBSTANCE TAG"
            override var value: String? = null
            override var limit: Int = 10
            //We do this because if you search by substances the first one will probably not be the one you want
        },

        /**
         * This type searches all the brand names of Drugs for a match.
         */
        BRAND {
            override val fullName: String = "openfda.brand_name"
            override val tag: String = "BRAND TAG"
            override var value: String? = null
            override var limit: Int = 10
        };

        abstract val fullName: String
        abstract val tag: String
        open var value: String? = null
        open var limit: Int = 1

    }

}
