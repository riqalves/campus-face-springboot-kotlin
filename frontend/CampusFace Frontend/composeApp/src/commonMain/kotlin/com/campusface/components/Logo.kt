package com.campusface.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import campusface.composeapp.generated.resources.Res
import campusface.composeapp.generated.resources.logo
import org.jetbrains.compose.resources.painterResource


@Composable
fun Logo(
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(Res.drawable.logo),
        contentDescription = "Logo",
        modifier = modifier
    )
}
