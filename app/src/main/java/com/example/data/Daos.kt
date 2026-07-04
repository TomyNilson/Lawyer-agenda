package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CaseDao {
    @Query("SELECT * FROM cases ORDER BY createdAt DESC")
    fun getAllCases(): Flow<List<CaseEntity>>

    @Query("SELECT * FROM cases WHERE isDisposed = :isDisposed ORDER BY createdAt DESC")
    fun getCasesByStatus(isDisposed: Boolean): Flow<List<CaseEntity>>

    @Query("SELECT * FROM cases WHERE id = :id")
    fun getCaseById(id: Int): Flow<CaseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCase(caseEntity: CaseEntity): Long

    @Update
    suspend fun updateCase(caseEntity: CaseEntity)

    @Delete
    suspend fun deleteCase(caseEntity: CaseEntity)

    @Query("UPDATE cases SET isDisposed = :isDisposed WHERE id = :caseId")
    suspend fun setCaseDisposedStatus(caseId: Int, isDisposed: Boolean)
}

@Dao
interface StepDao {
    @Query("SELECT * FROM steps WHERE caseId = :caseId ORDER BY id ASC")
    fun getStepsForCase(caseId: Int): Flow<List<StepEntity>>

    @Query("SELECT * FROM steps ORDER BY adjournDate ASC")
    fun getAllSteps(): Flow<List<StepEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStep(stepEntity: StepEntity): Long

    @Update
    suspend fun updateStep(stepEntity: StepEntity)

    @Delete
    suspend fun deleteStep(stepEntity: StepEntity)

    @Query("DELETE FROM steps WHERE caseId = :caseId")
    suspend fun deleteStepsForCase(caseId: Int)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents WHERE caseId = :caseId ORDER BY addedAt DESC")
    fun getDocumentsForCase(caseId: Int): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(documentEntity: DocumentEntity): Long

    @Delete
    suspend fun deleteDocument(documentEntity: DocumentEntity)

    @Query("DELETE FROM documents WHERE caseId = :caseId")
    suspend fun deleteDocumentsForCase(caseId: Int)
}

@Dao
interface CourtDao {
    @Query("SELECT * FROM courts ORDER BY name ASC")
    fun getAllCourts(): Flow<List<CourtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourt(courtEntity: CourtEntity): Long

    @Delete
    suspend fun deleteCourt(courtEntity: CourtEntity)

    @Query("SELECT COUNT(*) FROM courts")
    suspend fun getCount(): Int
}

@Dao
interface CaseTypeDao {
    @Query("SELECT * FROM case_types ORDER BY name ASC")
    fun getAllCaseTypes(): Flow<List<CaseTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaseType(caseTypeEntity: CaseTypeEntity): Long

    @Delete
    suspend fun deleteCaseType(caseTypeEntity: CaseTypeEntity)

    @Query("SELECT COUNT(*) FROM case_types")
    suspend fun getCount(): Int
}
