import { apiClient } from "./client";
import type { CreateNoteRequest, Note } from "../types/api";

export async function getNotes(applicationId: number): Promise<Note[]> {
  const response = await apiClient.get<Note[]>(`/api/applications/${applicationId}/notes`);
  return response.data;
}

export async function createNote(applicationId: number, request: CreateNoteRequest): Promise<Note> {
  const response = await apiClient.post<Note>(`/api/applications/${applicationId}/notes`, request);
  return response.data;
}

export async function deleteNote(id: number): Promise<void> {
  await apiClient.delete(`/api/notes/${id}`);
}
