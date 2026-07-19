package com.financeapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.financeapp.ui.utils.getTierAssetPath

/**
 * Displays tier badge SVG from assets.
 * Clickable — caller handles click (e.g. show XP summary dialog).
 */
@Composable
fun TierBadge(
    tier: Int,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(getTierAssetPath(tier))
            .decoderFactory(SvgDecoder.Factory())
            .size(128)  // Downscale for performance
            .build()
    )

    Image(
        painter = painter,
        contentDescription = "Tier $tier",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    )
}
