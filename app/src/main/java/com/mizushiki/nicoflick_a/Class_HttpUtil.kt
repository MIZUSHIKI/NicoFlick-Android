package com.mizushiki.nicoflick_a

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.HttpCookie
import java.net.URI


object HttpUtil {
    val client = OkHttpClient.Builder().cookieJar(JavaNetCookieJar(GLOBAL.COOKIE_MANAGER)).build()
    // Preserve the movie request timeout even when diagnostic listeners are disabled.
    private val movieClient by lazy {
        client.newBuilder().callTimeout(30, java.util.concurrent.TimeUnit.SECONDS).build()
    }
    private fun requestClient(diagnosticId: String?): OkHttpClient {
        if (diagnosticId == null) return client
        return if (DiagnosticSettings.enabled)
            movieClient.newBuilder().eventListener(MovieLoadLog.httpEvents(diagnosticId)).build()
        else movieClient
    }
    //叩きたいREST APIのURLを引数とします
    fun httpGET(url : String, noCache : Boolean = false, headers : Map<String,String> = mapOf(), diagnosticId: String? = null): String? {
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
        val started = if (DiagnosticSettings.enabled) android.os.SystemClock.elapsedRealtime() else 0L
        diagnosticId?.let { MovieLoadLog.log(it) { "HTTP_START method=${request.method} host=${request.url.host}" } }
        try {
            val requestClient = requestClient(diagnosticId)
            val response = requestClient.newCall(request).execute()
            if(!response.isSuccessful){
                diagnosticId?.let { MovieLoadLog.log(it) { "HTTP_FAILED status=${response.code} elapsed=${android.os.SystemClock.elapsedRealtime() - started}ms" } }
                response.close()
                return null
            }
            val body = response.use { it.body?.string() }
            diagnosticId?.let { MovieLoadLog.log(it) { "HTTP_BODY_END bytes=${body?.length} elapsed=${android.os.SystemClock.elapsedRealtime() - started}ms" } }
            return body
        }catch(e: Exception) {
            diagnosticId?.let { MovieLoadLog.failure(it, e) }
            return null
        }
    }

    fun httpPOST(url : String, postString : String, noCache: Boolean = false, headers : Map<String,String> = mapOf(), eatCookie : Boolean = false, diagnosticId: String? = null) : String? {
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
        val started = if (DiagnosticSettings.enabled) android.os.SystemClock.elapsedRealtime() else 0L
        diagnosticId?.let { MovieLoadLog.log(it) { "HTTP_START method=${request.method} host=${request.url.host}" } }
        try {
            val requestClient = requestClient(diagnosticId)
            val response = requestClient.newCall(request).execute()
            if(!response.isSuccessful){
                diagnosticId?.let { MovieLoadLog.log(it) { "HTTP_FAILED status=${response.code} elapsed=${android.os.SystemClock.elapsedRealtime() - started}ms" } }
                response.close()
                return null
            }
            val bod = response.use { it.body?.string() }
            diagnosticId?.let { MovieLoadLog.log(it) { "HTTP_BODY_END bytes=${bod?.length} elapsed=${android.os.SystemClock.elapsedRealtime() - started}ms" } }
            if (eatCookie) {
                for( (key, value) in response.headers ){
                    // Cookie values are not logged.
                    if (key != "set-cookie"){ continue }
                    val domand_bid = value.pregMatche_firstString("domand_bid=([0-9a-f]+?);")
                    //GLOBAL.COOKIE_MANAGER.cookieStore.add( URI(url), HttpCookie("domand_bid", domand_bid))
                    //println("set_cookie - key,value = \"domand_bid\", $domand_bid")
                    break
                }
                // Cookie values are not logged.
            }
            // Response bodies may contain signed media URLs.
            return bod//response.body?.string()
        }catch(e: Exception) {
            // println("error->")
            diagnosticId?.let { MovieLoadLog.failure(it, e) }
            return null
        }
    }
}