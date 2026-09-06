import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class OverviewType(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val backgroundColor: Color,
    val iconTint: Color,
    val titleColor: Color,
    val subtitleColor: Color
) {
    OVERDUE(
        title = "Overdue",
        subtitle = "Need attention",
        icon = Icons.Outlined.Warning,
        backgroundColor = Color(0xFFFCF3F2), // Light red wash
        iconTint = Color(0xFFEF4444),        // Semantic red
        titleColor = Color(0xFFB91C1C),      // Deep contrast red
        subtitleColor = Color(0xFFDC2626)    // Medium red
    ),

    TODAY(
        title = "Today",
        subtitle = "Due today",
        icon = Icons.Outlined.Today,
        backgroundColor = Color(0xFFFCF6E7), // Light amber wash
        iconTint = Color(0xFFF59E0B),        // Semantic amber/orange
        titleColor = Color(0xFFB45309),      // Deep contrast amber
        subtitleColor = Color(0xFFD97706)    // Medium amber
    ),

    TOMORROW(
        title = "Tomorrow",
        subtitle = "Due tomorrow",
        icon = Icons.Outlined.Event,
        backgroundColor = Color(0xFFF3E8FF), // Light purple wash
        iconTint = Color(0xFF9333EA),        // Accent purple
        titleColor = Color(0xFF6B21A8),      // Deep contrast purple
        subtitleColor = Color(0xFF7E22CE)    // Medium purple
    ),

    THIS_WEEK(
        title = "This Week",
        subtitle = "Due this week",
        icon = Icons.Outlined.DateRange,
        backgroundColor = Color(0xFFE8F3FC), // Light blue wash
        iconTint = Color(0xFF4F6EF7),        // Primary brand blue
        titleColor = Color(0xFF1E3A8A),      // Deep contrast blue
        subtitleColor = Color(0xFF3B82F6)    // Medium blue
    );
}