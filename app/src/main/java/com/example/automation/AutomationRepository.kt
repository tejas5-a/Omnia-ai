package com.example.automation

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.AutomationEntity
import com.example.data.local.AutomationRunLogEntity
import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AutomationRepository(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val dao = db.automationDao()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val actionListType = Types.newParameterizedType(List::class.java, AutomationAction::class.java)
    private val conditionListType = Types.newParameterizedType(List::class.java, AutomationCondition::class.java)
    private val stepResultListType = Types.newParameterizedType(List::class.java, StepExecutionResult::class.java)

    val allAutomations: Flow<List<AutomationWorkflow>> = dao.getAllAutomations().map { entities ->
        entities.map { entityToModel(it) }
    }

    val allRunLogs: Flow<List<AutomationRunLog>> = dao.getAllRunLogs().map { entities ->
        entities.map { entityToLog(it) }
    }

    suspend fun getAutomationById(id: String): AutomationWorkflow? = withContext(Dispatchers.IO) {
        dao.getAutomationById(id)?.let { entityToModel(it) }
    }

    suspend fun saveAutomation(workflow: AutomationWorkflow) = withContext(Dispatchers.IO) {
        val entity = modelToEntity(workflow)
        dao.insertAutomation(entity)
    }

    suspend fun toggleEnabled(id: String, isEnabled: Boolean) = withContext(Dispatchers.IO) {
        dao.setEnabled(id, isEnabled)
    }

    suspend fun deleteAutomation(id: String) = withContext(Dispatchers.IO) {
        dao.deleteAutomation(id)
    }

    suspend fun logRun(log: AutomationRunLog) = withContext(Dispatchers.IO) {
        val stepResultsJson = moshi.adapter<List<StepExecutionResult>>(stepResultListType).toJson(log.stepResults)
        val entity = AutomationRunLogEntity(
            id = log.id,
            automationId = log.automationId,
            automationName = log.automationName,
            status = log.status.name,
            stepResultsJson = stepResultsJson,
            outputSummary = log.outputSummary,
            durationMs = log.durationMs,
            timestamp = log.timestamp
        )
        dao.insertRunLog(entity)

        val automation = dao.getAutomationById(log.automationId)
        if (automation != null) {
            val newRunCount = automation.runCount + 1
            val newSuccess = if (log.status == ExecutionStatus.SUCCESS) automation.successCount + 1 else automation.successCount
            val newFailure = if (log.status == ExecutionStatus.FAILED) automation.failureCount + 1 else automation.failureCount
            dao.updateAutomation(
                automation.copy(
                    lastRunMs = log.timestamp,
                    runCount = newRunCount,
                    successCount = newSuccess,
                    failureCount = newFailure,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun seedDefaultTemplatesIfEmpty() = withContext(Dispatchers.IO) {
        val existing = dao.getAllAutomationsSync()
        if (existing.isEmpty()) {
            AutomationTemplates.ALL_TEMPLATES.forEach { template ->
                saveAutomation(template.workflow)
            }
        }
    }

    private fun entityToModel(e: AutomationEntity): AutomationWorkflow {
        val triggerType = try {
            TriggerType.valueOf(e.triggerType)
        } catch (ex: Exception) {
            TriggerType.MANUAL
        }

        val triggerConfig = try {
            moshi.adapter(TriggerConfig::class.java).fromJson(e.triggerConfigJson) ?: TriggerConfig()
        } catch (ex: Exception) {
            TriggerConfig()
        }

        val conditions = try {
            if (e.conditionsJson.isBlank()) emptyList()
            else moshi.adapter<List<AutomationCondition>>(conditionListType).fromJson(e.conditionsJson) ?: emptyList()
        } catch (ex: Exception) {
            emptyList()
        }

        val actions = try {
            if (e.actionsJson.isBlank()) emptyList()
            else moshi.adapter<List<AutomationAction>>(actionListType).fromJson(e.actionsJson) ?: emptyList()
        } catch (ex: Exception) {
            emptyList()
        }

        val targetApp = try {
            CrossAppTarget.valueOf(e.targetApp)
        } catch (ex: Exception) {
            CrossAppTarget.SYSTEM
        }

        return AutomationWorkflow(
            id = e.id,
            name = e.name,
            description = e.description,
            iconName = e.iconName,
            isEnabled = e.isEnabled,
            triggerType = triggerType,
            triggerConfig = triggerConfig,
            conditions = conditions,
            actions = actions,
            targetApp = targetApp,
            lastRunMs = e.lastRunMs,
            runCount = e.runCount,
            successCount = e.successCount,
            failureCount = e.failureCount,
            createdAt = e.createdAt,
            updatedAt = e.updatedAt
        )
    }

    private fun modelToEntity(w: AutomationWorkflow): AutomationEntity {
        val triggerConfigJson = moshi.adapter(TriggerConfig::class.java).toJson(w.triggerConfig)
        val conditionsJson = moshi.adapter<List<AutomationCondition>>(conditionListType).toJson(w.conditions)
        val actionsJson = moshi.adapter<List<AutomationAction>>(actionListType).toJson(w.actions)

        return AutomationEntity(
            id = w.id,
            name = w.name,
            description = w.description,
            iconName = w.iconName,
            isEnabled = w.isEnabled,
            triggerType = w.triggerType.name,
            triggerConfigJson = triggerConfigJson,
            conditionsJson = conditionsJson,
            actionsJson = actionsJson,
            targetApp = w.targetApp.name,
            lastRunMs = w.lastRunMs,
            runCount = w.runCount,
            successCount = w.successCount,
            failureCount = w.failureCount,
            createdAt = w.createdAt,
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun entityToLog(e: AutomationRunLogEntity): AutomationRunLog {
        val status = try { ExecutionStatus.valueOf(e.status) } catch (ex: Exception) { ExecutionStatus.FAILED }
        val stepResults = try {
            moshi.adapter<List<StepExecutionResult>>(stepResultListType).fromJson(e.stepResultsJson) ?: emptyList()
        } catch (ex: Exception) {
            emptyList()
        }

        return AutomationRunLog(
            id = e.id,
            automationId = e.automationId,
            automationName = e.automationName,
            status = status,
            stepResults = stepResults,
            outputSummary = e.outputSummary,
            durationMs = e.durationMs,
            timestamp = e.timestamp
        )
    }
}
