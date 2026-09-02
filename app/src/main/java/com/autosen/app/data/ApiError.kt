package com.autosen.app.data

import org.json.JSONObject
import retrofit2.Response

/**
 * Extrae un mensaje legible del cuerpo de error de Laravel.
 * Ejemplos de formato:
 *   {"message":"...", "errors":{"correo":["..."]}}
 *   {"message":"..."}
 */
fun mensajeDeErrorApi(response: Response<*>?): String {
    val cuerpo = response?.errorBody()?.string().orEmpty()
    if (cuerpo.isBlank()) return ""

    return try {
        val json = JSONObject(cuerpo)
        val message = json.optString("message")
        if (message.isNotBlank()) {
            message
        } else {
            val errors = json.optJSONObject("errors") ?: return message
            val primeraClave = errors.keys().asSequence().firstOrNull() ?: return message
            errors.optJSONArray(primeraClave)?.optString(0) ?: message
        }
    } catch (e: Exception) {
        ""
    }
}