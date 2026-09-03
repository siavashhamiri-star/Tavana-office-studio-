package com.tavana.studio.audio.project

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

interface ProjectRepository {
    fun getProjectsFlow(workspaceId: String? = null): Flow<List<AudioProject>>
    suspend fun getProjectById(id: String): AudioProject?
    suspend fun saveProject(project: AudioProject)
    suspend fun deleteProject(id: String): Boolean
    suspend fun addTrack(projectId: String, track: AudioTrack): AudioProject?
    suspend fun updateTrack(projectId: String, track: AudioTrack): AudioProject?
    suspend fun removeTrack(projectId: String, trackId: String): AudioProject?
    suspend fun saveTake(take: AudioTake)
    suspend fun getTakesForTrack(trackId: String): List<AudioTake>
    suspend fun getAllTakes(): List<AudioTake>
}

class InMemoryProjectRepository : ProjectRepository {
    private val _projects = MutableStateFlow<Map<String, AudioProject>>(emptyMap())
    private val _takes = MutableStateFlow<Map<String, AudioTake>>(emptyMap())

    override fun getProjectsFlow(workspaceId: String?): Flow<List<AudioProject>> {
        return _projects.asStateFlow().map { map ->
            if (workspaceId == null) {
                map.values.toList()
            } else {
                map.values.filter { it.metadata.workspaceId == workspaceId }
            }
        }
    }

    override suspend fun getProjectById(id: String): AudioProject? {
        return _projects.value[id]
    }

    override suspend fun saveProject(project: AudioProject) {
        val updated = project.copy(
            metadata = project.metadata.copy(updatedAtMs = System.currentTimeMillis())
        )
        _projects.value = _projects.value + (project.id to updated)
    }

    override suspend fun deleteProject(id: String): Boolean {
        if (!_projects.value.containsKey(id)) return false
        _projects.value = _projects.value - id
        return true
    }

    override suspend fun addTrack(projectId: String, track: AudioTrack): AudioProject? {
        val project = _projects.value[projectId] ?: return null
        val updated = project.copy(tracks = project.tracks + track)
        saveProject(updated)
        return updated
    }

    override suspend fun updateTrack(projectId: String, track: AudioTrack): AudioProject? {
        val project = _projects.value[projectId] ?: return null
        val updatedTracks = project.tracks.map { if (it.id == track.id) track else it }
        val updated = project.copy(tracks = updatedTracks)
        saveProject(updated)
        return updated
    }

    override suspend fun removeTrack(projectId: String, trackId: String): AudioProject? {
        val project = _projects.value[projectId] ?: return null
        val updatedTracks = project.tracks.filter { it.id != trackId }
        val updated = project.copy(tracks = updatedTracks)
        saveProject(updated)
        return updated
    }

    override suspend fun saveTake(take: AudioTake) {
        _takes.value = _takes.value + (take.id to take)
    }

    override suspend fun getTakesForTrack(trackId: String): List<AudioTake> {
        return _takes.value.values.filter { it.trackId == trackId }
    }

    override suspend fun getAllTakes(): List<AudioTake> {
        return _takes.value.values.toList()
    }
}
