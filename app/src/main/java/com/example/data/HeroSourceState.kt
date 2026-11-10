package com.example.data

import com.example.model.HeroSource

internal fun resolveHeroSource(
    selected: HeroSource?,
    isDraft: Boolean,
    lastOpened: HeroSource?,
    persisted: HeroSource?,
    fallback: HeroSource?
): HeroSource? = if (isDraft) selected else lastOpened ?: persisted ?: fallback
