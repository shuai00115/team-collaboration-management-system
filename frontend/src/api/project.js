import request from './index'

export function getTeamProjects(teamId, params) {
  return request.get(`/teams/${teamId}/projects`, { params })
}

export function createProject(teamId, data) {
  return request.post(`/teams/${teamId}/projects`, data)
}

export function getProjectDetail(projectId) {
  return request.get(`/projects/${projectId}`)
}

export function updateProject(projectId, data) {
  return request.put(`/projects/${projectId}`, data)
}

export function deleteProject(projectId) {
  return request.delete(`/projects/${projectId}`)
}

// ---- 阶段 ----
export function getStages(projectId) {
  return request.get(`/projects/${projectId}/stages`)
}

export function createStage(projectId, data) {
  return request.post(`/projects/${projectId}/stages`, data)
}

export function updateStage(stageId, data) {
  return request.put(`/stages/${stageId}`, data)
}

export function deleteStage(stageId) {
  return request.delete(`/stages/${stageId}`)
}

export function updateStageStatus(stageId, data) {
  return request.put(`/stages/${stageId}/status`, data)
}

export function getStageTemplates() {
  return request.get('/stage-templates')
}

export function createStagesFromTemplate(projectId, data) {
  return request.post(`/projects/${projectId}/stages/template`, data)
}

// ---- 统计 ----
export function getTeamStats(teamId, params) {
  return request.get(`/teams/${teamId}/stats`, { params })
}

export function getMemberStats(teamId, userId) {
  return request.get(`/teams/${teamId}/members/${userId}/stats`)
}
