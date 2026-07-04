package com.example.data

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LawyerRepository(private val db: AppDatabase) {
    val caseDao = db.caseDao()
    val stepDao = db.stepDao()
    val documentDao = db.documentDao()
    val courtDao = db.courtDao()
    val caseTypeDao = db.caseTypeDao()

    val allCases: Flow<List<CaseEntity>> = caseDao.getAllCases()
    val activeCases: Flow<List<CaseEntity>> = caseDao.getCasesByStatus(false)
    val disposedCases: Flow<List<CaseEntity>> = caseDao.getCasesByStatus(true)
    val allSteps: Flow<List<StepEntity>> = stepDao.getAllSteps()
    val allCourts: Flow<List<CourtEntity>> = courtDao.getAllCourts()
    val allCaseTypes: Flow<List<CaseTypeEntity>> = caseTypeDao.getAllCaseTypes()

    suspend fun seedInitialDataIfEmpty() {
        if (courtDao.getCount() == 0) {
            val defaultCourts = listOf(
                "محكمة تيارت",
                "مجلس قضاء تيارت",
                "المحكمة الابتدائية",
                "محكمة الأسرة",
                "محكمة الاستئناف",
                "المحكمة العليا",
                "محكمة الجنايات",
                "المحكمة التجارية"
            )
            defaultCourts.forEach { courtDao.insertCourt(CourtEntity(name = it)) }
        }

        if (caseTypeDao.getCount() == 0) {
            val defaultTypes = listOf(
                "جنح",
                "مدني",
                "عقاري",
                "بريد",
                "أسرة",
                "تجارية",
                "عمالي",
                "إداري",
                "استئناف"
            )
            defaultTypes.forEach { caseTypeDao.insertCaseType(CaseTypeEntity(name = it)) }
        }

        // Add demo cases if no cases exist
        val currentCases = caseDao.getAllCases().first()
        if (currentCases.isEmpty()) {
            val case1Id = caseDao.insertCase(
                CaseEntity(
                    title = "نصب واحتيال",
                    courtName = "محكمة تيارت",
                    caseType = "بريد",
                    caseNumber = "41236",
                    caseYear = "2026",
                    onBehalfOf = "شاكي / مدعي",
                    partyName = "فيلالي محمد",
                    partyContact = "0550123456",
                    adversePartyName = "رابة امين + رفيق",
                    adversePartyContact = "0660000000",
                    filedUnderSection = "المادة 372 قانون العقوبات",
                    isDisposed = false
                )
            )

            stepDao.insertStep(
                StepEntity(
                    caseId = case1Id.toInt(),
                    previousDate = "2026-06-20",
                    adjournDate = "2026-07-10",
                    purpose = "للحضور وتقديم المذكرة الدفاعية",
                    isReminder = true,
                    notes = "إعداد مستندات الإثبات والشهود"
                )
            )

            val case2Id = caseDao.insertCase(
                CaseEntity(
                    title = "سرقة موصوفة",
                    courtName = "محكمة تيارت",
                    caseType = "جنح",
                    caseNumber = "41384",
                    caseYear = "2026",
                    onBehalfOf = "مدعى عليه",
                    partyName = "مكي ذهبية",
                    partyContact = "0770987654",
                    adversePartyName = "النيابة العامة",
                    adversePartyContact = "",
                    filedUnderSection = "المادة 350",
                    isDisposed = false
                )
            )

            stepDao.insertStep(
                StepEntity(
                    caseId = case2Id.toInt(),
                    previousDate = "2026-06-25",
                    adjournDate = "2026-07-15",
                    purpose = "سماع أقوال الشهود ومرافعة الدفاع",
                    isReminder = true,
                    notes = "التأكد من إحضار التقرير الطبي"
                )
            )

            val case3Id = caseDao.insertCase(
                CaseEntity(
                    title = "نزاع ملكية عقارية",
                    courtName = "مجلس قضاء تيارت",
                    caseType = "عقاري",
                    caseNumber = "1158",
                    caseYear = "2026",
                    onBehalfOf = "مدعي",
                    partyName = "ولد مختار مختار",
                    partyContact = "0555112233",
                    adversePartyName = "ورثة بن علي",
                    adversePartyContact = "0666445566",
                    filedUnderSection = "قانون التوجيه العقاري",
                    isDisposed = false
                )
            )

            stepDao.insertStep(
                StepEntity(
                    caseId = case3Id.toInt(),
                    previousDate = "2026-05-10",
                    adjournDate = "2026-07-08",
                    purpose = "تعيين خبير عقاري وإيداع التقرير",
                    isReminder = true,
                    notes = "متابعة الخبير المنتدب في الميدان"
                )
            )
        }
    }

    suspend fun exportBackupJson(): String {
        val cases = caseDao.getAllCases().first()
        val steps = stepDao.getAllSteps().first()
        // For backup documents, get all
        val allDocs = mutableListOf<DocumentEntity>()
        for (c in cases) {
            allDocs.addAll(documentDao.getDocumentsForCase(c.id).first())
        }
        val courts = courtDao.getAllCourts().first()
        val types = caseTypeDao.getAllCaseTypes().first()

        val backup = BackupData(
            version = 1,
            exportDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date()),
            cases = cases,
            steps = steps,
            documents = allDocs,
            courts = courts,
            caseTypes = types
        )

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(BackupData::class.java)
        return adapter.toJson(backup)
    }

    suspend fun restoreBackupJson(jsonString: String): Boolean {
        return try {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(BackupData::class.java)
            val backup = adapter.fromJson(jsonString) ?: return false

            // Restore courts
            backup.courts.forEach { court ->
                val existing = courtDao.getAllCourts().first().any { it.name == court.name }
                if (!existing) courtDao.insertCourt(CourtEntity(name = court.name))
            }
            // Restore case types
            backup.caseTypes.forEach { type ->
                val existing = caseTypeDao.getAllCaseTypes().first().any { it.name == type.name }
                if (!existing) caseTypeDao.insertCaseType(CaseTypeEntity(name = type.name))
            }
            // Restore cases
            backup.cases.forEach { case ->
                val newId = caseDao.insertCase(case.copy(id = 0))
                // Restore steps for this old case id
                backup.steps.filter { it.caseId == case.id }.forEach { step ->
                    stepDao.insertStep(step.copy(id = 0, caseId = newId.toInt()))
                }
                // Restore documents
                backup.documents.filter { it.caseId == case.id }.forEach { doc ->
                    documentDao.insertDocument(doc.copy(id = 0, caseId = newId.toInt()))
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
