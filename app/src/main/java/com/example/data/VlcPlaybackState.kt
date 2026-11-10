package com.example.data

import com.example.model.VlcPlayback

internal fun mergeVlcPlayback(previous: VlcPlayback?, observed: VlcPlayback): VlcPlayback =
    if (observed.mediaId == previous?.mediaId)
        observed.copy(positionMs = observed.positionMs ?: previous.positionMs,
            mediaUri = observed.mediaUri ?: previous.mediaUri)
    else observed
