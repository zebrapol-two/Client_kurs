package com.example.client_kurs.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.client_kurs.domain.model.DailySales
import com.example.client_kurs.domain.model.TopSellingProduct
import com.example.client_kurs.domain.model.UserRole
import com.example.client_kurs.presentation.viewmodel.AnalyticsViewModel

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val overview by viewModel.overview.collectAsState()
    val topSelling by viewModel.topSelling.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedProductIds by viewModel.selectedProductIds.collectAsState()
    val productSalesSum by viewModel.productSalesSum.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error ?: "")
            viewModel.clearError()
        }
    }

    MainScaffold(
        role = UserRole.STOREKEEPER,
        title = "Аналитика",
        currentRoute = currentRoute,
        snackbarHostState = snackbarHostState,
        onNavigate = onNavigate,
        onLogout = onLogout
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isLoading && overview == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val currentOverview = overview
                if (currentOverview != null) {
                    OverviewCard(
                        totalStockValue = currentOverview.totalStockValue,
                        lowStockCount = currentOverview.lowStockCount
                    )

                    // Заголовок и кнопка сброса
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (selectedProductIds.isNotEmpty())
                                "Сравнение выбранных товаров"
                            else
                                "Продажи за 7 дней",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        if (selectedProductIds.isNotEmpty()) {
                            TextButton(onClick = { viewModel.clearSelection() }) {
                                Text("Сбросить все")
                            }
                        }
                    }

                    SectionCard(title = "") {
                        if (selectedProductIds.isEmpty()) {
                            Text(
                                "Нажмите на товар в списке ниже, чтобы увидеть его продажи",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            // Построим список (название, сумма) для графика
                            val topSellingList = topSelling   // уже есть в scope
                            val chartData = remember(selectedProductIds, productSalesSum, topSellingList) {
                                selectedProductIds.map { id ->
                                    val name = topSellingList.find { it.productId == id }?.productName ?: id
                                    val total = productSalesSum[id] ?: 0
                                    Pair(name, total)
                                }
                            }
                            SalesChart(data = chartData)
                        }
                    }
                }

                SectionCard(title = "Топ-5 товаров за неделю") {
                    TopSellingList(
                        items = topSelling,
                        selectedIds = selectedProductIds,
                        onProductClick = { productId -> viewModel.toggleProductSelection(productId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewCard(
    totalStockValue: Double,
    lowStockCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Общая стоимость склада: ${"%.2f".format(totalStockValue)} ₽",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Товаров с низким остатком: $lowStockCount",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (title.isNotBlank()) {
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            content()
        }
    }
}

/** График с осями Y и X, столбцами для списка пар (название, сумма продаж) */
@Composable
private fun SalesChart(data: List<Pair<String, Int>>) {
    if (data.isEmpty()) {
        Text("Нет данных")
        return
    }

    val maxVal = data.maxOf { it.second }.coerceAtLeast(1)
    val density = LocalDensity.current
    val barWidthPx = with(density) { 30.dp.toPx() }
    val axisLabelStyle = TextStyle(fontSize = 10.sp, color = Color.Gray)
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(start = 36.dp, end = 8.dp, top = 8.dp, bottom = 40.dp)
    ) {
        val chartWidth = size.width
        val chartHeight = size.height

        // ---------- Ось Y ----------
        val ySteps = 4
        val yStepValue = maxVal / ySteps
        val yStepPx = chartHeight / ySteps

        for (i in 0..ySteps) {
            val y = chartHeight - i * yStepPx
            val label = (i * yStepValue).toInt().toString()
            drawText(
                textLayoutResult = textMeasurer.measure(label, axisLabelStyle),
                topLeft = Offset(-32f, y - 6f)
            )
            if (i != 0) {
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                )
            }
        }

        // ---------- Столбцы и подписи X ----------
        val totalBarsWidth = barWidthPx * data.size
        val spacing = if (data.size > 1) (chartWidth - totalBarsWidth) / (data.size - 1) else 0f
        val startX = (chartWidth - (totalBarsWidth + spacing * (data.size - 1))) / 2f

        data.forEachIndexed { index, (name, total) ->
            val x = startX + index * (barWidthPx + spacing)
            val barHeight = (total.toFloat() / maxVal) * chartHeight
            drawRect(
                color = Color(0xFF6750A4),
                topLeft = Offset(x, chartHeight - barHeight),
                size = Size(barWidthPx, barHeight)
            )
            // Укороченное название товара для подписи
            val shortName = if (name.length > 10) name.take(9) + "…" else name
            val measured = textMeasurer.measure(shortName, axisLabelStyle)
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    x + barWidthPx / 2 - measured.size.width / 2,
                    chartHeight + 8f
                )
            )
        }

        // Оси
        drawLine(Color.DarkGray, Offset(0f, 0f), Offset(0f, chartHeight), 2f)
        drawLine(Color.DarkGray, Offset(0f, chartHeight), Offset(chartWidth, chartHeight), 2f)
    }
}

@Composable
private fun TopSellingList(
    items: List<TopSellingProduct>,
    selectedIds: Set<String>,
    onProductClick: (String) -> Unit
) {
    if (items.isEmpty()) {
        Text("Нет данных по топ продаж")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(items, key = { it.productId }) { item ->
            val isSelected = item.productId in selectedIds
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProductClick(item.productId) },
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.productName}: ${item.soldUnits} шт.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (isSelected) {
                        Text(
                            "✓",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}