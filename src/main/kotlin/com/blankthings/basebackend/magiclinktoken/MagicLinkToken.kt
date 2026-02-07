package com.blankthings.basebackend.magiclinktoken

import com.blankthings.basebackend.user.User
import jakarta.persistence.*
import org.jetbrains.annotations.NotNull
import java.time.LocalDateTime

@Entity
@Table(name = "magic_link_tokens")
data class MagicLinkToken(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mlt_seq")
    @SequenceGenerator(name = "mlt_seq", sequenceName = "mlt_id_seq", allocationSize = 1)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: User,

    @Column(nullable = false, unique = true)
    val tokenHash: String,

    @NotNull
    val expiresAt: LocalDateTime,

    @NotNull
    val createdAt: LocalDateTime,

    var used: Boolean = false
) {
    fun markAsUsed() {
        this.used = true
    }

    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(expiresAt)
    }

    fun isValid(): Boolean {
        return !used && !isExpired()
    }
}