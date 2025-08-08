package ecommerce.dto

import jakarta.validation.constraints.NotBlank

data class LoginForm(
    @field:NotBlank(message = "Please enter your email address")
    val email: String,
    @field:NotBlank(message = "Please enter your password")
    val password: String,
) {
    companion object {
        fun fromRegisterForm(registerForm: RegisterForm): LoginForm {
            return LoginForm(registerForm.email, registerForm.password)
        }
    }
}
