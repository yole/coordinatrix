package ru.yole.coordinatrix

import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.filters.ConsoleFilterProvider
import com.intellij.execution.filters.Filter
import com.intellij.execution.remote.RemoteConfiguration
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager

class CdxConsoleFilterProvider : ConsoleFilterProvider {
    override fun getDefaultFilters(project: Project): Array<Filter> {
        val builds = CoordinatedBuildModel.getInstance(project).builds
        if (builds.isNotEmpty()) {
            return arrayOf(CdxConsoleFilter(project))
        }
        return emptyArray()
    }
}

class CdxConsoleFilter(private val project: Project) : Filter {
    override fun applyFilter(line: String?, entireLength: Int): Filter.Result? {
        if (line != null && line.startsWith(dtSocketPrefix)) {
            LOG.info("Saw debugger connection line: $line")
            val port = line.removePrefix(dtSocketPrefix).trim().toIntOrNull() ?: run {
                LOG.info("Failed to find port")
                return null
            }
            val builds = CoordinatedBuildModel.getInstance(project).builds
            for (build in builds) {
                val targetProject = ProjectManager.getInstance().openProjects.find { it.basePath == build.projectPath }
                if (targetProject == null) {
                    LOG.info("Failed to find open project for path ${build.projectPath}")
                    return null
                }

                val runManager = RunManager.getInstance(targetProject)
                val configurationAndSettings = runManager.allSettings.find {
                    (it.configuration as? RemoteConfiguration)?.PORT == port.toString()
                }
                if (configurationAndSettings == null) {
                    LOG.info("Failed to find remote debug configuration with port $port")
                    return null
                }

                ExecutionEnvironmentBuilder.create(
                    DefaultDebugExecutor.getDebugExecutorInstance(),
                    configurationAndSettings
                ).buildAndExecute()
            }
        }
        return null
    }

    companion object {
        const val dtSocketPrefix = "Listening for transport dt_socket at address:"
        val LOG = Logger.getInstance(CdxConsoleFilter::class.java)
    }
}
