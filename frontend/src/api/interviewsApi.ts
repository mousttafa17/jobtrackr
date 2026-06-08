import { apiClient } from "./client";
import type { CreateInterviewRequest, Interview, UpdateInterviewRequest } from "../types/api";

export async function getInterviews(applicationId: number): Promise<Interview[]> {
  const response = await apiClient.get<Interview[]>(`/api/applications/${applicationId}/interviews`);
  return response.data;
}

export async function createInterview(
  applicationId: number,
  request: CreateInterviewRequest,
): Promise<Interview> {
  const response = await apiClient.post<Interview>(
    `/api/applications/${applicationId}/interviews`,
    request,
  );
  return response.data;
}

export async function updateInterview(
  id: number,
  request: UpdateInterviewRequest,
): Promise<Interview> {
  const response = await apiClient.put<Interview>(`/api/interviews/${id}`, request);
  return response.data;
}

export async function deleteInterview(id: number): Promise<void> {
  await apiClient.delete(`/api/interviews/${id}`);
}
