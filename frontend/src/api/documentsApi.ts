import { apiClient } from "./client";
import type { CreateJobDocumentRequest, JobDocument } from "../types/api";

export async function getDocuments(applicationId: number): Promise<JobDocument[]> {
  const response = await apiClient.get<JobDocument[]>(
    `/api/applications/${applicationId}/documents`,
  );
  return response.data;
}

export async function createDocument(
  applicationId: number,
  request: CreateJobDocumentRequest,
): Promise<JobDocument> {
  const response = await apiClient.post<JobDocument>(
    `/api/applications/${applicationId}/documents`,
    request,
  );
  return response.data;
}

export async function deleteDocument(id: number): Promise<void> {
  await apiClient.delete(`/api/documents/${id}`);
}
