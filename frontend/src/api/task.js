import request from './index'

export function getProjectLists(projectId) {
  return request.get(`/projects/${projectId}/lists`)
}

export function createTaskList(projectId, data) {
  return request.post(`/projects/${projectId}/lists`, data)
}

export function updateTaskList(listId, data) {
  return request.put(`/task-lists/${listId}`, data)
}

export function deleteTaskList(listId) {
  return request.delete(`/task-lists/${listId}`)
}

export function reorderLists(data) {
  return request.put('/task-lists/reorder', data)
}

export function getTasks(listId, params) {
  return request.get(`/task-lists/${listId}/tasks`, { params })
}

export function createTask(listId, data) {
  return request.post(`/task-lists/${listId}/tasks`, data)
}

export function getTaskDetail(taskId) {
  return request.get(`/tasks/${taskId}`)
}

export function updateTask(taskId, data) {
  return request.put(`/tasks/${taskId}`, data)
}

export function deleteTask(taskId) {
  return request.delete(`/tasks/${taskId}`)
}

export function moveTask(taskId, data) {
  return request.put(`/tasks/${taskId}/move`, data)
}

export function reorderTasks(data) {
  return request.put('/tasks/reorder', data)
}
