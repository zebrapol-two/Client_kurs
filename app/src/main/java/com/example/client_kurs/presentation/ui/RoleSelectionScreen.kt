package com.example.client_kurs.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.client_kurs.domain.model.UserRole
import com.example.client_kurs.R
import com.example.client_kurs.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun RoleSelectionScreen(
    viewModel: AuthViewModel,
    onBackToAuth: () -> Unit
) {
    val state by viewModel.authState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val backToAuthContentDescription = stringResource(R.string.back_to_auth_content_description)
    val backArrowLabel = stringResource(R.string.back_arrow_label)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(
            onClick = onBackToAuth,
            modifier = Modifier
                .align(Alignment.Start)
                .semantics { contentDescription = backToAuthContentDescription }
        ) {
            Text(backArrowLabel)
        }

        Text("Выберите роль", style = MaterialTheme.typography.headlineMedium)

        RoleCard(
            title = "Покупатель",
            onClick = {
                coroutineScope.launch {
                    viewModel.submitRole(UserRole.CUSTOMER)
                }
            }
        )

        RoleCard(
            title = "Кладовщик",
            onClick = {
                coroutineScope.launch {
                    viewModel.submitRole(UserRole.STOREKEEPER)
                }
            }
        )

        if (state.error != null) {
            Text(
                text = state.error.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
        }
    }
}

@Composable
private fun RoleCard(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(20.dp)
        )
    }
}