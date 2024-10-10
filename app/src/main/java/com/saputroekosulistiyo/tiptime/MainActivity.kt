/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.saputroekosulistiyo.tiptime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.saputroekosulistiyo.tiptime.ui.theme.TipTimeTheme
import java.text.NumberFormat

// Kelas utama untuk aktivitas aplikasi
class MainActivity : ComponentActivity() {
    // Metode ini dipanggil saat aktivitas dibuat
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge() // Mengaktifkan tampilan edge-to-edge
        super.onCreate(savedInstanceState)
        // Mengatur konten UI dengan tema TipTime dan menampilkan layout TipTimeLayout
        setContent {
            TipTimeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), // Mengisi seluruh ukuran layar
                ) {
                    TipTimeLayout() // Memanggil fungsi untuk menampilkan layout
                }
            }
        }
    }
}

@Composable
fun TipTimeLayout() {
    // State untuk menyimpan input jumlah tagihan dan persentase tip
    var amountInput by remember { mutableStateOf("") }
    var tipInput by remember { mutableStateOf("") }
    var roundUp by remember { mutableStateOf(false) }

    // Mengonversi input menjadi angka
    val amount = amountInput.toDoubleOrNull() ?: 0.0
    val tipPercent = tipInput.toDoubleOrNull() ?: 0.0
    // Menghitung jumlah tip berdasarkan input
    val tip = calculateTip(amount, tipPercent, roundUp)

    Column(
        modifier = Modifier
            .statusBarsPadding() // Padding untuk status bar
            .padding(horizontal = 40.dp) // Padding horizontal
            .verticalScroll(rememberScrollState()) // Mengizinkan scroll vertikal
            .safeDrawingPadding(), // Padding aman untuk menggambar
        horizontalAlignment = Alignment.CenterHorizontally, // Penyejajaran horizontal
        verticalArrangement = Arrangement.Center // Penyejajaran vertikal
    ) {
        // Teks judul untuk kalkulasi tip
        Text(
            text = stringResource(R.string.calculate_tip),
            modifier = Modifier
                .padding(bottom = 16.dp, top = 40.dp)
                .align(alignment = Alignment.Start) // Mengatur penyejajaran teks ke kiri
        )
        // Field untuk memasukkan jumlah tagihan
        EditNumberField(
            label = R.string.bill_amount, // Label untuk input
            leadingIcon = R.drawable.money, // Ikon yang ditampilkan di depan input
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number, // Jenis keyboard untuk angka
                imeAction = ImeAction.Next // Aksi IME berikutnya
            ),
            value = amountInput, // Nilai input saat ini
            onValueChanged = { amountInput = it }, // Mengubah state saat input berubah
            modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth(), // Padding dan mengisi lebar
        )
        // Field untuk memasukkan persentase tip
        EditNumberField(
            label = R.string.how_was_the_service,
            leadingIcon = R.drawable.percent,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done // Aksi IME selesai
            ),
            value = tipInput,
            onValueChanged = { tipInput = it },
            modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth(),
        )
        // Row untuk memilih apakah tip dibulatkan atau tidak
        RoundTheTipRow(
            roundUp = roundUp,
            onRoundUpChanged = { roundUp = it }, // Mengubah state saat switch berubah
            modifier = Modifier.padding(bottom = 32.dp)
        )
        // Menampilkan jumlah tip yang dihitung
        Text(
            text = stringResource(R.string.tip_amount, tip), // Teks dengan jumlah tip
            style = MaterialTheme.typography.displaySmall // Menggunakan gaya tipografi kecil
        )
        Spacer(modifier = Modifier.height(150.dp)) // Spacer untuk memberi jarak
    }
}

// Fungsi komposabel untuk field input angka
@Composable
fun EditNumberField(
    @StringRes label: Int, // Resource ID untuk label
    @DrawableRes leadingIcon: Int, // Resource ID untuk ikon
    keyboardOptions: KeyboardOptions, // Opsi keyboard
    value: String, // Nilai input
    onValueChanged: (String) -> Unit, // Lambda untuk mengubah nilai
    modifier: Modifier = Modifier // Modifier untuk pengaturan tambahan
) {
    // TextField untuk input angka
    TextField(
        value = value, // Nilai saat ini
        singleLine = true, // Hanya satu baris
        leadingIcon = { Icon(painter = painterResource(id = leadingIcon), null) }, // Ikon di depan
        modifier = modifier, // Modifier
        onValueChange = onValueChanged, // Mengubah nilai saat input berubah
        label = { Text(stringResource(label)) }, // Menampilkan label
        keyboardOptions = keyboardOptions // Opsi keyboard
    )
}

// Fungsi komposabel untuk memilih pembulatan tip
@Composable
fun RoundTheTipRow(
    roundUp: Boolean, // Status apakah tip dibulatkan
    onRoundUpChanged: (Boolean) -> Unit, // Lambda untuk mengubah status
    modifier: Modifier = Modifier // Modifier untuk pengaturan tambahan
) {
    Row(
        modifier = modifier.fillMaxWidth(), // Mengisi lebar
        verticalAlignment = Alignment.CenterVertically // Penyejajaran vertikal
    ) {
        Text(text = stringResource(R.string.round_up_tip)) // Teks untuk pilihan pembulatan
        Switch(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End), // Menempatkan switch di sebelah kanan
            checked = roundUp, // Status switch
            onCheckedChange = onRoundUpChanged // Mengubah status saat switch berubah
        )
    }
}

// Fungsi untuk menghitung jumlah tip
private fun calculateTip(amount: Double, tipPercent: Double = 15.0, roundUp: Boolean): String {
    var tip = tipPercent / 100 * amount // Menghitung tip
    if (roundUp) { // Jika dibulatkan
        tip = kotlin.math.ceil(tip) // Bulatkan ke atas
    }
    return NumberFormat.getCurrencyInstance().format(tip) // Format sebagai mata uang
}

// Fungsi untuk pratinjau layout TipTime
@Preview(showBackground = true)
@Composable
fun TipTimeLayoutPreview() {
    TipTimeTheme { // Menggunakan tema TipTime
        TipTimeLayout() // Menampilkan layout
    }
}
