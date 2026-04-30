package com.blankthings.basebackend.magiclinktoken

import com.blankthings.basebackend.user.User
import jakarta.persistence.*
import org.jetbrains.annotations.NotNull
import java.time.LocalDateTime

@Entity
@Table(name = "magic_link_tokens")
class MagicLinkToken(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mlt_seq")
    @SequenceGenerator(name = "mlt_seq", sequenceName = "mlt_id_seq", allocationSize = 1)
    val id: Long = 0,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: User,

    @Column(nullable = false, unique = true)
    val tokenHash: String,

    @NotNull
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @NotNull
    val expiresAt: LocalDateTime = LocalDateTime.now().plusMinutes(15),


    private var used: Boolean = false
) {
    fun copy(
        id: Long = this.id,
        user: User = this.user,
        tokenHash: String = this.tokenHash,
        expiresAt: LocalDateTime = this.expiresAt,
        createdAt: LocalDateTime = this.createdAt,
        used: Boolean = this.used
    ): MagicLinkToken {
        return MagicLinkToken(id, user, tokenHash, expiresAt, createdAt, used)
    }

    fun markAsUsed() {
        this.used = true
    }

    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(expiresAt)
    }

    fun isValid(): Boolean {
        return !used && !isExpired()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MagicLinkToken
        if (id != other.id) return false
        if (user.id != other.user.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + user.id.hashCode()
        return result
    }
}