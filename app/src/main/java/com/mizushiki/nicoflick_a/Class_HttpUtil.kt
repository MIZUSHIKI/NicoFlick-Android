package com.mizushiki.nicoflick_a

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.HttpCookie
import java.net.URI


object HttpUtil {
    val client = OkHttpClient.Builder().cookieJar(JavaNetCookieJar(GLOBAL.COOKIE_MANAGER)).build()
    //叩きたいREST APIのURLを引数とします
    fun httpGET(url : String, noCache : Boolean = false, headers : Map<String,String> = mapOf()): String? {
        val request : Request
        if( !noCache ){
            request= Request.Builder().apply {
                url(url)
                for (h in headers) {
                    header(h.key, h.value)
                }
            }.build()
        }else {
            request= Request.Builder().apply {
                cacheControl(CacheControl.FORCE_NETWORK)
                url(url)
                for (h in headers) {
                    header(h.key, h.value)
                }
            }.build()
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

    fun httpPOST(url : String, postString : String, noCache: Boolean = false, headers : Map<String,String> = mapOf(), eatCookie : Boolean = false) : String? {
        val MIMEType = "application/x-www-form-urlencoded; charset=utf-8".toMediaTypeOrNull()
        val requestBody = RequestBody.create(MIMEType,postString)

        // リクエストオブジェクトを作って
        val request : Request
        if( !noCache ){
            request = Request.Builder().apply {
                url(url)
                for (h in headers) {
                    header(h.key, h.value)
                }
                post(requestBody)
            }.build()

        }else {
            request = Request.Builder().apply {
                cacheControl(CacheControl.FORCE_NETWORK)
                url(url)
                for (h in headers) {
                    header(h.key, h.value)
                }
                post(requestBody)
            }.build()
        }
        try {
            val response = client.newCall(request).execute()
            if(!response.isSuccessful){
                println("response.message="+response.message)
                return null
            }
            val bod = response.body?.string()
            if (eatCookie) {
                for( (key, value) in response.headers ){
                    println("response.headers - key,value = $key, $value")
                    if (key != "set-cookie"){ continue }
                    val domand_bid = value.pregMatche_firstString("domand_bid=([0-9a-f]+?);")
                    //GLOBAL.COOKIE_MANAGER.cookieStore.add( URI(url), HttpCookie("domand_bid", domand_bid))
                    //println("set_cookie - key,value = \"domand_bid\", $domand_bid")
                    break
                }
                // 確認
                for( a in GLOBAL.COOKIE_MANAGER.cookieStore.cookies){
                    println("name=${a.name}, value= ${a.value}")
                }
            }
            println("response.body="+bod)
            return bod//response.body?.string()
        }catch(e: Exception) {
            println("error->")
            println(e)
            return null
        }
    }
}