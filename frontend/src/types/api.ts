export type Role = "USER" | "ADMIN";

export type UserResponse = {
  id: number;
  fullName: string;
  email: string;
  role: Role;
  createdAt: string;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type RegisterRequest = {
  fullName: string;
  email: string;
  password: string;
};

export type AuthResponse = {
  token: string;
  user: UserResponse;
};

export type ApplicationStatus =
  | "APPLIED"
  | "ONLINE_ASSESSMENT"
  | "INTERVIEW"
  | "OFFER"
  | "REJECTED"
  | "ACCEPTED";

export type JobApplication = {
  id: number;
  userId: number;
  companyName: string;
  jobTitle: string;
  location: string | null;
  salaryMin: number | null;
  salaryMax: number | null;
  jobLink: string | null;
  status: ApplicationStatus;
  applicationDate: string | null;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

export type ApplicationSortField =
  | "createdAt"
  | "updatedAt"
  | "applicationDate"
  | "companyName"
  | "jobTitle"
  | "status";

export type SortDirection = "asc" | "desc";

export type ApplicationFilters = {
  status?: ApplicationStatus | "";
  company?: string;
  search?: string;
  page: number;
  size: number;
  sortField: ApplicationSortField;
  sortDirection: SortDirection;
};

export type CreateJobApplicationRequest = {
  companyName: string;
  jobTitle: string;
  location?: string | null;
  salaryMin?: number | null;
  salaryMax?: number | null;
  jobLink?: string | null;
  status?: ApplicationStatus | null;
  applicationDate?: string | null;
  notes?: string | null;
};

export type UpdateJobApplicationRequest = CreateJobApplicationRequest;

export type UpdateApplicationStatusRequest = {
  status: ApplicationStatus;
};

export type JobApplicationStatusHistory = {
  id: number;
  applicationId: number;
  oldStatus: ApplicationStatus | null;
  newStatus: ApplicationStatus;
  changedAt: string;
};

export type InterviewType =
  | "PHONE_SCREEN"
  | "TECHNICAL"
  | "BEHAVIORAL"
  | "ONSITE"
  | "FINAL"
  | "HR"
  | "OTHER";

export type InterviewStatus = "SCHEDULED" | "COMPLETED" | "CANCELLED" | "RESCHEDULED";

export type Interview = {
  id: number;
  applicationId: number;
  type: InterviewType;
  status: InterviewStatus;
  scheduledAt: string;
  location: string | null;
  meetingLink: string | null;
  interviewerName: string | null;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
};

export type CreateInterviewRequest = {
  type: InterviewType;
  status?: InterviewStatus | null;
  scheduledAt: string;
  location?: string | null;
  meetingLink?: string | null;
  interviewerName?: string | null;
  notes?: string | null;
};

export type UpdateInterviewRequest = Required<Pick<CreateInterviewRequest, "type" | "scheduledAt">> & {
  status: InterviewStatus;
  location?: string | null;
  meetingLink?: string | null;
  interviewerName?: string | null;
  notes?: string | null;
};

export type JobTask = {
  id: number;
  applicationId: number;
  title: string;
  description: string | null;
  dueAt: string | null;
  completed: boolean;
  completedAt: string | null;
  createdAt: string;
  updatedAt: string;
};

export type CreateJobTaskRequest = {
  title: string;
  description?: string | null;
  dueAt?: string | null;
};

export type Note = {
  id: number;
  applicationId: number;
  content: string;
  createdAt: string;
  updatedAt: string;
};

export type CreateNoteRequest = {
  content: string;
};

export type DocumentType =
  | "RESUME"
  | "COVER_LETTER"
  | "PORTFOLIO"
  | "OFFER_LETTER"
  | "JOB_DESCRIPTION"
  | "OTHER";

export type JobDocument = {
  id: number;
  applicationId: number;
  name: string;
  type: DocumentType;
  url: string;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
};

export type CreateJobDocumentRequest = {
  name: string;
  type?: DocumentType | null;
  url: string;
  notes?: string | null;
};

export type ApiError = {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors?: Record<string, string>;
};
