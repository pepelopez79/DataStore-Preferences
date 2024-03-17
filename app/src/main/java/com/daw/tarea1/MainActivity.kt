package com.daw.tarea1

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.daw.tarea1.ui.theme.Tarea1Theme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("preferencias")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Tarea1Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Formulario(dataStore = dataStore)
                }
            }
        }
    }
}

@Composable
fun Formulario(dataStore: DataStore<Preferences>) {
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var mensajeDialogo by remember { mutableStateOf("") }

    var id by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var nacimiento by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // Campos de entrada
        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            OutlinedTextField(
                value = id,
                onValueChange = { id = it },
                label = { Text("ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = apellidos,
                onValueChange = { apellidos = it },
                label = { Text("Apellidos") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = nacimiento,
                onValueChange = { nacimiento = it },
                label = { Text("Año de Nacimiento") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        // Botones
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    scope.launch {
                        // Guardar los datos en el DataStore
                        guardarDatos(dataStore, id, nombre, apellidos, nacimiento.toIntOrNull())
                        // Vaciar los campos después de guardar los datos
                        id = ""
                        nombre = ""
                        apellidos = ""
                        nacimiento = ""
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Guardar")
            }
            Button(
                onClick = {
                    scope.launch {
                        // Leer y mostrar los datos almacenados
                        val datos = dataStore.data.first()
                        val mensaje = mostrarDatosAlmacenados(datos)
                        mensajeDialogo = mensaje
                        showDialog = true
                    }
                },
                modifier = Modifier.weight(1.5f)
            ) {
                Text("Leer/Visualizar")
            }
            Button(
                onClick = {
                    scope.launch {
                        // Borrar los datos almacenados
                        borrarDatos(dataStore)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Borrar")
            }
        }
    }

    // Mostrar el cuadro de diálogo
    if (showDialog) {
        if (mensajeDialogo.isNotBlank()) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                },
                title = { Text("Datos Almacenados") },
                text = { Text(mensajeDialogo) },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialog = false
                        }
                    ) {
                        Text("Cerrar")
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                },
                text = { Text("No hay datos almacenados") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDialog = false
                        }
                    ) {
                        Text("Cerrar")
                    }
                }
            )
        }
    }
}

private suspend fun guardarDatos(dataStore: DataStore<Preferences>, id: String, nombre: String, apellidos: String, nacimiento: Int?) {
    dataStore.edit { preferences ->
        preferences[stringPreferencesKey("id")] = id
        preferences[stringPreferencesKey("nombre")] = nombre
        preferences[stringPreferencesKey("apellidos")] = apellidos
        preferences[stringPreferencesKey("nacimiento")] = nacimiento?.toString() ?: ""
    }
}

private fun mostrarDatosAlmacenados(datos: Preferences): String {
    val id = datos[stringPreferencesKey("id")] ?: ""
    val nombre = datos[stringPreferencesKey("nombre")] ?: ""
    val apellidos = datos[stringPreferencesKey("apellidos")] ?: ""
    val nacimiento = datos[stringPreferencesKey("nacimiento")] ?: ""

    // Verificar si todos los campos están vacíos
    if (id.isEmpty() && nombre.isEmpty() && apellidos.isEmpty() && nacimiento.isEmpty()) {
        return ""
    }

    // Solo si hay datos disponibles
    return "ID: $id\nNombre: $nombre\nApellidos: $apellidos\nAño de Nacimiento: $nacimiento"
}

private suspend fun borrarDatos(dataStore: DataStore<Preferences>) {
    dataStore.edit { preferences ->
        preferences.remove(stringPreferencesKey("id"))
        preferences.remove(stringPreferencesKey("nombre"))
        preferences.remove(stringPreferencesKey("apellidos"))
        preferences.remove(stringPreferencesKey("nacimiento"))
    }
}