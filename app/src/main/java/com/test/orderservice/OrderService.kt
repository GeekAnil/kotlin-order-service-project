package com.test.orderservice

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class OrderService : Service() {


    private var binder = MyBinder()
    private var context: Context? = null
    private lateinit var callback: MainActivity.CallbackService
    var apples = 0
    var oranges = 0
    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        this.context = this
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    fun calculatePrice(inputs: List<String>) {
        var totalPrice: Int
        splitInput(inputs)
        totalPrice = apples * Utils.PRICE_APPLE + oranges * Utils.PRICE_ORANGE
        if (totalPrice > 0) {
            callback.getResult("Total price is " + totalPrice + " cents")
            callback.showNotification("Hi Customer, Thanks for placing order with us, will provide you update once it is out for delivery")
        } else {
            callback.getResult("Invalid input provided")
        }
    }

    fun addCallback(callback: MainActivity.CallbackService) {
        this.callback = callback;
    }

    fun calculatePriceWithOffer(inputs: List<String>) {
        val totalPrice: Int
        splitInput(inputs)
        val orangeSet = oranges / 2
        val offeredOranges = orangeSet
        val appleOffered = apples
        totalPrice = (apples * Utils.PRICE_APPLE + oranges * Utils.PRICE_ORANGE)
        if (totalPrice > 0) {
            val stringBuilder = StringBuilder()
            if (appleOffered > 0) {
                stringBuilder.append("\nfree " + appleOffered + " apples")
            }
            if (offeredOranges > 0) {
                stringBuilder.append("\nfree " + offeredOranges + " oranges")
            }
            callback.getResult("Total price is " + totalPrice + " cents" + stringBuilder)
            callback.showNotification(
                "Hi Customer, Thanks for placing order with us, will provide you update once it is out " +
                        "for delivery " + if (stringBuilder.length == 0) "" else "Congratulations you have got offer on your order: " + stringBuilder
            )
        } else {
            callback.getResult("Invalid input provided")
        }
    }

    private fun splitInput(inputs: List<String>) {
        apples = 0
        oranges = 0

        for (i in 0..inputs.size - 1) {
            if (inputs[i].equals(Utils.APPLE)) apples++
            else if (inputs[i].equals(Utils.ORANGE)) oranges++
        }
    }

    class MyBinder : Binder() {
        val service: OrderService
            get() = OrderService()
    }
}
