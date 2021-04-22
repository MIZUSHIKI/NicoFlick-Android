package com.mizushiki.nicoflick_a

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody


object HttpUtil {
    val client = OkHttpClient.Builder().cookieJar(JavaNetCookieJar(GLOBAL.COOKIE_MANAGER)).build()
    //叩きたいREST APIのURLを引数とします
    fun httpGET(url : String, noCache : Boolean = false): String? {
        val request : Request
        if( !noCache ){
            request= Request.Builder()
                .url(url)
                .build()
        }else {
            request= Request.Builder()
                .cacheControl(CacheControl.FORCE_NETWORK)
                .url(url)
                .build()
        }
        try {
            val response = client.newCall(request).execute()
            if(!response.isSuccessful){
                return null
            }
            return response.body?.string()
        }catch(e: Exception) {
            println(e)
            return null
        }
    }

    fun httpPOST(url : String, postString : String, noCache: Boolean = false) : String? {
        val MIMEType = "application/x-www-form-urlencoded; charset=utf-8".toMediaTypeOrNull()
        val requestBody = RequestBody.create(MIMEType,postString)

        // リクエストオブジェクトを作って
        val request : Request
        if( !noCache ){
            request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
        }else {
            request = Request.Builder()
                .cacheControl(CacheControl.FORCE_NETWORK)
                .url(url)
                .post(requestBody)
                .build()
        }
        try {
            val response = client.newCall(request).execute()
            if(!response.isSuccessful){
                println("response.message="+response.message)
                return null
            }
            val bod = response.body?.string()
            println("response.body="+bod)
            return bod//response.body?.string()
        }catch(e: Exception) {
            println("error->")
            println(e)
            return null
        }
    }
}