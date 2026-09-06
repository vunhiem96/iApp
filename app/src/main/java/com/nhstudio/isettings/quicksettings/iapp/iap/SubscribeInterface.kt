package com.nhstudio.isettings.quicksettings.iapp.iap

import com.nhstudio.isettings.quicksettings.iapp.iap.model.IapModel

interface SubscribeInterface {
    fun subscribeSuccess(productModel: IapModel)
    fun subscribeError(code: Int, error: String)
}
