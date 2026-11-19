
package com.example.gastikmp

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * Selector de fechas compartido de Gasti KMP.
 *
 * Funciona con Compose Multiplatform.
 * Guarda las fechas en formato AAAA-MM-DD.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorFechaGasti(
    fecha: String,
    onFechaSeleccionada: (String) -> Unit
) {
    var mostrarCalendario by remember {
        mutableStateOf(false)
    }

    OutlinedButton(
        onClick = {
            mostrarCalendario = true
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            if (fecha.isBlank()) {
                "📅 Seleccionar fecha"
            } else {
                "📅 $fecha"
            }
        )
    }

    if (mostrarCalendario) {

        val fechaInicial = remember(fecha) {
            fechaAMilisegundos(fecha)
        }

        val estado = rememberDatePickerState(
            initialSelectedDateMillis = fechaInicial
        )

        DatePickerDialog(
            onDismissRequest = {
                mostrarCalendario = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        estado.selectedDateMillis?.let {
                            onFechaSeleccionada(
                                milisegundosAFecha(it)
                            )
                        }
                        mostrarCalendario = false
                    },
                    enabled = estado.selectedDateMillis != null
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarCalendario = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = estado,
                title = {
                    Text("Selecciona la fecha")
                }
            )
        }
    }
}

private fun esBisiestoGasti(anio: Int): Boolean {
    return anio % 400 == 0 ||
            (anio % 4 == 0 && anio % 100 != 0)
}

private fun diasDelMesGasti(
    anio: Int,
    mes: Int
): Int {
    return when (mes) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (esBisiestoGasti(anio)) 29 else 28
        else -> 0
    }
}

/**
 * Convierte AAAA-MM-DD a milisegundos UTC.
 */
private fun fechaAMilisegundos(fecha: String): Long? {

    val partes = fecha.split("-")
    if (partes.size != 3) return null

    val anio = partes[0].toIntOrNull() ?: return null
    val mes = partes[1].toIntOrNull() ?: return null
    val dia = partes[2].toIntOrNull() ?: return null

    if (anio !in 1970..2100) return null
    if (mes !in 1..12) return null

    if (dia !in 1..diasDelMesGasti(anio, mes)) {
        return null
    }

    var dias = 0L

    for (a in 1970 until anio) {
        dias += if (esBisiestoGasti(a)) 366 else 365
    }

    for (m in 1 until mes) {
        dias += diasDelMesGasti(anio, m)
    }

    dias += dia - 1

    return dias * 86_400_000L
}

/**
 * Convierte milisegundos UTC a AAAA-MM-DD.
 */
private fun milisegundosAFecha(
    milisegundos: Long
): String {

    var dias = milisegundos / 86_400_000L

    var anio = 1970

    while (true) {
        val cantidad = if (esBisiestoGasti(anio)) {
            366
        } else {
            365
        }

        if (dias < cantidad) break

        dias -= cantidad
        anio++
    }

    var mes = 1

    while (mes <= 12) {
        val cantidad = diasDelMesGasti(anio, mes)

        if (dias < cantidad) break

        dias -= cantidad
        mes++
    }

    val dia = dias.toInt() + 1

    return "$anio-" +
            mes.toString().padStart(2, '0') +
            "-" +
            dia.toString().padStart(2, '0')
}
