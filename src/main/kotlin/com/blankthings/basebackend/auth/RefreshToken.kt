package com.blankthings.basebackend.auth

import com.blankthings.basebackend.user.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
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
    var tokenHash: String,
    val createdAt: Instant = Instant.now(),
    var expiresAt: Instant,
) {
    fun isExpired(): Boolean = Instant.now().isAfter(expiresAt)
}
