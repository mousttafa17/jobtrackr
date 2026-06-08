import { apiClient } from "./client";
import type {
  ApplicationFilters,
  CreateJobApplicationRequest,
  JobApplication,
  JobApplicationStatusHistory,
  PageResponse,
  UpdateApplicationStatusRequest,
  UpdateJobApplicationRequest,
} from "../types/api";

export async function getApplications(
  filters: ApplicationFilters,
): Promise<PageResponse<JobApplication>> {
  const params = new URLSearchParams();
  params.set("page", String(filters.page));
  params.set("size", String(filters.size));
  params.set("sort", `${filters.sortField},${filters.sortDirection}`);

  if (filters.status) {
    params.set("status", filters.status);
  }
  if (filters.company?.trim()) {
    params.set("company", filters.company.trim());
  }
  if (filters.search?.trim()) {
    params.set("search", filters.search.trim());
  }

  const response = await apiClient.get<PageResponse<JobApplication>>("/api/applications", {
    params,
  });
  return response.data;
}

export async function createApplication(
  request: CreateJobApplicationRequest,
): Promise<JobApplication> {
  const response = await apiClient.post<JobApplication>("/api/applications", request);
  return response.data;
}

export async function getApplication(id: number): Promise<JobApplication> {
  const response = await apiClient.get<JobApplication>(`/api/applications/${id}`);
  return response.data;
}

export async function updateApplication(
  id: number,
  request: UpdateJobApplicationRequest,
): Promise<JobApplication> {
  const response = await apiClient.put<JobApplication>(`/api/applications/${id}`, request);
  return response.data;
}

export async function updateApplicationStatus(
  id: number,
  request: UpdateApplicationStatusRequest,
): Promise<JobApplication> {
  const response = await apiClient.patch<JobApplication>(
    `/api/applications/${id}/status`,
    request,
  );
  return response.data;
}

export async function deleteApplication(id: number): Promise<void> {
  await apiClient.delete(`/api/applications/${id}`);
}

export async function getApplicationStatusHistory(
  id: number,
): Promise<JobApplicationStatusHistory[]> {
  const response = await apiClient.get<JobApplicationStatusHistory[]>(
    `/api/applications/${id}/status-history`,
  );
  return response.data;
}
