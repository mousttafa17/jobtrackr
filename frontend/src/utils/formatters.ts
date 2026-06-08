import type { ApplicationStatus, DocumentType, InterviewStatus, InterviewType } from "../types/api";

const statusLabels: Record<ApplicationStatus, string> = {
  APPLIED: "Applied",
  ONLINE_ASSESSMENT: "Online assessment",
  INTERVIEW: "Interview",
  OFFER: "Offer",
  REJECTED: "Rejected",
  ACCEPTED: "Accepted",
};

export function formatStatus(status: ApplicationStatus): string {
  return statusLabels[status];
}

const interviewTypeLabels: Record<InterviewType, string> = {
  PHONE_SCREEN: "Phone screen",
  TECHNICAL: "Technical",
  BEHAVIORAL: "Behavioral",
  ONSITE: "Onsite",
  FINAL: "Final",
  HR: "HR",
  OTHER: "Other",
};

const interviewStatusLabels: Record<InterviewStatus, string> = {
  SCHEDULED: "Scheduled",
  COMPLETED: "Completed",
  CANCELLED: "Cancelled",
  RESCHEDULED: "Rescheduled",
};

const documentTypeLabels: Record<DocumentType, string> = {
  RESUME: "Resume",
  COVER_LETTER: "Cover letter",
  PORTFOLIO: "Portfolio",
  OFFER_LETTER: "Offer letter",
  JOB_DESCRIPTION: "Job description",
  OTHER: "Other",
};

export function formatInterviewType(type: InterviewType): string {
  return interviewTypeLabels[type];
}

export function formatInterviewStatus(status: InterviewStatus): string {
  return interviewStatusLabels[status];
}

export function formatDocumentType(type: DocumentType): string {
  return documentTypeLabels[type];
}

export function formatDate(value: string | null): string {
  if (!value) {
    return "No date";
  }

  return new Intl.DateTimeFormat(undefined, {
    month: "short",
    day: "numeric",
    year: "numeric",
  }).format(new Date(value));
}

export function formatDateTime(value: string | null): string {
  if (!value) {
    return "No timestamp";
  }

  return new Intl.DateTimeFormat(undefined, {
    month: "short",
    day: "numeric",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
  }).format(new Date(value));
}

export function toDatetimeLocalValue(value: string | null): string {
  if (!value) {
    return "";
  }

  const date = new Date(value);
  const offsetMs = date.getTimezoneOffset() * 60_000;
  return new Date(date.getTime() - offsetMs).toISOString().slice(0, 16);
}

export function fromDatetimeLocalValue(value: string): string | null {
  return value ? new Date(value).toISOString() : null;
}

export function formatSalary(min: number | null, max: number | null): string {
  if (min == null && max == null) {
    return "Not set";
  }

  const currencyFormatter = new Intl.NumberFormat(undefined, {
    style: "currency",
    currency: "USD",
    maximumFractionDigits: 0,
  });

  if (min != null && max != null) {
    return `${currencyFormatter.format(min)} - ${currencyFormatter.format(max)}`;
  }

  return currencyFormatter.format(min ?? max ?? 0);
}
