package tech.nikku.learningapp.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class VaultModule(
  val id: String,
  val title: String,
  val badge: String,
  val description: String
)
