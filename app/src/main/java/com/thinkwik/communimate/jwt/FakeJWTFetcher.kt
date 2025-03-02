package com.thinkwik.communimate.jwt

import com.sinch.rtc.vvc.reference.app.utils.jwt.JWTFetcher

/**
 * DO NOT use this fetcher in your production application, instead implement here an async callback to your backend.
 * It might be tempting to re-use this class and store the APPLICATION_SECRET in your app, but that would
 * greatly compromise security.
 */
class FakeJWTFetcher() : JWTFetcher {

    override fun acquireJWT(applicationKey: String, userId: String, callback: (String) -> Unit) {
        callback(JWT.create(applicationKey, "2UJXBlBy30SNqrk/omlYLw==", userId))
    }

}