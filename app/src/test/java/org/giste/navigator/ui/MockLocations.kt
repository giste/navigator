package org.giste.navigator.ui

interface MockLocations {
    fun getDistance(): Int
    fun getLocations(): List<Location>
}

object EmptyRoute: MockLocations {
    override fun getDistance() = 0

    override fun getLocations() = listOf<Location>()
}

object TestRoute: MockLocations {
    override fun getDistance() = 1344

    override fun getLocations()= listOf(
        Location(latitude = 40.601367, longitude = -3.699775, altitude = 715.000000),
        Location(latitude = 40.601360, longitude = -3.699792, altitude = 715.000000),
        Location(latitude = 40.601299, longitude = -3.700251, altitude = 716.000000),
        Location(latitude = 40.601285, longitude = -3.700583, altitude = 717.000000),
        Location(latitude = 40.601292, longitude = -3.700853, altitude = 718.000000),
        Location(latitude = 40.601313, longitude = -3.701167, altitude = 718.000000),
        Location(latitude = 40.601354, longitude = -3.701481, altitude = 719.000000),
        Location(latitude = 40.601380, longitude = -3.701975, altitude = 720.000000),
        Location(latitude = 40.601374, longitude = -3.702102, altitude = 720.000000),
        Location(latitude = 40.601354, longitude = -3.702245, altitude = 721.000000),
        Location(latitude = 40.601313, longitude = -3.702461, altitude = 721.000000),
        Location(latitude = 40.601142, longitude = -3.702981, altitude = 722.000000),
        Location(latitude = 40.601128, longitude = -3.703018, altitude = 722.000000),
        Location(latitude = 40.600910, longitude = -3.703781, altitude = 722.000000),
        Location(latitude = 40.600787, longitude = -3.704733, altitude = 723.000000),
        Location(latitude = 40.600760, longitude = -3.704966, altitude = 723.000000),
        Location(latitude = 40.600760, longitude = -3.705102, altitude = 723.000000),
        Location(latitude = 40.600685, longitude = -3.705488, altitude = 722.000000),
        Location(latitude = 40.600542, longitude = -3.705775, altitude = 722.000000),
        Location(latitude = 40.600405, longitude = -3.706044, altitude = 723.000000),
        Location(latitude = 40.600344, longitude = -3.706161, altitude = 723.000000),
        Location(latitude = 40.600221, longitude = -3.706377, altitude = 724.000000),
        Location(latitude = 40.600057, longitude = -3.706683, altitude = 724.000000),
        Location(latitude = 40.599866, longitude = -3.706970, altitude = 724.000000),
        Location(latitude = 40.599621, longitude = -3.707213, altitude = 725.000000),
        Location(latitude = 40.599546, longitude = -3.707293, altitude = 725.000000),
        Location(latitude = 40.599485, longitude = -3.707436, altitude = 725.000000),
        Location(latitude = 40.599512, longitude = -3.707500, altitude = 725.000000),
        Location(latitude = 40.599526, longitude = -3.707563, altitude = 725.000000),
        Location(latitude = 40.599526, longitude = -3.707643, altitude = 725.000000),
        Location(latitude = 40.599512, longitude = -3.707725, altitude = 725.000000),
        Location(latitude = 40.599485, longitude = -3.707796, altitude = 725.000000),
        Location(latitude = 40.599444, longitude = -3.707851, altitude = 726.000000),
        Location(latitude = 40.599430, longitude = -3.707859, altitude = 726.000000),
        Location(latitude = 40.599389, longitude = -3.707886, altitude = 726.000000),
        Location(latitude = 40.599328, longitude = -3.707913, altitude = 726.000000),
        Location(latitude = 40.599287, longitude = -3.707994, altitude = 727.000000),
        Location(latitude = 40.599273, longitude = -3.708012, altitude = 727.000000),
        Location(latitude = 40.599205, longitude = -3.708128, altitude = 727.000000),
        Location(latitude = 40.599143, longitude = -3.708281, altitude = 727.000000),
        Location(latitude = 40.599027, longitude = -3.708560, altitude = 729.000000),
        Location(latitude = 40.599000, longitude = -3.708631, altitude = 730.000000),
        Location(latitude = 40.598877, longitude = -3.709027, altitude = 731.000000),
        Location(latitude = 40.598864, longitude = -3.709090, altitude = 731.000000),
        Location(latitude = 40.598809, longitude = -3.709377, altitude = 731.000000),
        Location(latitude = 40.598809, longitude = -3.709386, altitude = 731.000000),
        Location(latitude = 40.598782, longitude = -3.709610, altitude = 732.000000),
        Location(latitude = 40.598761, longitude = -3.709836, altitude = 733.000000),
        Location(latitude = 40.598755, longitude = -3.710033, altitude = 733.000000),
        Location(latitude = 40.598769, longitude = -3.710375, altitude = 734.000000),
        Location(latitude = 40.598789, longitude = -3.710671, altitude = 735.000000),
        Location(latitude = 40.598789, longitude = -3.710922, altitude = 737.000000),
        Location(latitude = 40.598782, longitude = -3.711048, altitude = 737.000000),
        Location(latitude = 40.598761, longitude = -3.711147, altitude = 738.000000),
        Location(latitude = 40.598973, longitude = -3.711308, altitude = 739.000000),
        Location(latitude = 40.598993, longitude = -3.711318, altitude = 739.000000),
        Location(latitude = 40.599198, longitude = -3.711471, altitude = 739.000000),
        Location(latitude = 40.599335, longitude = -3.711578, altitude = 739.000000),
        Location(latitude = 40.599355, longitude = -3.711597, altitude = 739.000000),
        Location(latitude = 40.599716, longitude = -3.711866, altitude = 738.000000),
        Location(latitude = 40.600085, longitude = -3.712153, altitude = 736.000000),
        Location(latitude = 40.600167, longitude = -3.712216, altitude = 735.000000),
        Location(latitude = 40.600139, longitude = -3.712360, altitude = 735.000000),
        Location(latitude = 40.600119, longitude = -3.712513, altitude = 735.000000),
        Location(latitude = 40.600037, longitude = -3.712459, altitude = 735.000000),
        Location(latitude = 40.599921, longitude = -3.712360, altitude = 736.000000),
        Location(latitude = 40.599668, longitude = -3.712180, altitude = 736.000000),
        Location(latitude = 40.599457, longitude = -3.712010, altitude = 737.000000),
    )
}
