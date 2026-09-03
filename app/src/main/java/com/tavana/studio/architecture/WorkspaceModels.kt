package com.tavana.studio.architecture

/**
 * Core conceptual model:
 * Identity → Organization → Role → Workspace → Permission
 *
 * Enforces strict architectural boundaries:
 * - Personal Space (private by default)
 * - My Studio (audio/creative production)
 * - My Work (projects & collaborative tasks)
 * - Teaching (pedagogical sessions & vocal coaching)
 * - TAVANA Governance (auditing, policy, oversight)
 *
 * Core rule: Governance access must NEVER automatically provide access to
 * Personal Space or private creative projects.
 */

data class Identity(
    val id: String,
    val displayName: String,
    val email: String,
    val isAnonymous: Boolean = false
)

data class Organization(
    val id: String,
    val name: String,
    val type: OrgType = OrgType.COMMUNITY
)

enum class OrgType {
    PERSONAL,
    STUDIO,
    EDUCATIONAL,
    ENTERPRISE,
    COMMUNITY
}

enum class Role {
    OWNER,
    ADMIN,
    CREATOR,
    EDUCATOR,
    STUDENT,
    MEMBER,
    AUDITOR_GOVERNANCE
}

enum class WorkspaceType {
    PERSONAL_SPACE,
    MY_STUDIO,
    MY_WORK,
    TEACHING,
    TAVANA_GOVERNANCE
}

enum class Permission {
    VIEW_WORKSPACE,
    ACCESS_PERSONAL_PROJECTS,
    CREATE_AUDIO_PROJECT,
    EDIT_AUDIO_PROJECT,
    RECORD_AUDIO,
    MIX_AUDIO,
    EXPORT_AUDIO,
    COACH_STUDENT,
    AUDIT_GOVERNANCE,
    MANAGE_MEMBERS
}

data class Workspace(
    val id: String,
    val name: String,
    val type: WorkspaceType,
    val ownerIdentityId: String,
    val organizationId: String? = null,
    val isPrivateByDefault: Boolean = (type == WorkspaceType.PERSONAL_SPACE),
    val explicitSharedWith: Set<String> = emptySet()
)

object WorkspaceAccessPolicy {

    /**
     * Evaluates whether an identity with a role in a workspace has a specific permission.
     *
     * Fundamental Rule:
     * Governance access NEVER automatically provides access to Personal Space
     * or private creative projects without explicit delegation from the owner.
     */
    fun hasPermission(
        identityId: String,
        role: Role,
        workspace: Workspace,
        permission: Permission
    ): Boolean {
        // Personal space is private to the owner unless explicitly shared
        if (workspace.type == WorkspaceType.PERSONAL_SPACE) {
            if (identityId == workspace.ownerIdentityId) {
                return true
            }
            // Governance/auditor role explicitly DENIED access to Personal Space
            if (role == Role.AUDITOR_GOVERNANCE) {
                return false
            }
            // Only explicitly shared members can view/read
            return workspace.explicitSharedWith.contains(identityId) &&
                    (permission == Permission.VIEW_WORKSPACE || permission == Permission.ACCESS_PERSONAL_PROJECTS)
        }

        // Governance workspace rules
        if (workspace.type == WorkspaceType.TAVANA_GOVERNANCE) {
            return when (permission) {
                Permission.AUDIT_GOVERNANCE -> role in listOf(Role.OWNER, Role.ADMIN, Role.AUDITOR_GOVERNANCE)
                Permission.VIEW_WORKSPACE -> true
                else -> role in listOf(Role.OWNER, Role.ADMIN)
            }
        }

        // Studio workspace rules
        if (workspace.type == WorkspaceType.MY_STUDIO) {
            if (role == Role.AUDITOR_GOVERNANCE && !workspace.explicitSharedWith.contains(identityId)) {
                // Governance cannot snoop private studio projects
                return false
            }
            return when (permission) {
                Permission.CREATE_AUDIO_PROJECT,
                Permission.EDIT_AUDIO_PROJECT,
                Permission.RECORD_AUDIO,
                Permission.MIX_AUDIO,
                Permission.EXPORT_AUDIO -> role in listOf(Role.OWNER, Role.ADMIN, Role.CREATOR)
                Permission.VIEW_WORKSPACE -> true
                else -> role in listOf(Role.OWNER, Role.ADMIN)
            }
        }

        // Teaching workspace rules
        if (workspace.type == WorkspaceType.TEACHING) {
            return when (permission) {
                Permission.COACH_STUDENT -> role in listOf(Role.OWNER, Role.EDUCATOR)
                Permission.RECORD_AUDIO,
                Permission.VIEW_WORKSPACE -> true
                else -> role in listOf(Role.OWNER, Role.ADMIN, Role.EDUCATOR)
            }
        }

        // Default workspace access
        return when (role) {
            Role.OWNER, Role.ADMIN -> true
            Role.CREATOR -> permission != Permission.MANAGE_MEMBERS && permission != Permission.AUDIT_GOVERNANCE
            Role.MEMBER -> permission == Permission.VIEW_WORKSPACE
            Role.AUDITOR_GOVERNANCE -> permission == Permission.AUDIT_GOVERNANCE
            else -> false
        }
    }
}
