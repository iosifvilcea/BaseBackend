package com.blankthings.basebackend.user

import com.blankthings.basebackend.magiclinktoken.MagicLinkToken
import com.blankthings.basebackend.magiclinktoken.MagicLinkToken_
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long,

    @Column(unique = true, nullable = false)
    val email: String,

    @OneToOne(mappedBy = MagicLinkToken_.USER, cascade = [CascadeType.ALL], orphanRemoval = true)
    val magicLinkToken: MagicLinkToken? = null
)