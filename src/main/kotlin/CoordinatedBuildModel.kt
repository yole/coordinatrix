package ru.yole.coordinatrix

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xml.Attribute
import com.intellij.util.xmlb.XmlSerializerUtil

class CoordinatedBuild : BaseState() {
    @get:Attribute
    var projectPath by string()

    @get:Attribute
    var artifactNames by string()

    override fun toString(): String {
        if (!artifactNames.isNullOrEmpty()) {
            return "Build artifacts $artifactNames in project $projectPath"
        }
        return "Build project $projectPath"
    }
}

@State(name = "CoordinatedBuildModel", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class CoordinatedBuildModel : PersistentStateComponent<CoordinatedBuildModel> {
    var builds = mutableListOf<CoordinatedBuild>()

    override fun getState(): CoordinatedBuildModel {
        return this
    }

    override fun loadState(state: CoordinatedBuildModel) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(project: Project) = ServiceManager.getService(project, CoordinatedBuildModel::class.java)
    }
}