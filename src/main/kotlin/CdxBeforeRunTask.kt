package ru.yole.coordinatrix

import com.intellij.execution.BeforeRunTask
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.util.xmlb.annotations.Attribute

class CdxBeforeRunTaskState : BaseState() {
    @get:Attribute
    var id by string()
}

class CdxBeforeRunTask : BeforeRunTask<CdxBeforeRunTask>(CdxBeforeRunTaskProvider.ID), PersistentStateComponent<CdxBeforeRunTaskState> {
    private var state = CdxBeforeRunTaskState()

    override fun getState(): CdxBeforeRunTaskState? = state

    override fun loadState(state: CdxBeforeRunTaskState) {
        state.resetModificationCount()
        this.state = state
    }
}

