package com.blankthings.basebackend.auth

import com.blankthings.basebackend.user.User
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "refresh_tokens")
class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rt_seq")
    @SequenceGenerator(name = "rt_seq", sequenceName = "rt_id_seq", allocationSize = 1)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false, unique = true)
    val tokenHash: String,

    val createdAt: Instant = Instant.now(),

    val expiresAt: Instant
) {
    fun copy(
        id: Long = this.id,
        user: User = this.user,
        tokenHash: String = this.tokenHash,
        createdAt: Instant = this.createdAt,
        expiresAt: Instant = this.expiresAt
    ): RefreshToken {
        return RefreshToken(
            id = id,
            user = user,
            tokenHash = tokenHash,
            createdAt = createdAt,
            expiresAt = expiresAt
        )
    }

    fun isExpired(): Boolean = Instant.now().isAfter(expiresAt)
}
