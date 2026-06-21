@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.slimyan.ui.meal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.slimyan.data.TemplateSummary

@Composable
fun AuditSheet(
    onDismiss: () -> Unit,
    vm: MealAuditViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var refinement by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = { vm.dismiss(); onDismiss() }) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("AI食事プラン監査", style = MaterialTheme.typography.titleLarge)

            when (val s = state) {
                AuditState.Idle -> {
                    Text(
                        "今の週間テンプレを Claude に診断してもらう。赤字が安定して出てるか・タンパク質・塩分・繊維・コストをチェックして、修正案を出すよ。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    OutlinedTextField(
                        value = refinement,
                        onValueChange = { refinement = it },
                        label = { Text("要望（任意）例: 土曜の昼は自由に") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { vm.runAudit(refinement.ifBlank { null }) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("診断する") }
                }

                AuditState.Loading -> {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("Claude が診断中...")
                    }
                }

                is AuditState.Ready -> {
                    Text(s.audit.diagnosis, style = MaterialTheme.typography.bodyMedium)
                    if (s.audit.issues.isNotEmpty()) {
                        HorizontalDivider()
                        Text("指摘", style = MaterialTheme.typography.titleSmall)
                        s.audit.issues.forEach { Text("・$it", style = MaterialTheme.typography.bodySmall) }
                    }
                    if (s.after != null) {
                        HorizontalDivider()
                        BeforeAfter(s.before, s.after)
                        Button(onClick = { vm.applyRevision() }, modifier = Modifier.fillMaxWidth()) {
                            Text("修正案をテンプレに反映")
                        }
                    } else {
                        Text("修正提案なし（今のままで問題なしと判断）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline)
                    }
                    OutlinedTextField(
                        value = refinement,
                        onValueChange = { refinement = it },
                        label = { Text("微調整して再診断（任意）") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = { vm.runAudit(refinement.ifBlank { null }) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("再診断") }
                }

                AuditState.Applied -> {
                    Text("✅ 修正案をテンプレに反映した", color = MaterialTheme.colorScheme.primary)
                    Button(onClick = { vm.dismiss(); onDismiss() }, modifier = Modifier.fillMaxWidth()) {
                        Text("閉じる")
                    }
                }

                is AuditState.Error -> {
                    Text(s.msg, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = { vm.dismiss() }, modifier = Modifier.fillMaxWidth()) {
                        Text("戻る")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun BeforeAfter(before: TemplateSummary.Totals, after: TemplateSummary.Totals) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("週平均（1日あたり）の変化", style = MaterialTheme.typography.titleSmall)
        Text("修正前: ${(before.kcal / 7).toInt()} kcal ・ P${(before.protein / 7).toInt()}g",
            style = MaterialTheme.typography.bodySmall)
        Text("修正後: ${(after.kcal / 7).toInt()} kcal ・ P${(after.protein / 7).toInt()}g",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
    }
}
