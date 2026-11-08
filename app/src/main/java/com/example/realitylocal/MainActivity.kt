package com.example.realitylocal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.realitylocal.data.LocalDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = LocalDatabase(this)
        setContent { MaterialTheme { LocalApp(db) } }
    }
}

@Composable
private fun LocalApp(db: LocalDatabase) {
    var page by remember { mutableStateOf("Home") }
    var refresh by remember { mutableIntStateOf(0) }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("REALITY Local Edition", style = MaterialTheme.typography.headlineSmall)
            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(12.dp)) {
                Text("OFFLINE MODE · ข้อมูลเก็บไว้ในเครื่องนี้เท่านั้น", Modifier.fillMaxWidth().padding(12.dp), style = MaterialTheme.typography.labelMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Home", "Chat", "Profile", "Admin").forEach { label ->
                    FilterChip(selected = page == label, onClick = { page = label }, label = { Text(label) })
                }
            }
            when (page) {
                "Chat" -> ChatPage(db, refresh) { refresh++ }
                "Profile" -> ProfilePage(db)
                "Admin" -> AdminPage(db)
                else -> HomePage()
            }
        }
    }
}

@Composable private fun HomePage() {
    Text("Your private offline space", style = MaterialTheme.typography.titleLarge)
    Text("This starter edition stores a local profile and personal chat notes. It does not sign in to REALITY or send messages to other users.")
    Text("Available locally: profile, notes, local settings and owner-only admin PIN.")
}

@Composable private fun ChatPage(db: LocalDatabase, refresh: Int, onRefresh: () -> Unit) {
    var draft by remember { mutableStateOf("") }
    val messages = remember(refresh) { db.messages() }
    Text("Local Chat Journal", style = MaterialTheme.typography.titleLarge)
    OutlinedTextField(draft, { draft = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Write a private note") })
    Button(onClick = { if (draft.isNotBlank()) { db.addMessage(draft.trim()); draft = ""; onRefresh() } }) { Text("Save on this device") }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(messages) { msg -> Card { Text(msg, Modifier.fillMaxWidth().padding(12.dp)) } }
    }
}

@Composable private fun ProfilePage(db: LocalDatabase) {
    val initial = remember { db.profile() }
    var nickname by remember { mutableStateOf(initial.first) }
    var bio by remember { mutableStateOf(initial.second) }
    Text("Local Profile", style = MaterialTheme.typography.titleLarge)
    OutlinedTextField(nickname, { nickname = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nickname") })
    OutlinedTextField(bio, { bio = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Bio") })
    Button(onClick = { db.saveProfile(nickname, bio) }) { Text("Save profile locally") }
}

@Composable private fun AdminPage(db: LocalDatabase) {
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var unlocked by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    Text("Local Super Admin", style = MaterialTheme.typography.titleLarge)
    Text("Controls only this app's local data. It does not grant moderator or admin rights on REALITY.")
    if (!db.hasAdminPin()) {
        Text("Create your owner PIN (4–12 digits).")
        OutlinedTextField(pin, { pin = it.filter(Char::isDigit).take(12) }, label = { Text("New PIN") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), visualTransformation = PasswordVisualTransformation())
        OutlinedTextField(confirm, { confirm = it.filter(Char::isDigit).take(12) }, label = { Text("Confirm PIN") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), visualTransformation = PasswordVisualTransformation())
        Button(onClick = { if (pin.length in 4..12 && pin == confirm) { db.saveAdminPin(pin); message = "Owner PIN saved" } else message = "PIN must match and contain 4–12 digits" }) { Text("Set owner PIN") }
    } else if (!unlocked) {
        OutlinedTextField(pin, { pin = it.filter(Char::isDigit).take(12) }, label = { Text("Owner PIN") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), visualTransformation = PasswordVisualTransformation())
        Button(onClick = { unlocked = db.checkAdminPin(pin); message = if (unlocked) "Local admin unlocked" else "Incorrect PIN" }) { Text("Unlock admin") }
    } else {
        Text("Admin access: this installation only", color = MaterialTheme.colorScheme.primary)
        Text("Local database is active. Chat entries and profile are stored in app-private SQLite.")
        Button(onClick = { unlocked = false; pin = "" }) { Text("Lock admin") }
    }
    if (message.isNotBlank()) Text(message)
}
