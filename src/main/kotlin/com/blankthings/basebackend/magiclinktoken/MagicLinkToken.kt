package com.blankthings.basebackend.magiclinktoken

import com.blankthings.basebackend.user.User
import jakarta.persistence.*
import org.jetbrains.annotations.NotNull
import java.time.LocalDate

@Entity
@Table(name = "magic_link_tokens")
data class MagicLinkToken(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: User,

    @Column(nullable = false, unique = true)
    val tokenHash: String,

    @NotNull
    val expiresAt: LocalDate,

    @NotNull
    val createdAt: LocalDate,

    val used: Boolean = false
)