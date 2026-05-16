package com.example.mmapp.remote

class RemoteSyncErrorFormatter {
    fun format(throwable: Throwable): String {
        val error = throwable as? RemoteSyncException
        return when (error) {
            is RemoteSyncException.NetworkUnavailable ->
                "No se ha podido actualizar. No hay conexión con GitHub o con Internet."

            is RemoteSyncException.Timeout ->
                "No se ha podido actualizar. GitHub ha tardado demasiado en responder."

            is RemoteSyncException.HttpError ->
                "No se ha podido actualizar. GitHub devolvió un error (${error.statusCode})."

            is RemoteSyncException.InvalidRemoteContent ->
                "No se ha podido actualizar. Los JSON remotos no tienen el formato esperado."

            is RemoteSyncException.EmptyRemoteIndex ->
                "No se ha podido actualizar. No se han encontrado los JSON esperados en el repositorio remoto."

            is RemoteSyncException.InvalidRepositoryConfig ->
                "No se ha podido actualizar. La configuración del repositorio remoto no es válida."

            else ->
                "No se ha podido actualizar por un error inesperado."
        }
    }
}
