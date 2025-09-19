package br.com.fatec.campusface.models

import com.google.firebase.database.Exclude
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails


data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val hashedPassword: String = "", // será criptografada
    val document: String = "",
    val faceImageId: String? = "",
    val role: Role = Role.MEMBER,
    val faceToken: String? = null
): UserDetails {
    @Exclude
    override fun getAuthorities(): Collection<GrantedAuthority> {
        val authorities = mutableListOf<GrantedAuthority>()

        // Admin tem acesso a tudo
        when (role) {
            Role.ADMIN -> {
                authorities.add(SimpleGrantedAuthority("ROLE_ADMIN"))
                authorities.add(SimpleGrantedAuthority("ROLE_VALIDATOR"))
                authorities.add(SimpleGrantedAuthority("ROLE_MEMBER"))
            }
            // Validator tem acesso a Validator e Member
            Role.VALIDATOR -> {
                authorities.add(SimpleGrantedAuthority("ROLE_VALIDATOR"))
                authorities.add(SimpleGrantedAuthority("ROLE_MEMBER"))
            }
            // Member tem acesso apenas a Member
            else -> {
                authorities.add(SimpleGrantedAuthority("ROLE_MEMBER"))
            }
        }

        return authorities
    }
    override fun getPassword(): String = hashedPassword
    override fun getUsername(): String = email
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true

}

enum class Role {
    MEMBER,
    VALIDATOR,
    ADMIN
}

