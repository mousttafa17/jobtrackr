import { apiClient } from "./client";
import type { CreateJobTaskRequest, JobTask } from "../types/api";

export async function getTasks(applicationId: number): Promise<JobTask[]> {
  const response = await apiClient.get<JobTask[]>(`/api/applications/${applicationId}/tasks`);
  return response.data;
}

export async function createTask(
  applicationId: number,
  request: CreateJobTaskRequest,
): Promise<JobTask> {
  const response = await apiClient.post<JobTask>(
    `/api/applications/${applicationId}/tasks`,
    request,
  );
  return response.data;
}

export async function completeTask(id: number): Promise<JobTask> {
  const response = await apiClient.patch<JobTask>(`/api/tasks/${id}/complete`);
  return response.data;
}

export async function deleteTask(id: number): Promise<void> {
  await apiClient.delete(`/api/tasks/${id}`);
}
