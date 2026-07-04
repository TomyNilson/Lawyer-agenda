package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "cases")
@JsonClass(generateAdapter = true)
data class CaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val courtName: String,
    val caseType: String,
    val caseNumber: String,
    val caseYear: String,
    val onBehalfOf: String = "مدعي / شاكي",
    val partyName: String = "",
    val partyContact: String = "",
    val adversePartyName: String = "",
    val adversePartyContact: String = "",
    val adverseAdvocateName: String = "",
    val adverseAdvocateContact: String = "",
    val respondentName: String = "",
    val filedUnderSection: String = "",
    val isDisposed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "steps")
@JsonClass(generateAdapter = true)
data class StepEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val caseId: Int,
    val previousDate: String = "",
    val adjournDate: String = "",
    val purpose: String = "",
    val isReminder: Boolean = true,
    val notes: String = ""
)

@Entity(tableName = "documents")
@JsonClass(generateAdapter = true)
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val caseId: Int,
    val title: String,
    val imageUriOrBase64: String,
    val fileType: String = "image", // image, pdf_ref, note
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "courts")
@JsonClass(generateAdapter = true)
data class CourtEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

@Entity(tableName = "case_types")
@JsonClass(generateAdapter = true)
data class CaseTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

@JsonClass(generateAdapter = true)
data class CaseWithDetails(
    val case: CaseEntity,
    val steps: List<StepEntity>,
    val documents: List<DocumentEntity>
)

@JsonClass(generateAdapter = true)
data class BackupData(
    val version: Int = 1,
    val exportDate: String,
    val cases: List<CaseEntity>,
    val steps: List<StepEntity>,
    val documents: List<DocumentEntity>,
    val courts: List<CourtEntity>,
    val caseTypes: List<CaseTypeEntity>
)
