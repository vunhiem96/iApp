package com.nhstudio.isettings.quicksettings.iapp.ui


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.iaplibrary.IapConnector
import com.example.iaplibrary.SubscribeInterface
import com.example.iaplibrary.model.IapModel
import com.nhstudio.iapp.appmanager.R
import com.nhstudio.iapp.appmanager.databinding.FragmentRemoveAdBinding
import com.nhstudio.isettings.quicksettings.iapp.MainActivity
import com.nhstudio.isettings.quicksettings.iapp.extension.PRODUCT_ID
import com.nhstudio.isettings.quicksettings.iapp.extension.PRODUCT_ID_FAKE
import com.nhstudio.isettings.quicksettings.iapp.extension.config
import com.nhstudio.isettings.quicksettings.iapp.extension.haveInternet
import com.nhstudio.isettings.quicksettings.iapp.extension.loadInterAd
import com.nhstudio.isettings.quicksettings.iapp.extension.priceString
import com.nhstudio.isettings.quicksettings.iapp.extension.priceStringFake
import com.nhstudio.isettings.quicksettings.iapp.extension.setFullScreen
import com.nhstudio.isettings.quicksettings.iapp.extension.setPreventDoubleClickAlphaItemView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class IapFragment : Fragment() {
    private lateinit var binding: FragmentRemoveAdBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRemoveAdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getIap()
        binding.btnBuy.setPreventDoubleClickAlphaItemView(1000) {
            buyIAP()
        }
    }

    private fun getIap() {
        binding.pricePiap.text = priceString
        binding.pricePiapFake.text = priceStringFake
        IapConnector.addIAPListener(iapListener)
        IapConnector.listPurchased.observe(viewLifecycleOwner) {
            val data = IapConnector.getAllProductModel()
            data.forEach { product ->
                if (product.productId == PRODUCT_ID) {
                    priceString = product.inAppDetails?.formattedPrice.toString()
                }
                if (product.productId == PRODUCT_ID_FAKE) {
                    priceStringFake = product.inAppDetails?.formattedPrice.toString()
                }
            }
            it?.let {
                CoroutineScope(Dispatchers.Main).launch {
                    binding.pricePiap.text = priceString
                    binding.pricePiapFake.text = priceStringFake
                }
            }
        }


    }

    fun buyIAP() {
        try {
            if (binding.btnBuy.haveInternet()) {
                (activity as MainActivity).buyIAP()
            } else {
                Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Try again", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onResume() {
        super.onResume()
        activity?.setFullScreen()
    }

    val iapListener = object : SubscribeInterface {
        override fun subscribeSuccess(productModel: IapModel) {
            CoroutineScope(Dispatchers.Main).launch {
                config?.pu = false
                loadInterAd = false
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        val toast =
                            Toast.makeText(
                                context,
                                getString(R.string.reset_upate),
                                Toast.LENGTH_SHORT
                            )
                        toast.setGravity(Gravity.BOTTOM, 0, 210)
                        toast.show()
                        findNavController().popBackStack()
                    } catch (e: Exception) {
                    }
                }, 500)
            }
        }

        override fun subscribeError(error: String) {
            if (error != "") {

                //  Toast.makeText(this@MainActivity, "Purchase failed", Toast.LENGTH_LONG).show()
            }
        }

    }


}