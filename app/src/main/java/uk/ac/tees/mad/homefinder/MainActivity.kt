package uk.ac.tees.mad.homefinder

import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.firestore.FirebaseFirestore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import uk.ac.tees.mad.homefinder.ui.theme.HOMEFINDERTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val auth = FirebaseAuth.getInstance()
        setContent {
            HOMEFINDERTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Dynamically display the appropriate screen based on user authentication status
                    if (auth.currentUser != null) {
                        UserInfoForm()
                    } else {
                        LoginPage(
                            onLoginSuccess = { setContent { UserInfoForm() } }, // Reset the UI to display UserInfoForm after successful login
                            onNavigateToSignUp = { /* Handle navigation to the sign-up screen */ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoginPage(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }
    val firebaseAuth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                loginError = null
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onLoginSuccess()
                        } else {
                            loginError = task.exception?.localizedMessage ?: "Login failed"
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Login")
        }

        loginError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onNavigateToSignUp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Don't have an account? Sign Up")
        }
    }
}

// Implement UserInfoForm as previously provided

@Composable
fun UserInfoForm() {
    val firebaseFirestore = FirebaseFirestore.getInstance()
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("buyer") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextFieldComponent(label = "Full Name", value = fullName) { fullName = it }
        Spacer(modifier = Modifier.height(16.dp))
        TextFieldComponent(label = "Phone Number", value = phoneNumber, keyboardType = KeyboardType.Phone) { phoneNumber = it }
        Spacer(modifier = Modifier.height(16.dp))
        RoleSelector(role) { role = it }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val userMap = hashMapOf(
                    "fullName" to fullName,
                    "phoneNumber" to phoneNumber,
                    "role" to role
                )
                firebaseFirestore.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .set(userMap)
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener { e ->
                        // Handle failure, show an error message
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Submit")
        }
    }
}

@Composable
fun TextFieldComponent(label: String, value: String, keyboardType: KeyboardType = KeyboardType.Text, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next, keyboardType = keyboardType)
    )
}

@Composable
fun RoleSelector(role: String, onRoleSelected: (String) -> Unit) {
    Column {
        Text("I am a: ", style = MaterialTheme.typography.bodyLarge)
        Row {
            RadioButton(selected = role == "buyer", onClick = { onRoleSelected("buyer") })
            Text("Buyer", modifier = Modifier.padding(start = 4.dp, end = 16.dp))
            RadioButton(selected = role == "seller", onClick = { onRoleSelected("seller") })
            Text("Seller", modifier = Modifier.padding(start = 4.dp))
        }
    }
}




@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HOMEFINDERTheme {
        LoginPage(
            onLoginSuccess = {  },
            onNavigateToSignUp = { /* Preview doesn't handle navigation to sign up */ }
        )
    }
}