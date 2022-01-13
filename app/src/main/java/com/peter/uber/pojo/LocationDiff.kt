package com.peter.uber.pojo

class LocationDiff(name: String, latitude: Double, longitude: Double) :
    Location(name, latitude, longitude) {
    var diff: Double = 0.0
    fun calc(current: Location) {
        var R = 3958.8; // Radius of the Earth in miles
        var rlat1 = current.latitude * (Math.PI / 180); // Convert degrees to radians
        var rlat2 = this.latitude * (Math.PI / 180); // Convert degrees to radians
        var difflat = rlat2 - rlat1; // Radian difference (latitudes)
        var difflon =
            (this.longitude - current.longitude) * (Math.PI / 180); // Radian difference (longitudes)

        this.diff = 2 * R * Math.asin(
            Math.sqrt(
                Math.sin(difflat / 2) * Math.sin(difflat / 2) + Math.cos(rlat1) * Math.cos(rlat2) * Math.sin(
                    difflon / 2
                ) * Math.sin(difflon / 2)
            )
        );


    }
}