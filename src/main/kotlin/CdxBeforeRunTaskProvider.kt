package ru.yole.coordinatrix

import com.intellij.execution.BeforeRunTaskProvider
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.TransactionGuard
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.Key
import com.intellij.task.ProjectTaskManager
import com.intellij.util.concurrency.Semaphore
import org.jetbrains.concurrency.Promise

class CdxBeforeRunTaskProvider : BeforeRunTaskProvider<CdxBeforeRunTask>() {
    override fun getName(): String = "Run coordinated build"

    override fun isConfigurable(): Boolean = true

    override fun getId(): Key<CdxBeforeRunTask> = ID

    override fun createTask(runConfiguration: RunConfiguration): CdxBeforeRunTask? {
        return CdxBeforeRunTask()
    }

    override fun configureTask(context: DataContext, configuration: RunConfiguration, task: CdxBeforeRunTask): Promise<Boolean> {
        val configurationDialog = CdxConfigurationDialog(configuration.project)
        configurationDialog.show()
        return Promise.resolve(configurationDialog.isOK)
    }

    override fun executeTask(context: DataContext, configuration: RunConfiguration, env: ExecutionEnvironment, task: CdxBeforeRunTask): Boolean {
        val coordinatedBuildModel = CoordinatedBuildModel.getInstance(configuration.project)
        for (build in coordinatedBuildModel.builds) {
            val project = ProjectManager.getInstance().openProjects.find { it.basePath == build.projectPath }
            if (project == null) {
                Notification(
                    notificationGroup.displayId, "Coordinated Build", "Project ${build.projectPath} is not open",
                    NotificationType.ERROR, null).notify(configuration.project)
                return false
            }
            val done = Semaphore()
            done.down()
            val projectTaskManager = ProjectTaskManager.getInstance(project)
            var result = false
            TransactionGuard.submitTransaction(project, Runnable {
                if (!configuration.project.isDisposed) {
                    projectTaskManager.buildAllModules { projectTaskResult ->
                        if (projectTaskResult.errors == 0 && !projectTaskResult.isAborted) {
                            result = true
                        }
                        done.up()
                    }
                }
                else {
                    done.up()
                }
            })
            done.waitFor()
            if (!result) {
                Notification(notificationGroup.displayId, "Coordinated Build", "Build failed for ${build.projectPath}",
                    NotificationType.ERROR, null).notify(configuration.project)
                return false
            }
        }

        return true
    }

    companion object {
        val ID = Key.create<CdxBeforeRunTask>("CdxBeforeRun")

        val notificationGroup = NotificationGroup.balloonGroup("Coordinatrix")
    }
}