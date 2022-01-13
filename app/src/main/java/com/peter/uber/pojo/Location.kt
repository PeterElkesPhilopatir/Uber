package com.peter.uber.pojo

 open class Location(
    var name:String,
    var latitude: Double,
    var longitude: Double
){
    fun hash(): HashMap<String, Any> {
        return hashMapOf(
            "name" to this.name,
            "latitude" to this.latitude,
            "longitude" to this.longitude
        )
    }
}