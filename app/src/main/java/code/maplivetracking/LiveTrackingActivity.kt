package code.maplivetracking

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import code.utils.AppConstants
import code.utils.AppSettings
import code.utils.AppUrls
import code.utils.AppUtils
import code.utils.WebServices
import code.utils.WebServicesCallback
import code.view.BaseActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hathme.android.R
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class LiveTrackingActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var defaultLocation: LatLng
    private var originMarker: Marker? = null
    private var destinationMarker: Marker? = null
    private var movingCabMarker: Marker? = null
    private var previousLatLng: LatLng? = null
    private var currentLatLng: LatLng? = null
    private var arrayList = ArrayList<HashMap<String, String>>()
    private var orderId: String = ""

    private lateinit var locationRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var ivImage:ImageView
    private lateinit var tvShopName:TextView
    private lateinit var tvDeliveryPersonName:TextView
    private lateinit var tvDeliveryPersonStatus:TextView
    private lateinit var rlBottom:RelativeLayout
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_tracking)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        ivImage = findViewById(R.id.ivImage)
        tvShopName = findViewById(R.id.tvShopName)
        rlBottom = findViewById(R.id.rlBottom)
        tvDeliveryPersonName = findViewById(R.id.tvDeliveryPersonName)
        tvDeliveryPersonStatus = findViewById(R.id.tvDeliveryPersonStatus)
        if (intent.extras != null && intent.hasExtra("orderId")) {
            orderId = intent.getStringExtra("orderId").toString()
        }
        else
        {
            orderId = ""
        }


        auth = FirebaseAuth.getInstance()

    }

    private fun hitGetOrderDetailApi() {

        val jsonObject = JSONObject()
        val json = JSONObject()

        try {
            jsonObject.put("orderId", orderId)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        json.put(AppConstants.projectName, jsonObject)

        WebServices.postApi(
            mActivity,
            AppUrls.orderDetail,
            json,
            false,
            false,
            object : WebServicesCallback {

                override fun OnJsonSuccess(response: JSONObject?) {

                    parseDetail(response)
                }

                override fun OnFail(response: String?) {

                }

            })
    }

    private fun parseDetail(response: JSONObject?) {
        arrayList.clear()
        try {

            val jsonObject = response?.getJSONObject(AppConstants.projectName)

            if (jsonObject?.getString(AppConstants.resCode).equals("1")) {

                val jsonData = jsonObject?.getJSONObject("data");

                tvShopName.text = jsonData?.getString("businessName")
                val jsonArray = jsonData!!.getJSONArray("imagesBussiness")
                 if (jsonData.getString("status").equals("1"))
                 {
                     tvDeliveryPersonStatus.text= getString(R.string.yourOrderPending)
                 }
                 else if (jsonData.getString("status").equals("2"))
                 {
                     tvDeliveryPersonStatus.text= getString(R.string.acceptedOrder)
                 }
                 else if (jsonData.getString("status").equals("4"))
                 {
                     tvDeliveryPersonStatus.text= getString(R.string.deliveryReachedAtPickup)
                 }
                 else if (jsonData.getString("status").equals("5"))
                 {
                     tvDeliveryPersonStatus.text= getString(R.string.pickedUpOrder)
                 }
                 else if (jsonData.getString("status").equals("6"))
                 {
                     tvDeliveryPersonStatus.text= getString(R.string.deliveredDelivered)
                     Looper.myLooper()?.let {
                         Handler(it).postDelayed({
                             rlBottom.visibility = View.GONE
                             completeOrder(getString(R.string.deliveredDelivered))
                         }, 1000)
                     }
                 }

                for (i in 0 until jsonArray!!.length()) {
                    val job = jsonArray!!.getJSONObject(i)
                    val hashMap = HashMap<String, String>()
                    hashMap["imageUrl"] = job.getString("imageUrl")
                    arrayList.add(hashMap)
                }
                AppUtils.loadPicassoImage(arrayList[0]["imageUrl"], ivImage)
                //Order Accepted & Driver assigned
//                 if (jsonData?.getString("status") == "1" && jsonData.getString("merchantLatitude").isNotEmpty()) {
//
//                     val origin = LatLng(AppUtils.returnDouble(jsonData.getString("merchantLatitude")), AppUtils.returnDouble(jsonData.getString("merchantLongitude")))
//                     val destination = LatLng(AppUtils.returnDouble(jsonData.getString("latitude")), AppUtils.returnDouble(jsonData.getString("longitude")))
//                     tvDeliveryPersonName.text = AppSettings.getString(AppSettings.name)
//                     originMarker = addOriginDestinationMarkerAndGet(origin)
//                     originMarker?.setAnchor(0.5f, 0.5f)
//
//                     destinationMarker = addOriginDestinationMarkerAndGet(destination)
//                     destinationMarker?.setAnchor(0.5f, 0.5f)
//
//                     drawRoute(origin, destination)
//
//                     locationRef = FirebaseDatabase.getInstance().getReference("locations").child(jsonData.getJSONObject("driverData").getString("_id"))
//                     listenForLocationUpdates()
//
//
//
//
//                }
                if (jsonData?.getString("status") == "2" && jsonData.getString("driverLatitude").isNotEmpty()) {
//                     if (jsonData.getString("driverLatitude").isEmpty()||jsonData.getString("driverLatitude").equals(""))
//                     {
//
//                         val origin = LatLng(AppUtils.returnDouble(jsonData.getString("merchantLatitude")), AppUtils.returnDouble(jsonData.getString("merchantLongitude")))
//                         val destination = LatLng(AppUtils.returnDouble(jsonData.getString("latitude")), AppUtils.returnDouble(jsonData.getString("longitude")))
//                         tvDeliveryPersonName.text = AppSettings.getString(AppSettings.name)
//                         originMarker = addOriginDestinationMarkerAndGet(origin)
//                         originMarker?.setAnchor(0.5f, 0.5f)
//
//                         destinationMarker = addOriginDestinationMarkerAndGet(destination)
//                         destinationMarker?.setAnchor(0.5f, 0.5f)
//
//                         drawRoute(origin, destination)
//
//                         locationRef = FirebaseDatabase.getInstance().getReference("locations").child(jsonData.getJSONObject("driverData").getString("_id"))
//                         listenForLocationUpdates()
//
//                     }
//                     else
//                     {
                         val origin = LatLng(AppUtils.returnDouble(jsonData.getString("driverLatitude")), AppUtils.returnDouble(jsonData.getString("driverLongitude")))
                         val destination = LatLng(AppUtils.returnDouble(jsonData.getString("merchantLatitude")), AppUtils.returnDouble(jsonData.getString("merchantLongitude")))
                         val jsonObject = jsonData.getJSONObject("driverData")
                         tvDeliveryPersonName.text = jsonObject.getString("name")
                         originMarker = addOriginDestinationMarkerAndGet(origin)
                         originMarker?.setAnchor(0.5f, 0.5f)

                         destinationMarker = addOriginDestinationMarkerAndGet(destination)
                         destinationMarker?.setAnchor(0.5f, 0.5f)

                         drawRoute(origin, destination)

                         locationRef = FirebaseDatabase.getInstance().getReference("locations").child(jsonData.getJSONObject("driverData").getString("_id"))
                         listenForLocationUpdates()

                 //    }

                }
                else if (jsonData?.getString("status") == "4" && jsonData.getString("driverLatitude").isNotEmpty()) {

                    val origin = LatLng(AppUtils.returnDouble(jsonData.getString("driverLatitude")), AppUtils.returnDouble(jsonData.getString("driverLongitude")))
                    val destination = LatLng(AppUtils.returnDouble(jsonData.getString("latitude")), AppUtils.returnDouble(jsonData.getString("longitude")))
                     val jsonObject = jsonData.getJSONObject("driverData")
                     tvDeliveryPersonName.text = jsonObject.getString("name")
                    originMarker = addOriginDestinationMarkerAndGet(origin)
                    originMarker?.setAnchor(0.5f, 0.5f)

                    destinationMarker = addOriginDestinationMarkerAndGet(destination)
                    destinationMarker?.setAnchor(0.5f, 0.5f)

                    drawRoute(origin, destination)

                    locationRef = FirebaseDatabase.getInstance().getReference("locations").child(jsonData.getJSONObject("driverData").getString("_id"))
                    listenForLocationUpdates()

                }
                else if (jsonData?.getString("status") == "5" && jsonData.getString("driverLatitude").isNotEmpty()) {

                    val origin = LatLng(AppUtils.returnDouble(jsonData.getString("driverLatitude")), AppUtils.returnDouble(jsonData.getString("driverLongitude")))
                    val destination = LatLng(AppUtils.returnDouble(jsonData.getString("latitude")), AppUtils.returnDouble(jsonData.getString("longitude")))
                     val jsonObject = jsonData.getJSONObject("driverData")
                     tvDeliveryPersonName.text = jsonObject.getString("name")
                    originMarker = addOriginDestinationMarkerAndGet(origin)
                    originMarker?.setAnchor(0.5f, 0.5f)

                    destinationMarker = addOriginDestinationMarkerAndGet(destination)
                    destinationMarker?.setAnchor(0.5f, 0.5f)

                    drawRoute(origin, destination)

                    locationRef = FirebaseDatabase.getInstance().getReference("locations").child(jsonData.getJSONObject("driverData").getString("_id"))
                    listenForLocationUpdates()
                }
                else
                {

                    defaultLocation = LatLng(
                        AppUtils.returnDouble(jsonData?.getString("latitude")),
                        AppUtils.returnDouble(jsonData?.getString("longitude"))
                    )
                    tvDeliveryPersonName.text = AppSettings.getString(AppSettings.name)
                    showDefaultLocationOnMap(defaultLocation)
                }

            } else
                AppUtils.showMessageDialog(
                    mActivity,
                    getString(R.string.orderDetails),
                    jsonObject?.getString(AppConstants.resMsg),
                    2
                )

        } catch (e: Exception) {
            e.printStackTrace();
        }

    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun drawRoute(origin: LatLng, destination: LatLng) {
        val apiKey = getString(R.string.google_api_key)
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&mode=driving" +
                "&key=$apiKey"

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = downloadUrl(url)
                val points = parseDirections(result)

                launch(Dispatchers.Main) {
                    // Draw the polyline on the map
                    val polylineOptions = PolylineOptions()
                        .addAll(points)
                        .width(12f)
                        .color(resources.getColor(R.color.blue)) // You can set your desired color

                    val polyline = googleMap.addPolyline(polylineOptions)

                    // Move the camera to the center of the polyline
                    val builder = LatLngBounds.Builder()
                    builder.include(origin)
                    builder.include(destination)
                    val bounds = builder.build()
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun downloadUrl(strUrl: String): String {
        val url = URL(strUrl)
        val connection = url.openConnection() as HttpURLConnection
        val inputStream = connection.inputStream
        val reader = BufferedReader(InputStreamReader(inputStream))
        val response = StringBuilder()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            response.append(line)
        }

        reader.close()
        inputStream.close()
        connection.disconnect()

        return response.toString()
    }

    private fun parseDirections(result: String): List<LatLng> {
        val points = ArrayList<LatLng>()
        try {
            val jsonObject = JSONObject(result)
            val routes = jsonObject.getJSONArray("routes")

            for (i in 0 until routes.length()) {
                val legs = (routes[i] as JSONObject).getJSONArray("legs")
                for (j in 0 until legs.length()) {
                    val steps = (legs[j] as JSONObject).getJSONArray("steps")
                    for (k in 0 until steps.length()) {
                        val polyline = ((steps[k] as JSONObject)["polyline"] as JSONObject)["points"] as String
                        points.addAll(decodePolyline(polyline))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return points
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(p)
        }
        return poly
    }

    private fun moveCamera(latLng: LatLng) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    private fun animateCamera(latLng: LatLng) {
        val cameraPosition = CameraPosition.Builder().target(latLng).zoom(15.5f).build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun addCarMarkerAndGet(latLng: LatLng): Marker? {
        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(MapUtils.getCarBitmap(this))
        return googleMap.addMarker(
            MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor)
        )
    }

    private fun addOriginDestinationMarkerAndGet(latLng: LatLng): Marker? {
        val bitmapDescriptor =
            BitmapDescriptorFactory.fromBitmap(MapUtils.getOriginDestinationMarkerBitmap())
        return googleMap.addMarker(
            MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor)
        )
    }

    private fun showDefaultLocationOnMap(latLng: LatLng) {
        moveCamera(latLng)
        animateCamera(latLng)
    }

    /**
     * This function is used to update the location of the Cab while moving from Origin to Destination
     */
    private fun updateCarLocation(latLng: LatLng) {
        if (movingCabMarker == null) {
            movingCabMarker = addCarMarkerAndGet(latLng)
        }
        if (previousLatLng == null) {
            currentLatLng = latLng
            previousLatLng = currentLatLng
            movingCabMarker?.position = currentLatLng as LatLng
            movingCabMarker?.setAnchor(0.5f, 0.5f)
            animateCamera(currentLatLng!!)
        } else {
            previousLatLng = currentLatLng
            currentLatLng = latLng
            val valueAnimator = AnimationUtils.carAnimator()
            valueAnimator.addUpdateListener { va ->
                if (currentLatLng != null && previousLatLng != null) {
                    val multiplier = va.animatedFraction
                    val nextLocation = LatLng(
                        multiplier * currentLatLng!!.latitude + (1 - multiplier) * previousLatLng!!.latitude,
                        multiplier * currentLatLng!!.longitude + (1 - multiplier) * previousLatLng!!.longitude
                    )
                    movingCabMarker?.position = nextLocation
                    val rotation = MapUtils.getRotation(previousLatLng!!, nextLocation)
                    if (!rotation.isNaN()) {
                        movingCabMarker?.rotation = rotation
                    }
                    movingCabMarker?.setAnchor(0.5f, 0.5f)
                    animateCamera(nextLocation)
                }
            }
            valueAnimator.start()
        }
    }

    private fun showMovingCab(driverLatitude: Double, driverLongitude: Double) {

        updateCarLocation(LatLng(driverLatitude, driverLongitude))
    }

    override fun onMapReady(googleMap: GoogleMap) {

        this.googleMap = googleMap
        if (orderId == "")
        {

        }
        else
        {
            hitGetOrderDetailApi()
        }


        /* defaultLocation = LatLng(28.435350000000003, 77.11368)
         showDefaultLocationOnMap(defaultLocation)*/

      /*  Handler().postDelayed(Runnable {
            showPath(MapUtils.getListOfLocations())
            showMovingCab(MapUtils.getListOfLocations())
        }, 3000)*/
    }
    private fun listenForLocationUpdates() {

        locationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Iterate through all drivers
                try {
                    val driverLocation = dataSnapshot.getValue(LocationData::class.java)
                    driverLocation?.let {
                        val driverLatitude = it.latitude
                        val driverLongitude = it.longitude
                        showMovingCab(driverLatitude, driverLongitude)
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseError", "Error parsing driver location: ${e.message}")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }
    override fun onResume() {
        super.onResume()
        registerReceiver(mMessageReceiver, IntentFilter("RefreshDetails"))
        if (orderId == "")
        {

        }
        else
        {
            hitGetOrderDetailApi()
        }
    }

    override fun onPause() {
        unregisterReceiver(mMessageReceiver)
        super.onPause()
    }
    private val mMessageReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (orderId == "")
            {

            }
            else
            {
                hitGetOrderDetailApi()
            }
        }

    }
    private fun completeOrder(message: String?) {
        val dialog = Dialog(mActivity, R.style.AppCompatAlertDialogStyleBig)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.exit_confirmation)
        val window = dialog.window
        window!!.addFlags(Window.FEATURE_NO_TITLE)
        window.setLayout(950, 700)
        val localLayoutParams = window.attributes
        localLayoutParams.gravity = Gravity.CENTER
        window.attributes = localLayoutParams
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        val msg = dialog.findViewById<TextView>(R.id.tvDesc)
        val No = dialog.findViewById<TextView>(R.id.tvNo)
        val yes = dialog.findViewById<TextView>(R.id.tvYes)
        msg.text = message
        yes.text = getString(R.string.ok)
        No.visibility = View.GONE
        yes.setOnClickListener {
            onBackPressed()

            dialog.dismiss()
        }
        No.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
