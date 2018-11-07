package ru.yole.coordinatrix

import com.intellij.execution.BeforeRunTaskProvider
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.util.Key
import org.jetbrains.concurrency.Promise

class CdxBeforeRunTaskProvider : BeforeRunTaskProvider<CdxBeforeRunTask>() {
    override fun getName(): String = "Run coordinated build"

    override fun isConfigurable(): Boolean = true

    override fun getId(): Key<CdxBeforeRunTask> = ID

    override fun createTask(runConfiguration: RunConfiguration): CdxBeforeRunTask? {
        return CdxBeforeRunTask()
    }

    override fun configureTask(context: DataContext, configuration: RunConfiguration, task: CdxBeforeRunTask): Promise<Boolean> {
        return super.configureTask(context, configuration, task)
    }

    override fun executeTask(context: DataContext?, configuration: RunConfiguration, env: ExecutionEnvironment, task: CdxBeforeRunTask): Boolean {
        return true
    }

    companion object {
        val ID = Key.create<CdxBeforeRunTask>("CdxBeforeRun")
    }
}