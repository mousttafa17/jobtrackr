import { FormEvent, useEffect, useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  ArrowLeft,
  BriefcaseBusiness,
  CalendarDays,
  ExternalLink,
  Pencil,
  Save,
  Trash2,
  X,
} from "lucide-react";
import { Link, useNavigate, useParams } from "react-router-dom";
import {
  DocumentsTab,
  InterviewsTab,
  NotesTab,
  TasksTab,
} from "./ApplicationResourceTabs";
import {
  deleteApplication,
  getApplication,
  getApplicationStatusHistory,
  updateApplication,
  updateApplicationStatus,
} from "../api/applicationsApi";
import { Button } from "../components/Button";
import { LoadingState } from "../components/LoadingState";
import { InputField } from "../components/InputField";
import { SelectField } from "../components/SelectField";
import { TextAreaField } from "../components/TextAreaField";
import { useConfirmDialog } from "../components/ConfirmDialogProvider";
import { useToast } from "../components/ToastProvider";
import type {
  ApplicationStatus,
  JobApplication,
  UpdateJobApplicationRequest,
} from "../types/api";
import { getApiErrorMessage } from "../utils/errors";
import { formatDate, formatDateTime, formatSalary, formatStatus } from "../utils/formatters";

const statuses: ApplicationStatus[] = [
  "APPLIED",
  "ONLINE_ASSESSMENT",
  "INTERVIEW",
  "OFFER",
  "REJECTED",
  "ACCEPTED",
];

type DetailTab = "overview" | "interviews" | "tasks" | "notes" | "documents" | "history";

const tabs: Array<{ id: DetailTab; label: string }> = [
  { id: "overview", label: "Overview" },
  { id: "interviews", label: "Interviews" },
  { id: "tasks", label: "Tasks" },
  { id: "notes", label: "Notes" },
  { id: "documents", label: "Documents" },
  { id: "history", label: "Status history" },
];

type ApplicationFormState = {
  companyName: string;
  jobTitle: string;
  location: string;
  salaryMin: string;
  salaryMax: string;
  jobLink: string;
  status: ApplicationStatus;
  applicationDate: string;
  notes: string;
};

export function ApplicationDetailPage() {
  const { id } = useParams();
  const applicationId = Number(id);
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { confirm } = useConfirmDialog();
  const { showToast } = useToast();
  const [activeTab, setActiveTab] = useState<DetailTab>("overview");
  const [selectedStatus, setSelectedStatus] = useState<ApplicationStatus>("APPLIED");
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [form, setForm] = useState<ApplicationFormState | null>(null);
  const [formError, setFormError] = useState("");

  const applicationQuery = useQuery({
    queryKey: ["application", applicationId],
    queryFn: () => getApplication(applicationId),
    enabled: Number.isFinite(applicationId),
  });

  const historyQuery = useQuery({
    queryKey: ["application-status-history", applicationId],
    queryFn: () => getApplicationStatusHistory(applicationId),
    enabled: Number.isFinite(applicationId),
  });

  useEffect(() => {
    if (applicationQuery.data) {
      setSelectedStatus(applicationQuery.data.status);
    }
  }, [applicationQuery.data]);

  const updateMutation = useMutation({
    mutationFn: (request: UpdateJobApplicationRequest) => updateApplication(applicationId, request),
    onSuccess: (updatedApplication) => {
      setIsEditOpen(false);
      setFormError("");
      setForm(toFormState(updatedApplication));
      setSelectedStatus(updatedApplication.status);
      showToast("Application updated");
      void queryClient.invalidateQueries({ queryKey: ["applications"] });
      void queryClient.invalidateQueries({ queryKey: ["application", applicationId] });
      void queryClient.invalidateQueries({
        queryKey: ["application-status-history", applicationId],
      });
    },
    onError: (error) => {
      const message = getApiErrorMessage(error);
      setFormError(message);
      showToast(message, "error");
    },
  });

  const statusMutation = useMutation({
    mutationFn: (status: ApplicationStatus) =>
      updateApplicationStatus(applicationId, {
        status,
      }),
    onSuccess: (updatedApplication) => {
      setSelectedStatus(updatedApplication.status);
      showToast("Status updated");
      void queryClient.invalidateQueries({ queryKey: ["applications"] });
      void queryClient.invalidateQueries({ queryKey: ["application", applicationId] });
      void queryClient.invalidateQueries({
        queryKey: ["application-status-history", applicationId],
      });
    },
    onError: (error) => {
      showToast(getApiErrorMessage(error), "error");
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => deleteApplication(applicationId),
    onSuccess: () => {
      showToast("Application deleted");
      void queryClient.invalidateQueries({ queryKey: ["applications"] });
      navigate("/applications", { replace: true });
    },
    onError: (error) => {
      showToast(getApiErrorMessage(error), "error");
    },
  });

  const application = applicationQuery.data;
  const history = historyQuery.data ?? [];

  const statusChanged = useMemo(
    () => Boolean(application && selectedStatus !== application.status),
    [application, selectedStatus],
  );

  function openEditModal() {
    if (!application) {
      return;
    }

    setForm(toFormState(application));
    setFormError("");
    setIsEditOpen(true);
  }

  function submitEdit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!form) {
      return;
    }

    setFormError("");
    updateMutation.mutate(toUpdateRequest(form));
  }

  async function confirmDelete() {
    const confirmed = await confirm({
      title: "Delete application?",
      message: "This will remove the application and its related records. This cannot be undone.",
      confirmLabel: "Delete",
      dangerous: true,
    });

    if (confirmed) {
      deleteMutation.mutate();
    }
  }

  if (applicationQuery.isLoading) {
    return (
      <main className="centered-page">
        <LoadingState label="Loading application..." />
      </main>
    );
  }

  if (applicationQuery.isError || !application) {
    return (
      <main className="centered-page">
        <section className="error-panel">
          <h1>Application not found</h1>
          <p>{applicationQuery.isError ? getApiErrorMessage(applicationQuery.error) : ""}</p>
          <Link to="/applications">Back to applications</Link>
        </section>
      </main>
    );
  }

  return (
    <main className="app-page">
      <header className="app-header detail-header">
        <div>
          <Link className="back-link" to="/applications">
            <ArrowLeft size={18} />
            Applications
          </Link>
          <h1>{application.companyName}</h1>
          <p className="detail-subtitle">{application.jobTitle}</p>
        </div>
        <div className="detail-actions">
          <Button type="button" variant="ghost" onClick={openEditModal}>
            <Pencil size={18} />
            Edit
          </Button>
          <Button type="button" variant="ghost" onClick={() => void confirmDelete()} disabled={deleteMutation.isPending}>
            <Trash2 size={18} />
            {deleteMutation.isPending ? "Deleting..." : "Delete"}
          </Button>
        </div>
      </header>

      <section className="detail-shell">
        <div className="detail-summary">
          <div className="detail-summary-main">
            <div className="role-icon large" aria-hidden="true">
              <BriefcaseBusiness size={24} />
            </div>
            <div>
              <span className={`status-badge status-${application.status.toLowerCase()}`}>
                {formatStatus(application.status)}
              </span>
              <h2>{application.jobTitle}</h2>
              <p>{application.location || "Remote/unspecified"}</p>
            </div>
          </div>
          <div className="status-control">
            <SelectField
              id="detailStatus"
              label="Update status"
              value={selectedStatus}
              onChange={(event) => setSelectedStatus(event.target.value as ApplicationStatus)}
            >
              {statuses.map((status) => (
                <option key={status} value={status}>
                  {formatStatus(status)}
                </option>
              ))}
            </SelectField>
            <Button
              type="button"
              disabled={!statusChanged || statusMutation.isPending}
              onClick={() => statusMutation.mutate(selectedStatus)}
            >
              <Save size={18} />
              {statusMutation.isPending ? "Saving..." : "Save"}
            </Button>
          </div>
        </div>

        {statusMutation.isError && (
          <div className="form-error">{getApiErrorMessage(statusMutation.error)}</div>
        )}

        <nav className="detail-tabs" aria-label="Application sections">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              type="button"
              className={activeTab === tab.id ? "active" : ""}
              onClick={() => setActiveTab(tab.id)}
            >
              {tab.label}
            </button>
          ))}
        </nav>

        {activeTab === "overview" && <OverviewTab application={application} />}
        {activeTab === "history" && (
          <StatusHistoryTab
            history={history}
            isLoading={historyQuery.isLoading}
            error={historyQuery.error}
          />
        )}
        {activeTab === "interviews" && <InterviewsTab applicationId={applicationId} />}
        {activeTab === "tasks" && <TasksTab applicationId={applicationId} />}
        {activeTab === "notes" && <NotesTab applicationId={applicationId} />}
        {activeTab === "documents" && <DocumentsTab applicationId={applicationId} />}
      </section>

      {isEditOpen && form && (
        <div className="modal-backdrop" role="presentation">
          <section className="modal-panel" aria-labelledby="edit-application-title">
            <header className="modal-header">
              <div>
                <p className="eyebrow">Edit application</p>
                <h2 id="edit-application-title">Update details</h2>
              </div>
              <button
                type="button"
                className="icon-button"
                onClick={() => setIsEditOpen(false)}
                aria-label="Close"
              >
                <X size={20} />
              </button>
            </header>

            <form className="application-form" onSubmit={submitEdit}>
              {formError && <div className="form-error">{formError}</div>}
              <div className="form-grid">
                <InputField
                  id="editCompanyName"
                  label="Company"
                  value={form.companyName}
                  onChange={(event) => setFormField("companyName", event.target.value)}
                  required
                />
                <InputField
                  id="editJobTitle"
                  label="Job title"
                  value={form.jobTitle}
                  onChange={(event) => setFormField("jobTitle", event.target.value)}
                  required
                />
                <InputField
                  id="editLocation"
                  label="Location"
                  value={form.location}
                  onChange={(event) => setFormField("location", event.target.value)}
                />
                <SelectField
                  id="editStatus"
                  label="Status"
                  value={form.status}
                  onChange={(event) =>
                    setFormField("status", event.target.value as ApplicationStatus)
                  }
                >
                  {statuses.map((status) => (
                    <option key={status} value={status}>
                      {formatStatus(status)}
                    </option>
                  ))}
                </SelectField>
                <InputField
                  id="editSalaryMin"
                  label="Salary min"
                  type="number"
                  min="0"
                  value={form.salaryMin}
                  onChange={(event) => setFormField("salaryMin", event.target.value)}
                />
                <InputField
                  id="editSalaryMax"
                  label="Salary max"
                  type="number"
                  min="0"
                  value={form.salaryMax}
                  onChange={(event) => setFormField("salaryMax", event.target.value)}
                />
                <InputField
                  id="editApplicationDate"
                  label="Application date"
                  type="date"
                  value={form.applicationDate}
                  onChange={(event) => setFormField("applicationDate", event.target.value)}
                />
                <InputField
                  id="editJobLink"
                  label="Job link"
                  type="url"
                  value={form.jobLink}
                  onChange={(event) => setFormField("jobLink", event.target.value)}
                />
              </div>
              <TextAreaField
                id="editNotes"
                label="Notes"
                rows={4}
                value={form.notes}
                onChange={(event) => setFormField("notes", event.target.value)}
              />
              <div className="modal-actions">
                <Button type="button" variant="ghost" onClick={() => setIsEditOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit" disabled={updateMutation.isPending}>
                  <Save size={18} />
                  {updateMutation.isPending ? "Saving..." : "Save changes"}
                </Button>
              </div>
            </form>
          </section>
        </div>
      )}
    </main>
  );

  function setFormField<Key extends keyof ApplicationFormState>(
    key: Key,
    value: ApplicationFormState[Key],
  ) {
    setForm((current) => (current ? { ...current, [key]: value } : current));
  }
}

function OverviewTab({ application }: { application: JobApplication }) {
  return (
    <section className="detail-grid">
      <InfoTile label="Company" value={application.companyName} />
      <InfoTile label="Job title" value={application.jobTitle} />
      <InfoTile label="Location" value={application.location || "Remote/unspecified"} />
      <InfoTile label="Salary" value={formatSalary(application.salaryMin, application.salaryMax)} />
      <InfoTile label="Applied" value={formatDate(application.applicationDate)} />
      <InfoTile label="Created" value={formatDateTime(application.createdAt)} />
      <InfoTile label="Updated" value={formatDateTime(application.updatedAt)} />
      <div className="info-tile">
        <span>Job link</span>
        {application.jobLink ? (
          <a href={application.jobLink} target="_blank" rel="noreferrer">
            Open job post <ExternalLink size={16} />
          </a>
        ) : (
          <strong>Not set</strong>
        )}
      </div>
      <div className="info-tile wide">
        <span>Notes</span>
        <p>{application.notes || "No notes yet."}</p>
      </div>
    </section>
  );
}

function InfoTile({ label, value }: { label: string; value: string }) {
  return (
    <div className="info-tile">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function StatusHistoryTab({
  history,
  isLoading,
  error,
}: {
  history: Array<{
    id: number;
    oldStatus: ApplicationStatus | null;
    newStatus: ApplicationStatus;
    changedAt: string;
  }>;
  isLoading: boolean;
  error: unknown;
}) {
  if (isLoading) {
    return <section className="tab-panel">Loading status history...</section>;
  }

  if (error) {
    return <section className="form-error">{getApiErrorMessage(error)}</section>;
  }

  if (history.length === 0) {
    return <section className="tab-panel">No status history yet.</section>;
  }

  return (
    <section className="history-list">
      {history.map((item) => (
        <article key={item.id} className="history-item">
          <div className="history-dot" aria-hidden="true">
            <CalendarDays size={16} />
          </div>
          <div>
            <strong>
              {item.oldStatus ? formatStatus(item.oldStatus) : "Created"} to{" "}
              {formatStatus(item.newStatus)}
            </strong>
            <span>{formatDateTime(item.changedAt)}</span>
          </div>
        </article>
      ))}
    </section>
  );
}

function toFormState(application: JobApplication): ApplicationFormState {
  return {
    companyName: application.companyName,
    jobTitle: application.jobTitle,
    location: application.location ?? "",
    salaryMin: application.salaryMin?.toString() ?? "",
    salaryMax: application.salaryMax?.toString() ?? "",
    jobLink: application.jobLink ?? "",
    status: application.status,
    applicationDate: application.applicationDate ?? "",
    notes: application.notes ?? "",
  };
}

function toUpdateRequest(form: ApplicationFormState): UpdateJobApplicationRequest {
  return {
    companyName: form.companyName.trim(),
    jobTitle: form.jobTitle.trim(),
    location: optionalString(form.location),
    salaryMin: optionalNumber(form.salaryMin),
    salaryMax: optionalNumber(form.salaryMax),
    jobLink: optionalString(form.jobLink),
    status: form.status,
    applicationDate: optionalString(form.applicationDate),
    notes: optionalString(form.notes),
  };
}

function optionalString(value: string): string | null {
  const trimmed = value.trim();
  return trimmed ? trimmed : null;
}

function optionalNumber(value: string): number | null {
  return value ? Number(value) : null;
}
