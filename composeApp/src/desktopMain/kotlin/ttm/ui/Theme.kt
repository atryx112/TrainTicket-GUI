package ttm.ui

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.material.Typography

private val Primary = Color(0xFF1E88E5)   // blue
private val PrimaryDark = Color(0xFF1565C0)
private val Accent = Color(0xFF43A047)    // green
private val ErrorRed = Color(0xFFD32F2F)
private val Bg = Color(0xFFF8FAFC)        // near-white
private val SurfaceColor = Color(0xFFFFFFFF)
private val OnPrimary = Color(0xFFFFFFFF)
private val OnBg = Color(0xFF0F172A)      // deep slate

private val AppColors = lightColors(
    primary = Primary,
    primaryVariant = PrimaryDark,
    secondary = Accent,
    background = Bg,
    surface = SurfaceColor,
    error = ErrorRed,
    onPrimary = OnPrimary,
    onBackground = OnBg,
    onSurface = OnBg,
)

private val AppTypography = Typography(
    h4 = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
    h5 = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    subtitle1 = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium),
    body1 = TextStyle(fontSize = 15.sp),
    button = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colors = AppColors, typography = AppTypography, content = content)
}
