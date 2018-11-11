package eu.miaounyan.isthereanynetwork.service.isthereanynetwork

import eu.miaounyan.isthereanynetwork.service.DateFormatter
import java.util.*

class IsThereAnyNetworkParams(val params: MutableMap<String, String>) : MutableMap<String, String> by params {
    private val dateFormatter  = DateFormatter();

    fun fromDate(v : Date) : IsThereAnyNetworkParams {
        params["from"] = dateFormatter.toISO8601String(v);
        return this;
    }

    fun toDate(v : Date) : IsThereAnyNetworkParams {
        params["to"] = dateFormatter.toISO8601String(v);
        return this;
    }

    fun withOperatorName(v : String) : IsThereAnyNetworkParams {
        params["operatorName"] = v;
        return this;
    }

    fun withNetworkProtocol(v : String) : IsThereAnyNetworkParams {
        params["networkProtocol"] = v;
        return this;
    }

    fun withSignalStrengthGreaterThan(v : Int) : IsThereAnyNetworkParams {
        params["signalStrengthGreaterThan"] = v.toString();
        return this;
    }

    fun withSignalStrengthLowerThan(v : Int) : IsThereAnyNetworkParams {
        params["signalStrengthLowerThan"] = v.toString();
        return this;
    }

    fun withLatitudeGreaterThan(v : Double) : IsThereAnyNetworkParams {
        params["latitudeGreaterThan"] = v.toString();
        return this;
    }

    fun withLatitudeLowerThan(v : Double) : IsThereAnyNetworkParams {
        params["latitudeLowerThan"] = v.toString();
        return this;
    }

    fun withLongitudeGreaterThan(v : Double) : IsThereAnyNetworkParams {
        params["longitudeGreaterThan"] = v.toString();
        return this;
    }

    fun withLongitudeLowerThan(v : Double) : IsThereAnyNetworkParams {
        params["longitudeLowerThan"] = v.toString();
        return this;
    }
}