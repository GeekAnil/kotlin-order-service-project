package com.test.orderservice

import android.app.NotificationManager
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.test.orderservice.OrderService.MyBinder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var serviceBinder: OrderService ?= null
    companion object {
        var stockAvailable = true
    }
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.v("Tag", "Order received")
            if (intent?.extras?.containsKey("stock_available")!!) {
                stockAvailable = (intent?.extras?.getString("stock_available")).equals("true")
            } else {
                if (stockAvailable) {
                    val list = intent?.extras?.getString("order_items")?.split(",")!!
                    if (intent.extras?.containsKey("is_offer")!!) {
                        serviceBinder?.calculatePriceWithOffer(list)
                    } else {
                        serviceBinder?.calculatePrice(list)
                    }
                } else {
                    generateNotification("Your order is cancelled as we are running out of stock")
                }
            }
        }
    }

    interface CallbackService {
        fun getResult(result : String)
        fun showNotification(message: String)
    }
    var connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            serviceBinder = (service as MyBinder).service
            serviceBinder?.addCallback(callback = object: CallbackService{
                override fun getResult(result: String) {
                    txt_result.setText(result)
                }

                override fun showNotification(message: String) {
                    generateNotification(message)
                }
            })
        }
        override fun onServiceDisconnected(name: ComponentName) {
            serviceBinder = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initConnectionToService()
        btn_calculate.setOnClickListener{
            var input = edit_inputs.text.toString()
            input = input.replace("[","")
            input = input.replace("]","")
            val inputList = input.split(",")
            if (serviceBinder != null) {
                serviceBinder?.calculatePrice(inputList)
            } else {
               Toast.makeText(this, getString(R.string.service_not_binded), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.test.orderservice.receiveOrders")
        registerReceiver(receiver, intentFilter)
    }

    override fun onPause() {
        unregisterReceiver(receiver)
        super.onPause()
    }

    private fun initConnectionToService() {
        val intent = Intent(this, OrderService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun generateNotification(message: String) {
        val builder = NotificationCompat.Builder(this)
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle("Notification from OrderService Team")
        builder.setContentText(message)
        builder.setStyle(NotificationCompat.BigTextStyle())
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(1, builder.build())
    }
}