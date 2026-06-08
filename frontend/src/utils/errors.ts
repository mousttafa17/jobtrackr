import { AxiosError } from "axios";
import type { ApiError } from "../types/api";

export function getApiErrorMessage(error: unknown): string {
  if (error instanceof AxiosError) {
    const data = error.response?.data as ApiError | undefined;
    if (data?.fieldErrors && Object.keys(data.fieldErrors).length > 0) {
      return Object.values(data.fieldErrors)[0];
    }

    return data?.message ?? "Something went wrong. Please try again.";
  }

  return "Something went wrong. Please try again.";
}
