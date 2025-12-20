package com.nesrine.smartshop.ui.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nesrine.smartshop.data.local.Product
import kotlin.math.roundToInt

private enum class ChartMetric { VALUE, QUANTITY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockChartCard(
    products: List<Product>,
    modifier: Modifier = Modifier
) {
    var metric by rememberSaveable { mutableStateOf(ChartMetric.VALUE) }

    val data = remember(products, metric) {
        val pairs: List<Pair<String, Float>> = when (metric) {
            ChartMetric.VALUE -> products.map { it.name to (it.quantity * it.price).toFloat() }
            ChartMetric.QUANTITY -> products.map { it.name to it.quantity.toFloat() }
            // safety (should never happen, but avoids "when not exhaustive" in some setups)
            else -> emptyList()
        }

        pairs
            .filter { it.second >= 0f }
            .sortedByDescending { it.second }
            .take(6)
    }

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Graphique", style = MaterialTheme.typography.titleMedium)
                    Text(
                        when (metric) {
                            ChartMetric.VALUE -> "Top produits par valeur"
                            ChartMetric.QUANTITY -> "Top produits par quantité"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = metric == ChartMetric.VALUE,
                        onClick = { metric = ChartMetric.VALUE },
                        label = { Text("Valeur") }
                    )
                    FilterChip(
                        selected = metric == ChartMetric.QUANTITY,
                        onClick = { metric = ChartMetric.QUANTITY },
                        label = { Text("Quantité") }
                    )
                }
            }

            if (data.isEmpty()) {
                Text(
                    "Ajoute des produits pour voir le graphique.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                SimpleBarChart(
                    data = data,
                    maxBarHeight = 140.dp,
                    valueFormatter = { v ->
                        when (metric) {
                            ChartMetric.VALUE -> "${"%.0f".format(v)} DT"
                            ChartMetric.QUANTITY -> v.roundToInt().toString()
                            else -> ""
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SimpleBarChart(
    data: List<Pair<String, Float>>,
    maxBarHeight: Dp,
    valueFormatter: (Float) -> String
) {
    val maxValue = (data.maxOfOrNull { it.second } ?: 0f).coerceAtLeast(1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(maxBarHeight + 48.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (label, value) ->
            val fraction = (value / maxValue).coerceIn(0f, 1f)

            val animatedFraction by animateFloatAsState(
                targetValue = fraction,
                animationSpec = tween(600),
                label = "barAnim"
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    valueFormatter(value),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(6.dp))

                // Height = maxBarHeight * animatedFraction (done via dp conversion)
                val barHeight = (maxBarHeight.value * animatedFraction).dp

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(barHeight)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )

                Spacer(Modifier.height(8.dp))

                AssistChip(
                    onClick = { },
                    label = { Text(label.take(10)) } // keep it short & clean
                )
            }
        }
    }
}
