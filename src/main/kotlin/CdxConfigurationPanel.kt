package ru.yole.coordinatrix

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.CollectionListModel
import com.intellij.ui.ComboboxWithBrowseButton
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.layout.panel
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

class CdxEditBuildDialog(project: Project, private val build: CoordinatedBuild) : DialogWrapper(project) {
    private val projectPathCombo = ComboBox<String>().apply { isEditable = true }
    private val projectPathField = ComboboxWithBrowseButton(projectPathCombo)
    private val artifactNamesField = JTextField()

    init {
        init()
        title = "Edit Coordinated Build"
        val otherProjectPaths = ProjectManager.getInstance().openProjects
            .filter { it != project }
            .mapNotNull { it.basePath }
        projectPathCombo.model = CollectionComboBoxModel(otherProjectPaths, build.projectPath)
        projectPathCombo.editor.item = build.projectPath
        artifactNamesField.text = build.artifactNames
    }

    override fun createCenterPanel(): JComponent? {
        return panel {
            row("Path:") {
                projectPathField()
            }
            row("Artifacts to build:") {
                artifactNamesField()
            }
        }
    }

    override fun doOKAction() {
        build.projectPath = projectPathField.comboBox.editor.item.toString()
        build.artifactNames = artifactNamesField.text
        super.doOKAction()
    }
}

class CdxConfigurationPanel(private val project: Project) : JPanel(BorderLayout()) {
    private val buildListModel = CollectionListModel(CoordinatedBuildModel.getInstance(project).builds)
    private val buildList = JBList<CoordinatedBuild>(buildListModel)

    init {
        val decorator = ToolbarDecorator.createDecorator(buildList)
        decorator.setAddAction {
            val build = CoordinatedBuild()
            val dialog = CdxEditBuildDialog(project, build)
            dialog.show()
            if (dialog.isOK) {
                buildListModel.add(build)
            }
        }
        decorator.setEditAction {
            buildList.selectedValue?.let { selectedBuild ->
                val dialog = CdxEditBuildDialog(project, selectedBuild)
                dialog.show()
                buildListModel.contentsChanged(selectedBuild)
            }
        }
        add(decorator.createPanel(), BorderLayout.CENTER)
    }
}

class CdxConfigurationDialog(project: Project) : DialogWrapper(project) {
    private val panel = CdxConfigurationPanel(project)

    init {
        title = "Configure Coordinated Builds"
        init()
    }

    override fun createCenterPanel(): JComponent? {
        return panel
    }
}
